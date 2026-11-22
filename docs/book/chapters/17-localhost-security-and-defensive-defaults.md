<!-- DataOrbit V1 Architecture & Engineering Handbook · Part IV · Safety & Testing · PDF page 51. Converted from ../DataOrbit-V1-Handbook.pdf, which remains authoritative. Diagrams appear only in the PDF. -->

# Localhost security and defensive defaults

*A local single-user product still processes untrusted bytes and must enforce meaningful boundaries.*

## The local machine is a security boundary

> **FIGURE 12. Default trust boundary for a single-user localhost application with untrusted uploaded CSV bytes.**  
> *(Diagram: see page 51 of the PDF.)*

The backend should bind to `127.0.0.1`, with the React development server likewise restricted to loopback by default. The frontend origin is explicitly allowed by CORS, and state-changing requests reject unexpected browser origins. CORS alone is not authorization or CSRF protection. No login is required in V1, so exposing the service to a network would materially change the threat model and would require a new design review.

## Defensive path and object handling

The backend never trusts `originalFilename` as a directory path. The Storage Local adapter derives safe object placement from dataset and attempt identities, rejects traversal patterns and escaping symlinks, and confines temporary objects to controlled roots. HTTP downloads use sanitized filenames and safe attachment headers. No Java business module outside Storage Local converts an object key into an operating-system path.

## Input bounds, information disclosure and log hygiene

Actual upload bytes are bounded while streaming, CSV fields and logical records are bounded while tokenizing, and list/event API responses have page-size limits. Public errors contain stable codes and safe messages rather than stack traces, SQL connection strings or full local paths. Logs correlate dataset ID, job ID, upload attempt, application run, operation and duration but do not dump raw CSV cell contents or secrets. CSV data must never be interpreted as an executable command.

## Single active backend ownership

The V1 deployment assumes one active backend owner for a database/storage root. Startup must establish exclusive ownership before accepting uploads or launching destructive maintenance. A process restart triggers evidence-based reconciliation of upload, processing and maintenance claims. A stale owner cannot commit a claim merely because an old thread or request wakes up after replacement.

| Threat or mistake | Required safeguard | Verification |
|---|---|---|
| Browser on another origin sends a state-changing request | Explicit origin validation and loopback default | Rejected cross-origin write test |
| Filename contains traversal or absolute path | Safe filename/key derivation inside Storage Local | Traversal and symlink-escape tests |
| Declared upload size understates actual bytes | Enforce streamed actual byte count | Cap + 1 byte input test |
| Malformed CSV provokes excessive memory use | Tokenizer-level field/record/column limits | Pathological UTF-8 and quote fixtures |
| Backend exception leaks internal details | Safe API envelope and controlled logging | No paths/passwords in public responses |
| Two backend processes use the same data root | Exclusive ownership enforcement | Second-start rejection test |

### Explicitly outside the first release

Authentication, user roles, remote TLS hosting, distributed locks, antivirus scanning and advanced telemetry are future capabilities. Their absence is acceptable only under the agreed local, trusted-user deployment assumptions.
