# TeacherAid backend

Multi-platform API for TeacherAid v1 (Spring Boot modular monolith). Native student apps live in another repository.

Start here: [AGENTS.md](AGENTS.md). Spec: [docs/spec/tea-for-the-boys.html](docs/spec/tea-for-the-boys.html). Architecture: [docs/architecture/overview.md](docs/architecture/overview.md).

## Skeleton only

Hexagonal skeleton plus Flyway `V1` school/roster tables. No REST feature APIs or JPA entities yet (Phase 1.2+).

```bash
docker compose up -d   # Postgres + Redis; required for local boot
./mvnw test            # Windows: mvnw.cmd test — needs Docker for Testcontainers
```

Health (after `./mvnw spring-boot:run`): `GET /actuator/health`.

Local databases (when Docker is running): PostgreSQL `teacheraid_dev` on port 5432, Redis on 6379. Flyway runs on startup (`ddl-auto=none`). Copy `.env.example` to `.env` if you need local overrides. Never point tools at a school database. Redis auto-configuration stays excluded until live-session work.
