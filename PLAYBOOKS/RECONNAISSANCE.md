# Safe Reconnaissance and Program Scoping

## 1. Find legitimate programs
Prefer official program directories and the organization's own security page. Verify the program is active and read its current policy directly.

## 2. Extract scope
Create an explicit allowlist of:
- Domains and subdomains
- Mobile application package/bundle identifiers and supported versions
- API hosts and documented endpoints
- Repositories and downloadable artifacts
- Test environments and approved accounts

Record exclusions separately. A related brand, vendor, CDN, identity provider, or acquired company is not automatically in scope.

## 3. Passive-first review
Within the policy, review public documentation, release notes, security.txt, published API specifications, app metadata, and source code that the owner has made available for review. Do not treat DNS discovery or a public hostname as permission to test it.

## 4. Before active validation
Confirm written authorization, exact target, permitted methods, rate limits, test accounts, and reporting channel. Use the least intrusive test that can establish the issue.

## 5. Engagement log
Record date/time, operator, target, authorization reference, tools and versions, test account, actions taken, observations, and cleanup. Never include secrets in logs.

## Stop conditions
Stop if scope is ambiguous, a third-party system is implicated, real user data appears, service stability changes, or the test behaves unexpectedly.
