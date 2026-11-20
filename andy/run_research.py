"""Fetch public security advisories and create a daily research briefing.

This runtime is intentionally passive: it reads public advisory feeds only.
It never scans targets, exploits vulnerabilities, or contacts bounty assets.
"""
from __future__ import annotations
import json
import os
import sys
import urllib.request
from datetime import datetime, timezone
from pathlib import Path
from research_agents import AGENTS, Advisory

CISA_KEV = "https://www.cisa.gov/sites/default/files/feeds/known_exploited_vulnerabilities.json"
GHSA = "https://api.github.com/advisories?per_page=100"
OUT = Path(os.environ.get("ANDY_REPORT_DIR", "RESEARCH/DAILY"))

def get_json(url: str):
    req = urllib.request.Request(url, headers={"User-Agent": "Andy-Security-Research/1.0", "Accept": "application/json"})
    with urllib.request.urlopen(req, timeout=30) as response:
        return json.loads(response.read().decode("utf-8"))

def fetch_kev() -> list[Advisory]:
    data = get_json(CISA_KEV)
    rows = []
    for x in data.get("vulnerabilities", []):
        rows.append(Advisory("CISA KEV", x.get("cveID", "Unknown"), x.get("vulnerabilityName", "Untitled"),
            x.get("shortDescription", ""), "known-exploited", x.get("dateAdded", ""), x.get("dateAdded", ""),
            "https://www.cisa.gov/known-exploited-vulnerabilities-catalog",
            tuple(filter(None, [x.get("vendorProject", ""), x.get("product", "")]))))
    return rows

def fetch_ghsa() -> list[Advisory]:
    data = get_json(GHSA)
    rows = []
    for x in data if isinstance(data, list) else []:
        vulns = x.get("vulnerabilities") or []
        products = []
        for v in vulns:
            p = v.get("package") or {}
            products.extend(filter(None, [p.get("name"), p.get("ecosystem")]))
        ident = x.get("cve_id") or x.get("ghsa_id") or "GHSA"
        rows.append(Advisory("GitHub Advisory Database", ident, x.get("summary") or "Untitled advisory",
            x.get("description") or "", (x.get("severity") or "unknown").lower(),
            x.get("published_at") or "", x.get("updated_at") or "",
            x.get("html_url") or "https://github.com/advisories", tuple(sorted(set(products)))))
    return rows

def esc(value: str) -> str:
    return str(value or "").replace("|", "\\|").replace("\n", " ").strip()

def render(items: list[Advisory], errors: list[str], now: datetime) -> str:
    unique = {}
    for item in items:
        unique[(item.source, item.identifier)] = item
    all_items = sorted(unique.values(), key=lambda x: (x.severity == "known-exploited", x.updated or x.published), reverse=True)
    lines = [f"# Andy Security Intelligence Briefing: {now:%Y-%m-%d}", "",
        f"> Generated at {now:%Y-%m-%d %H:%M UTC}. Public advisory research only. No target testing was performed.", "",
        "## Executive summary", "",
        f"- Public advisories collected: **{len(all_items)}**",
        f"- CISA Known Exploited Vulnerabilities: **{sum(x.source == 'CISA KEV' for x in all_items)}**",
        f"- Feed errors: **{len(errors)}**", "",
        "## Specialist agent findings", ""]
    for agent in AGENTS:
        matches = agent.review(all_items)
        lines += [f"### {agent.name}", ""]
        if not matches:
            lines += ["No matching advisories in the feeds retrieved during this run.", ""]
            continue
        for x in matches[:15]:
            lines += [f"- **[{esc(x.identifier)}]({x.url})**: {esc(x.title)}",
                      f"  - Source: {esc(x.source)} | Severity/status: {esc(x.severity)} | Published: {esc(x.published) or 'Not supplied'}",
                      f"  - Products: {esc(', '.join(x.products)) or 'Not specified'}",
                      f"  - {esc(x.summary)[:500]}"]
        lines.append("")
    lines += ["## Feed and execution notes", "",
        "- CISA KEV identifies vulnerabilities known to be exploited in the wild; prioritize checking whether affected products exist in your own environment.",
        "- GitHub advisories are public ecosystem intelligence, not proof that any particular project is vulnerable.",
        "- Keyword-based agent routing is triage, not a complete product classifier. Review original advisories before making decisions.",
        "- This run did not scan, probe, exploit, or validate any website or bounty asset.", ""]
    if errors:
        lines += ["### Collection errors", ""] + [f"- {esc(e)}" for e in errors] + [""]
    return "\n".join(lines)

def main() -> int:
    now = datetime.now(timezone.utc)
    records, errors = [], []
    for label, fn in (("CISA KEV", fetch_kev), ("GitHub advisories", fetch_ghsa)):
        try:
            records.extend(fn())
        except Exception as exc:
            errors.append(f"{label}: {type(exc).__name__}: {exc}")
    if not records:
        print("No advisory records collected. " + "; ".join(errors), file=sys.stderr)
        return 1
    OUT.mkdir(parents=True, exist_ok=True)
    report = render(records, errors, now)
    date = now.strftime("%Y-%m-%d")
    (OUT / f"{date}.md").write_text(report, encoding="utf-8")
    (OUT / "latest.md").write_text(report, encoding="utf-8")
    print(f"Andy research briefing written: {OUT / (date + '.md')}")
    print(f"Collected {len(records)} source records; {len(errors)} feed errors.")
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
