# CampusFind API

A Spring Boot REST API for a campus lost-and-found board. The companion [React frontend](https://github.com/nsks2509-coder/campusfind-frontend) provides report search, item submissions, and claim submissions.

## Stack

- Java 17 and Spring Boot 3.3
- Spring Web, Spring Data JPA, and Bean Validation
- H2 for local development; PostgreSQL-compatible JDBC configuration for hosting

## API

| Method | Route | Purpose |
| --- | --- | --- |
| `GET` | `/api/health` | Health check |
| `GET` | `/api/items?q=&type=&category=` | List and filter open reports |
| `POST` | `/api/items` | Create a lost or found report |
| `POST` | `/api/items/{id}/claims` | Submit an ownership claim |

The public item response omits reporter names and email addresses. Claims can be submitted, but this MVP does not yet include authentication, a claim-review dashboard, or notifications to reporters. There is no public endpoint for listing claims.

## Run locally

Requirements: JDK 17+ and Maven 3.9+.

```bash
mvn spring-boot:run
```

The API listens on [http://localhost:8080](http://localhost:8080). The default H2 file database is stored under `./data`.

## Configuration

| Variable | Default | Purpose |
| --- | --- | --- |
| `PORT` | `8080` | HTTP port |
| `DB_URL` | `jdbc:h2:file:./data/campusfind` | JDBC URL |
| `DB_USER` | `sa` | Database user |
| `DB_PASS` | empty | Database password |
| `FRONTEND_URL` | `http://localhost:5173` | Additional allowed CORS origin |

For the included Dockerfile:

```bash
docker build -t campusfind-backend .
docker run --rm -p 8080:8080 campusfind-backend
```

## Example report

```json
{
  "title": "Blue water bottle",
  "type": "FOUND",
  "category": "Accessories",
  "description": "Blue bottle with a sticker near the library.",
  "location": "Central Library",
  "contactName": "Demo Student",
  "contactEmail": "student@example.com"
}
```

The contact fields are accepted for storage but are not returned by the public listing API.

## Demo limitations

This is a portfolio MVP, not a production service. The API currently accepts public submissions and has no user accounts, abuse controls, or claim review/notification workflow. Use fictional data only until those controls are implemented. A production deployment should also use managed database migrations, persistent storage, HTTPS, and automated tests.
