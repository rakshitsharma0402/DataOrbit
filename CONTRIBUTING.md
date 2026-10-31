# Contributing to DataOrbit

DataOrbit is being bootstrapped. The application and standalone parser experiment
are planned, not implemented. See the [architecture overview](docs/architecture/overview.md)
and [project README](README.md) for the approved scope.

## Practical workflow

- Initial repository bootstrap may be committed directly to `main` after review.
- Substantive experiments, features, and significant fixes should use a short-lived
  branch, a Pull Request, review, and relevant tests. Open an Issue where useful to
  clarify scope and acceptance criteria before implementation.
- A separate Issue or Pull Request is not required for every trivial change.
  Keep changes focused and use review appropriate to their impact.
- Describe the change, motivation, testing evidence, and risks. Link related Issues
  or approved design decisions when applicable. Obtain approval before committing
  or pushing changes prepared for review.
- Use Conventional Commits, for example `docs: clarify storage tiers`,
  `chore(repo): refine fixture protections`, or `fix(validation): handle quoted fields`.
  These examples describe commit naming, not implemented functionality.

## Scope and validation

Follow the approved V1 architecture. HLD and LLD decisions through #61 are already
approved; migration of those records into `docs/decisions/` is deferred.
Seek review before changing approved architecture or introducing tooling.

Run checks relevant to the change and report the commands and results, including
anything not tested. For documentation changes, verify links, template metadata
where relevant, and `git diff --check`. No automated CI or runnable application/test
commands are configured yet; document such commands when their code exists.

## Data and fixture safety

Never commit private or real uploaded datasets, database contents, credentials,
secrets, or runtime files under `dataorbit-data/`. Use small synthetic examples in
Issues, Pull Requests, and automated tests.

Synthetic CSV/TSV fixtures can be tracked under `src/test/resources/`,
`test/fixtures/`, or `tests/fixtures/`, including within future subprojects.
Preserve their exact bytes: encoding, CR/LF/CRLF or mixed endings, trailing
whitespace, and presence or absence of a final newline may be test inputs.
Do not run formatters over these fixtures. The repository's EditorConfig and Git
attributes protect CSV/TSV files from automatic normalization; check separate
editor or formatter settings as well. Review fixture changes deliberately.
