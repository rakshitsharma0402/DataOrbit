# DataOrbit GitHub Engineering Standard v1.0

**Repository:** `DataOrbit`  
**Owner:** `rakshitsharma0402`  
**SSH URL:** `git@github.com:rakshitsharma0402/DataOrbit.git`  
**Clone command:**

```bash
git clone git@github.com:rakshitsharma0402/DataOrbit.git
```

**Status:** Approved standard  
**Applies to:** The DataOrbit repository and its solo-maintainer development workflow

---

## 1. Purpose

This document defines the GitHub conventions used to develop and maintain DataOrbit. It covers repository organization, branches, issues, pull requests, commits, labels, milestones, GitHub Projects, versioning, and repository safeguards.

The standard is intentionally lightweight because DataOrbit has one contributor and maintainer. It provides consistency and traceability without imposing team processes that do not apply to a solo-maintained project.

## 2. Repository

### 2.1 Repository identity

- Repository name: `DataOrbit`
- Owner: `rakshitsharma0402`
- Canonical SSH URL: `git@github.com:rakshitsharma0402/DataOrbit.git`
- Clone using:

  ```bash
  git clone git@github.com:rakshitsharma0402/DataOrbit.git
  ```

### 2.2 Repository structure

Use one repository for the DataOrbit application and its supporting project materials.

The repository may contain:

- Backend source code, including the approved nine-module Maven structure.
- Frontend source code.
- Documentation and architecture material.
- Build, test, and CI configuration.
- The engineering handbook PDF under `docs/book/`.
- Repository guidance files such as `README.md`, `CONTRIBUTING.md`, `CHANGELOG.md`, and `LICENSE`.

Keep experiments and temporary investigation material separate from production application code. Do not introduce a second repository or restructure the approved application architecture without an explicit decision.

## 3. Branching

### 3.1 Branch naming

Use this format:

```text
<type>/<short-description>
```

Use lowercase words separated by hyphens. Branch names do not need to contain an issue number.

Examples:

```text
feat/csv-upload
fix/upload-size-check
hotfix/storage-recovery
refactor/storage-service
test/csv-parser-cases
docs/architecture-handbook
build/maven-setup
ci/backend-checks
chore/repository-cleanup
perf/streaming-memory-use
```

Common branch types:

| Type | Purpose |
|---|---|
| `feat` | New functionality |
| `fix` | Bug fixes |
| `hotfix` | Urgent fixes |
| `refactor` | Internal restructuring without intended behavior changes |
| `test` | Test additions or changes |
| `docs` | Documentation changes |
| `build` | Build system and dependency configuration |
| `ci` | Continuous integration changes |
| `chore` | Maintenance tasks |
| `perf` | Performance improvements |

### 3.2 Branch workflow

- `main` is the permanent primary branch.
- Do not create a `develop` branch.
- Use a working branch for meaningful development rather than committing substantial changes directly to `main`.
- A branch is normally associated with an issue when an issue exists and the work benefits from that association.
- Branch-to-issue association is optional. Do not create an issue solely to justify a branch.
- Do not require issue numbers in branch names.
- Link the branch from the issue when appropriate using GitHub's issue development links, and reference the issue in the pull request when one exists.
- **Do not automatically delete merged branches.** Keep merged branches unless there is a deliberate reason to remove one.

## 4. Issues

### 4.1 Issue types

Use these issue types:

- Feature
- Bug
- Task
- Research
- Docs

### 4.2 Issue title

Use this format:

```text
[TYPE] Short description
```

Examples:

```text
[FEATURE] Implement streaming CSV upload
[BUG] Prevent duplicate processing execution
[TASK] Configure backend build workflow
[RESEARCH] Evaluate CSV parser libraries
[DOCS] Add storage recovery explanation
```

### 4.3 Issue template

Use this lightweight issue template:

```markdown
## Description

What needs to be done?

## Motivation

Why is this work needed?

## Expected outcome

What should the result look like?

## Acceptance criteria

- [ ]
- [ ]
- [ ]

## References

Related architecture sections, documentation,
issues, or useful links.

## Notes

Additional implementation details or observations.
```

### 4.4 Issue conventions

