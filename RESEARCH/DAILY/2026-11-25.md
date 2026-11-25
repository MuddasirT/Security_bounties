# Andy Security Intelligence Briefing
**Date:** 25 November 2026  
**Research cut-off:** 08:09 IST  
**Scope:** Public-source intelligence only. No target scans, probes or exploitation performed.

## Executive assessment

The most urgent developments are four recent additions to CISA's Known Exploited Vulnerabilities (KEV) catalog: Drupal Core, Optimizely Web Experimentation, MONAI Label and N-able MailAssure. Separately, critical SSRF and authentication issues affect Apache Jena Fuseki and Genetec Security Center. A notable cluster of new disclosures affects local LLM and MCP software, including LM Studio, llama.cpp and Bytebase DBHub.

## 1. Exploited vulnerabilities and KEV priorities

### Drupal Core — CVE-2026-132165
- **Vendor advisory:** 18 November 2026. **Exploitation:** Drupal says live-site exploitation was reported as early as 19 November. Added to CISA KEV on 24 November.
- The installer file install.php can, under specific conditions, be used to attempt a reinstall of an existing site, potentially causing site takeover or remote code execution.
- **Affected:** before 10.6.22; 11.0.0–11.3.21; 11.4.0–11.4.12. Older unsupported branches receive no security fixes.
- **Fix:** Drupal 10.6.22, 11.3.22 or 11.4.13. Also secure/remove access to the installer after installation. Making settings.php unwritable mitigates some, not all, paths.
- Sources: [Drupal SA-CORE-2026-017](https://www.drupal.org/sa-core-2026-017) · [CISA KEV catalog](https://www.cisa.gov/known-exploited-vulnerabilities-catalog)

### Optimizely Web Experimentation — CVE-2026-132224
- **Incident disclosed:** 18 November 2026. **KEV addition:** 24 November.
- An account-signup flaw allowed an unauthorized party to impersonate users on targeted customer accounts and inject malicious JavaScript into project assets delivered to customer sites.
- Optimizely says it detected the activity on 16 November, blocked malicious-code delivery and closed the flaw within three hours; it says fewer than 5% of customers were affected and injected code was removed.
- **Action:** Web Experimentation customers should review the vendor's incident communications and audit project JavaScript/configuration for unauthorized changes. Optimizely says remediation is complete; this is a hosted-service incident, not a customer-installed package update.
- Sources: [Optimizely incident report](https://status.optimizely.com/incidents/ftw0fh9wx43z) · [CISA KEV catalog](https://www.cisa.gov/known-exploited-vulnerabilities-catalog)

### MONAI Label — CVE-2026-133968
- **Project advisory:** 18 November; **CVE published:** 23 November; **KEV addition:** 23 November.
- Path traversal in datastore upload endpoints permits arbitrary file writes. Authentication is disabled by default; any network client that can reach the service may exploit it. With authentication enabled, the ANNOTATOR role is sufficient. Arbitrary writes can lead to code execution in the service context.
- **Affected:** through 0.8.5. **Fixed:** 0.8.6, released 22 November. The same release also fixes path traversal in session archive uploads.
- **Action:** Upgrade to 0.8.6, enable authentication, keep the service off untrusted networks and run it as an unprivileged account with write access limited to its datastore. Review old exposed installations for unexpected file changes.
- Sources: [MONAI Label advisory](https://github.com/Project-MONAI/MONAILabel/security/advisories/GHSA-qc22-rmgc-vg94) · [0.8.6 release notes](https://github.com/Project-MONAI/MONAILabel/releases/tag/0.8.6) · [CISA KEV catalog](https://www.cisa.gov/known-exploited-vulnerabilities-catalog)

### N-able MailAssure — CVE-2026-134624
- **Vendor advisory:** 22 November; **KEV addition:** 23 November.
- SSRF in the LDAP synchronization connection check when a relay is configured. An authenticated API user with high privileges can make the service connect to arbitrary internal hosts/ports and send crafted data to internal services. This is not an unauthenticated flaw.
- **Affected:** before build 20261121.1943. **Fixed:** 20261121.1943.
- N-able says its incident investigation found no evidence of exploitation beyond one N-able-hosted server and that an independent DFIR investigation is ongoing. The company has not established publicly that this CVE was the initial entry point.
- Sources: [N-able CVE advisory](https://me.n-able.com/s/security-advisory/aArVy0000002mrlKAA/cve2026134624-mailassure-ldap-sync-connection-check-ssrf) · [N-able incident update, 22 November](https://www.n-able.com/blog/mail-assure-incident-update) · [CISA KEV catalog](https://www.cisa.gov/known-exploited-vulnerabilities-catalog)

## 2. Other high-impact disclosures

### Apache Jena Fuseki — CVE-2026-86155
- **Published:** 24 November. **CVSS 4.0:** 10.0 Critical.
- SSRF through SHACL validation and SHACL SPARQL targets; Apache lists versions through 6.2.0 as affected.
- **Fix:** Apache Jena Fuseki 6.3.0.
- No exploitation claim appears in the Apache notice. Treat exploitation as unconfirmed.
- Source: [Apache security disclosure on oss-security](https://www.openwall.com/lists/oss-security/2026/11/23/32)

### Microsoft Office Visio — CVE-2026-112732
- **Published:** 24 November. **CVSS 3.1:** 9.8 Critical.
- A double-free vulnerability is described as allowing remote code execution.
- Microsoft says the issue was already addressed by November 2026 updates and the CVE was inadvertently omitted from the November security-update list. Systems that have installed those updates need no additional action.
- Fixed LTSC builds include 16.0.14334.21048 (2021) and 16.0.17932.21140 (2024); Microsoft 365 Apps users should verify their update-channel build.
- Source: [Microsoft MSRC advisory](https://msrc.microsoft.com/update-guide/vulnerability/CVE-2026-112732)

### Genetec Security Center — CVE-2026-124284
- **Published:** 24 November. **CVSS 3.1:** 9.8 Critical.
- Under specific conditions, authentication using external OIDC/SAML 2.0 identity providers or AD FS with WS-Trust/WS-Federation can permit unauthorized access with the privileges of an affected user.
- **Action:** On-premises operators using these authentication methods should obtain and apply the branch-specific Genetec security update/hotfix. Do not assume native authentication or every deployment is affected; the vendor record says configuration conditions matter.
- Sources: [CVE record assigned by Genetec](https://www.cve.org/CVERecord?id=CVE-2026-124284) · [Genetec Security Center 5.12 security updates](https://techdocs.genetec.com/r/en-US/Security-Updates-for-Security-Center-5.12)

### Acer Connect M6E 5G router
- Acer's security advisory covers multiple findings in firmware M6E_AI_1.00.000019 or earlier, including unauthenticated API access, remote deletion of meeting recordings, metadata disclosure, unrestricted modem AT commands from any app, and a web administration panel exposed on public IPv6 interfaces.
- **Fix:** OTA firmware M6E_GBL_1.00.000107.
- **Action:** Apply the OTA update promptly; restrict management exposure and review whether the device was reachable over public IPv6.
- Source: [Acer security advisory](https://community.acer.com/en/en/kb/articles/20236-security-advisory-firmware-update-for-acer-connect-m6e-5g-portable-wi-fi-router)

## 3. AI, MCP and local-inference security

### LM Studio — CVE-2026-133995
- **ZDI disclosure:** 24 November. **CVSS:** 9.8.
- Unauthenticated remote code execution through insufficient validation of MCP tool schemas before code generation.
- **Fix:** LM Studio 0.4.16.
- Source: [ZDI-26-1121](https://www.zerodayinitiative.com/advisories/ZDI-26-1121/) · [LM Studio 0.4.16 changelog](https://lmstudio.ai/changelog/lmstudio/lmstudio-v0.4.16)

### llama.cpp — CVE-2026-131837 and CVE-2026-131836
- **ZDI disclosures:** 24 November.
- CVE-2026-131837 (CVSS 8.1): unauthenticated RCE in the server during a sleep-transition request path; ZDI links upstream fix PR #32133.
- CVE-2026-131836 (CVSS 7.8): malformed MiniCPM-V GGUF model file can trigger RCE when loaded; ZDI links fix PR #32132.
- **Action:** Use an updated build containing the upstream fixes, bind local inference services to localhost or a trusted isolated network, and load model files only from trusted sources with integrity checks.
- Sources: [ZDI-26-1079](https://www.zerodayinitiative.com/advisories/ZDI-26-1079/) · [ZDI-26-1078](https://www.zerodayinitiative.com/advisories/ZDI-26-1078/) · [upstream server fix](https://github.com/ggml-org/llama.cpp/pull/32133) · [upstream model-parser fix](https://github.com/ggml-org/llama.cpp/pull/32132)

### Bytebase DBHub MCP — CVE-2026-136441
- **ZDI disclosure:** 24 November. **CVSS:** 9.8. ZDI labels this an unpatched zero-day.
- Incorrect authorization while parsing untrusted SQL can allow unauthenticated authentication bypass in the MCP server. ZDI's advisory does not name a fixed version.
- **Action:** Do not expose DBHub's HTTP transport to untrusted networks; prefer local stdio transport, restrict database credentials at the database itself, and do not rely on a read-only SQL parser as the security boundary.
- Source: [ZDI published-advisory list](https://www.zerodayinitiative.com/advisories/published/) · [ZDI-26-1067](https://www.zerodayinitiative.com/advisories/ZDI-26-1067/)

### LiteLLM — coordinated ZDI disclosures, 24 November
- CVE-2026-136539 (CVSS 7.2): authenticated high-privilege RCE in pass-through endpoint code handling; ZDI links [fix PR #27801](https://github.com/BerriAI/litellm/pull/27801).
- CVE-2026-136537 (CVSS 6.5): missing authorization in key-management endpoints; ZDI links [LiteLLM v1.104.0](https://github.com/BerriAI/litellm/releases/tag/v1.104.0).
- CVE-2026-136538 (CVSS 6.5): incorrect authorization involving virtual-key model aliases; ZDI links [fix PR #43049](https://github.com/BerriAI/litellm/pull/43049).
- A separate MCP stdio command-injection report, CVE-2026-136535, is disputed: ZDI calls it a zero-day, while LiteLLM says stdio commands are administrator-selected and disabled by default from v1.104.0 unless LITELLM_ENABLE_MCP_STDIO=true is set. This is a vendor/researcher disagreement, not a settled patch status.
- Sources: [ZDI-26-1084](https://www.zerodayinitiative.com/advisories/ZDI-26-1084/) · [ZDI-26-1082](https://www.zerodayinitiative.com/advisories/ZDI-26-1082/) · [ZDI-26-1083](https://www.zerodayinitiative.com/advisories/ZDI-26-1083/) · [ZDI-26-1031](https://www.zerodayinitiative.com/advisories/ZDI-26-1031/)

## 4. REST API, RAG and tenant-isolation developments

Spring AI published several advisories on 19 November; CVE records for some appeared on 24 November:

- **CVE-2026-59417 (Critical):** Redis Semantic Cache can return one user's cached response to another user or allow cache poisoning when shared across users/tenants. Fixed in Spring AI 2.0.3 (OSS), 2.0.2.1, 1.1.10 and 1.0.11 (enterprise support branches). The fix requires a SemanticCacheScopeResolver derived from a trusted authenticated principal. If unable to upgrade, do not use the advisor in multi-user applications.
- **CVE-2026-59369 (High):** Typesense filter-expression injection can bypass metadata/tenant restrictions. Same fixed versions.
- **CVE-2026-59415 (High per Spring):** Typesense document-ID injection can delete unintended documents. Spring recommends validating IDs; same fixed versions. NVD later assigned a CVSS 10.0 score, which differs from Spring's own HIGH rating. Impact requires attacker-controlled IDs to reach the delete operation.
- Sources: [Spring CVE-2026-59417](https://spring.io/security/cve-2026-59417/) · [Spring CVE-2026-59369](https://spring.io/security/cve-2026-59369/) · [Spring CVE-2026-59415](https://spring.io/security/cve-2026-59415/)

## 5. Android and iOS

- **Android November Security Bulletin:** published 2 November, updated 6 November. The bulletin includes critical Framework/System issues; patch level 2026-11-05 or later addresses the listed Android issues. Check OEM delivery status.
  - [Android November bulletin](https://source.android.com/docs/security/bulletin/2026/2026-11-01)
  - [Pixel November bulletin](https://source.android.com/docs/security/bulletin/pixel/2026/2026-11-01)
- **Apple:** the latest iOS/iPadOS security releases located in Apple's index remain the 9 November releases (including 27.2.1, 26.7.3 and 18.7.12). No newer iOS/iPadOS security bulletin was found in the reviewed Apple release index as of this cut-off.
  - [Apple security releases](https://support.apple.com/en-us/100100)

## 6. Bug bounty and disclosure-program intelligence

- No new official bounty-scope expansion was verified for 24–25 November.
- **HackerOne, 19 November:** CVE-specific nginx and Ruby research was explicitly brought into scope against HackerOne-owned assets, subject to the live asset list and program rules. This does not authorize testing arbitrary third-party nginx/Ruby systems.
  - [HackerOne security updates](https://www.hackerone.com/security-updates)
- **Bugcrowd, 18 November:** new researchers on Managed Bug Bounty programs face a one-pending-report limit and six submissions per week until accuracy exceeds 50%; VDP and penetration-test engagements are excluded.
  - [Bugcrowd submission limits](https://docs.bugcrowd.com/researchers/reporting-managing-submissions/reporting-a-bug/submissions-limit/)

## 7. Defensive research

**AWS Security Blog, 20 November:** Amazon Threat Intelligence described autonomous Cairn agents finding a path-traversal zero-day in an open-source medical-imaging AI application and reaching arbitrary file write in about 11 minutes; a later session achieved root-level code execution. The MONAI Label advisory credits AWS VDP as the original reporter, strongly linking the two reports, although AWS's blog does not name MONAI Label. AWS attributes the activity to a Chinese-nexus actor with **moderate confidence**, not certainty.
- [AWS investigation](https://aws.amazon.com/blogs/security/chinese-nexus-actors-cairn-ai-agents-found-a-zero-day-on-their-own/)
- [MONAI Label advisory](https://github.com/Project-MONAI/MONAILabel/security/advisories/GHSA-qc22-rmgc-vg94)

The ZDI disclosures against LM Studio, llama.cpp, LiteLLM and DBHub reinforce that MCP schemas, model-file parsers, local inference APIs and database-tool authorization are now meaningful attack surfaces.

## 8. Authorized research queue

1. In isolated labs, regression-test path confinement, API authentication, SSRF destination controls and tenant-bound cache/filter isolation.
2. For local AI stacks, verify LM Studio/llama.cpp fixes, keep MCP transports local, and enforce permissions at the database rather than relying on an agent-side read-only guard.
3. For third-party bounty work, confirm the exact asset and permitted methods in the current program policy before any testing. A CVE, public PoC or domain name is not authorization.

**Limitations:** This is a point-in-time review of public sources. No scans, probes, exploitation, or continuous monitoring are claimed. CISA KEV status reflects the catalog as reviewed for this briefing; vendor incident statements remain attributed to those vendors.
