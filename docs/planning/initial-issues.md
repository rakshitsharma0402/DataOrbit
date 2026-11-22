# Initial issue backlog

Proposed issues for each milestone, to be created with `gh issue create` using the templates in `.github/ISSUE_TEMPLATE/`. Issues are intentionally meaningful slices, not tiny tasks; progress inside an issue is recorded with commits. Each issue gets one `type:` label, the listed `component:` labels, and the milestone shown. Test IDs refer to the handbook's verification catalog. Milestone 3 implementation issues depend on the outcome of the parser research issue.

## Milestone 1: Project Foundation

1. **[TASK] Verify the development toolchain and document versions** (`component: app`)
   - [ ] JDK 25 ([ADR-0006](../adr/0006-java-25-baseline.md)), Maven, Node/npm, PostgreSQL and Git versions recorded in `docs/setup.md`
   - [ ] Local PostgreSQL database and role created, with no secrets committed
2. **[TASK] Create the parent Maven project with the nine modules** (`component: app`)
   - [ ] Exactly the nine approved modules, single `${revision}` version property
   - [ ] Dependency direction matches the handbook and is acyclic; the build enforces it
3. **[FEATURE] Spring Boot entry point, configuration loading and version endpoint** (`component: app`, `configuration`, `api`)
   - [ ] Backend binds to `127.0.0.1`; invalid or missing required settings fail startup
   - [ ] `GET /api/v1/...` health/version request succeeds
4. **[TASK] Flyway baseline and PostgreSQL connection** (`component: database`)
   - [ ] Baseline migration applied on startup; Flyway history table shows it
   - [ ] Failed migration prevents readiness; Hibernate (if ever added) never generates schema
5. **[TASK] Minimal React + TypeScript app talking to the backend** (`component: frontend`, `api`)
   - [ ] Dev server on loopback; explicit CORS origin; page shows the backend version
6. **[TASK] Continuous integration for backend and frontend** (`component: app`, `frontend`)
   - [ ] Backend: Maven build and tests. Frontend: lint, type check, production build
   - [ ] Required checks enabled on `main` only after the first successful run
7. **[DOCS] Repository documentation and getting-started guide** (`component: app`)
   - [ ] Docs pack applied: handbook PDF and Markdown chapters in `docs/book/`, MIT `LICENSE`, GitHub Engineering Standard in `docs/engineering/`
   - [ ] README "Getting started" filled in with the real build, run and test commands

## Milestone 2: Storage Foundation

1. **[FEATURE] Storage API contracts** (`component: storage`)
   - [ ] `StorageReference`, `StorageTier`, request/result records and `StorageService` with constructor invariants
2. **[FEATURE] Local adapter: safe paths and streaming stage with SHA-256** (`component: storage`)
   - [ ] Keys built per [ADR-0003](../adr/0003-upload-attempt-attribution-in-object-key.md); traversal and escaping symlinks rejected
   - [ ] Actual byte count enforced (cap + 1 rejected); `.part` finalized atomically; no overwrite (STO-01)
3. **[FEATURE] Closeable streaming read (`open`)** (`component: storage`)
   - [ ] No whole-file buffering; stream closed on success and error
4. **[FEATURE] Verified transfer to HOT/WARM/COLD and quarantine copy** (`component: storage`)
   - [ ] Destination verified before finalize; source preserved on any failure (STO-02, STO-03)
5. **[FEATURE] Authorized cleanup, quarantine removal and inspection** (`component: storage`)
   - [ ] Idempotent removal, foreign objects protected (STO-04); `inspect` is strictly read-only and finds unrecorded `.part` files
6. **[TASK] Storage failure-injection test suite** (`component: storage`)
   - [ ] Copy failure, checksum mismatch, destination exists, repeated cleanup, traversal and symlink escape, all on temporary roots

## Milestone 3: CSV Validation & Classification

1. **[RESEARCH] CSV parser conformance experiment** (`component: validation`)
   - [ ] `experiments/csv-parser-conformance/` with raw-byte fixtures from the workbook; PASS / FAIL / PASS_WITH_ADAPTER / UNVERIFIED per candidate
   - [ ] Memory measured at 10, 100 and 250 MB; ADR-0004 updated with the decision
2. **[FEATURE] Validation contracts and `ValidationResult` invariants** (`component: validation`)
   - [ ] Ten issue codes; defensive copies; VALID has no ERROR, INVALID has at least one
3. **[FEATURE] Strict streaming `FileValidator`** (`component: validation`, `configuration`)
   - [ ] CSV-ENC-01 to CSV-DIA-10 pass; limits enforced during tokenization; stream always closed
