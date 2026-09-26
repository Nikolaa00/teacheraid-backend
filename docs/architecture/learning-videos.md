# Learning videos

YouTube watch list for this backend, in **official build order**. One primary video per topic. Product spec and ADRs win over anything a tutorial does.

Companion to [build-and-architecture-guide.md](build-and-architecture-guide.md). Do not skip phases. Do not binge Phase 3–6 while identity is still next.

---

## DDD tactics we use (watch + read first)

TeacherAid uses **tactical DDD inside each module**, not a full strategic DDD programme with separately deployed bounded contexts. The spec is an invariant machine; these patterns keep rules in `domain` and frameworks at the edges.

| Pattern | What it means here | Primary modules |
|---|---|---|
| **Aggregate** | Consistency boundary — one root object owns invariants (`Session`, `Lesson`, `SchoolClass`) | `live-session`, `lesson`, `school` |
| **Value object** | Immutable typed ids and small concepts (`SchoolId`, `UserId`, `ClassId`, timer policy) | `sharedkernel`, all modules |
| **Domain event** | Immutable record of something that happened (`SessionEnded`, `AnswerAccepted`) — published after state change | `live-session`, `scoring` |
| **Port** | Interface the domain/application calls (`SessionRepository`, `GenerationPort`, `Clock`) | all modules |
| **Specification** | Reusable rule object (join eligibility, “can this student answer now?”) | `live-session` |
| **State machine** | Aggregate enforces legal transitions only | `live-session` (`lobby → … → ended`) |
| **Transactional outbox** | End session writes Postgres + outbox row in **one** transaction; scoring reacts after commit | `live-session` → `scoring` |
| **CQRS-lite** | Same Postgres; different read models for teacher console vs class-view STOMP (no names/inbox on class view) | `live-session` |
| **Append-only ledger** | Points are inserted, never updated | `scoring` |

**We do not use:** event sourcing as the system of record, a separate read database, Kafka in v1, or chatbot-style LLM flows.

### DDD + events + outbox + CQRS — YouTube

Watch in this order before Phase 3 live session work:

