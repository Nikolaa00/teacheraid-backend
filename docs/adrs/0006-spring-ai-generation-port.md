# ADR 0006 — Spring AI behind GenerationPort

- Status: Accepted
- Spec: FR-14–FR-17, FR-25, FR-70, BR-16, BR-17, BR-55, E-27

## Context

The spec forbids an open-ended teacher chatbot (BR-17). Generation must stay inside the curriculum, be editable before students see it (BR-16), and must never receive the roster (FR-70). The provider is unspecified (E-27).

## Decision

`generation.domain.GenerationPort` exposes only `generate(GenerateCommand)` and `refine(RefineCommand)` with five typed actions: Easier, Harder, More questions, Make opener debate/prediction, Shorter timers. The Spring AI `ChatClient` adapter lives in `adapter.out`. Structured JSON output. RAG over `curriculum_chunk` for a loaded unit; three-sentence fallback skips RAG. Null `rights_cleared_at` does not call the LLM. A PII firewall fails closed. Provider is configuration; local/dev uses a stub until a provider is chosen.

## Consequences

`lesson` never imports Spring AI. A later tool-calling adapter can replace the current one. Characterization tests must prove student names/emails never reach `ChatClient`. No chat UI in v1.
