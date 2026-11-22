# Andy Security Intelligence Briefing: 2026-11-22

> Research cut-off: 2026-11-22. Focus: material disclosures and exploitation intelligence published or updated during 2026-11-17 to 2026-11-22, with earlier platform bulletins included where still operationally relevant. Public-source research only. No target scanning, probing, exploitation, or validation was performed.

## Executive summary

1. **Patch VMware vCenter immediately**: CVE-2026-123890 (CVSS 9.8) is an authentication bypass. Google GTIG/Mandiant says it was exploited in early September, and the Canadian Centre for Cyber Security reports CISA added it to KEV on 20 November.
2. **Treat Cisco Expressway as actively exploited**: CVE-2026-76466 (CVSS 9.8) moved into CISA KEV on 20 November. It permits unauthenticated root command execution on vulnerable clustered Expressway-C/E deployments.
3. **Apache Roller has an actively exploited pre-auth RCE**: CVE-2026-129448 affects 4.0.0 through 6.1.4 through XML-RPC deserialization. Apache recommends 6.1.6 or later.
4. **Review SAP NetWeaver Enterprise Portal 7.50**: older CVE-2025-42884 was added to KEV on 20 November; apply SAP Note 3660969 if not already installed.
5. **Node.js, nginx, Ruby, Drupal REST authentication, Android and Apple platform updates** warrant dependency and fleet inventory.

## 1. High-priority exploited vulnerabilities

### VMware vCenter — CVE-2026-123890

- **Published:** 19 November 2026; Broadcom advisory updated 20 November.
- **Severity:** Critical, CVSS 9.8.
- **Impact:** Authentication bypass in VMware Directory Service. An attacker with network access to vCenter can obtain unauthorized access and an administrator session.
- **Exploitation:** Google Threat Intelligence Group and Mandiant report forensic evidence of exploitation by suspected PRC-nexus actor ARMORY CASTLE in early September 2026. Their report describes theft of directory data, SAML STS key abuse, and creation of an SSO administrator account. Attribution is the researchers' assessment, not a vendor-confirmed identity.
- **Fix:** vCenter 8.0 U3m; 9.0.2.0200; 9.1.1.0200; VCF 5.x 5.2.5. For vCenter 7.0, Broadcom directs customers to support if covered by extended support. No workaround is listed.
- **Defender action:** Patch, restrict vCenter management access, and investigate pre-patch signs of compromise. Review unexpected SSO administrators, SAML signing material, directory database access and suspicious management-plane activity. A patch does not undo a prior compromise.

