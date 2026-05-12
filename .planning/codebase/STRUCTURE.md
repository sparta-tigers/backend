# Directory Structure (Backend)

## Key Locations

- `src/main/java/com/sparta/spartatigers/domain/`: Core business logic organized into domain-driven sub-packages.
  - `chat/`: General chat room management and messaging entities.
  - `stompchat/`: Implementation-specific logic for WebSocket and STOMP message broadcasting.
  - `directRoom/`: Private room logic specifically for the trade/exchange domain.
  - `match/`: Match schedule, live scoreboard, and team statistics logic.
  - `dashboard/`: Aggregation logic for the main landing page data.
- `src/main/java/com/sparta/spartatigers/global/`: Cross-cutting concerns that apply across the entire application.
  - `config/`: Spring configurations for Security, Redis, WebSocket (STOMP), and AWS S3.
  - `exception/`: Global Exception Handler and domain-specific error codes.
  - `response/`: Standardized `ApiResponse` wrapping logic.
- `src/main/resources/`: Contains `application.yml`, static assets, and Flyway migration scripts in `db/migration/`.
- `docs/`: Includes API specifications (OpenAPI/Swagger) and technical design documents.

## Naming Conventions

- **Class Names**: Always use PascalCase (e.g., `StadiumService.java`, `UserPrincipal.java`).
- **Interfaces**: Use PascalCase without prefixes like 'I' or suffixes like 'Interface'.
- **Repository Methods**: Follow Spring Data JPA query method naming conventions (e.g., `findByEmail`).
- **Endpoint URIs**: Exclusively use kebab-case for all REST endpoints (e.g., `/api/v1/user-profiles`).

## Organization Principles

- **Cohesion over Coupling**: Keep logic related to a single domain within its package.
- **Layered Flow**: Strictly maintain the flow of Request -> Controller -> Service -> Repository.
