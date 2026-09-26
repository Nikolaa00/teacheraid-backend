# Architecture overview

Build roadmap and learning guide: [build-and-architecture-guide.md](build-and-architecture-guide.md). YouTube watch list: [learning-videos.md](learning-videos.md). Caching: [caching.md](caching.md).

TeacherAid v1 is a **modular monolith** (Spring Modulith) with **hexagonal modules** and **DDD tactical patterns** where invariants exist. One Spring Boot process, one PostgreSQL, one Redis. Multi-platform clients share one API.

Canonical decisions: [ADRs](../adrs/README.md). Agent rules: [AGENTS.md](../../AGENTS.md). Spec: [tea-for-the-boys.html](../spec/tea-for-the-boys.html).

## Why this shape

- **Modular monolith** — one live classroom needs join, timer, answers, and End in one consistency boundary (FR-44, BR-23). Extract a module later if needed.
- **Hexagonal internals** — domain does not import Spring Web, JPA, Redis, or Spring AI. Web, persistence, and the LLM are adapters.
- **DDD tactics, not a DDD programme** — aggregates, value objects, a session state machine, join specification, append-only ledger. Not separately deployed bounded contexts.

Not microservices. Not classic Controller–Service–Repository as the application. Not Clean Architecture ceremony (four rings, a mapper on every hop).

## Clients (other repositories)

- Teacher web — authoring, console, class view, dashboard
- School admin web — roster, usage (never answers), staff-access log
- Student native iOS/Android — join, answer, focus lock
- Headmaster — **no account**; monthly PDF + `GET /r/{token}`
- Support — time-boxed `staff_access_grant` only

## Modules

`identity` `school` `curriculum` `lesson` `generation` `live-session` `scoring` `practice` `reporting` `privacy` `focus`

Package layout: `com.teacheraid.<module>.{domain, application, adapter.in, adapter.out}`.

Shared kernel: `SchoolId`, `UserId`, `ClassId`, `Clock` only.

## Data stores

- **PostgreSQL** — system of record, including `pgvector` for curriculum chunks.
- **Redis** — join HMAC TTL ≤ 30s, countdown `deadline_at`, WS presence, nudge clock, active student `jti`. Durable answers stay in Postgres. Redis loss: refuse new joins, pause timers, still accept answers.
- **Object storage (EU)** — question images; timer must not wait for load (BR-21).

## Auth audiences

- Teacher / school admin — email + password JWT (BR-6). No magic links, no school SSO for teachers.
- Student — school-issued email is identity (BR-1). OIDC (Google Workspace or Entra). Blocked until Assumption A is confirmed.
- Support — separate staff IdP; school data only via grant.
- Headmaster — `school.headmaster_email`, never `app_user` (BR-5).

Tenant is always `school_id` from the principal.

## Build order

1. Identity / school / roster
2. Curriculum + lesson fixtures (no LLM)
3. Live session + scoring
4. Spring AI behind `GenerationPort`
5. Practice, reports, privacy
6. Focus APIs

Sub-phases (`1.1` … `6.3`), one slice at a time: [build-and-architecture-guide.md](build-and-architecture-guide.md). Do not skip. Feature Flyway, entities, and controllers wait until identity work is explicitly started.

## Sign-off still required before shipping auth or ingest

- Assumption A — student mailboxes
- Assumption B — e-учебник rights (`rights_cleared_at`)
- Timers (FR-23) and points (FR-74) — proposed; store as `timer_policy` / `scoring_policy`
- EU region / AI provider (E-41, E-27)
