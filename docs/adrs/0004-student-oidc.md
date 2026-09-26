# ADR 0004 — Student OIDC identity

- Status: Accepted (pending product sign-off — Assumption A)
- Spec: FR-5, FR-6, BR-1, BR-2, BR-6

## Context

A student’s identity is the school-issued email (BR-1). Teachers sign in with email and password; no magic links, no SSO for teachers (BR-6). One active student sign-in at a time (FR-6). Assumption A (every target school issues mailboxes) is unverified; if false, student login collapses.

## Decision

Students authenticate with OIDC (Google Workspace or Microsoft Entra), not passwords. Store `student_oidc_link` and a single `student_device_session`; a second device revokes the previous `jti`. Teachers and school admins use password JWT. Do not build student auth until Assumption A is confirmed with the pilot school.

## Consequences

Impersonation via “type your name” is impossible. Password-reset UX is teacher/admin only and must be fast on a phone (FR-2). Invite and first-enrolment screens are empty cases (E-12, E-15): hashed tokens and routes only, no invented UX.
