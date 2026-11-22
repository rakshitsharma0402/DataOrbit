<!-- DataOrbit V1 Architecture & Engineering Handbook · Reference material · PDF page 82. Converted from ../DataOrbit-V1-Handbook.pdf, which remains authoritative. Diagrams appear only in the PDF. -->

# Technical references and further reading

*Official documentation relevant to implementation; links aid learning rather than modifying the approved design.*

## Authoritative documentation starting points

Software versions and documentation links may evolve. During implementation, pin exact dependency versions and read the corresponding versioned guides. These external resources explain tools and standards; DataOrbit’s application-specific CSV rules and safety invariants remain those defined in this book.

| Technology or subject | Official resource | Why to consult it |
|---|---|---|
| Java 21 | Java SE 21 API documentation | Streams, records, Unicode strings and I/O |
| Spring Boot | Spring Boot Reference | Configuration, controllers, wiring and runtime behavior |
| Maven | Maven POM guide | Parent and child module organization |
| PostgreSQL | PostgreSQL documentation | Constraints, indexes, transactions and TIMESTAMPTZ |
| Flyway | Flyway documentation | Versioned migrations and schema-history behavior |
| React | React learning documentation | Components, hooks and application state |
| React Flow | React Flow | Fixed pipeline diagrams and node interactions |
| Unicode NFC | Unicode Normalization Forms | Why canonical normalization must be defined precisely |
| OWASP guidance | File Upload Cheat Sheet | Security considerations for untrusted file handling |

## Architecture evidence versus external references

An official Java or PostgreSQL reference supports understanding of a language or platform. It does not replace the approved DataOrbit contracts. Likewise, generic CSV examples do not override the strict V1 quoted-field grammar, decoded UTF-16 field caps or original-source-byte record caps. The executable tests built during implementation will become the strongest evidence that the chosen libraries honor these commitments.
