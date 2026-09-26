# TeacherAid backend

Multi-platform API for TeacherAid v1 (Spring Boot modular monolith). Native student apps live in another repository.

Start here: [AGENTS.md](AGENTS.md). Spec: [docs/spec/tea-for-the-boys.html](docs/spec/tea-for-the-boys.html). Architecture: [docs/architecture/overview.md](docs/architecture/overview.md).

## Skeleton only

This tree is an empty hexagonal skeleton: health endpoint, Docker dependencies, ArchUnit. No REST feature APIs, no JPA entities, no Flyway domain schema.

```bash
docker compose up -d   # Postgres + Redis; requires Docker Desktop
./mvnw test            # Windows: mvnw.cmd test — does not need Docker yet
```

Health (after `./mvnw spring-boot:run`): `GET /actuator/health`.

Local databases (when Docker is running): PostgreSQL `teacheraid_dev` on port 5432, Redis on 6379. Copy `.env.example` to `.env` if you need local overrides. Never point tools at a school database. Datasource, JPA, Flyway, and Redis auto-configuration are excluded until identity/schema work starts.