- Create issues for meaningful work, defects, research, and documentation tasks.
- Do not create an issue for every tiny change.
- Do not require large tasks to be split into smaller issues. A large task may remain one issue; use meaningful commits to record progress.
- Link an issue to a branch when that relationship is useful. It is acceptable for an issue not to have a branch.
- Reference and close the related issue from a pull request when applicable.

## 5. Pull Requests

### 5.1 Pull request title

Use Conventional Commit-style titles:

```text
type(scope): short description
```

Examples:

```text
feat(ingestion): add streaming CSV upload
fix(storage): preserve source during failed transfer
docs(book): explain classification policy
test(validation): cover malformed quoted records
```

### 5.2 Pull request template

Use this simplified template:

```markdown
## Related issue

Closes #

## Summary

What changed and why?

## Changes

-
-
-

## Testing

- [ ] Tests added or updated where necessary.
- [ ] Existing relevant tests pass.
- [ ] Changes manually verified where appropriate.

## Self-review

- [ ] Follows approved DataOrbit architecture.
- [ ] No unrelated changes included.
- [ ] Documentation updated where necessary.
```

### 5.3 Pull request conventions

- Use pull requests for meaningful changes.
- Reference the related issue when one exists.
- Perform a self-review before merging.
- Ensure relevant automated checks pass.
- Do not require a second-person review; DataOrbit is maintained by one contributor.
- Use **merge commits** as the repository's merge strategy.
- Do not squash commits as the standard merge approach.
- Keep merged branches; do not enable automatic head-branch deletion.

## 6. Commit conventions

Use the Conventional Commits format:

```text
type(scope): short description
```

Common commit types:

| Type | Purpose |
|---|---|
| `feat` | Add functionality |
| `fix` | Fix a defect |
| `refactor` | Restructure code without intended behavior changes |
| `test` | Add or update tests |
| `docs` | Change documentation |
| `build` | Change build configuration or dependencies |
| `ci` | Change CI configuration |
| `perf` | Improve performance |
| `chore` | Perform maintenance |

Commit guidance:

- Use a meaningful scope where it improves clarity.
- Write concise descriptions that explain the change.
- Prefer imperative descriptions, such as `add upload validation`.
- Keep commits logically focused.
- For large tasks that remain in one issue, use commits to show implementation progress.
- Avoid unrelated changes in the same commit.
- Preserve useful commit history; use merge commits rather than routinely squashing pull request history.

Examples:

```text
feat(ingestion): add upload initialization endpoint
feat(ingestion): stream uploaded files to staging
test(validation): cover duplicate headers
fix(storage): verify checksum before finalizing transfer
docs(storage): describe recovery behavior
```

## 7. Labels

Use labels to classify work by type, component, and important characteristics. Do not introduce a priority system.

### 7.1 Type labels

```text
type: feature
type: bug
type: task
type: research
type: docs
```

### 7.2 Component labels

```text
component: app
component: workflow
component: ingestion
component: validation
component: classification
component: storage
component: status
component: database
component: frontend
component: api
component: configuration
```

### 7.3 Special labels

```text
architecture change
breaking change
security
good first issue
```

### 7.4 Label guidance

- Apply labels when they help organize or find work.
- Do not create priority labels or a priority field.
- Avoid duplicating status information that is already tracked in GitHub Projects.
- Keep the label set understandable and maintainable.

## 8. Milestones

Use the approved seven implementation milestones:

1. **Project Foundation**
2. **Storage Foundation**
3. **CSV Validation & Classification**
4. **Ingestion & Persistence**
5. **Workflow, Status & Recovery**
6. **React Frontend**
7. **Integration Testing & V1 Hardening**

Assign issues and pull requests to the milestone that best represents the work. Milestones track progress through the approved implementation sequence.

## 9. GitHub Projects

Create one GitHub Project:

**Project name:** `DataOrbit V1 Development`

### 9.1 Fields

| Field | Values |
|---|---|
| Status | Backlog, Ready, In Progress, In Review, Blocked, Done |
| Component | Approved component labels |
| Work Type | Feature, Bug, Task, Research, Docs |
| Milestone | The seven approved implementation milestones |
| Size | XS, S, M, L, XL |

Do not add a priority field.

### 9.2 Views

Create these views:

1. **Development Board** — primary workflow view grouped by Status.
2. **Milestone Progress** — work organized by implementation milestone.
3. **Component View** — work organized by application component.
4. **Work Queue** — the current set of open and actionable work.

