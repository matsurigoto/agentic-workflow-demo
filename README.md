# TaskFlow API

[![CI Build & Test](https://github.com/matsurigoto/agentic-workflow-demo/actions/workflows/ci.yml/badge.svg)](https://github.com/matsurigoto/agentic-workflow-demo/actions/workflows/ci.yml)
[![GitHub Issues](https://img.shields.io/github/issues/matsurigoto/agentic-workflow-demo)](https://github.com/matsurigoto/agentic-workflow-demo/issues)
[![Java Version](https://img.shields.io/badge/Java-11-orange)](https://github.com/matsurigoto/agentic-workflow-demo/blob/main/pom.xml)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.18-red)](https://github.com/matsurigoto/agentic-workflow-demo/blob/main/pom.xml)

Task Management System API

## Setup

```bash
mvn spring-boot:run
```

## API Endpoints

- GET /api/tasks
- POST /api/tasks
- GET /api/tasks/{id}
- PUT /api/tasks/{id}
- DELETE /api/tasks/{id}
- GET /api/users
- POST /api/users/register
- POST /api/users/login

## Tech Stack

- Java 11
- Spring Boot 2.7
- H2 Database (dev)
- MySQL (prod)

## Team

- Kevin (Tech Lead)
- Jennifer (Backend Developer)
- David (QA)

## TODO

- [ ] Add documentation
- [ ] Add tests
- [ ] Fix known bugs
- [ ] Upgrade dependencies
