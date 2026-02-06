# Backend Microservices Platform

## Project overview
This repository contains a Java Spring Boot microservices platform with three services: authentication, order management, and inventory management. It includes business logic, local JWT validation, RBAC, Docker-based deployment, and a basic CI/CD pipeline.

## Services
- **auth-service**: user registration, login, JWT issuance, refresh tokens, and logout.
- **order-service**: order creation, order listing, and cancellation with role-based access.
- **inventory-service**: stock reservation, release, and admin stock updates with optimistic locking.

## Prerequisites
- Java 17
- Maven 3.9+
- Docker and Docker Compose

## Local deployment with Docker Compose
1. Build JARs for all services:
   ```bash
   cd backend-microservices/auth-service && mvn clean package
   cd ../order-service && mvn clean package
   cd ../inventory-service && mvn clean package
   ```
2. Start all containers from repository root:
   ```bash
   docker compose up --build
   ```
3. Stop containers:
   ```bash
   docker compose down
   ```

## Ports
| Service | External Port | Internal Port |
|---|---:|---:|
| auth-service | 8081 | 8080 |
| order-service | 8082 | 8080 |
| inventory-service | 8083 | 8080 |
| postgres | 5432 | 5432 |
| redis | 6379 | 6379 |

## Swagger usage
Swagger UI endpoints:
- Auth: `http://localhost:8081/swagger-ui/index.html`
- Order: `http://localhost:8082/swagger-ui/index.html`
- Inventory: `http://localhost:8083/swagger-ui/index.html`

## CI/CD
GitHub Actions workflow (`.github/workflows/ci-cd.yml`) runs on pushes to `main` and performs:
1. Java 17 setup
2. Maven test and package for each service
3. Docker Hub login using `DOCKER_USERNAME` and `DOCKER_PASSWORD`
4. Docker image build and push

## Production VM deployment using Docker
1. Install Docker and Docker Compose on the VM.
2. Pull source code and configure environment variables for DB and JWT keys.
3. Build JAR files per service.
4. Run `docker compose up -d --build`.
5. Use reverse proxy / firewall rules for secure external exposure.

## Environment variables
Common variables:
- `SERVER_PORT`
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`

Auth-specific:
- `JWT_ISSUER`
- `JWT_ACCESS_TOKEN_MINUTES`
- `JWT_REFRESH_TOKEN_DAYS`
- `JWT_PRIVATE_KEY` (Base64 PKCS8)
- `JWT_PUBLIC_KEY` (Base64 X509)

Order/Inventory:
- `JWT_PUBLIC_KEY_LOCATION` (defaults to `classpath:public.pem`)
