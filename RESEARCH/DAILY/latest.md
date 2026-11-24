# Andy Security Intelligence Briefing: 2026-11-24

> Research snapshot: 24 November 2026. Public-source advisory review only. No target scans, probes, exploitation, or continuous monitoring were performed.

## Executive assessment

The most urgent item is a critical Xen Orchestra REST authentication bypass that Vates says has been actively exploited. PHP's 19 November security release fixes several serious remote-code-execution paths. Also prioritize Gradle 8.14.8, Node.js November security updates, and nginx HTTP/2 fixes. Android's November bulletin contains critical RCEs; Apple's latest listed iOS/iPadOS security releases remain dated 9 November.

## 1. Highest-priority vulnerabilities

### CVE-2026-133469 — Xen Orchestra authentication bypass

- **CVE publication:** 23 November 2026. GitHub advisory GHSA-2r3w-c2vx-fv5g was published 20 November; Vates' original vendor advisory VSA-2026-062 was published 10 November.
- **Severity:** CVSS 9.8 Critical.
- **Impact:** REST cookie parsing can turn an empty-object token into an unfiltered token lookup. If the selected token is unexpired, the request can authenticate as its owner and expose reusable token identifiers. Vates says exploitation by a threat actor is active. Compromise can grant Admin privileges and lead to arbitrary code execution on managed pools or root takeover of XCP-ng hosts.
- **Affected/fixed:** Xen Orchestra before 6.9.2 (stable) or 6.10.1 (latest); affected Xen Orchestra Appliance deployments. Fixed xo-server packages: 5.211.2 (stable) and 5.213.1 (latest).
- **Mitigation:** Upgrade urgently; do not expose the management interface to the public Internet; place it on a dedicated management network. Review Vates' campaign-specific indicators and host artifacts. If compromise is suspected, rotate Vates VMS secrets; after confirmed compromise, Vates recommends full reinstallation. Absence of the published indicators alone does not prove a system is clean.

