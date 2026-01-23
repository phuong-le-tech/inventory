# Inventory Management Application

A full-stack inventory management application for monitoring, adding, editing, and deleting items. Features a dashboard for overview and detailed list views with search and filtering.

## Tech Stack

- **Backend**: Java 21, Spring Boot 3.x, PostgreSQL
- **Frontend**: React 18, TypeScript, Vite, TailwindCSS
- **Containerization**: Docker, Docker Compose

## Features

- Dashboard with inventory statistics (total items, status breakdown, category counts)
- Inventory list with search and status filtering
- Add/Edit items with image upload
- Responsive sidebar navigation

## Prerequisites

- Docker and Docker Compose (recommended)
- Or for local development:
  - Java 21
  - Node.js 20+
  - PostgreSQL 16 (optional, H2 used in dev mode)

## Quick Start with Docker

```bash
# Clone and start all services
docker compose up --build

# Access the application
# Frontend: http://localhost:3000
# Database UI (Adminer): http://localhost:8081
```

### Adminer Database Access

- **System**: PostgreSQL
- **Server**: postgres
- **Username**: postgres
- **Password**: postgres
- **Database**: inventory

## Local Development

### Backend

```bash
cd src/backend
./mvnw spring-boot:run    # Runs on port 8080 with H2 in-memory database
```

H2 Console available at http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:inventorydb`)

### Frontend

```bash
cd src/frontend
npm install
npm run dev               # Runs on port 5173, proxies /api to backend
```
