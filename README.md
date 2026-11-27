# CampusFind Backend

Spring Boot REST API for the CampusFind campus Lost & Found Portal.

## Stack
- Java 17
- Spring Boot 3.3
- Spring Web (REST)
- Spring Data JPA / Hibernate
- H2 database
- Bean Validation

## API endpoints
- `GET /api/health` — service health
- `GET /api/items?q=&type=&category=` — search/filter open reports
- `POST /api/items` — create a LOST or FOUND report
- `POST /api/items/{id}/claims` — submit a private ownership claim

Claim proofs are accepted but are not exposed through a public read endpoint. A future authenticated owner/admin workflow must authorize claim access before it is added.

Example item request:
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

Example claim request:
```json
{
  "claimantName": "Demo Student",
  "claimantEmail": "student@example.com",
  "message": "It has a small scratch under the lid."
}
```

## Run locally
Requirements: JDK 17+ and Maven 3.9+, or Docker Desktop.

With Maven:
```bash
mvn spring-boot:run
```
API URL: http://localhost:8080

With Docker:
```bash
docker build -t campusfind-backend .
docker run --rm -p 8080:8080 campusfind-backend
```

## Environment variables
- `PORT` — server port (default 8080)
- `DB_URL` — H2 JDBC URL (default `jdbc:h2:file:./data/campusfind`)
- `DB_USER` — database username (default `sa`)
- `DB_PASS` — database password (default empty)
- `FRONTEND_URL` — frontend origin allowed by CORS (default `http://localhost:5173`)

For a live demo using H2, remember that H2 file data on many cloud hosts will be lost when the container is replaced unless a persistent disk/volume is configured. Don't store sensitive student data in this demonstration deployment.

## Deploy
Use a Docker-capable Java host and deploy this repository. It builds from the Dockerfile. Add `FRONTEND_URL` containing the frontend's deployed origin, such as a Vercel URL (without `/api`). The hosting platform should pass its port in `PORT`.

## Related repository
Frontend: https://github.com/nsks2509-coder/campusfind-frontend

## Project scope
This is an educational MVP. Report creation and claim submission do not yet require login and are not protected by roles. Claim proof text and claimant details are private input and must not be exposed publicly. Add authentication, authorization, rate limiting, and a production database before handling real personal data.
