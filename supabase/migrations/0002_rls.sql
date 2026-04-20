-- Solo ToDo · Row-Level Security
-- Every synced table enforces: a row is visible/mutable iff auth.uid() = user_id.
-- This is what actually protects user data — the publishable key in the client
-- doesn't matter because RLS checks the authenticated session's user_id.

-- Enable RLS on every table
ALTER TABLE public.task              ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.daily_quest_item  ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.daily_quest_log   ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.rank_event        ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.dungeon           ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.dungeon_floor     ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.task_list         ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.reflection        ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.stat              ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.user_settings     ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.op_log            ENABLE ROW LEVEL SECURITY;

-- Per-table policies. Each table uses the same shape, so we define once per table.
DO $$
DECLARE
  t TEXT;
BEGIN
  FOR t IN SELECT unnest(ARRAY[
    'task','daily_quest_item','daily_quest_log','rank_event',
    'dungeon','dungeon_floor','task_list','reflection','stat',
    'user_settings','op_log'
  ]) LOOP
    EXECUTE format('DROP POLICY IF EXISTS %I_select ON public.%I', t, t);
    EXECUTE format('DROP POLICY IF EXISTS %I_insert ON public.%I', t, t);
    EXECUTE format('DROP POLICY IF EXISTS %I_update ON public.%I', t, t);
    EXECUTE format('DROP POLICY IF EXISTS %I_delete ON public.%I', t, t);

    EXECUTE format('CREATE POLICY %I_select ON public.%I FOR SELECT USING (auth.uid() = user_id)', t, t);
    EXECUTE format('CREATE POLICY %I_insert ON public.%I FOR INSERT WITH CHECK (auth.uid() = user_id)', t, t);
    EXECUTE format('CREATE POLICY %I_update ON public.%I FOR UPDATE USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id)', t, t);
    EXECUTE format('CREATE POLICY %I_delete ON public.%I FOR DELETE USING (auth.uid() = user_id)', t, t);
  END LOOP;
END$$;
