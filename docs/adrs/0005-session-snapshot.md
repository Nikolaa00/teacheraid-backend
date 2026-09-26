# ADR 0005 — Session snapshot on run

- Status: Accepted
- Spec: FR-24, BR-19, EC-13, BR-31

## Context

A lesson is authored once and run many times. Editing the lesson between runs must not rewrite earlier results. Live text editing during a session is forbidden (BR-31).

## Decision

When a session starts, copy opener, questions, timers, and scoring flags into `session_question` (and related session-owned rows). Answers reference `session_question_id`, never live `question_id`. Skip sets `skipped` and creates no scoring rows. One non-ended session per teacher (E-51 is empty — fail closed).

## Consequences

History stays comparable across classes (EC-13). Library edits affect later runs only. The session aggregate owns the state machine `lobby → opener → teaching → review → ended`.
