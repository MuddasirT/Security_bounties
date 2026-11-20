# Android Application Security Review

Review only apps and backends you own or are explicitly authorized to assess.

## Platform configuration
- [ ] Manifest permissions are minimal and justified.
- [ ] Exported activities, services, receivers, and providers are intentional and protected.
- [ ] Deep links and intent handling validate untrusted input.
- [ ] Network Security Config does not permit unintended cleartext traffic.
- [ ] WebView JavaScript bridges and file access are tightly controlled.
- [ ] Debuggable/test configurations are not shipped in production.

## Secrets and local data
- [ ] No API secrets, private keys, or credentials embedded in APK/AAB.
- [ ] Sensitive values are protected using Android Keystore-backed cryptography.
- [ ] Private data uses app-private storage and appropriate encryption.
- [ ] Backups and device-to-device transfer exclude sensitive data as appropriate.
- [ ] Logs, crash reports, clipboard, screenshots, and notifications do not leak secrets.
- [ ] Tokens are scoped, short-lived, and revocable.

## Network and backend
- [ ] TLS validation is standard and certificate validation is not disabled.
- [ ] Authentication and authorization are enforced by the backend.
- [ ] API responses and object access are checked server-side.
- [ ] Sensitive operations use appropriate re-authentication and replay defenses.

## Build and dependencies
- [ ] Release builds disable debugging and remove test endpoints.
- [ ] Dependencies and SDKs are inventoried and updated.
- [ ] R8/obfuscation is treated as hardening, not a substitute for security.
- [ ] Signing keys are protected and release provenance is controlled.

## Test discipline
Use emulator/device test accounts and synthetic data. Do not bypass protections on third-party apps or extract data from devices/accounts without permission. Document app version, Android version, device state, steps, evidence, and fixes.