| Topic | Watch | How it maps here |
|---|---|---|
| Tactical DDD (Java + Spring) | [Oliver Drotbohm — Tactical DDD with Java and Spring](https://www.youtube.com/watch?v=k3o1nSh3g2Q) | Closest match to our stack: aggregates, jMolecules ideas, **Spring Modulith + events** |
| Tactical DDD (shorter intro) | [Introduction to Tactical DDD with Java](https://www.youtube.com/watch?v=XUQfA2pqZ2c) | Aggregates, domain services, application services, domain events |
| CQRS concept (5 min) | [CQRS Explained in 5 MINUTES](https://www.youtube.com/watch?v=eiut3FIY1Cg) | Commands vs queries — **not** “must use Kafka or second DB” |
| CQRS concept (CQS → CQRS) | [Really Simple CQRS](https://www.youtube.com/watch?v=_pjqhwYR1Kc) | Same idea; we use **CQRS-lite** inside one monolith |
| Commands vs events | [Commands, Queries, and Events](https://www.youtube.com/watch?v=G9LjwV8yDZU) | Good mental model for domain events; ignore microservices framing |
| Outbox pattern | [Outbox pattern — Spring Boot and Kafka](https://www.youtube.com/watch?v=fQKbUmqkzzg) | Learn dual-write in one DB transaction; **v1 uses Modulith event registry + outbox table, not Kafka** |
| Module interaction | [Spring Modulith events (official docs)](https://docs.spring.io/spring-modulith/reference/events.html) | `@ApplicationModuleListener`, event publication registry, post-commit delivery |

### Spring reference docs (official)

Use these alongside videos. Prefer Spring docs over random blog posts when they disagree with this repo.

| Topic | Official doc | TeacherAid note |
|---|---|---|
| **STOMP / WebSocket** | [Spring Framework — STOMP](https://docs.spring.io/spring-framework/reference/web/websocket/stomp.html) | Clients connect to `/ws`; class-view payloads exclude names and inbox (BR-7) |
| **Spring Data JPA** | [Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa/) | JPA entities live in `adapter.out` only — not on domain aggregates |
| **Database initialization** | [Spring Boot — SQL init](https://docs.spring.io/spring-boot/reference/sql/init.html) | **Flyway** owns schema in this repo; never `ddl-auto=update` against Postgres |
| **JUnit 5 (Jupiter)** | [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/) | Included via `spring-boot-starter-test` |
| **Spring Boot testing** | [Spring Boot — Testing](https://docs.spring.io/spring-boot/reference/testing/index.html) | ArchUnit + Testcontainers + slice tests (`@WebMvcTest`, `@DataJpaTest`) |
| **Method security** | [Spring Security — Method Security](https://docs.spring.io/spring-security/reference/servlet/authorization/method-security.html) | `@EnableMethodSecurity`, `@PreAuthorize` on application services |
| **Testing method security** | [Spring Security — Testing Method Security](https://docs.spring.io/spring-security/reference/servlet/test/method.html) | `@WithMockUser`, test the Spring-managed bean (not `new Service()`) |
| **Spring Modulith events** | [Spring Modulith — Application Events](https://docs.spring.io/spring-modulith/reference/events.html) | Cross-module traffic via events + application APIs |

---

## Practical order this month

1. Finish Amigoscode Spring Boot, then Mosh **DI + JPA/Flyway** only.
2. Modulith + hexagonal + ArchUnit (the three talks under Phase 0).
3. **DDD tactics block above** — Drotbohm tactical DDD + CQRS 5-min + outbox video (concept only).
4. JWT + method security (Amigoscode, Dan Vega JWT + `@PreAuthorize`).
5. Flyway + Testcontainers + JUnit 5 docs.
6. Stop. Do not jump to WebSockets or AI until Phase 1 slices ship.

---

## Do not watch (wrong product)

- Microservices / Spring Cloud / Kubernetes series
- Python LangChain agent bootcamps
- Chatbot UIs
- Spring Boot 4 + Spring AI 2.0 (this repo is Spring Boot 3.4+)
- Using Redis or H2 as the system of record
- Billing, Stripe, e-commerce (out of v1)

Copy ideas (typed commands, JSON out, tenant from the principal). Do not copy tutorial `ChatController` free-text prompts, `ddl-auto`, MySQL, or controller → service → repository as the application shape.

---

## Phase 0 — Spring Boot + skeleton

Skeleton is already in the repo. Watch these to understand what you are looking at.

| Topic | Watch | Skip in the video |
|---|---|---|
| Spring Boot, Java 21, Postgres, Docker, JPA, REST | [Amigoscode — Spring Boot Tutorial for Beginners (Full Course 2025)](https://www.youtube.com/watch?v=Cw0J6jYJtzw) | Recreating this project from start.spring.io |
| DI, then JPA / Flyway | [Mosh — Spring Boot Tutorial for Beginners](https://www.youtube.com/watch?v=gJrjgg1KVL4) | HTML views, DevTools, MVC as the product |
| REST, DTOs, validation (after DI) | [Mosh — REST API for an E-commerce Platform](https://www.youtube.com/watch?v=EWd3_I4X32g) | Carts, Stripe, deploy, MySQL as the TeacherAid store |
| Modular monolith | [The Modern Monolith, Spring Modulith](https://www.youtube.com/watch?v=Pae2D4XcEIg) | Microservices extraction |
| Hexagonal | [Hexagonal Architecture in Practice](https://www.youtube.com/watch?v=3siPsq17NAU) | Extra ceremony / extra Maven modules |
| ArchUnit | [Unit Test Your Spring Architecture](https://www.youtube.com/watch?v=sGmhaizFcEA) | PetClinic specifics |

**Skip entirely:** [First Spring Boot App (theory)](https://www.youtube.com/watch?v=M2U4_t_PSRM) (Spring Boot 4, H2) and [Master Spring Boot — 1st video](https://www.youtube.com/watch?v=_oDpUs65OiA) (freshers intro).

This repo is one process with hexagonal packages (`domain` / `application` / `adapter.in` / `adapter.out`). Domain stays free of Spring Web, JPA, Redis, and Spring AI.

---

## Phase 1 — identity / school / roster

Next when identity work is explicitly started.

| Topic | Watch / read | How it maps here |
|---|---|---|
| JWT + roles | [Spring Boot 3 + Security 6 JWT](https://www.youtube.com/watch?v=KxqlJblhzfI) | Teacher/admin password login. Put `school_id` and role in the token. Tenant comes from the principal, never from a query param. |
| Spring-native JWT | [Dan Vega — Spring Security JWT](https://www.youtube.com/watch?v=KYNR5js2cXE) | Prefer this *style* (OAuth2 resource server) over a hand-rolled JJWT filter copied from the first video. |
| Method security | [Dan Vega — Spring Method Security](https://www.youtube.com/watch?v=BizWiKrRa8Y) | `@EnableMethodSecurity`, `@PreAuthorize("hasRole('TEACHER')")` on application services. Also read [Method Security docs](https://docs.spring.io/spring-security/reference/servlet/authorization/method-security.html). |
| Method security (alt) | [Authorization Roles with Spring Security](https://www.youtube.com/watch?v=ZBeyy4Q3nIw) | Authentication vs authorization, `hasRole`, SpEL on `@PreAuthorize`. |
| Spring Data JPA | [Dan Vega — Learn Spring Boot 3 (JPA section ~1:02)](https://www.youtube.com/watch?v=-mwpoE0x0JQ) | Repositories in `adapter.out`; map to domain in the adapter. Also do [Accessing Data with JPA guide](https://spring.io/guides/gs/accessing-data-jpa/). |
| Database init | [Spring Boot — SQL initialization](https://docs.spring.io/spring-boot/reference/sql/init.html) | **Flyway** is the real init path here; read this to understand what Spring would auto-run (we disable that for domain schema). |
| Flyway | [Spring Boot 3 Flyway](https://www.youtube.com/watch?v=p1V5GcKUJv0) | SQL migrations only. Never `ddl-auto=update`. Every tenant table gets `school_id`. |
| JUnit 5 + Spring tests | [Amigoscode — Spring Boot (testing sections)](https://www.youtube.com/watch?v=Cw0J6jYJtzw) | `spring-boot-starter-test` = JUnit Jupiter + Mockito + AssertJ. Read [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/). |
| Testcontainers | [Spring Boot Testcontainers](https://www.youtube.com/watch?v=erp-7MCK5BU) | Integration tests against real Postgres, not H2. |
| Testing `@PreAuthorize` | [Testing Method Security docs](https://docs.spring.io/spring-security/reference/servlet/test/method.html) | `@WithMockUser(roles = "ADMIN")` — inject the proxied service bean. |
| OIDC (students) | [OAuth2 login with Google](https://www.youtube.com/watch?v=f1h4GkhxMp8) | Students only. Skip JTE/HTML login pages. **Do not build this until Assumption A is confirmed.** |
| API errors | [Spring 6 Problem Details](https://www.youtube.com/watch?v=4YyJUS_7rQE) | RFC 7807 under `/api/v1`. |

Passwords: those JWT videos use BCrypt. That is acceptable here (Argon2id or current Spring Security BCrypt).

---

## Phase 2 — curriculum / lessons

After identity. No LLM yet. Fixture lessons only.

| Topic | Watch | Notes |
|---|---|---|
| pgvector | [pgvector for developers](https://www.youtube.com/watch?v=MJHUVUXBRFE) | Language is Python; the SQL is what you need: `vector` column, distance query, index. Chunks live in Postgres, not a separate vector DB. |
| RAG (with Phase 4) | [Java + RAG, Spring AI](https://www.youtube.com/watch?v=6Pgmr7xMjiY) | Only when you generate lessons. RAG over a **loaded unit**. Three-sentence fallback skips RAG. |

No must-watch for copy-on-write school library or EU object storage. Read the spec and ADRs when you get there. Do not ingest textbooks until `rights_cleared_at` exists (Assumption B).

---

## Phase 3 — live session + scoring

Hardest phase. Do not start early. Complete the **DDD tactics** section above first.

| Topic | Watch / read | Notes |
|---|---|---|
| STOMP / WebSocket | [Spring Boot WebSockets](https://www.youtube.com/watch?v=rHSerbX_zsg) | Learn `/ws`, `/topic`, `@MessageMapping`. Skip the chat UI. |
| STOMP (official) | [Spring Framework — STOMP](https://docs.spring.io/spring-framework/reference/web/websocket/stomp.html) | Broker prefixes, `/topic` vs `/queue`, heartbeats. **Authoritative** for message flow. |
| CQRS-lite (reads) | [CQRS Explained in 5 MINUTES](https://www.youtube.com/watch?v=eiut3FIY1Cg) | Teacher **console** vs **class view** = two read projections; same Postgres. Class view: **no names, no inbox** (BR-7). |
| Domain events | [Oliver Drotbohm — Tactical DDD with Java and Spring](https://www.youtube.com/watch?v=k3o1nSh3g2Q) | `SessionEnded` → scoring listens; modules decouple via events, not shared JPA. |
| Modulith events | [Spring Modulith — Application Events](https://docs.spring.io/spring-modulith/reference/events.html) | Event publication registry, `@ApplicationModuleListener`, post-commit async. |
| Redis TTL | [Redis Crash Course](https://www.youtube.com/watch?v=jgpVdJB2sKQ) | Join tokens ≤ 30s, countdown `deadline_at`, presence. Postgres stays the source of truth. Skip using Redis as the database. |
| Outbox | [Outbox pattern, Spring + Kafka](https://www.youtube.com/watch?v=fQKbUmqkzzg) | Learn dual-write: End session writes DB + outbox in one transaction. You do **not** need Kafka for v1; Modulith events + outbox table is enough. |
| JUnit state tests | [JUnit 5 User Guide — nested tests](https://junit.org/junit5/docs/current/user-guide/#writing-tests-declarative) | Illegal session transitions: `@Test` per allowed edge + `@Test` per forbidden transition. |

State machine (`lobby → opener → teaching → review → ended`) and HMAC join tokens: implement in `live-session.domain` + illegal-transition tests. Photographed QR expires (EC-9). No chat-app STOMP tutorial replaces reading BR-7.

---

## Phase 4 — Spring AI generation

After live session. Not a chatbot (BR-17). `GenerationPort` is `generate` / `refine` only. Roster never reaches the LLM (FR-70).

| Topic | Watch | Notes |
|---|---|---|
| LLM mental model (1h) | [Karpathy — Intro to Large Language Models](https://www.youtube.com/watch?v=zjkBMFhNj_g) | Hallucinations, weights vs retrieved context, why you do not trust memory alone. |
| LLM mental model (3.5h) | [Karpathy — Deep Dive into LLMs like ChatGPT](https://www.youtube.com/watch?v=7xTGNNLPyMI) | Optional longer version of the same picture. Not neural-net-from-scratch. |
| Spring AI in Java | [Dan Vega — AI for Java Developers (Spring AI workshop)](https://www.youtube.com/watch?v=FzLABAppJfM) | ChatClient, structured JSON, prompt guarding, RAG. Skip multimodal, chat memory, tools/function calling, MCP. |

Spring AI lives in `generation.adapter.out` only. Teacher sends typed refine actions (Easier, Harder, More questions, Make opener debate/prediction, Shorter timers), not a free-text `userPrompt`. Local/dev uses a stub until an EU provider is chosen (E-27).

---

## Phase 5–6 — practice, reports, privacy, focus

Almost no YouTube. PDF reports, hashed `GET /r/{token}`, GDPR / ЗЗЛП export/delete, `push_token` as the only device id: follow the spec when you get there. A generic “make a PDF in Java” video will not teach headmaster-has-no-login or admin-must-not-see-answers (BR-4, BR-5).
