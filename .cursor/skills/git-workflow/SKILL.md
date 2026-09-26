---
name: git-workflow
description: TeacherAid GitHub workflow — short descriptive commits, feature branches from main per phase, always open a pull request. Use when committing, branching, pushing, or opening a PR.
---

# Git workflow (TeacherAid)

Remote: `https://github.com/Nikolaa00/teacheraid-backend.git`. Default branch: `main`. Never commit feature work directly to `main`.

## Branches

| When | Branch from `main` | Example |
|---|---|---|
| New parent phase | `feature/phase-N-<short>` | `feature/phase-1-identity` |
| Optional one sub-phase PR | `feature/<id>-<short>` | `feature/1.1-flyway` |
| Docs / agent rules only | `chore/<short>` or `docs/<short>` | `chore/git-workflow` |

```bash
git checkout main
git pull origin main
git checkout -b feature/phase-1-identity
```

Do not start Phase 2 until Phase 1 is merged to `main`. Refresh `main` before every new branch.

## Commits

Small, one concern each. Prefer one commit per finished sub-phase (`1.1`, `1.2`, …).

Format (subject ≤ ~72 chars). Imperative. Say **why** if it is not obvious:

```
Add Flyway baseline for school and roster tables.

Enable migrations with ddl-auto=none so Hibernate cannot change schema.
```

Prefixes when useful: `Add`, `Fix`, `Update`, `Remove`, `Document`. Do not write “WIP”, “misc”, or “updates”.

Never commit `.env`, school data, passwords, join tokens, or `target/`.

## Pull requests

Always open a PR into `main`. Do not merge your own work on `main` by pushing the phase branch as default.

```bash
git push -u origin HEAD
gh pr create --base main --title "Phase 1.1: Flyway school and roster schema" --body "..."
```

PR title: phase/sub-phase + outcome. Body: what changed, how to test (`./mvnw test`, `docker compose up` when needed). Cite FR/BR when the change is product-facing.

After merge: delete the feature branch; next phase starts from updated `main`.
