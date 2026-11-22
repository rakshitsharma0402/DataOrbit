# GitHub setup runbook

One-time setup for `rakshitsharma0402/DataOrbit`, following the [GitHub Engineering Standard](../engineering/GitHub-Engineering-Standard-v1.0.md). Run from a clone of the repository with the GitHub CLI authenticated (`gh auth status`).

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

Settings, Rules, Rulesets, new branch ruleset targeting `main`:

- Block force pushes and block deletion.
- Require status checks to pass: the backend and frontend CI jobs.
- Do **not** require approvals (single maintainer).
- Add yourself (repository admin) as a bypass actor, so trivial documentation commits can still go straight to `main` as the standard allows. Required checks otherwise block any direct push.

Also enable secret scanning and Dependabot alerts, if your plan includes them.

## 6. Tag protection

Add a tag ruleset for `v*` that blocks deletion and updates, so published tags stay immutable.

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
