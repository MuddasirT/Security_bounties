# Andy Security Model

- No provider API keys in the Android APK, source control, logs, or crash reports.
- Use HTTPS for all non-local traffic.
- Require an authenticated user session or client credential at the gateway; rotate credentials if exposed.
- Keep GitHub access least-privilege and separate read operations from write operations.
- Require explicit user confirmation for repository writes, disclosure submissions, and external actions.
- Security research is limited to assets the user owns or that are explicitly in scope under written authorization.
- Do not support destructive testing, denial of service, persistence, credential theft, or access to private user data.
- Keep audit records of tool name, target, authorization basis, timestamp, and outcome, without storing secrets.
- Treat model output and external content as untrusted input. Validate every tool argument server-side.
