# Andy Cloud Deployment Plan

This is a deployment blueprint. No cloud account, API key, runtime, database, or external agent has been provisioned yet.

## Phase 0: prerequisites
- Choose a cloud provider and region.
- Create a dedicated cloud project/account for Andy.
- Create a dedicated GitHub bot identity with least-privilege access to this repository only.
- Create model-provider API credentials and store them in the cloud secret manager.
- Choose managed PostgreSQL, a queue, encrypted object storage, and a container runtime.
- Define a human owner and emergency-stop contact.

## Phase 1: minimum viable control plane
Deploy a FastAPI control-plane API, Andy orchestrator worker, managed queue, PostgreSQL, cloud secret manager, centralized logs, and metrics.

Start with advisory research, source-code review of user-provided repositories, and passive analysis of supplied program documentation. Keep target-facing active tools disabled.

## Phase 2: specialist agents
Deploy separate logical agents for scope research, recon, web, API, Android, iOS, identity, source/dependency review, advisory research, and independent finding review. They may share a runtime initially, but must have separate instructions and minimal tool allowlists. Do not give specialists cloud administrator credentials or unrestricted shell/network access.

## Phase 3: controlled target Tool Gateway
All target-facing requests pass through one gateway. It must:
- Resolve hostnames and check exact allowlisted domains/IP ranges.
- Reject redirects outside approved scope.
- Enforce approved methods, rate limits, concurrency, and testing windows.
- Block prohibited paths and techniques from the program policy.
- Log request metadata without storing credentials or sensitive response bodies.
- Provide a global kill switch and per-assessment disable switch.

Start in passive-only mode. Enable active checks only after a human approves a complete authorization record.

## Phase 4: release and hardening
- Use infrastructure as code and separate dev/staging/production.
- Pin dependencies and scan container images.
- Run unit, integration, policy, and scope-gateway tests in CI.
- Require code review for production changes.
- Use short-lived identity credentials and rotate provider keys.
- Encrypt databases, queues, object storage, and backups.
- Restrict outbound network access to required model, GitHub, and explicitly approved target endpoints.
- Configure evidence and log retention/deletion.
- Test backup restoration and emergency shutdown.

## GitHub integration
Use a dedicated fine-grained token or GitHub App restricted to this repository. Prefer read access by default. Grant Contents write only to a report publisher after human approval. Never expose the token to specialist agents or store it in repository files. Use a cloud secret manager and short-lived OIDC credentials where supported.

## Suggested rollout
1. Local tests with mock targets and synthetic findings.
2. Cloud deployment with passive research only.
3. Human-approved test environment owned by the user.
4. Limited bug bounty pilot with one explicitly authorized asset.
5. Expand coverage only after policy tests and audit review pass.

## Still required
Cloud provider, region, billing, domain, API credentials, and deployment approvals. This repository does not provision infrastructure. Do not call the system live until health checks and a controlled end-to-end test succeed.
