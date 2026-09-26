# ADR 0007 — Redis for live clocks and join tokens

- Status: Accepted
- Spec: FR-31, FR-42, BR-23, BR-24, EC-9

## Context

The join QR rotates every 30 seconds (BR-24). The server is timer authority (BR-23). The console must distinguish offline from unanswered (FR-42). These facts are hot and short-lived. Answers, attendance, and points must survive Redis loss.

## Decision

Redis holds join HMAC tokens (TTL ≤ 30s), countdown `deadline_at`, WebSocket presence, nudge clock, and the active student auth key. PostgreSQL remains the system of record for answers, attendance, and the point ledger. On Redis loss: refuse new joins, pause timers, still accept answers into Postgres.

## Consequences

Photographed QR codes expire (EC-9). Clients only render the server clock. Presence TTL plus heartbeat implements EC-3. Do not treat Redis as the answer store.
