# iOS Application Security Review

Review only apps and services that are owned or explicitly authorized for assessment.

## App configuration
- [ ] Entitlements and URL schemes are minimal and intentional.
- [ ] Universal Links and custom URL handlers validate parameters.
- [ ] App Transport Security is not weakened without a documented need.
- [ ] Debug and development configurations are excluded from production.

## Data protection
- [ ] Keychain accessibility classes match the sensitivity and lifecycle of data.
- [ ] Sensitive files use appropriate Data Protection classes.
- [ ] Secrets are not embedded in the app bundle.
- [ ] Logs, analytics, crash reports, clipboard, screenshots, and notifications do not leak sensitive data.
- [ ] Backups and local caches are reviewed for sensitive content.

## Network and backend
- [ ] TLS validation remains enabled.
- [ ] Authentication, authorization, and object access are enforced server-side.
- [ ] Tokens have appropriate scope, expiry, and revocation.
- [ ] Sensitive actions have suitable re-authentication and replay protections.

## Build and dependencies
- [ ] Signing certificates and provisioning assets are protected.
- [ ] Third-party SDKs and dependencies are inventoried and maintained.
- [ ] Production builds do not expose debug menus or test endpoints.

Use synthetic data and approved test accounts. Record app/build version, iOS version, reproduction steps, minimal evidence, impact, and remediation.