4. **[FEATURE] `DatasetClassifier` and `ClassificationPolicy`** (`component: classification`)
   - [ ] CLS-01 (±1 byte boundaries), CLS-02 (identity mismatch); invalid thresholds rejected at construction

## Milestone 4: Ingestion & Persistence

1. **[FEATURE] Database schema: five tables and one-active-job index** (`component: database`)
   - [ ] Flyway migrations; constraints, foreign keys and indexes per the handbook; `TIMESTAMPTZ`
2. **[FEATURE] Startup configuration validation and immutable policy revisions** (`component: configuration`, `database`)
   - [ ] New revision recorded when effective thresholds differ (CLS-03)
3. **[FEATURE] Exclusive runtime ownership and application-run ID** (`component: app`, `database`)
   - [ ] [ADR-0002](../adr/0002-exclusive-runtime-ownership-via-advisory-lock.md); second start rejected
4. **[FEATURE] Ingestion: initialize and upload claim** (`component: ingestion`, `database`)
   - [ ] Conditional claim with attempt and run owner; exactly one row must change (UPL-01, UPL-02)
5. **[FEATURE] Streaming upload endpoint to STAGED** (`component: ingestion`, `api`)
   - [ ] Success only after durable metadata commit; 409, 413, 415 behave deterministically (API-01)
   - [ ] Ambiguous evidence leads to `RECOVERY_REQUIRED` (UPL-03)

## Milestone 5: Workflow, Status & Recovery

1. **[FEATURE] Execute: job claim and the seven job states** (`component: workflow`, `status`)
   - [ ] One active job per dataset; duplicate Execute returns 409 (JOB-01)
2. **[FEATURE] Valid path: classify, verified transfer, atomic final commit** (`component: workflow`, `database`)
   - [ ] Dataset, job, event and cleanup task commit together or not at all (DB-01, DB-02)
3. **[FEATURE] Invalid path: quarantine with seven-day expiry** (`component: workflow`, `storage`)
   - [ ] Quarantined invalid file leaves a COMPLETED job; expiry timestamp persisted once
4. **[FEATURE] Status service, append-only events and read endpoints** (`component: status`, `api`)
   - [ ] Status, datasets, jobs, events (bounded pagination) and download endpoints; download only for stored valid datasets (API-02)
5. **[FEATURE] Durable maintenance tasks** (`component: workflow`, `database`)
   - [ ] Staging cleanup and quarantine expiry fenced and idempotent (MNT-01, MNT-02)
6. **[FEATURE] Startup reconciliation and controlled manual retry** (`component: workflow`, `ingestion`)
   - [ ] All four interruption windows covered (JOB-02); no automatic full-pipeline rerun; retry creates a new job and keeps history

## Milestone 6: React Frontend

1. **[FEATURE] Typed API client and upload panel with transfer progress** (`component: frontend`, `api`)
2. **[FEATURE] Fixed React Flow pipeline diagram driven by durable status** (`component: frontend`)
3. **[FEATURE] Status polling hook, dataset table, history views and download** (`component: frontend`)
   - [ ] Polling stops on terminal state; fetch failures shown as observation problems, not processing failures
4. **[TASK] Frontend tests for lost responses and polling cleanup** (`component: frontend`)
   - [ ] UI-01 passes
5. **[TASK] Release `v0.1.0`** (`component: app`)
   - [ ] CHANGELOG updated, tag and GitHub Release published per ADR-0005

## Milestone 7: Integration Testing & V1 Hardening

1. **[TASK] Full conformance and failure-injection suites on real PostgreSQL and storage** (`component: app`)
   - [ ] All twelve recovery drills automated; evidence includes bytes, checksums, job and dataset states, events and tasks
2. **[TASK] Maximum-size (250 MB) upload under constrained memory** (`component: ingestion`, `validation`)
   - [ ] Heap and timing recorded; no whole-file buffering
3. **[TASK] Coordinated backup and restore rehearsal** (`component: database`, `storage`)
   - [ ] OPS-01 passes; database and bytes agree after restore
4. **[TASK] Security and log-hygiene review** (`component: app`; label `security`)
   - [ ] Traversal, symlink escape, cross-origin write rejection, no paths or secrets in responses or logs
5. **[DOCS] Test report, setup guide and known limitations** (`component: app`)
   - [ ] Reproducible startup and restore procedure documented
6. **[TASK] Release candidate `v0.9.0` and stable `v1.0.0`** (`component: app`)
   - [ ] Critical safety findings resolved; releases per ADR-0005
