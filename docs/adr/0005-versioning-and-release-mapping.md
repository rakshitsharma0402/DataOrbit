# ADR-0005: Versioning and release mapping

- Status: Accepted
- Date: 2026-11-23
- Handbook references: "Seven milestones to a working system"; GitHub Engineering Standard sections 8 and 10.

## Context

The standard fixes Semantic Versioning with `v`-prefixed tags and gives example meanings (`v0.1.0` first usable development release, `v0.9.0` V1 release candidate, `v1.0.0` stable V1). It does not say which milestone each release corresponds to. The standard also requires the software version, the handbook revision and the implementation milestone to be tracked separately.

## Decision

- **No tags before `v0.1.0`.** Milestones 1 to 5 are progress markers, not releases; they close on GitHub when their acceptance evidence exists.
- **`v0.1.0`** is tagged at the end of Milestone 6 (React Frontend): the first point where a user can upload, execute, inspect and download through the browser without touching the database or filesystem.
- **`v0.9.0`** is tagged during Milestone 7 (Integration Testing & V1 Hardening) when the release candidate is ready for the full failure-injection and restore rehearsals. Further `v0.9.x` tags are allowed for candidate fixes.
- **`v1.0.0`** is tagged when Milestone 7's acceptance evidence is complete (written test report, reproducible startup and restore procedure, recorded known limitations).
- After V1, patches are `v1.0.x`, backward-compatible features `v1.x.0`. The product's later versions are intended to map to later majors (`v2.0.0`, `v3.0.0`); confirm when each is planned.
- Maven versions use a single `${revision}` property so the build version always matches the tag (e.g. `0.1.0-SNAPSHOT` during development). The frontend `package.json` version is kept equal.
- The handbook revision (currently "First book-style edition, 10 November 2026") and the milestone number are never encoded in the software version.
- Each release gets a GitHub Release with a summary, and `CHANGELOG.md` is updated in the same pull request. Published tags are immutable.

## Consequences

The first tag arrives late (after six milestones), so the GitHub Milestones and `CHANGELOG.md` `Unreleased` section carry the progress story until then.

## Verification

Release checklist in `CONTRIBUTING.md`; the release pull request updates `CHANGELOG.md` and the `${revision}` property together.
