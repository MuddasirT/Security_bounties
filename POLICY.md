# Research and Authorization Policy

## Authorization gate
Before any active test, record:
- Asset owner and the person/team granting permission.
- Exact in-scope hostnames, app identifiers, API base URLs, repositories, and environments.
- Written authorization or public bug bounty policy URL and the date it was checked.
- Explicit exclusions, prohibited test classes, rate limits, test windows, and disclosure rules.
- Approved test accounts and permitted data.

If authorization or scope is unclear, stop at passive research and request clarification.

## Safe testing boundaries
- Use accounts and data created for testing.
- Do not enumerate, access, modify, or disclose another person's information.
- Do not perform denial-of-service, load, stress, destructive, or persistence testing.
- Do not phish, socially engineer, deploy malware, steal credentials, or bypass physical controls.
- Do not test third-party integrations unless their owner and the program explicitly authorize it.
- Avoid bypassing rate limits or safeguards; respect published limits.
- Stop immediately if testing affects availability, exposes real user data, or causes unexpected impact.

## Evidence handling
- Capture the minimum evidence needed to demonstrate the issue.
- Redact tokens, cookies, passwords, keys, personal data, and internal identifiers.
- Store evidence only in approved private locations.
- Never commit secrets or sensitive raw captures to this repository.
- Remove temporary test data and revoke test credentials when work is complete.

## Responsible disclosure
Use the program's official reporting channel. Include impact, affected in-scope asset, reproducible steps, minimal evidence, and practical remediation. Follow the program's disclosure timeline and do not publish details without permission.

## Research record
Each engagement should have a scope record based on `TEMPLATES/ASSET_AUTHORIZATION.md`. Each suspected issue should use `TEMPLATES/FINDING_REPORT.md`.

This policy does not grant authorization. Authorization must come from the asset owner or the applicable program.
