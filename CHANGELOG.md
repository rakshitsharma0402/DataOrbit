# Changelog

All notable changes to DataOrbit are recorded here. The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and the project uses [Semantic Versioning](https://semver.org/).

## [Unreleased]

### Added

- Repository documentation: README, CONTRIBUTING, architecture decision records (ADR-0001 to ADR-0005), milestone and backlog plans, issue and pull request templates, and the GitHub Engineering Standard.
- DataOrbit V1 Architecture & Engineering Handbook (first book-style edition, 10 November 2026) as a PDF, with a Markdown edition split into 31 chapters.
- MIT License.
- Development setup guide (`docs/setup.md`) with verified toolchain versions and local PostgreSQL setup.
- ADR-0006: Java 25 as the development baseline, in place of the handbook's proposed Java 21.
- Backend Maven parent project (`io.github.rakshitsharma0402:dataorbit-parent`, version `${revision}` = `0.1.0-SNAPSHOT`) with the nine approved modules. The build enforces JDK 25+, Maven 3.9+ and each module's allowed dependency direction.
- Spring Boot 4.2.0 backend entry point with `GET /api/v1/version`. Startup fails, with a short report naming the setting, unless `server.address` is a literal loopback IP and `dataorbit.frontend-origin` is a loopback http(s) origin.
- PostgreSQL connection and Flyway baseline migration (`V1__baseline.sql`), applied on startup. Startup fails, before any SQL runs, if `DATAORBIT_DB_PASSWORD` is not set; a failed migration or an unreachable database also stops startup. The build bans JPA/Hibernate and jOOQ (ADR-0001).
- Minimal React 19 + TypeScript frontend (Vite 8, Vitest) that shows the backend version, served on `127.0.0.1:5173` only. The backend allows CORS from exactly `dataorbit.frontend-origin` (now `http://127.0.0.1:5173` by default) and rejects other browser origins with 403.
- Continuous integration: GitHub Actions jobs `backend` (Maven build and tests against PostgreSQL 18.6) and `frontend` (type check, lint, tests, production build) on every pull request and push to `main`.
- README getting-started guide (build, run, check and settings), and the GitHub setup runbook updated with the applied `main` and tag rulesets.
