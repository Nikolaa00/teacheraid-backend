# TeacherAid backend — agent instructions

Working spec: [docs/spec/tea-for-the-boys.html](docs/spec/tea-for-the-boys.html). How to read it: [docs/spec/README.md](docs/spec/README.md). Architecture: [docs/architecture/overview.md](docs/architecture/overview.md). Cite FR / BR / EC / UF / E. Where older notes disagree, **the specification wins** (BR-60).

Native iOS/Android live in another repository. This repo is the multi-platform API: teacher web, admin web, and student native call JSON `/api/v1` plus STOMP `/ws`. Headmaster has **no login** (`GET /r/{token}` only).

## Architecture freeze

Skeleton (Maven, Docker, empty hexagonal packages, Actuator health, ArchUnit) is in place. **Do not add** REST controllers, JPA entities, Flyway domain migrations, Security filter chains beyond health, ChatClient beans, or use-cases until identity/roster work is explicitly started.

Empty cases stay empty. Do not invent UX, extra APIs, or product behaviour. Record an ADR if an engineering default is required to unstick a schema.

Build order: identity/roster → lesson fixtures → live session → Spring AI → practice/reports → focus APIs. Do not skip to generation or live session.

**Out of v1:** universities, practical lessons, e-Дневник sync, parent login, billing, national library, streaks, chatbot, class reassignment, co-teaching, grades, GPS, extra device ids, Albanian UI.

## Stack

Java 21 · Spring Boot 3.4+ · Spring Modulith · PostgreSQL + pgvector · Redis · Flyway (when schema starts) · Spring Security (password JWT + student OIDC) · Spring AI `ChatClient` behind `GenerationPort` · STOMP `/ws` · Actuator · Testcontainers · ArchUnit.

Passwords: Argon2id or current Spring Security BCrypt. Tenant is `school_id` from the principal, never a client-supplied school id. Headmaster is `school.headmaster_email`, never an `app_user`.

Postgres MCP, when added, is localhost `teacheraid_dev` only — never a school database.

## Modules

Package `com.teacheraid.<module>.{domain, application, adapter.in, adapter.out}`:

| Module | Owns |
|---|---|
| `identity` | teacher/admin password JWT; student OIDC; one active student session; hashed invites/resets |
| `school` | tenant, classes, roster, one teacher per class, distraction list, `headmaster_email` |
| `curriculum` | subjects, years, units, chunks + embeddings; `rights_cleared_at` |
| `lesson` | reusable lesson, questions, media, school-library copy-on-write |
| `generation` | `GenerationPort` only (`generate` / `refine`). No chat UI |
| `live-session` | run, join, attendance, answers, inbox, skip, ad-hoc, nudge, STOMP |
| `scoring` | append-only ledger, seasons, leaderboards |
| `practice` | sets from that session’s missed questions only |
| `reporting` | dashboards, monthly PDF, unguessable `/r/{token}` |
| `privacy` | staff grants, audit log, export/delete tickets, retention |
| `focus` | authorization flag, `LEFT_FOREGROUND` events, present-without-phone |

Shared kernel stays tiny: `SchoolId`, `UserId`, `ClassId`, `Clock`. Cross-module traffic is application APIs and outbox events, not shared JPA entities.

## Patterns

Hexagonal ports/adapters · aggregates (`Lesson`, `Session`, `SchoolClass`) · session state machine `lobby → opener → teaching → review → ended` · session snapshot into `session_question` · strategy for four question types and five typed refinements · join specification · CQRS-lite STOMP projectors (class-view has no names and no inbox) · answer idempotency · transactional outbox on End · clock port · PII firewall (roster never to LLM) · append-only `point_ledger` · soft-delete enrollments · copy-on-write library.

Not microservices. Not classic three-layer MVC as the app. Not Clean Architecture ceremony. Not full strategic DDD.

## Security and privacy (always)

- Teacher scoped to `class.teacher_id = principal` (BR-3).
- Admin **must not** read answers, reasons, anonymous bodies, or per-topic student results (BR-4). Enforce in queries, not UI.
- Class-view STOMP payloads: no names, no inbox (BR-7).
- Join tokens HMAC, TTL ≤ 30s, hashed invites/report tokens at rest.
- One active student session (FR-6).
- Never log answer bodies, reasons, inbox text, passwords, join tokens, report URLs.
- No IDFV/GAID; `push_token` is the only device identifier (FR-66).
- Roster never sent to an LLM (FR-70). Support access only via time-boxed `staff_access_grant`.

When writing Java, follow `.cursor/skills/spring-boot-professional/SKILL.md`.

## Git

Remote: [github.com/Nikolaa00/teacheraid-backend](https://github.com/Nikolaa00/teacheraid-backend). Branch from `main` per phase (`feature/phase-1-identity`). Short descriptive commits (one sub-phase when possible). Always open a pull request into `main`. See `.cursor/skills/git-workflow/SKILL.md`.