Sources: [Broadcom VMSA-2026-0009](https://support.broadcom.com/web/ecx/support-content-notification/-/external/content/SecurityAdvisories/0/39495) · [Google GTIG/Mandiant investigation, 19 Nov](https://cloud.google.com/blog/topics/threat-intelligence/armory-castle-exploits-vsphere) · [Canadian Centre advisory, updated 20 Nov](https://www.cyber.gc.ca/en/alerts-advisories/vmware-security-advisory-av26-1185)

### Cisco Expressway — CVE-2026-76466

- **Published:** 21 October 2026; CISA KEV addition reported 20 November.
- **Severity:** Critical, CVSS 9.8.
- **Impact:** An unauthenticated remote attacker with network access can execute OS commands as root on vulnerable clustered Expressway-C or Expressway-E devices. The issue involves persistent cookies and insufficient client-certificate validation in the clustering configuration.
- **Important status distinction:** Cisco's original 21 October advisory said PSIRT was not aware of malicious use at that time. The Canadian Centre reports that CISA added the CVE to KEV on 20 November, a later exploitation-status update.
- **Fix:** X15.5.20; X14 and earlier must migrate to a fixed release. Cisco lists no workaround.
- **Defender action:** Identify clustered deployments and version, upgrade, restrict access to management and clustering interfaces, and review logs for unexpected privileged activity.

Sources: [Cisco advisory](https://www.cisco.com/c/en/us/support/docs/csa/cisco-sa-vcs-static-cred-hr7EWZ8t.html) · [Canadian Centre update confirming CISA KEV addition](https://www.cyber.gc.ca/en/alerts-advisories/cisco-security-advisory-av26-1073) · [CISA KEV catalog](https://www.cisa.gov/known-exploited-vulnerabilities-catalog?field_cve=CVE-2026-76466)

### Apache Roller — CVE-2026-129448

- **Published:** 17 November 2026.
- **Severity:** Critical, CVSS 9.8.
- **Impact:** Unauthenticated remote code execution through the XML-RPC endpoint. The endpoint deserializes vendor extension types before authenticating the requester. Apache says the issue is actively exploited.
- **Affected:** Roller 4.0.0 through 6.1.4.
- **Fix:** Upgrade to 6.1.6 or later. Temporary mitigation: set `enabledForExtensions` to `false` in `WEB-INF/web.xml`, or remove the `/roller-services/xmlrpc` servlet mapping, then restart. Apache warns that merely disabling XML-RPC in the administration UI is insufficient on affected versions.
- **Defender action:** Inventory Roller instances, patch, and review XML-RPC access and application execution logs.

Sources: [Apache security announcement, 17 Nov](https://lists.apache.org/thread/lnz4s26tv6hdo03wngtdbpfj4hg4gtds) · [GitHub Advisory Database record](https://github.com/advisories/GHSA-pxp4-phq9-fx2q) · [CISA KEV catalog](https://www.cisa.gov/known-exploited-vulnerabilities-catalog?field_cve=CVE-2026-129448)

### SAP NetWeaver Enterprise Portal — CVE-2025-42884

- **Originally published:** 11 November 2025; exploitation status updated and CISA KEV addition reported 20 November 2026.
- **Severity:** CVSS 6.5 (Medium).
- **Impact:** Unauthenticated JNDI injection may allow access to an unintended JNDI provider and disclosure or modification of server information. SAP's published impact does not claim availability impact.
- **Affected:** NetWeaver Enterprise Portal EP-BASIS 7.50 and EP-RUNTIME 7.50.
- **Fix:** Apply [SAP Security Note 3660969](https://me.sap.com/notes/3660969). The note requires SAP Support Portal access.
- **Defender action:** Verify the note is applied to production, test, DR and partner-facing portal instances. Review portal and outbound connection logs for suspicious JNDI activity. Public reporting does not identify specific victims or an actor.

Sources: [SAP Security Note 3660969](https://me.sap.com/notes/3660969) · [CISA KEV catalog entry](https://www.cisa.gov/known-exploited-vulnerabilities-catalog?field_cve=CVE-2025-42884)

## 2. Major vendor and dependency advisories

### Node.js — 11 November security release

Node.js published security releases for the 22.x, 24.x and 26.x lines. High-severity issues include HTTP/2 remote process termination, memory disclosure and resource leaks; TLS callback crashes; and TLS 1.2 renegotiation that can bypass per-host client-certificate policy.

Particularly relevant:
- **CVE-2026-123864:** HTTPS/TLS parser re-entrancy can disclose heap memory from other connections.
- **CVE-2026-123872:** TLS 1.2 renegotiation can bypass stricter per-host client-certificate authentication.
- **CVE-2026-123863 / 123865:** unauthenticated HTTP/2 clients can crash servers or cause persistent memory/file-descriptor retention.
- **CVE-2026-123866 / 123874:** HTTP/2 stream teardown defects can disclose memory or corrupt server memory under documented conditions.

**Fixed releases:** Node.js 22.23.5, 24.22.3 and 26.13.1. Upgrade supported release lines and restart all workloads; do not assume rebuilding a container changed a still-running process.

Source: [Node.js 11 November 2026 security release](https://nodejs.org/en/blog/vulnerability/november-2026-security-releases)

### nginx — HTTP/2 and related memory-safety issues

The nginx advisory page lists major/medium issues including **CVE-2026-123007** (HTTP/2 flow-control memory corruption), **CVE-2026-123633** (HTTP/2 use-after-free), **CVE-2026-118899** (TLS 1.3 request-body infinite loop), and **CVE-2026-113425** (mail-proxy use-after-free).

- **Fixed:** nginx 1.31.9+ or 1.30.7+ for the listed issues.
- **Action:** Upgrade, verify the running binary/version and enabled modules, then reload/restart according to the vendor procedure.

Sources: [nginx security advisories](https://nginx.org/en/security_advisories.html) · [HackerOne's 19 November response](https://www.hackerone.com/security-updates/cve-2026-123633-and-cve-2026-123007)

### Ruby — seven Regexp/String vulnerabilities

- **Published:** 18 November 2026.
- Ruby disclosed seven vulnerabilities in regular-expression handling and string case conversion. The project says PoCs demonstrate RCE in some scenarios.
- **Important identifier correction:** Ruby explicitly says CVE identifiers had been requested but were **not yet assigned** at publication. Track these under their GHSA identifiers, not unverified CVE numbers.
- **Fixed:** Ruby 4.0.10, 3.4.13 and 3.3.14. Ruby 3.2 and earlier are end-of-life and will not receive these fixes.
- **Action:** Upgrade; avoid compiling regular expressions from attacker-controlled input until patched.

Source: [Ruby security advisory, 18 Nov](https://www.ruby-lang.org/en/news/2026/11/18/multiple-vulnerabilities-in-regexp-and-string/)

## 3. Android, iOS and REST API security

### Android — November 2026 bulletin

- **Published:** 2 November; updated 6 November.
- The bulletin includes critical System RCEs **CVE-2026-58876** and **CVE-2026-58936**, described in the System section as requiring no additional execution privileges or user interaction. It also lists critical modem, Bluetooth and pKVM issues.
- **Patch level:** 2026-11-05 or later covers the full November bulletin.
- **Action:** Check device security patch levels, not just Android version. OEM rollout timing varies; verify chipset and device-specific bulletins too.

Source: [Android Security Bulletin—November 2026](https://source.android.com/docs/security/bulletin/2026/2026-11-01)

### Apple — iOS/iPadOS November updates

- **Released:** 9 November 2026. Current listed branches include iOS/iPadOS 27.2.1, 26.7.3 and 18.7.12.
- Fixes include Bluetooth-range pairing-rate-limit bypass (**CVE-2026-113968**), Bluetooth use-after-free memory corruption (**CVE-2026-113970**), malicious Bluetooth pairing without user interaction (**CVE-2026-113965**), and iMessage group-join authorization issues (**CVE-2026-113961 / 113960**). The releases also address Brotli memory corruption (**CVE-2026-111497**).
- **Action:** Install the latest update offered for the device's supported branch.

Sources: [Apple iOS 27.2.1 security content](https://support.apple.com/en-hk/149715) · [Apple iOS 26.7.3 security content](https://support.apple.com/en-om/149716) · [Apple security releases](https://support.apple.com/en-us/100100)

### REST API and identity

- **Drupal REST & JSON API Authentication — CVE-2026-122067. Published 4 November.** If API authentication is enabled but no application API key has been generated, the module may fail to reject requests, allowing unauthenticated access or modification of protected resources. Affected versions are before 3.2.1. **Fix:** upgrade to 3.2.1 and audit privileged-user activity. [Drupal advisory](https://www.drupal.org/sa-contrib-2026-298)
- **OAuth security topics draft — IETF, 6 July 2026.** The active Internet-Draft discusses audience-injection, cross-toolkit account takeover, cross-user session fixation and shared consent in brokered OAuth. It is a work-in-progress draft, not a finalized RFC. **Defensive takeaway:** bind tokens to the intended issuer/audience/client, isolate sessions across users and toolkits, and review brokered-consent boundaries. [IETF draft](https://datatracker.ietf.org/doc/html/draft-ietf-oauth-security-topics-update)

## 4. Bug bounty and scope intelligence

### HackerOne: explicit CVE-related scope notes

- **19 November:** HackerOne stated that findings against **HackerOne-owned assets only** related to nginx **CVE-2026-123633** and **CVE-2026-123007** are in scope. HackerOne says its public web traffic is served through Cloudflare and that it applied fixes to its production platform and Hacker101 CTF on 19 November; it reported no confirmed exploitation indicators on its infrastructure at the time of the update.
- **2 November:** HackerOne stated that findings against **HackerOne-owned assets only** related to Brotli **CVE-2026-111497** are in scope.
- These are narrow, program-specific statements, not permission to test other organizations' nginx/Brotli deployments. Confirm the current asset list, exclusions and testing rules at [HackerOne Security](https://hackerone.com/security) before any activity.

Sources: [HackerOne nginx response, updated 19 Nov](https://www.hackerone.com/security-updates/cve-2026-123633-and-cve-2026-123007) · [HackerOne Brotli response, updated 2 Nov](https://www.hackerone.com/security-updates/cve-2026-111497)

### Bugcrowd platform changes

- **18 November:** New researcher accounts may have one report awaiting review at a time on Managed Bug Bounty programs. The limit is removed after report accuracy exceeds 50%; drafts can still be prepared.
- **3 November:** Bugcrowd API v1.4.0 added engagement-brief endpoints and fixed ordering/pagination for `GET /engagements`.
- These are platform/process updates, not evidence of a new asset scope. Always rely on each program's current scope and policy.

Sources: [Bugcrowd changelog](https://docs.bugcrowd.com/changelog/) · [New-account submission limit](https://docs.bugcrowd.com/changelog/researchers/new-account-submission-limit/) · [API changelog](https://docs.bugcrowd.com/api/changelog/)

## 5. Defensive research worth tracking

- **GTIG/Mandiant, 19 November — vSphere intrusion analysis:** The investigation shows why vCenter should be treated as identity-critical infrastructure. Compromise can extend beyond the original authentication flaw into directory-data theft, SAML trust abuse and persistent SSO administration. This supports post-patch compromise review, not patch-only closure. [Research](https://cloud.google.com/blog/topics/threat-intelligence/armory-castle-exploits-vsphere)
- **Apple Security Engineering, 12 November — Secure Exclaves:** Apple describes isolated runtimes for processing camera and microphone data, with tightly controlled interfaces to the rest of the system. The design is relevant to mobile privacy architecture and hardware-backed isolation. [Research](https://security.apple.com/blog/secure-exclaves)

## 6. Authorized research queue

Suitable work is limited to:
1. Reproduce fixes in a local lab for Node.js, nginx, Ruby, Drupal and Android/iOS application components.
2. Review the user's own code and dependency inventory for affected versions and vulnerable API/authentication patterns.
3. For HackerOne, consider only the CVE-specific findings expressly listed above and only on assets that the current program page identifies as HackerOne-owned and in scope.

**No third-party asset should be scanned or tested without explicit written authorization covering the exact asset, technique and time window.** A CVE, public PoC, bounty listing, or domain name alone is not authorization. No active testing or report submission was performed in this briefing.

## Source and confidence notes

- Vendor advisories, platform security bulletins, IETF documents and the Apache/Ruby project announcements are treated as primary sources.
- Exploitation status is stated only where the vendor, CISA-linked government advisory or named threat-intelligence source explicitly reports it. Actor attribution remains an assessment where described as suspected.
- Ruby's 18 November advisory did not yet have CVE IDs assigned; GHSA identifiers are the correct references at that publication time.
- This is a point-in-time public-source briefing, not a scan, continuous monitor, or assurance that every affected product has been discovered in any environment.
