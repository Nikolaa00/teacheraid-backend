# ADR 0001 — Modular monolith

- Status: Accepted
- Spec: FR-44, BR-23, BR-11, E-05

## Context

TeacherAid is one product: a theory lesson in a Macedonian secondary classroom. A live session of ~35 students bursts answers in 15–45 seconds. End must settle points, write attendance, and archive once. There is no named engineering organisation.

## Decision

Ship one Spring Boot 3.4+ application with Spring Modulith package boundaries (`com.teacheraid.<module>`). One PostgreSQL, one Redis. Clients (teacher web, admin web, student native) call this API. Do not deploy microservices in v1.

## Consequences

Join, timer authority, answers, and End stay in one consistency boundary. Modules can be extracted later. Operational cost stays one JVM. Cross-module calls go through application APIs and outbox events, not shared JPA entities.
