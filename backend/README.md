# DSA QUEST backend

The backend is a Spring Boot service using Java 21, Maven, PostgreSQL, Flyway, JPA, Bean Validation, Actuator, and Springdoc OpenAPI.

## Structure

```text
src/main/java/com/dsaquest/
  config/       Cross-cutting application configuration
  controller/   HTTP endpoints
  dto/          API request and response types
  entity/       Reserved for future persistence models
  exception/    Shared exception handling
  repository/   Reserved for future Spring Data repositories
  service/      Reserved for future application services
src/main/resources/
  db/migration/ Flyway migrations
  application.yml
```

Stage 2 provides only health, error handling, API documentation, and database wiring. Business entities and APIs are intentionally deferred.

## Run

From the repository root, start PostgreSQL with `docker compose up -d postgres`, then run:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

The service listens on `http://localhost:8080`.

When port `5432` is already occupied, choose another host port without
changing the container port:

```powershell
$env:DB_PORT = "55432"
docker compose up -d postgres
$env:DB_URL = "jdbc:postgresql://localhost:55432/dsaquest"
```

The database integration test is opt-in because it requires a running
PostgreSQL instance. With the Compose service running:

```powershell
$env:DSA_QUEST_DB_INTEGRATION = "true"
$env:DB_URL = "jdbc:postgresql://localhost:55432/dsaquest"
$env:DB_USERNAME = "dsaquest"
$env:DB_PASSWORD = "your-local-password"
.\mvnw.cmd -Dtest=DsaQuestApplicationIntegrationTest test
```

## Endpoints

- `GET /api/v1/health`
- `GET /actuator/health` (includes database component status)
- `GET /swagger-ui.html`
- `GET /v3/api-docs`
