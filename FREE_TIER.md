# Andy Zero-Cost Operating Model

## What runs for free
- GitHub Actions scheduled/manual workflow for a public repository, subject to GitHub's current fair-use and service limits.
- Python standard-library runtime and unit tests.
- Public CISA KEV and GitHub Advisory Database feeds.
- Specialist triage modules for Android, iOS, web, API, cloud, and general advisories.
- Versioned reports committed to this repository.

## What is not included
- No always-on server or background process.
- No paid LLM API calls or autonomous third-party AI agents.
- No guaranteed free compute for private-repository usage beyond the applicable account quota.
- No active target scanning, exploitation, or validation.

## Why this design
A genuinely independent AI-agent fleet needs model inference and compute. Those services are not reliably free at production scale. This version provides real cloud-scheduled automation and modular specialist research without hidden charges. A provider-backed LLM can be added later as an optional adapter, but the core pipeline must remain usable without it.

## Operations
Open the repository's Actions tab and select **Andy Daily Security Research**. Use **Run workflow** for an on-demand briefing. Scheduled runs are configured for 02:30 UTC daily (08:00 India Standard Time).

The workflow needs Actions enabled in repository settings. Its publishing step uses the workflow's repository-scoped GitHub token. Do not add personal access tokens or API keys to source files.
