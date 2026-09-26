---
name: caching-performance
description: TeacherAid caching and performance — device, CDN, HTTP Cache-Control, Redis live clocks, Postgres as source of truth. Use when adding Redis TTL, @Cacheable, HTTP cache headers, CDN/object-storage images, offline/lesson cache, or performance work in this repository.
---

# Caching and performance (TeacherAid)

Read [AGENTS.md](../../../AGENTS.md) first. Cite FR/BR/EC/E. Empty cases stay empty (especially E-33, E-49). Human map: [docs/architecture/caching.md](../../../docs/architecture/caching.md). Redis role: [ADR 0007](../../../docs/adrs/0007-redis-live-clocks.md).

This backend states **contracts**. Teacher/admin web and student native live in **other repositories** — do not invent their APIs, service workers, or queue limits here.

## Layers (closest to the user first)

```
Device (IndexedDB / SQLite) → CDN (images + static) → HTTP Cache-Control → Redis (ephemeral live) → Postgres (truth)
```

| Layer | What | Never |
|---|---|---|
| Device | Teacher lesson (FR-43, EC-4); student answer queue (§5.9) | Invent queue TTL or post-End behaviour (E-33) |
| CDN | Question images (BR-21); frontend JS/CSS in other repos | Cache tenant- or session-scoped `/api/v1` JSON |
| HTTP | `Cache-Control` / ETag on **public or versioned** GETs only | Cache answers, inbox, roster, join tokens, report URLs |
| Redis | Join HMAC TTL ≤ 30s, `deadline_at`, presence, student `jti` | Answers, attendance, or `point_ledger` as cache |
| Postgres | System of record | Bypass with `@Cacheable` on writes or admin-sensitive queries (BR-4) |

## Phase map

| Phase | Caching? | What |
|---|---|---|
| 0 Skeleton | No | Docker Redis exists; no cache code |
| 1 Identity / roster | Almost none | Client holds JWT; no Redis user cache; no `@Cacheable` on roster |
| 2 Curriculum / lessons | First real need | Object storage + CDN for images (BR-21); optional short TTL on rights-cleared unit metadata only after measuring |
| 3 Live session + scoring | Required (hot path) | Redis clocks/join/presence only; answers → Postgres; FR-43 is a **client** contract; STOMP is push, not HTTP cache |
| 4 Generation | No result cache | Do not cache LLM output as truth; teacher reviews (BR-16); RAG hits Postgres/pgvector |
| 5 Practice / reports / privacy | Light | Short TTL on dashboard aggregates only after measuring; never put `/r/{token}` URLs on a shared CDN; admin queries still exclude answers (BR-4) |
| 6 Focus | None as product cache | Persist events; `push_token` is the only device id (FR-66) |

Do not add Spring `@Cacheable` in Phase 0–1.

## Backend performance rules

- Cache keys always include `schoolId` (and `classId` / `sessionId` when scoped). No global unkeyed lists.
- Prefer read-through + **explicit invalidate** on write. No Hibernate second-level cache in v1 unless measured.
- Redis loss (ADR 0007): refuse new joins, pause timers, still accept answers into Postgres.
- STOMP payloads are push. Class view: no names, no inbox (BR-7).
- Images: CDN or signed URLs; timer must not wait (BR-21).
- Never log cached answer bodies, join tokens, or report URLs.

## Examples

```java
// BAD — unkeyed roster cache; Redis as answer store
@Cacheable("roster")
List<Student> listRoster() { ... }

redis.opsForValue().set("answer:" + sessionId, answerBody);

// GOOD — tenant in the key; Redis is ephemeral join/clock only
@Cacheable(cacheNames = "unitMeta", key = "#schoolId + ':' + #unitId")
Optional<UnitMeta> unitMeta(SchoolId schoolId, UnitId unitId) { ... }

stringRedisTemplate.opsForValue()
    .set("join:%s:%s".formatted(sessionId, hmac), "1", Duration.ofSeconds(30));

// GOOD — private API must not be shared-cached
response.setHeader("Cache-Control", "private, no-store");
```
