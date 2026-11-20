# Specialist Agent Roster

All agents are subordinate to Andy and operate under the Authorization Gate. Each receives only task-specific context and tools.

| Agent | Responsibility | Default capability |
|---|---|---|
| Scope Researcher | Extract program scope, exclusions, testing rules | Public documentation only |
| Recon Researcher | Organize approved asset inventory | Passive only |
| Web Reviewer | Review web application design and supplied evidence | Code/docs review; active tests gated |
| API Reviewer | Review REST API authentication, authorization, validation, rate controls | Code/docs review; active tests gated |
| Android Reviewer | Review APK/source, storage, network config and platform controls | Local artifact analysis |
| iOS Reviewer | Review app source/artifacts, keychain and transport controls | Local artifact analysis |
| Identity Reviewer | Review OAuth/OIDC, sessions, MFA and access control | Design/code review |
| Code Reviewer | Review source, dependencies, secrets and insecure patterns | Repository read-only |
| Advisory Researcher | Summarize CVEs and vendor advisories | Public sources |
| Finding Reviewer | Challenge evidence, scope, severity and remediation | No target access |

## Shared output contract
Each agent returns assessment_id, agent, status, scope_reference, summary, candidate_findings, limitations, sources, and recommended_next_step.

Status must be complete, blocked, or needs_human. Candidate findings include confidence and evidence quality. Do not label a vulnerability confirmed without reproducible evidence within authorized scope.

## Isolation
Agents do not share credentials. The orchestrator passes minimal sanitized task context. Tool permissions are assigned by the server, never requested or expanded by a model.
