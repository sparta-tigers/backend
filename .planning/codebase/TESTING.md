# Testing Patterns (Backend)

## Infrastructure

- **JUnit 5**: Primary testing framework for unit and integration tests.
- **H2 Database**: In-memory database used for test runtime isolation to ensure fast and deterministic execution.
- **MockMvc**: Used for controller testing to verify HTTP status, response body, and validation logic without spawning a full server.
- **Testcontainers**: (Potential future addition) for more realistic Redis/MySQL integration testing in CI environments.

## Strategy

- **Unit Testing**: Focused on service logic and domain rules. Target 80% coverage for core business entities and complex calculation logic.
- **Integration Testing**: Verified DB interactions (JPA/QueryDSL) and Redis pub/sub flows. Ensures that the persistence layer behaves as expected with the real schema.
- **E2E Testing**: Verified via manual verification by the developers and documented in `docs/qa-reports/`.

## Patterns

- **Standardized Response Validation**: Verify `resultType` (SUCCESS/ERROR) and `data` structure in all API tests to ensure frontend compatibility.
- **Fail-Fast Assertion**: Use AssertJ for readable and robust assertions (e.g., `assertThat(result).isNotNull()`).
- **Transaction Rollback**: Ensure all integration tests are `@Transactional` to maintain database cleanliness between test runs.
- **Mocking**: Use `@MockBean` to isolate the system under test from external services like S3 or Firebase.