Use GitHub Projects as the central place to understand work status. Avoid maintaining redundant status labels when the Project field already provides that information.

## 10. Versioning and releases

Use Semantic Versioning (SemVer):

```text
MAJOR.MINOR.PATCH
```

Prefix Git tags and release versions with `v`.

### 10.1 Version meanings

- **MAJOR** — incompatible changes to the public behavior or supported interfaces.
- **MINOR** — backward-compatible functionality.
- **PATCH** — backward-compatible fixes.

### 10.2 DataOrbit version examples

| Version | Meaning |
|---|---|
| `v0.1.0` | First usable development release |
| `v0.9.0` | V1 release candidate |
| `v1.0.0` | Stable V1 release |
| `v1.0.1` | Patch release |
| `v1.1.0` | Backward-compatible feature release |
| `v2.0.0` | Release containing breaking changes |

### 10.3 Release conventions

- Use Git tags in the `vMAJOR.MINOR.PATCH` format.
- Publish meaningful GitHub Releases with a clear summary of changes.
- Maintain `CHANGELOG.md` to record notable changes.
- Treat published version tags as immutable.
- Track the software version, engineering handbook revision, and implementation milestone separately. They represent different things and should not be conflated.

## 11. Repository safeguards and automation

### 11.1 Main branch safeguards

- Protect `main` against accidental force pushes.
- Use pull requests for meaningful changes.
- Require relevant automated checks to pass before merging pull requests.
- Do not require multi-person approval.

### 11.2 Continuous integration

Configure automated checks appropriate to the repository:

**Backend**
- Run the Maven build.
- Run relevant backend tests.

**Frontend**
- Run linting.
- Run TypeScript type checking.
- Run the production build.

Keep checks focused on validating the repository. Add further automation when it provides clear value.

### 11.3 Security and maintenance

- Never commit credentials, tokens, passwords, private keys, or other secrets.
- Keep configuration examples free of real secret values.
- Document significant architectural, database migration, and recovery-related changes.
- Keep repository guidance and build instructions up to date.

## 12. Working principles

The following principles govern how this standard is applied:

1. **Solo-maintainer friendly:** Do not impose team-only processes such as mandatory second-person reviews.
2. **Traceable, not bureaucratic:** Use issues, branches, pull requests, and commits where they add value; do not create administrative work for its own sake.
3. **Issue links are useful but optional:** Associate branches with issues when appropriate, without requiring issue numbers in branch names.
4. **Commit history records progress:** Large tasks do not have to be split into smaller issues.
5. **Preserve branch history:** Merged branches are retained and are not automatically deleted.
6. **Merge commits:** Pull requests use merge commits rather than squash merging.
7. **No priority system:** Do not use priority labels or a priority field in GitHub Projects.
8. **Architecture integrity:** Changes that alter the approved DataOrbit architecture require explicit review before implementation.
9. **Consistent releases:** Use SemVer and maintain meaningful release notes.
10. **Incremental improvement:** Keep the standard lightweight and revise it when the project needs change.

---

## Appendix A — Quick reference

| Area | Standard |
|---|---|
| Repository | `DataOrbit` |
| SSH URL | `git@github.com:rakshitsharma0402/DataOrbit.git` |
| Primary branch | `main` |
| Branch format | `<type>/<short-description>` |
| Issue title | `[TYPE] Short description` |
| PR title | `type(scope): short description` |
| Commit format | `type(scope): short description` |
| PR merge strategy | Merge commit |
| Merged branches | Keep; no automatic deletion |
| Issue number in branch name | Not required |
| Issue-to-branch association | Optional, when useful |
| Large issue splitting | Not required; use commits to track progress |
| Priority labels/field | None |
| Project management | GitHub Projects |
| Versioning | Semantic Versioning |
| Release tag format | `vMAJOR.MINOR.PATCH` |

## Appendix B — Approved implementation milestones

1. Project Foundation
2. Storage Foundation
3. CSV Validation & Classification
4. Ingestion & Persistence
5. Workflow, Status & Recovery
6. React Frontend
7. Integration Testing & V1 Hardening

---

**End of DataOrbit GitHub Engineering Standard v1.0**
