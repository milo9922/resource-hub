# ResourceHub

A lightweight resource reservation system built with [Quarkus](https://quarkus.io/). ResourceHub lets users book rooms, equipment, and other shared resources for specific time slots while preventing overlapping reservations.

The project is intentionally small in scope, but it is designed to follow practices commonly used in production Quarkus applications: database migrations, integration tests against a real database, environment-specific configuration, health checks, metrics, and containerized deployment.

## Goals

- Explore Quarkus in a realistic, production-oriented setup
- Practice clean REST API design and reliable booking logic
- Keep the codebase compact and easy to reason about

## Features

- Manage bookable resources (e.g. rooms, equipment)
- Create and cancel reservations for a given time range
- Conflict detection for overlapping reservations
- Simple web UI for browsing resources and making reservations
- Health and metrics endpoints for observability

## Tech Stack

| Area | Technology |
|------|------------|
| Language | Java |
| Framework | Quarkus |
| REST API | Quarkus REST (Jackson) |
| Persistence | Hibernate ORM with Panache |
| Database | PostgreSQL |
| Migrations | Flyway |
| Validation | Hibernate Validator |
| Testing | JUnit 5, REST Assured, Testcontainers |
| Observability | SmallRye Health |
| Frontend | Static HTML/JavaScript served by Quarkus |
| Packaging | Docker (JVM mode, native image optional) |

## Planned Roadmap

- [x] Project setup and database schema
- [x] Core REST API for resources and reservations
- [x] Reservation conflict handling
- [x] Profile-based configuration (`dev`, `test`, `prod`)
- [x] Simple web interface
- [x] Health checks
- [ ] Docker image

## Getting Started

### Prerequisites

- JDK 21 or newer
- Maven (or the included Maven wrapper)
- Docker (used by Quarkus Dev Services and Testcontainers)

### Run in development mode

```shell
./mvnw quarkus:dev
```

Quarkus Dev Services automatically starts a PostgreSQL container, so no manual database setup is required in dev mode.

### Run tests

```shell
./mvnw test
```

## License

This project is released for learning and portfolio purposes.
