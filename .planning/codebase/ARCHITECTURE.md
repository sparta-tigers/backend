# Codebase Architecture (Backend)

## Pattern
- **Domain-Driven Design (DDD)**: Logic organized by domain boundaries in `com.sparta.spartatigers.domain`.
- **Zero-Magic**: Transparent persistence and query management. Detest over-engineering.

## Layers
1. **Controller Layer**: REST endpoints and request validation.
2. **Service Layer**: Business logic orchestration.
3. **Domain/Model Layer**: Entities and business rules.
4. **Repository Layer**: JPA and QueryDSL-based data access.
5. **Global Layer**: Shared configurations, exception handlers, and security.

## Data Flow
- **Request -> Response**: Controller -> Service -> Repository -> Entity -> DTO.
- **Real-time**: STOMP Handler -> Redis Pub/Sub -> STOMP Broker -> Clients.

## Abstractions
- **ApiResponse**: Standardized success/error response structure.
- **ExceptionCode**: Enum-based error management.
