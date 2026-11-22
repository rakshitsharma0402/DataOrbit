# Contributing to DataOrbit

DataOrbit is maintained by one person, so the process is deliberately light. The full rules are in the [GitHub Engineering Standard](docs/engineering/GitHub-Engineering-Standard-v1.0.md); this page is the quick version.

## Source of truth

1. The approved handbook in `docs/book/` (architecture and invariants). The PDF is authoritative; `docs/book/chapters/` holds the same text as Markdown for searching, linking and review. Link chapters from issues and PRs, for example `docs/book/chapters/10-the-storage-abstraction-and-integrity-protocol.md`.
2. Architecture decision records in `docs/adr/` (implementation-time choices).
3. The GitHub Engineering Standard (workflow).

A change that alters an approved architectural invariant needs the `architecture change` label and an explicit review **before** implementation.

## Branches

- `main` is the only permanent branch. No `develop`.
- Name branches `<type>/<short-description>`, lowercase with hyphens: `feat`, `fix`, `hotfix`, `refactor`, `test`, `docs`, `build`, `ci`, `chore`, `perf`. Example: `feat/csv-upload`.
- Issue numbers are not required in branch names. Link the branch from the issue when useful.
- Merged branches are kept; do not delete them.

## Issues

- Title format: `[FEATURE]`, `[BUG]`, `[TASK]`, `[RESEARCH]` or `[DOCS]` followed by a short description.
- Use the issue templates. Create issues for meaningful work, not every tiny change.
- Large tasks may stay as one issue; use focused commits to show progress.
- Labels: one `type:` label, one or more `component:` labels, and the special labels only when they apply. No priority labels. Status lives in the GitHub Project.

## Commits

Conventional Commits: `type(scope): short description`, imperative mood, one logical change per commit.

```text
feat(ingestion): add upload initialization endpoint
fix(storage): verify checksum before finalizing transfer
test(validation): cover duplicate headers
docs(storage): describe recovery behavior
```

Scopes follow the module and area names: `app`, `workflow`, `ingestion`, `validation`, `classification`, `storage`, `status`, `database`, `frontend`, `api`, `configuration`, `book`, `adr`.

Because pull requests are merged with merge commits, every commit on a branch lands in `main` as written. Keep branch history clean.

## Pull requests

- Title: `type(scope): short description`. Use the PR template; add `Closes #N` when an issue exists.
- Self-review before merging. Required automated checks must pass. No second reviewer is required.
- **Merge strategy: merge commit.** Do not squash. Do not delete the branch afterwards.

## Tests

A narrative statement counts as implemented only when tests demonstrate it. Add tests with the change, using the test IDs from the handbook's verification catalog (for example `STO-02`, `UPL-01`) in test names and PR descriptions where relevant. Failure-injection and deletion tests must use isolated temporary roots, never a real DataOrbit storage root.

## Security

Never commit credentials, tokens, passwords, private keys or real connection strings. Keep example configuration free of real secret values.

## Releases

1. Update `CHANGELOG.md` and the `${revision}` version in one pull request.
2. Merge, then tag `vMAJOR.MINOR.PATCH` on `main`.
3. Publish a GitHub Release with a clear summary.
4. Never move or reuse a published tag.

The release mapping is in [ADR-0005](docs/adr/0005-versioning-and-release-mapping.md).

## Handbook changes

The handbook changes only through a new revision. Replace the PDF and the matching Markdown chapters in the same `docs(book):` commit, and note the revision in `CHANGELOG.md`. If you find the Markdown differs from the PDF, fix the Markdown to match the PDF.

## License

By contributing, you agree that your contributions are released under the project's [MIT License](LICENSE).
