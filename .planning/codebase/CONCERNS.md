# Technical Concerns (Backend)

## Technical Debt

- **Redis Sync**: Lineup data currently uses non-persistent Redis caching. Ensure synchronization logic is robust against Redis restarts and network partitions.
- **Legacy Logging**: Cleaning up legacy Lombok-based logging (@Slf4j) in favor of standardized SLF4J/Logback patterns to reduce "magic" annotations.
- **API Versioning**: Current endpoints lack formal versioning prefix in some areas; need to standardize on `/api/v1/` for all public controllers.
- **DTO Duplication**: Some request/response DTOs share similar structures; consider consolidation if business rules allow.

## Fragile Areas

- **Timezone Management**: Discrepancies in weather data mapping due to timezone/KMA timing issues. Phase 1 remediation complete, but needs continuous monitoring.
- **Multi-Session Support**: Currently limited session management for multi-device environments. WebSocket logic assumes single active session per user in some registry implementations.
- **Error Propagation**: Standardizing how domain-specific exceptions are translated into ApiResponse error codes across all service layers to avoid leakage.

## Future Work

- **Sentry Integration**: Planned for improved error observability and crash reporting in production environments.
- **ReadOnly Trade Rooms**: Implement auto-deletion and read-only status for trade rooms after 6 hours to reduce Redis memory footprint and stale state.
- **Query Optimization**: Audit QueryDSL queries for potential N+1 issues or missing indexes on high-traffic endpoints.
- **Infrastructure Automation**: Containerizing the dev environment with Docker Compose for consistent local testing.
