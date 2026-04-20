# Solo ToDo · Supabase

Server-side schema, migrations, and (future) Edge Functions.

## How to apply migrations

Until the Supabase CLI is wired into CI, **apply migrations manually in the
Supabase dashboard**:

1. Open https://supabase.com/dashboard/project/_/sql/new (your project)
2. Paste the contents of each migration file **in numeric order**
3. Click **Run**

Files apply cleanly on a fresh project. They use `IF NOT EXISTS` / `DROP POLICY
IF EXISTS` so re-runs are idempotent (except the rank_event dedup trigger,
which needs a clean slate — see below).

### Order

```
0001_init.sql       — tables + indexes
0002_rls.sql        — Row-Level Security policies
0003_triggers.sql   — updated_at + rank_event dedup
```

### Re-running

All three are idempotent. Safe to paste all three at once.

## Schema parity with Room

Every table here corresponds to a Room entity at
`app/src/main/java/com/solotodo/data/local/entity/`. The main difference is
the `user_id UUID` column, which doesn't exist locally yet (it lands in Room
schema V2 — Phase 4.2b).

## Adding a new migration

1. Create `supabase/migrations/NNNN_description.sql`
2. Bump Room's `SCHEMA_VERSION` in `SoloTodoDb.kt`
3. Add a `Migration(N, N+1)` implementation
4. Run locally; commit the updated `app/schemas/N+1.json`
5. Apply the SQL in Supabase dashboard

## Edge Functions (Phase 7)

Will live under `supabase/functions/<name>/index.ts`. Deployed via
`supabase functions deploy`. Triggered on cron or HTTP.
