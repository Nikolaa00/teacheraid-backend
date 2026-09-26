# ADR 0002 — Hexagonal modules

- Status: Accepted
- Spec: BR-7, BR-4, FR-70, BR-17

## Context

Classic Controller–Service–Repository leaks invariants into `@Service` if-else and lets JPA entities become the model. Admin must not see answers (BR-4); class-view STOMP must not carry names or inbox (BR-7); the roster must never reach an LLM (FR-70). Spring AI, Redis, and STOMP would otherwise sit in the same service class.

Full Clean Architecture (four rings, a port per table) adds ceremony without extra safety for a single-team pilot.

## Decision

Each module is hexagonal: `domain`, `application`, `adapter.in`, `adapter.out`. Domain does not import Spring Web, JPA, Redis, or Spring AI. Thin MVC adapters exist only at the edges (`adapter.in.web`). Use DDD tactics (aggregates, value objects, specifications, domain events) where the spec is an invariant machine. Do not run a strategic DDD programme with separately deployed contexts.

## Consequences

ArchUnit can forbid illegal imports. Generation can swap providers without changing `lesson`. Tests freeze a `Clock` port. Feature work starts inside a module, not a global service layer.
