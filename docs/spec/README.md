# TeacherAid v1 specification

Working document: [tea-for-the-boys.html](tea-for-the-boys.html) (organized 17 September 2026 from `feature-spec-v1.md`, dated 15 September 2026).

Status in the source: **draft**. Where this file disagrees with older product notes, **this specification is right** (BR-60). Items marked `(verify)` are unchecked and must not be treated as fact (BR-61).

## How to read

The HTML is seven categories plus an appendix. Each item has a stable id.

| Prefix | Meaning |
|---|---|
| FR | Functional requirement — what to build |
| BR | Business rule — invariant regardless of screen |
| UF | User flow — sequenced behaviour the spec actually spells out |
| EC | Edge case the spec anticipates |
| E | Empty case — the spec does not say; leave open |
| CTX | Product context |

Skip the user-stories slide (E-09) and the acceptance slide (E-58). They are empty. Do not derive stories or “done when” statements on the team’s behalf.

## Cite in code and tickets

Use the identifiers in Javadoc, tests, ADRs, and commit messages (`FR-44`, `BR-7`, `EC-1`). Prefer the spec over any architecture summary if they conflict.

## Empty cases

Empty cases stay empty. Do not invent UX, extra APIs, or product behaviour to fill them. If an engineering default is required to unstick a schema (for example one open session per teacher), record it in an ADR and fail closed.

Timers (FR-23) and point values (FR-74) are **proposed, not signed**. Store them as configuration when schema work starts; do not hard-code them as product truth.

## Blocking assumptions

- **A** — every target school issues student mailboxes. If false, student identity collapses. Confirm before any auth build.
- **B** — e-учебник rights with МОН/БРО. Do not ship textbook ingest until written clearance exists (`rights_cleared_at`).
- **F** — Apple Family Controls entitlement. Native apps are a separate repository.

## Out of v1

Universities, practical/laboratory lessons, e-Дневник sync, parent login, billing, national library, streaks, open teacher chatbot, class reassignment, co-teaching, grades, GPS, extra device ids, Albanian UI.
