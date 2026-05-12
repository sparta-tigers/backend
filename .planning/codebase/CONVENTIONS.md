# Coding Conventions (Backend)

## Code Style

- **Explicit Data Fetching**: Prefer clear JPA queries or QueryDSL over nested FetchJoins. Avoid relying on Hibernate's lazy loading magic for complex object graphs.
- **Transactional Scope**: Minimize `@Transactional` range to avoid DB connection exhaustion. Do not wrap external API calls (e.g., KMA, Firebase) inside a database transaction.
- **DTO Mapping**: Use explicit static factory methods (`from`, `of`) instead of mapping libraries like MapStruct. This ensures transparency in how data is transformed between layers.
- **Zero-Lombok Policy (Moving towards)**: Gradually reducing reliance on Lombok annotations that hide implementation details (e.g., `@AllArgsConstructor` is fine, but avoid complex `@Data` on entities).

## Error Handling

- **ApiResponse**: All public APIs must return `ApiResponse<T>` to maintain a consistent contract with the frontend.
- **Exception Handling**: Use `ExceptionCode` enum for consistent error messaging. Custom exceptions should inherit from a base domain exception.
- **Validation**: Use `@Valid` and Bean Validation annotations (e.g., `@NotBlank`, `@Size`) in DTOs to catch invalid input at the entry point.

## Documentation

- **RELEASE_NOTES.md**: Track version-specific changes and breaking API modifications.
- **backend-context.txt**: Maintain high-level architectural context for AI agents to understand the 'Why' behind core services.
- **JSDoc Style (JavaDoc)**: Use JavaDoc to explain complex business rules in service methods.
