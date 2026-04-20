-- Solo ToDo · V1 schema
-- Mirror of Room entities in app/src/main/java/com/solotodo/data/local/entity/
-- with a user_id column on every synced table for Row-Level Security.
--
-- Convention:
--   * Primary keys are TEXT (ULIDs from the Kotlin client).
--   * Timestamps are stored as TIMESTAMPTZ (server converts Unix millis on write).
--   * JSON-shaped fields (repeat, target, summary, haptics, notifications) are JSONB.
--   * Every synced row carries user_id referencing auth.users.

-- ──────────────── task ────────────────
CREATE TABLE IF NOT EXISTS public.task (
  id                TEXT PRIMARY KEY,
  user_id           UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
  title             TEXT NOT NULL,
  raw_input         TEXT,
  due_at            TIMESTAMPTZ,
  repeat            JSONB,
  stat              TEXT CHECK (stat IN ('STR','INT','SEN','VIT')),
  xp                INTEGER NOT NULL DEFAULT 0,
  list_id           TEXT,
  priority          INTEGER NOT NULL DEFAULT 0,
  completed_at      TIMESTAMPTZ,
  shadowed_at       TIMESTAMPTZ,
  subtasks          JSONB,
  created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
  origin_device_id  TEXT NOT NULL,
  deleted_at        TIMESTAMPTZ
);
CREATE INDEX IF NOT EXISTS task_user_updated
  ON public.task (user_id, updated_at);
CREATE INDEX IF NOT EXISTS task_user_due
  ON public.task (user_id, list_id, due_at)
  WHERE deleted_at IS NULL;

-- ──────────────── daily_quest_item ────────────────
CREATE TABLE IF NOT EXISTS public.daily_quest_item (
  id                TEXT PRIMARY KEY,
  user_id           UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
  title             TEXT NOT NULL,
  target            JSONB NOT NULL,
  stat              TEXT NOT NULL CHECK (stat IN ('STR','INT','SEN','VIT')),
  order_index       INTEGER NOT NULL DEFAULT 0,
  active            BOOLEAN NOT NULL DEFAULT TRUE,
  created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
  origin_device_id  TEXT NOT NULL
);
CREATE INDEX IF NOT EXISTS dqi_user_updated ON public.daily_quest_item (user_id, updated_at);

-- ──────────────── daily_quest_log (append-only) ────────────────
CREATE TABLE IF NOT EXISTS public.daily_quest_log (
  id            TEXT PRIMARY KEY,
  user_id       UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
  quest_id      TEXT NOT NULL,
  day           DATE NOT NULL,
  progress      INTEGER NOT NULL DEFAULT 0,
  completed_at  TIMESTAMPTZ,
  created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (user_id, quest_id, day)
);
CREATE INDEX IF NOT EXISTS dql_user_day ON public.daily_quest_log (user_id, day);

-- ──────────────── rank_event (append-only + server dedup) ────────────────
CREATE TABLE IF NOT EXISTS public.rank_event (
  id                TEXT PRIMARY KEY,
  user_id           UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
  from_rank         TEXT NOT NULL CHECK (from_rank IN ('E','D','C','B','A','S')),
  to_rank           TEXT NOT NULL CHECK (to_rank IN ('E','D','C','B','A','S')),
  consecutive_days  INTEGER NOT NULL,
  occurred_at       TIMESTAMPTZ NOT NULL,
  cinematic_played  BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX IF NOT EXISTS rank_event_occurred
  ON public.rank_event (user_id, occurred_at);

-- ──────────────── dungeon ────────────────
CREATE TABLE IF NOT EXISTS public.dungeon (
  id                TEXT PRIMARY KEY,
  user_id           UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
  title             TEXT NOT NULL,
  description       TEXT,
  rank              TEXT NOT NULL CHECK (rank IN ('E','D','C','B','A','S')),
  due_at            TIMESTAMPTZ,
  cleared_at        TIMESTAMPTZ,
  abandoned_at      TIMESTAMPTZ,
  created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
  origin_device_id  TEXT NOT NULL
);
CREATE INDEX IF NOT EXISTS dungeon_user_updated ON public.dungeon (user_id, updated_at);

-- ──────────────── dungeon_floor ────────────────
CREATE TABLE IF NOT EXISTS public.dungeon_floor (
  id            TEXT PRIMARY KEY,
  user_id       UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
  dungeon_id    TEXT NOT NULL REFERENCES public.dungeon(id) ON DELETE CASCADE,
  title         TEXT NOT NULL,
  order_index   INTEGER NOT NULL,
  task_ids      JSONB NOT NULL DEFAULT '[]'::jsonb,
  state         TEXT NOT NULL CHECK (state IN ('LOCKED','OPEN','CLEARING','CLEARED')),
  cleared_at    TIMESTAMPTZ,
  updated_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS floor_dungeon ON public.dungeon_floor (dungeon_id);

-- ──────────────── task_list ────────────────
CREATE TABLE IF NOT EXISTS public.task_list (
  id           TEXT PRIMARY KEY,
  user_id      UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
  name         TEXT NOT NULL,
  color_token  TEXT,
  order_index  INTEGER NOT NULL DEFAULT 0
);

-- ──────────────── reflection (weekly rollup) ────────────────
CREATE TABLE IF NOT EXISTS public.reflection (
  id            TEXT PRIMARY KEY,
  user_id       UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
  week_start    DATE NOT NULL,
  summary       JSONB NOT NULL,
  generated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (user_id, week_start)
);

-- ──────────────── stat (derived cache; synced rarely) ────────────────
CREATE TABLE IF NOT EXISTS public.stat (
  user_id     UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
  kind        TEXT NOT NULL CHECK (kind IN ('STR','INT','SEN','VIT')),
  value       INTEGER NOT NULL DEFAULT 0,
  updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (user_id, kind)
);

-- ──────────────── user_settings (singleton per user) ────────────────
CREATE TABLE IF NOT EXISTS public.user_settings (
  user_id          UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
  designation      TEXT NOT NULL DEFAULT 'HUNTER',
  theme            TEXT NOT NULL DEFAULT 'CYAN' CHECK (theme IN ('CYAN','GOLD','SHADOW')),
  haptics          JSONB NOT NULL DEFAULT '{}'::jsonb,
  notifications    JSONB NOT NULL DEFAULT '{}'::jsonb,
  reduce_motion    BOOLEAN NOT NULL DEFAULT FALSE,
  vacation_until   DATE,
  streak_freezes   INTEGER NOT NULL DEFAULT 0,
  updated_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ──────────────── op_log (client outbox mirror, server observes) ────────────────
-- Optional server-side op log; clients may or may not push ops here directly.
-- Currently sync engine upserts entity tables directly; op_log kept for auditing.
CREATE TABLE IF NOT EXISTS public.op_log (
  op_id             TEXT PRIMARY KEY,
  user_id           UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
  entity            TEXT NOT NULL,
  entity_id         TEXT NOT NULL,
  kind              TEXT NOT NULL CHECK (kind IN ('CREATE','PATCH','DELETE')),
  fields            JSONB,
  field_timestamps  JSONB NOT NULL DEFAULT '{}'::jsonb,
  origin_device_id  TEXT NOT NULL,
  applied_at        TIMESTAMPTZ NOT NULL,
  synced_at         TIMESTAMPTZ DEFAULT now()
);
CREATE INDEX IF NOT EXISTS op_log_user_applied
  ON public.op_log (user_id, applied_at);
