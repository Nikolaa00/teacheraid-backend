# ADR 0003 — School tenancy

- Status: Accepted
- Spec: BR-3, BR-4, BR-5, FR-12, FR-65

## Context

Each school is a data controller (BR-49). Teachers see only their classes (BR-3). Admins see usage, never answers (BR-4). The headmaster has no login (BR-5).

## Decision

Every tenant table has `school_id`. The authenticated principal supplies it; clients never choose a school id alone. Headmaster is `school.headmaster_email`, never a row in `app_user`. Support has null `school_id` and reaches school data only through a time-boxed `staff_access_grant`. Hibernate/Flyway tenant filters apply when schema work starts. Unique student email is per school.

## Consequences

Queries, not UI, enforce BR-3 and BR-4. `GET /r/{token}` is the only headmaster path. Cross-school library sharing is structurally impossible in v1 (BR-20).