Sources:
- [Vates VSA-2026-062](https://docs.vates.tech/security-advisories/advisories/2026/vates-sa-2026-062/)
- [GitHub advisory GHSA-2r3w-c2vx-fv5g](https://github.com/vatesfr/xen-orchestra/security/advisories/GHSA-2r3w-c2vx-fv5g)

### PHP November security release — seven severe CVEs

- **Published:** PHP security releases on 19 November 2026; Red Hat bulletin RHSB-2026-020 updated 23 November.
- **Critical issues:** CVE-2026-115560 (session serializer mismatch), CVE-2026-126714 (partially overwritten session files, called phpAfterShell), CVE-2026-129289 (PHP-FPM request smuggling), and CVE-2026-129496 (timezone-cache use-after-free). CVE-2026-129481 is rated Important. CVE-2026-129182 and CVE-2026-129474 are critical but affect PHP 8.5 only.
- **Impact:** Most of the listed severe flaws can permit unauthenticated code execution in the PHP worker's security context, subject to the affected configuration and request path.
- **Fixed upstream releases:** PHP 8.2.36, 8.3.37, 8.4.28, 8.5.13 and 8.6.0. Check distribution errata because vendors may backport fixes without changing the upstream version string.
- **Mitigation:** Upgrade. Temporary Red Hat guidance includes disabling session upload progress for the session-related flaws, disabling FastCGI backend connection reuse for CVE-2026-129289, and applying issue-specific PHP-FPM logging/backtrace settings. Verify effective settings for every FPM pool and virtual host; restart PHP master and worker processes after changes. Treat workarounds as temporary.

Sources:
- [Red Hat RHSB-2026-020](https://access.redhat.com/security/vulnerabilities/RHSB-2026-020)
- [PHP 8.3.37 security release](https://www.php.net/releases/8_3_37.php)
- [Depthfirst phpAfterShell research](https://depthfirst.com/research/phpaftershell)

**Exploitation status:** The sources reviewed do not establish widespread in-the-wild exploitation of the PHP November CVEs. Do not confuse public technical research or proof-of-concept availability with confirmed exploitation.

## 2. Major vendor and dependency advisories

### Gradle 8.14.8 — four additional security fixes

- **Release:** 19 November 2026.
- Gradle 8.14.8 addresses four security issues: SFTP repositories not verifying host keys (8.9 High), transitive snapshot dependencies skipping dependency verification (8.8 High), predictable TestKit temporary directories (7.0 High), and a daemon registry briefly readable by other local users (7.0 High).
- The release notes also list security fixes delivered in earlier 8.14 patch releases, including repository fallback and daemon communication issues.
- **Mitigation:** Upgrade the Gradle 8.14 line to 8.14.8 or a later supported fixed release. Review repository host-key verification, dependency verification, wrapper versions and CI runner isolation.

Source: [Gradle 8.14.8 release notes](https://docs.gradle.org/8.14.8/release-notes.html)

### Gradle CVE identifiers newly published on 23 November

- **CVE-2026-22816 / GHSA-w78c-w6vf-rw82:** An unresolved repository hostname may not disable the repository, allowing Gradle to continue to another configured repository and potentially resolve malicious artifacts if an attacker can control the later repository.
- **CVE-2026-22865 / GHSA-mqwm-5m85-gmcv:** A repository that repeatedly fails to answer may not be disabled, allowing fallback to another repository and possible malicious artifact resolution.
- **Important date qualification:** The CVE identifiers appeared on 23 November, but the underlying Gradle advisories and fixes predate that date. These are not newly discovered zero-days. Gradle lists fixes in 8.14.4 and 9.3.0; repository content filtering and dependency verification are additional defenses.

Sources:
- [Gradle advisory: unknown host](https://github.com/gradle/gradle/security/advisories/GHSA-w78c-w6vf-rw82)
- [Gradle 8.14.4 release notes](https://docs.gradle.org/8.14.4/release-notes.html)
- [Gradle 9.3.0 release notes](https://docs.gradle.org/9.3.0/release-notes.html)

### Node.js — November security release

- **Release:** 11 November 2026. Fixed release lines: 22.23.5, 24.22.3 and 26.13.1.
- High-impact examples include CVE-2026-123864 (HTTPS heap disclosure under specific handler behavior), CVE-2026-123872 (TLS 1.2 renegotiation can bypass per-host client-certificate policy), CVE-2026-123874 (HTTP/2 use-after-free in file-serving paths), and CVE-2026-123863 (unauthenticated HTTP/2 server abort).
- **Mitigation:** Upgrade to the fixed release line. Review HTTPS request-body handling, HTTP/2 file-serving handlers, TLS SNI/mTLS policy boundaries and TLS 1.2 renegotiation. Do not rely on per-host mTLS policies without testing their exact configuration in a controlled environment.

Source: [Node.js November 2026 security releases](https://nodejs.org/en/blog/vulnerability/november-2026-security-releases/)

### nginx HTTP/2 and HTTP/3 advisories

- **Published:** 18 November 2026.
- CVE-2026-123007 concerns memory corruption in HTTP/2 flow control; CVE-2026-123633 concerns use-after-free in HTTP/2. The vendor also lists other HTTP/2, HTTP/3, resolver and TLS-related issues.
- **Fixed versions:** nginx 1.31.9+ or 1.30.7+.
- **Mitigation:** Upgrade and restart affected instances; verify all deployed nginx binaries and container images, not only the primary edge proxy.

Source: [Official nginx security advisories](https://nginx.org/en/security_advisories.html)

## 3. Android and iOS security

### Android November 2026 bulletin

- **Published:** 2 November; updated 6 November.
- The bulletin includes critical RCE entries CVE-2026-58876 and CVE-2026-58936, plus critical privilege-escalation issues. Google says the most severe Framework issue requires neither additional execution privileges nor user interaction.
- **Patch level:** 2026-11-05 or later addresses all issues in the bulletin.
- **Mitigation:** Install the latest OEM-provided security update; check the actual device patch string because delivery timing differs by manufacturer.

Source: [Android November 2026 Security Bulletin](https://source.android.com/docs/security/bulletin/2026/2026-11-01)

### Apple security releases

- Apple's release index lists iOS/iPadOS 27.2.1, 26.7.3 and 18.7.12 as released on **9 November 2026** for their respective supported devices.
- HomeView Software Version 27.2.1 was listed on **18 November** and has no published CVE entries; it is not a new iOS/iPadOS CVE bulletin.
- **Mitigation:** Install the latest update available for the specific device and supported OS branch.

Source: [Apple security releases](https://support.apple.com/en-us/100100)

## 4. REST API and identity security developments

### Vates VSA-2026-064 — proxy token disclosure through REST API

- **Published:** 19 November 2026. No CVE assigned in the advisory.
- A user with ACL v2 read permission on a proxy resource can retrieve its authentication token because the token remains embedded in the URL returned by **GET /rest/v0/proxies**.
- **Impact:** An unprivileged user may reuse the proxy token to perform actions equivalent to Admin.
- **Status at publication:** Vates marked the issue in progress and said the latest/stable channels did not yet contain a fix.
- **Mitigation:** Restrict proxy-resource read access to Admin, remove **proxy/action:read** from non-admin users, and firewall proxy access so only Xen Orchestra can communicate with the XO proxy. Recheck the current vendor advisory for later remediation.

Source: [Vates VSA-2026-064](https://docs.vates.tech/security-advisories/advisories/2026/vates-sa-2026-064/)

### Node.js mTLS policy weaknesses

CVE-2026-123867 and CVE-2026-123872 demonstrate that per-host client-certificate authorization can fail when SNI context configuration or TLS 1.2 renegotiation is handled incorrectly. API gateways and service-to-service clients should review trust-store separation, SNI-specific certificate policies, session reuse and renegotiation settings. Upgrade Node.js to the fixed security releases.

Source: [Node.js November 2026 security releases](https://nodejs.org/en/blog/vulnerability/november-2026-security-releases/)

## 5. Legitimate bug bounty and platform updates

### HackerOne — CVE-specific in-scope research

- **19 November:** HackerOne explicitly listed findings related to nginx CVE-2026-123633 and CVE-2026-123007 as in scope against HackerOne-owned assets, subject to its current asset list.
- **19 November:** Ruby2Shell and related Ruby advisory findings were also listed as in scope against HackerOne-owned assets.
- HackerOne said its production platform was patched for the nginx issues and upgraded to Ruby 3.4.13; it reported no confirmed indicators of exploitation on its operated infrastructure in those updates.

Sources:
- [HackerOne nginx update](https://www.hackerone.com/security-updates/cve-2026-123633-and-cve-2026-123007)
- [HackerOne Ruby update](https://www.hackerone.com/security-updates/ghsa-34m4-m9fh-gqp5-and-related-ruby-advisories-2026)

**Scope boundary:** These statements apply to HackerOne-owned assets only. They do not authorize testing arbitrary third-party nginx or Ruby deployments. Confirm the current program asset list, rules and allowed methods before any testing.

### Bugcrowd — researcher submission limits

- **Updated:** 18 November 2026.
- New accounts on Managed Bug Bounty engagements are limited to one report awaiting review at a time and six submissions per week. Limits are removed once accuracy is above 50%.
- The policy applies to Managed Bug Bounty programs; Vulnerability Disclosure Programs and penetration tests are excluded.

Source: [Bugcrowd submission-limit policy](https://docs.bugcrowd.com/researchers/reporting-managing-submissions/reporting-a-bug/submissions-limit/)

No broader public target-scope expansion was verified in the official program updates reviewed for this briefing.

## 6. Notable defensive research

### phpAfterShell — Depthfirst, 19 November

Depthfirst's research shows how interrupted, non-atomic writes to file-backed PHP sessions can leave a mixture of old and new serialized state. A later normal session read can interpret the modified state. The defensive lesson is important: a patch that blocks one deserialization path may leave the underlying storage-integrity problem intact.

**Defensive takeaway:** Review session storage semantics, disable upload progress where not needed until patched, use atomic session-store updates, and review the actual root cause addressed by vendor fixes.

Source: [Depthfirst technical research](https://depthfirst.com/research/phpaftershell)

### Secure Exclaves — Apple Security Engineering, 12 November

Apple described protected, compartmentalized runtimes for processing camera and microphone data separately from the main OS, apps and Apple. The architecture is a useful reference for mobile sensor privacy and isolation.

Source: [Apple Secure Exclaves and Ambient Sensing](https://security.apple.com/blog/secure-exclaves)

### AI-assisted vulnerability discovery

The Node.js security release credits researchers working with Claude and Anthropic Research for multiple findings. This is documented researcher-assisted vulnerability discovery, not evidence of autonomous AI exploitation.

Source: [Node.js security release](https://nodejs.org/en/blog/vulnerability/november-2026-security-releases/)

## 7. Andy's priority queue

1. Patch Xen Orchestra immediately and investigate for compromise if deployed.
2. Inventory PHP 8.2–8.6 services, FPM pools, containers and file-backed session configurations; apply the November fixes.
3. Update Gradle 8.14 users to 8.14.8 and review dependency verification, SFTP host-key checks and repository fallback.
4. Upgrade Node.js and nginx; audit HTTP/2 handlers and mTLS boundaries.
5. Verify Android patch level 2026-11-05 or later and install the latest supported Apple updates.
6. Review REST API authorization and secret exposure, especially tokens embedded in returned URLs.

## Authorized research queue

- Reproduce relevant behavior only in a local lab, owned application or intentionally vulnerable environment.
- For HackerOne's nginx/Ruby CVE-specific scope, test only assets explicitly listed in the current HackerOne program and only within its stated methods and limits.
- A CVE, public proof of concept, domain name or program announcement is not by itself authorization.

## Research limitations

This is a point-in-time review of public vendor, project and program sources as of 24 November 2026. No target scans, probes, exploitation or continuous monitoring were performed. Exploitation claims are attributed to the vendor or source making them; public disclosure and proof-of-concept availability are not treated as proof of in-the-wild exploitation.
