---
name: spring-boot-professional
description: Professional Spring Boot 3.4+ and Spring AI coding for TeacherAid. Use when writing or reviewing Java, JPA, Flyway, REST, WebSocket, hexagonal modules, tactical DDD (aggregates, value objects, use-cases, ports), Testcontainers, ArchUnit, Spring Security, or Spring AI ChatClient in this repository.
---

# Spring Boot professional (TeacherAid)

Read [AGENTS.md](../../../AGENTS.md) first. Cite FR/BR/EC/UF/E. Empty cases stay empty.

## Stack

Java 21, Spring Boot 3.4+, Spring Modulith, constructor injection only, records for commands/queries/events. Prefer explicit configuration over magic.

Do not use Lombok `@Data` / `@EqualsAndHashCode` on aggregates or entities. MapStruct only if mapping is mechanical and tested.

## Module layout

```
com.teacheraid.<module>
  domain          # aggregates, value objects, domain events, ports (interfaces)
  application     # use-cases, @Transactional here only
  adapter.in      # web, STOMP, schedulers
  adapter.out     # JPA, Redis, mail, Spring AI, object storage
```

`domain` must not import `org.springframework.web`, `org.springframework.ai`, `org.springframework.data.redis`, or JDBC/JPA annotations on the aggregate itself. Persistence models live in `adapter.out`. Cross-module traffic: application APIs and outbox events, never shared JPA entities. Shared kernel: `SchoolId`, `UserId`, `ClassId`, `Clock` only.

Longer narrative: [docs/architecture/build-and-architecture-guide.md](../../../docs/architecture/build-and-architecture-guide.md) (§6.1). Caching and Redis TTL: [caching-performance](../caching-performance/SKILL.md).

## Tactical DDD — what goes where

Not a strategic DDD programme. Use tactics where the spec is an invariant machine. Primary aggregates: `Lesson`, `Session`, `SchoolClass`.

| Building block | Package | TeacherAid rules |
|---|---|---|
| **Aggregate root** | `domain` | Owns invariants and state transitions. Outside code references by id only (`SessionId`, `ClassId`). No `@Entity`, no Spring. |
| **Entity (inside aggregate)** | `domain` | Child objects with identity under the root (e.g. enrollment line on `SchoolClass`). Still not JPA. |
| **Value object** | `domain` / `sharedkernel` | Immutable; equality by value. Prefer Java `record` (`SchoolId`, money-like types, address-like types). |
| **Domain event** | `domain` | Past-tense record (`SessionEnded`). Raised from aggregate; published after save from `application`. |
| **Port** | `domain` | Interface for outbound needs (`GenerationPort`, `Clock`, mail, storage). |
| **Repository** | `domain` (port) + `adapter.out` (impl) | Interface in domain: `SessionRepository.load/save`. JPA `*Entity` and `Jpa*Repository` only in `adapter.out`. |
| **Domain service** | `domain` | Stateless logic spanning **multiple aggregates** when it does not fit one root. Prefer aggregate methods first. |
| **Factory** | `domain` (usually) | Safe construction of complex aggregates. Use when `new`/builder would bypass invariants. |
| **Use case** | `application` | One class per operation (`EnrollStudentUseCase.run(EnrollStudentCommand)`). `@Transactional` here only. Orchestrate; do not replace aggregate rules. |
| **Command / query** | `application` or passed into domain | Java `record`. Not HTTP DTOs — map in `adapter.in`. |

**JPA `@Entity` is not a domain entity.** Persistence types are `*Entity` in `adapter.out`; map to/from domain in the adapter. Flyway owns schema; `ddl-auto=none`.

**Use case vs Laravel-style Action:** Same slot as an Action (one operation per class). Do not put business rules inside the use case or a separate `Actions` package — rules stay on aggregates. Controllers stay thin in `adapter.in`.

**Request path:** `adapter.in` → command record → `application` use case → aggregate method → port → `adapter.out`.

## REST and errors

JSON under `/api/v1`. RFC 7807 (`application/problem+json`). No stack traces to clients. Idempotency keys on answer submit (`clientAnswerId`). Tenant from `principal.schoolId()`, never `request.getParameter("schoolId")`.

## Persistence

Flyway migrations, never Hibernate `ddl-auto=update` in any profile that talks to Postgres. UUID PKs, `timestamptz`. No `grade` column. Session answers reference `session_question_id`. Soft-delete enrollments (`left_at`).

## Security

Method security + query filters. Teacher: `class.teacher_id = principal`. Admin queries must not select answers, reasons, inbox bodies, or per-topic student results. Passwords: Argon2id or current Spring Security BCrypt. Hashed tokens at rest. Join HMAC TTL ≤ 30s.

## Spring AI

Only `generation.adapter.out`. Port: `generate` / `refine` with five typed commands. Structured output. Never send roster or names. Fail closed if `rights_cleared_at` is null. No chat UI. No `userPrompt` string from the client.

## Logging and tracing

Structured logs with `schoolId` / `sessionId`. Never log answer bodies, reasons, inbox text, passwords, join tokens, report URLs. Span names: Join, Fire, SubmitAnswer, End, Generate — no stems in attributes.

## Tests

ArchUnit first on illegal imports. Testcontainers for Postgres and Redis. Modulith `ApplicationModules.verify()`. State-machine tests for illegal session transitions. Characterization test: a school email in emphasis must not reach `ChatClient`. Slice tests for web; no `@SpringBootTest` for every mapper.

## Examples

```java
// BAD
String schoolId = request.getParameter("schoolId");
log.info("answer={}", answer.getBody());

// GOOD
SchoolId schoolId = principal.schoolId();
log.info("answer accepted", kv("sessionId", sessionId), kv("studentId", studentId));
```

```java
// BAD — domain depending on Spring AI
class LessonService {
  private final ChatClient chat;
}

// GOOD
public interface GenerationPort {
  GeneratedLesson generate(GenerateCommand command);
  GeneratedLesson refine(RefineCommand command);
}
```

```java
// BAD — business rules in application / Action-style class
@Service
class EnrollStudentUseCase {
  void run(EnrollStudentCommand cmd) {
    if (!enrollmentRepo.exists(...)) { /* invariant belongs on SchoolClass */ }
    enrollmentRepo.save(...);
  }
}

// GOOD — use case orchestrates; aggregate enforces
@Service
class EnrollStudentUseCase {
  @Transactional
  void run(EnrollStudentCommand cmd) {
    SchoolClass cls = schoolClassRepository.load(cmd.classId());
    cls.enrollStudent(cmd.studentId(), cmd.schoolId());
    schoolClassRepository.save(cls);
  }
}
```
