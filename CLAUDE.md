# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What This Project Is

A full-stack web application for basketball referee coaching. Coaches evaluate referees via video reports (with timestamped comments, scores, and tags), and
referees can view feedback and participate in game discussions. Built with Spring Boot 4 (REST API) + Angular 21 (frontend), deployed on Heroku.

## Build & Run Commands

### Backend (Maven Wrapper)

```bash
./mvnw clean verify              # Full build with tests
./mvnw clean verify -DskipTests=true  # Build without tests
./mvnw test                      # Run all tests
./mvnw test -Dtest=VideoReportServiceTest          # Single test class
./mvnw test -Dtest=VideoReportServiceTest#someMethod  # Single test method
```

### Database (Docker required for local dev)

```bash
# Start local MySQL
docker compose -p referee-coach -f src/main/docker/mysql.yml up --build

# Start test MySQL (port 3333)
docker compose -p referee-coach -f src/test/docker/mysql-test.yml up --build
```

### Frontend (Angular, from project root)

```bash
cd src/main/webapp
npm start          # Dev server on port 4200
npm run lint       # ESLint
npm run build      # Production build (output: target/webapp/browser)
```

The Maven build compiles the Angular frontend automatically (via `frontend-maven-plugin`) and embeds it in the JAR. For local frontend development,
`application-local.properties` proxies API calls to Spring Boot running on port 8080.

### Dependency Updates

```bash
./mvnw -U versions:update-parent       # Update Spring Boot parent
./mvnw -U versions:update-properties   # Update dependency versions
```

## Architecture

### Backend Layers

```
REST Controllers (/api/*)
    └── Services (business logic)
        └── Repositories (Spring Data JPA)
            └── MySQL (Flyway migrations)
```

- **Controllers** (`*Resource.java`): `VideoReportResource`, `GameDiscussionResource`, `AuthenticationResource`, `BasketplanResource`, `AdminResource`
- **Services**: `VideoReportService`, `GameDiscussionService`, `SearchService`, `LoginService`, `BasketplanService`, `ExportService`
- **Domain entities**: `User`, `VideoReport`, `VideoComment`, `VideoCommentReply`, `GameDiscussion` and its comments/replies, `Tags`
- **DTOs**: MapStruct (`DTOMapper`) handles entity↔DTO conversion; TypeScript types auto-generated into `src/main/webapp/app/rest.ts` by a Maven plugin — do not
  edit this file manually
- **Security**: JWT tokens via `JwtService`/`JwtRequestFilter`; roles are `COACH`, `REFEREE`, `REFEREE_COACH`, `ADMIN`

### Key Configuration

- `application-local.properties` — local dev values (DB, mail, JWT secret, port 4200 CORS)
- `RefereeCoachProperties.java` — typed config for `referee.coach.*` properties
- `SecurityConfiguration.java` — CORS, JWT filter chain, public vs. protected routes
- `src/main/resources/db/migration/` — Flyway migrations (V1–V22+); always add new migrations here

### Testing

Integration tests extend `AbstractIntegrationTest`, which uses **Testcontainers** to spin up MySQL (so the test DB container must be reachable). Tests are in
service and repository layers; `ArchUnitTest` enforces layering rules.

## Deployment

- **Heroku** — `main` → production, `develop` → staging
- GitHub Actions runs tests + SonarCloud on push to both branches (`.github/workflows/maven.yml`)
- Java runtime version set in `system.properties` (currently Java 25)
- Production config supplied entirely via Heroku environment variables (datasource, JWT secret, mail, base URL)
