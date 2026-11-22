-- Test fixture only: a migration that always fails, to prove a failed migration stops startup.
-- Lives outside classpath:db/migration, so the real application never sees it.
CREATE TABLE deliberately_broken (id UUID PRIMARY KEY, missing_type_here);
