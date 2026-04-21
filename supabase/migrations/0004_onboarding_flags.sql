-- Phase 6.1 Awakening: add onboarding flags to user_settings.
-- Mirrors the Room V3→V4 migration at app/src/main/java/com/solotodo/data/local/Migrations.kt.

ALTER TABLE public.user_settings
  ADD COLUMN IF NOT EXISTS onboarding_completed BOOLEAN   NOT NULL DEFAULT FALSE,
  ADD COLUMN IF NOT EXISTS awakened_at          TIMESTAMPTZ,
  ADD COLUMN IF NOT EXISTS daily_quest_count    INTEGER   NOT NULL DEFAULT 3
     CHECK (daily_quest_count BETWEEN 3 AND 5),
  ADD COLUMN IF NOT EXISTS hard_mode            BOOLEAN   NOT NULL DEFAULT FALSE;
