# Development setup

How to prepare a machine to build and run DataOrbit V1 locally. Build, run and test commands are added to the [README](../README.md) as Milestone 1 delivers them.

## Toolchain

The JDK version is a project decision ([ADR-0006](adr/0006-java-25-baseline.md)). For the other tools, use a current release; the versions below are the ones the project was verified with.

| Tool | Required | Verified version | Check with |
|---|---|---|---|
| JDK | 25 (LTS) | OpenJDK 25.0.5 (Homebrew `openjdk@25`) | `java -version` and `javac -version` |
| Maven | current 3.x | 3.10.0 | `mvn -v` (also shows which JDK Maven uses) |
| Node.js | current LTS | 24.21.0 | `node -v` |
| npm | bundled with Node | 11.19.0 | `npm -v` |
| PostgreSQL | current | 18.6 (Homebrew `postgresql@18`) | `psql --version` and `pg_ctl --version` |
| Git | current | 2.56.0 | `git --version` |

Verified on macOS (Apple silicon) on 2026-11-23.

### Selecting the JDK

Maven builds with the JDK that `JAVA_HOME` points to. Make sure `mvn -v` reports Java 25. On macOS with several JDKs installed:

```bash
/usr/libexec/java_home -V
```

```bash
export JAVA_HOME="$(/usr/libexec/java_home -v 25)"
```

Put the `export` line in your shell profile (for example `~/.zshrc`) to make it permanent.

## Local PostgreSQL

DataOrbit uses one local database owned by a dedicated, non-superuser login role. The server should accept connections only from this machine (`listen_addresses = localhost`, the Homebrew default).

1. Check that the server is running:

   ```bash
   pg_isready
   ```

   If it is not running and was installed with Homebrew:

   ```bash
   brew services start postgresql@18
   ```

2. Create the role and the database (run once, as your PostgreSQL admin user):

   ```bash
   psql -d postgres -c "CREATE ROLE dataorbit LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE NOREPLICATION;"
   ```

   ```bash
   psql -d postgres -c "CREATE DATABASE dataorbit OWNER dataorbit ENCODING 'UTF8' TEMPLATE template0;"
   ```

3. Set the role's password interactively, so it never appears in shell history or in the repository:

   ```bash
   psql -d postgres -c '\password dataorbit'
   ```

4. Check that you can connect as the new role (it prompts for the password):

   ```bash
   psql -h localhost -U dataorbit -d dataorbit -c 'select current_user, current_database();'
   ```

### Credentials

Never commit the password or a real connection string. The backend reads its database settings from configuration and environment variables. The exact setting names are defined with the Spring Boot configuration and the Flyway connection work in Milestone 1. Keep real values in local, untracked files or in your shell environment only.
