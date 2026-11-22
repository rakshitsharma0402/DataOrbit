-- DataOrbit baseline migration.
--
-- Establishes Flyway ownership of the schema. It intentionally creates no application tables:
-- the five lifecycle tables (handbook chapter 13) arrive as later versioned migrations in
-- Milestone 4. Migrations are never edited after merge (ADR-0001); add a new version instead.
--
-- Timestamps in every later migration use TIMESTAMPTZ (UTC instants).

SELECT 1;
