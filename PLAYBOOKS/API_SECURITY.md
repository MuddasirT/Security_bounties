# REST API Security Review Checklist

Use only against APIs explicitly authorized for assessment.

## Identity and access
- [ ] Authentication required where expected; unauthenticated routes documented.
- [ ] Authorization enforced server-side for every object and action.
- [ ] Object-level access controls prevent cross-account access (use only test-owned objects).
- [ ] Role and function permissions are enforced consistently.
- [ ] Token expiry, revocation, audience, issuer, and signature validation are correct.
- [ ] Password reset, account recovery, and session lifecycle are safe.

## Input and business logic
- [ ] Server validates types, lengths, formats, and allowed values.
- [ ] Mass assignment and unexpected writable fields are prevented.
- [ ] State transitions and transaction rules are enforced server-side.
- [ ] Pagination, filtering, sorting, and exports enforce authorization.
- [ ] Uploads validate content, size, storage location, and access policy.
- [ ] Idempotency and replay protections are appropriate for sensitive actions.

## Transport and data
- [ ] TLS is required; sensitive data is not sent in URLs.
- [ ] Responses expose only necessary fields.
- [ ] Errors do not reveal stack traces, secrets, or internal topology.
- [ ] CORS is restrictive and appropriate to the client model.
- [ ] Cache headers prevent shared caching of private responses.
- [ ] Logs avoid credentials, tokens, and sensitive personal data.

## Abuse resistance
- [ ] Rate limits and quotas are appropriate and documented.
- [ ] Expensive operations have bounded resource use.
- [ ] Security events are logged without leaking secrets.
- [ ] Webhook signatures, timestamps, and replay handling are verified.
- [ ] API versioning and deprecated routes preserve security controls.

## Validation notes
Use synthetic records and approved test accounts. Avoid brute force, load testing, destructive requests, and accessing other users' data. Record endpoint, method, role, expected result, actual result, minimal evidence, and remediation.
