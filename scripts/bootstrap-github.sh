#!/usr/bin/env bash
# One-time GitHub bootstrap for DataOrbit: merge settings, labels and milestones.
# Requires the GitHub CLI authenticated with repo scope (gh auth status).
# Labels and settings are safe to re-run. Existing milestones are skipped.
# Note: this deletes GitHub's default labels (bug, enhancement, ...) in favour of the standard's set.
set -euo pipefail

REPO="${REPO:-rakshitsharma0402/DataOrbit}"

echo "==> Repository merge settings (merge commits only, keep merged branches)"
gh api -X PATCH "repos/${REPO}" \
  -F allow_merge_commit=true \
  -F allow_squash_merge=false \
  -F allow_rebase_merge=false \
  -F delete_branch_on_merge=false >/dev/null

echo "==> Removing default labels not in the standard"
for l in "bug" "documentation" "duplicate" "enhancement" "help wanted" "invalid" "question" "wontfix"; do
  gh label delete "$l" --repo "$REPO" --yes 2>/dev/null || true
done

label() {
  gh label create "$1" --repo "$REPO" --color "$2" --description "$3" --force >/dev/null
  echo "label: $1"
}

# Type labels
label "type: feature"  1D76DB "New functionality"
label "type: bug"      D73A4A "Something is not working"
label "type: task"     0E8A16 "Maintenance, setup or test work"
label "type: research" 5319E7 "Investigation or comparison"
label "type: docs"     0075CA "Documentation"

# Component labels
label "component: app"            C5DEF5 "Application assembly, build and repository"
label "component: workflow"       C5DEF5 "Processing orchestration, jobs, recovery"
label "component: ingestion"      C5DEF5 "Initialize and upload"
label "component: validation"     C5DEF5 "CSV validation"
label "component: classification" C5DEF5 "Storage-tier classification"
label "component: storage"        C5DEF5 "Storage API and local adapter"
label "component: status"         C5DEF5 "Status, events and operational persistence"
label "component: database"       C5DEF5 "PostgreSQL schema and migrations"
label "component: frontend"       C5DEF5 "React application"
label "component: api"            C5DEF5 "REST contracts"
label "component: configuration"  C5DEF5 "Settings and policy revisions"

# Special labels
label "architecture change" B60205 "Alters an approved architectural invariant; review before implementing"
label "breaking change"     E99695 "Incompatible change to public behavior or interfaces"
label "security"            000000 "Security-related work"
label "good first issue"    7057FF "Good for newcomers"

milestone() {
  local title="$1" desc="$2"
  if gh api "repos/${REPO}/milestones?state=all&per_page=100" --jq '.[].title' | grep -Fxq "$title"; then
    echo "milestone exists: $title"
  else
    gh api "repos/${REPO}/milestones" -f title="$title" -f description="$desc" >/dev/null
    echo "milestone: $title"
  fi
}

milestone "Project Foundation" \
  "Evidence: clean repository build; local startup from documented commands; Flyway migration history; basic frontend/backend communication."
milestone "Storage Foundation" \
  "Evidence: exact byte round trips; checksum-mismatch handling; source preservation; no path escape."
milestone "CSV Validation & Classification" \
  "Evidence: conformance suite passes; candidate memory behavior measured; all classification boundaries pass."
milestone "Ingestion & Persistence" \
  "Evidence: API-driven full upload with correct DB rows, SHA-256 metadata and deterministic 409/413 behavior."
milestone "Workflow, Status & Recovery" \
  "Evidence: valid and invalid end-to-end backend paths; injected crash scenarios; durable status history."
milestone "React Frontend" \
  "Evidence: a user can upload, execute, inspect and download a file without direct database or filesystem commands. Release: v0.1.0."
milestone "Integration Testing & V1 Hardening" \
  "Evidence: written test report; reproducible startup and restore procedure; recorded known limitations. Releases: v0.9.0 (RC), v1.0.0."

echo "Done."
