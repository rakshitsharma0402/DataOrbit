# GitHub setup runbook

One-time setup for `rakshitsharma0402/DataOrbit`, following the [GitHub Engineering Standard](../engineering/GitHub-Engineering-Standard-v1.0.md). Run from a clone of the repository with the GitHub CLI authenticated (`gh auth status`).

## Status (23 November 2026)

| Step | State |
|---|---|
| 1. Initial commit | Done (`bbe4659`) |
| 2. Labels, milestones, merge settings | Done (bootstrap script; the leftover default `accessibility` label was also deleted) |
| 3. Issues | Milestone 1 issues #1 to #7 created |
| 4. CI | Done: `.github/workflows/ci.yml`, jobs `backend` and `frontend` |
| 5. Protect `main` | Done: rulesets "main – safety" and "main – required checks" |
| 6. Tag protection | Done: ruleset "tags – releases" |
| Secret scanning and push protection | Enabled |
| Dependabot alerts | Not enabled |
| 7. GitHub Project | Not created yet |

## 1. Initial commit

Apply the docs pack to an empty repository and commit it directly to `main` (the initial documentation commit is the one exception to the PR rule):

```bash
git add -A
git commit -m "docs: add repository documentation, ADRs and planning"
git push -u origin main
```

The pack already contains the handbook (PDF and Markdown chapters) and the MIT `LICENSE`. Before committing, make sure `docs/engineering/GitHub-Engineering-Standard-v1.0.md` is present (copy it from your own file); several documents link to it.

## 2. Labels, milestones and merge settings

```bash
./scripts/bootstrap-github.sh
```

It sets merge-commit-only merging with no automatic branch deletion, replaces the default labels with the standard's label set, and creates the seven milestones with their acceptance evidence as descriptions. Labels and settings are safe to re-run; existing milestones are skipped.

## 3. Create issues

Create the issues from [`initial-issues.md`](initial-issues.md) with `gh issue create`, using the title format `[TYPE] Description`, the labels listed and the matching milestone. Start with Milestone 1 and create later milestones' issues when you reach them, if you prefer a short backlog.

## 4. CI before protection

Create the first workflows (Milestone 1, issue 6). Required status checks can only be selected after the workflow has run once, so enable the ruleset below **after** the first green run.

## 5. Protect `main`

Settings, Rules, Rulesets. Use **two** branch rulesets targeting the default branch, because a bypass actor skips every rule in a ruleset: with one ruleset, the admin bypass needed for direct documentation commits would also allow force pushes.

| Ruleset | Rules | Bypass |
|---|---|---|
| main – safety | Block force pushes; block deletion | None |
| main – required checks | Require status checks `backend` and `frontend` (GitHub Actions); branch need not be up to date | Repository admin, always |

- Do **not** require approvals or pull requests (single maintainer).
- The admin bypass lets trivial documentation commits go straight to `main`, as the standard allows. Required checks otherwise block any direct push.
- Renaming a CI job means updating "main – required checks" too.

Also enable secret scanning and Dependabot alerts, if your plan includes them.

## 6. Tag protection

Ruleset "tags – releases" targets `refs/tags/v*` and blocks deletion, updates and force updates, so published tags stay immutable. No bypass.

## 7. GitHub Project: `DataOrbit V1 Development`

`gh` needs the project scope first: `gh auth refresh -s project`. Then create the project from the web UI (simplest) or with `gh project create`.

**Fields**

| Field | Type | Values |
|---|---|---|
| Status | single select | Backlog, Ready, In Progress, In Review, Blocked, Done |
| Component | single select | app, workflow, ingestion, validation, classification, storage, status, database, frontend, api, configuration |
| Work Type | single select | Feature, Bug, Task, Research, Docs |
| Milestone | built-in | the seven milestones |
| Size | single select | XS, S, M, L, XL |

No priority field.

**Views**

1. Development Board: board layout grouped by Status.
2. Milestone Progress: table grouped by Milestone.
3. Component View: table grouped by Component.
4. Work Queue: table filtered to open items that are not Done or Backlog.

**Built-in workflows:** enable "Item added to project → Status: Backlog" and "Pull request merged → Status: Done". Add all issues to the project.

## 8. Milestone closing routine

Before closing a milestone, confirm each line of its acceptance evidence in [`milestones.md`](milestones.md) and link the proof (test names, report or run notes) in the milestone's closing comment.
