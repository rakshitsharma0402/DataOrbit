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
