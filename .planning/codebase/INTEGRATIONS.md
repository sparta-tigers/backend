# External Integrations (Backend)

## Services

- **Firebase Admin SDK**: Used for sending real-time push notifications to mobile clients. Requires `firebase-key.json` for authentication.
- **AWS S3**: Primary storage for user-uploaded images, trade item photos, and profile pictures. Integrated via `spring-cloud-starter-aws`.
- **Korea Meteorological Administration (KMA) API**: Fetches real-time weather and forecast data for stadium locations using a specialized service layer.
- **Google Cloud Vision API**: Utilized for advanced image analysis, specifically for OCR verification of stadium grids to ensure data integrity in trade posts.

## Data Persistence & Messaging

- **MySQL**: Relational database for persistent storage of users, teams, matches, trade history, and room metadata.
- **Redis**: High-performance key-value store used for:
  - WebSocket session registry.
  - Real-time message broadcasting via Pub/Sub.
  - Volatile caching of high-frequency data like starting lineups.

## Communication Protocols

- **RESTful API**: Standard Spring MVC controllers for CRUD operations and business logic requests.
- **STOMP (WebSocket)**: Sub-protocol used for real-time, bidirectional communication between the server and the mobile app.
- **CORS/Security**: Configured to allow secure communication with the mobile app environment, especially considering WSL2 networking constraints.

## Third-Party Libraries

- **QueryDSL**: For building type-safe dynamic SQL queries that are complex or require conditional filtering.
- **Flyway**: Manages database schema migrations across different environments (Dev, Prod) to ensure schema consistency.
