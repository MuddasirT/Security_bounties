# Andy Security Intelligence Briefing: 2026-11-23

> Research date: 23 November 2026. Public-source review only. No target scans, probes, exploitation, or vulnerability validation were performed.

## Executive summary

The most urgent confirmed issues are the VMware vCenter authentication bypass exploited before disclosure, Apache Roller unauthenticated XML-RPC deserialization under active exploitation, and two high-impact runtime flaws in Ruby and OpenJDK. Apache Tomcat also disclosed a critical cluster peer-authentication flaw, but exposure is limited to a specific TLS 1.3/TLS-PSK cluster configuration.

## 1. High-priority vulnerabilities

### VMware vCenter — CVE-2026-123890
- **Published:** 19 November 2026; Broadcom updated its advisory on 20 November. **CVSS:** 9.8 Critical.
- **Impact:** A network-reachable attacker can bypass authentication in VMware Directory Service and obtain an administrator session. Google GTIG/Mandiant report that suspected PRC-nexus actor ARMORY CASTLE exploited it in early September, using the access to extract directory data, compromise SAML trust and create an SSO administrator.
- **Fix:** Apply the Broadcom fixed releases: vCenter 8.0 U3m, 9.0.2.0200, 9.1.1.0200; VCF 5.x 5.2.5. Check the response matrix for other product branches. No workaround is listed.
- **Defensive action:** Patch urgently, restrict vCenter management-plane access, and investigate unexpected SSO administrators, SAML signing material, directory changes and suspicious management activity. If compromise is suspected, treat this as an incident, not just a patching task.
- Sources: [Broadcom VMSA-2026-0009](https://support.broadcom.com/web/ecx/support-content-notification/-/external/content/SecurityAdvisories/0/39495) · [Google GTIG/Mandiant, 19 Nov](https://cloud.google.com/blog/topics/threat-intelligence/armory-castle-exploits-vsphere)

### Apache Roller — CVE-2026-129448
- **Published:** 17 November 2026. **CVSS:** 9.8 Critical.
- **Impact:** Unauthenticated remote code execution through XML-RPC deserialization before requester authentication. Apache says the issue is actively exploited. The expanded affected range is Roller 4.0.0 through 6.1.4; CVE-2026-82384 describes the same underlying issue for 6.1.5.
- **Fix:** Upgrade to Roller 6.1.6 or later. Interim mitigation: set `enabledForExtensions` to `false` in `WEB-INF/web.xml`, or remove the `/roller-services/xmlrpc` servlet mapping, then restart. Disabling XML-RPC only in the administration UI is insufficient on affected versions.
- Sources: [Apache security announcement](https://lists.apache.org/thread/lnz4s26tv6hdo03wngtdbpfj4hg4gtds) · [GitHub Advisory Database](https://github.com/advisories/GHSA-pxp4-phq9-fx2q)

### Ruby “Ruby2Shell” — CVE-2026-132825 / GHSA-34m4-m9fh-gqp5
- **Published:** GitHub Ruby advisory 19 November; Red Hat bulletin public 20 November. **Red Hat severity:** Critical (9.8).
- **Impact:** A one-byte heap overflow in string case conversion can crash a Ruby process and may allow code execution in some scenarios. The trigger requires attacker-controlled text in certain single-byte non-UTF-8 encodings (for example ISO-8859-1 or Windows-1252) to reach affected case-conversion methods. UTF-8-only processing is not exposed to this specific trigger condition.
- **Affected upstream versions:** Ruby through 3.3.13, 3.4.0–3.4.12, and 4.0.0–4.0.9. **Fixed:** 3.3.14, 3.4.13 and 4.0.10.
- **Mitigation:** Upgrade. Red Hat says a general mitigation is still under investigation; meanwhile reduce network exposure of affected self-hosted Ruby services and review whether untrusted non-UTF-8 input reaches case-conversion code.
- **Scoring note:** Red Hat rates its product context 9.8, while the upstream/CVE record has different attack-complexity assumptions and a lower score. The conditions matter more than treating one score as universal.
- Sources: [Ruby upstream advisory](https://github.com/ruby/ruby/security/advisories/GHSA-34m4-m9fh-gqp5) · [Red Hat bulletin](https://access.redhat.com/security/vulnerabilities/RHSB-2026-021)

### OpenJDK “Java4Shell” — CVE-2026-114938
- **Published:** 17 November 2026. CISA issued an alert the same day urging immediate patching.
- **Impact:** Memory corruption in the accelerated Base64 decoder can permit unauthenticated remote code execution when an application passes attacker-controlled Base64 data to affected OpenJDK. The vulnerable path requires OpenJDK 17.0.6 or later on x86-64 hardware exposing AVX-512 VBMI; ARM64 and OpenJDK 8/11 are not known to be affected by this flaw.
- **Mitigation:** Apply the vendor's fixed JDK update. As an interim Red Hat mitigation, use `-XX:-UseBASE64Intrinsics` on affected JVMs. Inventory Java runtimes embedded in containers, products and appliances, not only separately installed JDKs.
- Sources: [CISA alert, 17 Nov](https://www.cisa.gov/news-events/alerts/2026/11/17/critical-java-vulnerability-requires-immediate-patching) · [Red Hat CVE bulletin](https://access.redhat.com/security/vulnerabilities/RHSB-2026-018) · [Oracle November 2026 CSPU](https://www.oracle.com/security-alerts/cspunov2026.html)

### Apache Tomcat — CVE-2026-133325
- **Published:** 19 November 2026. Apache classifies it as Critical.
- **Impact:** An unauthenticated node may join a Tomcat cluster when cluster communication is configured with TLS 1.3 and TLS-PSK. This is an authentication/peer-validation failure, not evidence of remote code execution.
- **Affected versions:** 11.0.28–11.0.29, 10.1.62 and 9.0.124, subject to the affected cluster configuration.
- **Mitigation:** Apache's interim instruction is to use TLS 1.2 until fixed releases 11.0.30, 10.1.63 and 9.0.125 are available. Verify cluster transport settings rather than assuming every Tomcat installation is exposed.
- Sources: [Apache Tomcat 11 security page](https://tomcat.apache.org/security-11) · [Apache Tomcat 10 security page](https://tomcat.apache.org/security-10) · [Canadian Centre for Cyber Security advisory, 21 Nov](https://www.cyber.gc.ca/en/alerts-advisories/apache-security-advisory-av26-1188)

### Cisco Expressway — CVE-2026-76466
- **Published:** 21 October 2026. **CVSS:** 9.8 Critical.
- **Impact:** On affected clustered Expressway-C/E systems, a remote attacker with the required persistent cookies may execute OS commands as root because of insufficient client-certificate validation in the clustering database service.
- **Fix:** X15.5.20; X14 and earlier must migrate to a fixed release. No workaround is available.
- **Exploitation status:** Cisco's advisory says its PSIRT was not aware of public announcements or malicious use at the time of publication. Do not describe exploitation as confirmed on the basis of that advisory.
- Source: [Cisco security advisory](https://www.cisco.com/c/en/us/support/docs/csa/cisco-sa-vcs-static-cred-hr7EWZ8t.html)

## 2. Major vendor and platform advisories

- **nginx, 18 November:** Release 1.31.9 (mainline) and 1.30.7 (stable) fix multiple issues, including major HTTP/2 flow-control memory corruption (CVE-2026-123007), HTTP/2 use-after-free (CVE-2026-123633), TLS 1.3 request-body infinite loop (CVE-2026-118899), and other HTTP/3, resolver and mail-proxy issues. Upgrade to 1.31.9+ or 1.30.7+ as appropriate. [Official nginx advisory list](https://nginx.org/en/security_advisories.html) · [Release announcement](https://nginx.org/index.html)
- **Node.js, 11 November:** Security releases are available for 22.x, 24.x and 26.x: 22.23.5, 24.22.3 and 26.13.1. High-impact issues include HTTP/2 crashes and memory disclosure, plus TLS/mTLS authorization and certificate-validation bypasses. Upgrade all supported release lines and rebuild containers. [Official Node.js release notes](https://nodejs.org/en/blog/vulnerability/november-2026-security-releases/)
- **Oracle, 17 November:** November Critical Security Patch Update contains 655 new patches across product families. Review the affected-product matrix and apply the November CSPU plus any separately published security alerts. [Oracle November CSPU](https://www.oracle.com/security-alerts/cspunov2026.html)
- **Chrome, 17 November:** Chrome 158 stable for desktop and Android includes 204 security fixes. The release lists critical issues including use-after-free in WebView, an out-of-bounds GPU write and a media buffer overflow. Update Chrome and Chromium-based browsers through their supported update channels. [Chrome Releases](https://chromereleases.googleblog.com/)

## 3. Android, iOS and REST API security

### Android
- **November Android bulletin:** Published 2 November, updated 6 November.
- Includes critical Framework RCEs CVE-2026-58876 and CVE-2026-58936, plus critical privilege-escalation issues.
- Security patch level **2026-11-05 or later** addresses the bulletin.
- Source: [Android Security Bulletin](https://source.android.com/docs/security/bulletin/2026/2026-11-01)

### Apple
- Apple's release index lists **iOS/iPadOS 27.2.1**, released 9 November, as the latest listed iOS/iPadOS security release at this review.
- Fixes include Bluetooth pairing-rate-limit bypass (CVE-2026-113968), Bluetooth use-after-free (CVE-2026-113970), App Intents sensitive-data authorization issue (CVE-2026-113963) and Brotli memory corruption (CVE-2026-111497).
- Install the latest release available for the device's supported branch.
- Sources: [Apple security release index](https://support.apple.com/en-us/100100) · [iOS 27.2.1 security content](https://support.apple.com/en-asia/149715)

### REST API / service-to-service security
The Node.js November release is especially relevant to API gateways, backend clients and mTLS deployments:
- **CVE-2026-123867:** per-host mTLS CA configuration can fail open to the default CA in a specific SNI-context setup.
- **CVE-2026-123877:** HTTPS Agent TLS session-cache key reuse can carry authentication from a broader CA policy into a stricter one.
- **CVE-2026-123872:** TLS 1.2 renegotiation can bypass per-host client-certificate policy in certain server configurations.
- **CVE-2026-123864:** a conditional HTTPS memory disclosure issue can expose data from other connections.
- **Defensive action:** Upgrade Node.js, audit per-host client-certificate policy and TLS session reuse, and ensure credentials or tokens are never written to error logs.
- Source: [Node.js November security release](https://nodejs.org/en/blog/vulnerability/november-2026-security-releases/)

## 4. Bug bounty program intelligence

- **HackerOne, 19 November:** Findings related to the Ruby2Shell and related Ruby advisories are in scope against **HackerOne-owned assets**, subject to the current asset list and program rules. HackerOne says its main platform had moved to Ruby 3.4.13 and reports no confirmed exploitation indicators on its infrastructure; supporting services were still being upgraded, and Hacker101 CTF was temporarily taken offline as a precaution. This is a narrow, explicit scope statement, not permission to test arbitrary Ruby applications. [HackerOne incident update](https://www.hackerone.com/security-updates/ghsa-34m4-m9fh-gqp5-and-related-ruby-advisories-2026)
- **Bugcrowd, 18 November:** New researcher accounts on Managed Bug Bounty engagements are limited to one report awaiting review at a time and up to six submissions per week; limits are removed after accuracy exceeds 50%. The policy excludes VDPs and pen tests. [Bugcrowd submission-limit policy](https://docs.bugcrowd.com/researchers/reporting-managing-submissions/reporting-a-bug/submissions-limit/)
- **Bugcrowd, 17 November:** Savant Vista and Savant Pathseeker reached general availability for customers. These are platform capability updates, not blanket permission or a public scope expansion. [Customer changelog](https://docs.bugcrowd.com/customers/changelog/)
- No broad public target-scope expansion was confirmed in the program changelogs reviewed. Always use the exact live engagement brief and asset list.

## 5. Notable defensive research

- **GTIG/Mandiant, 19 November:** The vCenter investigation documents how an infrastructure-management compromise can become an identity-plane compromise through directory extraction, SAML signing-key theft and rogue SSO administrators. It supports treating vCenter as a tier-zero control plane and combining patching with identity and forensic review. [Research report](https://cloud.google.com/blog/topics/threat-intelligence/armory-castle-exploits-vsphere)
- **Node.js security research, 11 November:** The project credits Chris Rohlf working with Claude and Anthropic Research for several HTTP/2 and HTTPS memory-safety findings, and credits Trail of Bits (OpenAI) for a Node.js Permission Model information-disclosure issue. This is a documented example of AI-assisted vulnerability research; it does not establish that an AI system independently performed the research. [Node.js acknowledgements and technical details](https://nodejs.org/en/blog/vulnerability/november-2026-security-releases/)
- **Chrome, 17 November:** The Chrome release credits Xinyang Ge (Anthropic), assisted by Claude, for critical GPU and media memory-safety findings. [Chrome release notes](https://chromereleases.googleblog.com/)

## 6. Authorized research queue

1. Reproduce affected conditions only in isolated labs or software you own.
2. Audit owned Android and backend projects for Node.js dependencies, mTLS policy boundaries, TLS session reuse and vulnerable transitive components.
3. For bounty work, the only specific opportunity identified here is the HackerOne Ruby advisory scope on HackerOne-owned assets. Confirm each exact asset and current rules before any testing.
4. Do not probe public vCenter, Roller, Tomcat, Cisco, nginx or other third-party systems based solely on a CVE or public advisory.

## Confidence and limitations

- **Confirmed:** Vendor advisories, fixed-version guidance, Android/Apple bulletins, Node.js release notes and the explicit HackerOne scope statement.
- **Exploitation confirmed by source:** Mandiant reports prior exploitation of VMware CVE-2026-123890; Apache states CVE-2026-129448 is actively exploited.
- **Not confirmed:** Cisco's advisory says PSIRT was not aware of malicious use at its publication date. Tomcat's disclosed impact is unauthorized cluster membership; do not inflate it into RCE.
- This is a point-in-time public-source review. No scans, probes, exploitation or continuous monitoring are claimed.
