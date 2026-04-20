-- Solo ToDo · Triggers
--  1. Auto-bump updated_at on every UPDATE (belt-and-suspenders; client sets it too)
--  2. rank_event server-side dedup: reject duplicate inserts within 5 min of an
--     existing (user, from, to) pair. Two-devices-same-rank-up resolves to one row.

-- ──────────────── updated_at trigger ────────────────
CREATE OR REPLACE FUNCTION public.touch_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = GREATEST(NEW.updated_at, now());
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DO $$
DECLARE
  t TEXT;
BEGIN
  FOR t IN SELECT unnest(ARRAY[
    'task','daily_quest_item','dungeon','dungeon_floor','stat','user_settings'
  ]) LOOP
    EXECUTE format('DROP TRIGGER IF EXISTS %I_updated_at ON public.%I', t, t);
    EXECUTE format(
      'CREATE TRIGGER %I_updated_at BEFORE UPDATE ON public.%I ' ||
      'FOR EACH ROW EXECUTE FUNCTION public.touch_updated_at()', t, t);
  END LOOP;
END$$;

-- ──────────────── rank_event dedup ────────────────
-- If a row already exists with the same (user_id, from_rank, to_rank) within
-- 5 minutes of the incoming occurred_at, reject the new insert silently.
CREATE OR REPLACE FUNCTION public.rank_event_dedup()
RETURNS TRIGGER AS $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM public.rank_event
    WHERE user_id = NEW.user_id
      AND from_rank = NEW.from_rank
      AND to_rank = NEW.to_rank
      AND occurred_at BETWEEN NEW.occurred_at - INTERVAL '5 minutes'
                           AND NEW.occurred_at + INTERVAL '5 minutes'
      AND id <> NEW.id
  ) THEN
    RETURN NULL; -- silently drop; client already marked cinematic_played locally
  END IF;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS rank_event_dedup_tr ON public.rank_event;
CREATE TRIGGER rank_event_dedup_tr
  BEFORE INSERT ON public.rank_event
  FOR EACH ROW EXECUTE FUNCTION public.rank_event_dedup();
