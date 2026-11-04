# Spring Boot TODO — CI/CD homework

An in-memory Spring Boot REST API for a TODO list, with a GitHub Actions pipeline that tests, packages, builds a Docker image and can deploy it to an Ubuntu VM.

## Run locally

```bash
mvn spring-boot:run
```

The API is available at `http://localhost:8080/tasks`.

```bash
curl -X POST http://localhost:8080/tasks -H 'Content-Type: application/json' -d '{"title":"Buy milk"}'
curl http://localhost:8080/tasks
curl -X PUT http://localhost:8080/tasks/1 -H 'Content-Type: application/json' -d '{"title":"Done","completed":true}'
curl -i -X DELETE http://localhost:8080/tasks/1
```

Task data stays in memory and resets when the process restarts. Titles are required, non-blank and limited to 200 characters. Missing IDs return 404; invalid input returns 400. Run `mvn clean verify` to execute tests and enforce at least 70% line coverage.

## CI/CD pipeline

`.github/workflows/ci-cd.yml` runs for pull requests and pushes to `main`:

1. Sets up Java 21 and caches Maven dependencies.
2. Runs tests and the JaCoCo 70% coverage gate, then uploads the executable JAR for 14 days.
3. On `main`, builds a multi-stage Docker image and publishes it to GitHub Container Registry (GHCR).
4. If VM secrets exist, deploys over SSH with blue-green slots and health checking through nginx. The workflow summary reports build, image and deployment results.

Configure the VM and GitHub Actions secrets using [deploy/README.md](deploy/README.md). Until VM secrets are set, deployment is explicitly skipped; CI and image publishing still run.
