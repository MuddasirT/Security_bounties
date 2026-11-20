# Authorization Gate

## Default state
DENY ACTIVE TESTING. A URL by itself is not proof of authorization.

## Required assessment record
Before active testing, record:
- Program owner and canonical program policy URL.
- Exact approved hostnames, apps, API base URLs, repositories, or IP ranges.
- Explicit exclusions and third-party services.
- Permitted test categories and prohibited techniques.
- Rate limits, concurrency limits, required test headers/accounts.
- Approved testing window and expiry timestamp.
- Required contact or disclosure channel.
- Authorization evidence reference, human approver, and approval time.

## Enforcement rules
1. A human must review and approve the scope record.
2. Authorization expires at the earlier of program expiry, assessment end, or policy change.
3. Every target request is checked by the gateway, including redirects, DNS resolution, ports, and destination IP.
4. Out-of-scope redirects and third-party dependencies are blocked.
5. If scope is ambiguous, stale, unavailable, or contradictory, stop and ask the user.
6. Model interpretations never override the original program policy.
7. Any change to target, technique, rate, or time window requires renewed approval.
8. A kill switch immediately disables target-facing tools.

## Prohibited actions
- Denial-of-service, stress testing, destructive testing, or service degradation.
- Accessing, modifying, or retaining other users' data.
- Credential theft, phishing, malware, persistence, stealth, or evasion.
- Exfiltration, lateral movement, or testing unrelated third-party infrastructure.
- Automated report submission or bounty negotiation without human approval.

## Safe fallback
Without active authorization, agents may summarize public program rules, perform passive analysis of supplied documentation, review user-owned source code, and provide defensive recommendations. They must not probe the target.
