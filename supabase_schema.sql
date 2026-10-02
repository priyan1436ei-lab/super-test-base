-- ==============================================================================
-- FITTRACK AI - Supabase Database Schema & Row Level Security (RLS)
-- Run this in your Supabase Project -> SQL Editor -> New Query -> Run
-- ==============================================================================

-- 1. ATHLETES / USERS TABLE
CREATE TABLE IF NOT EXISTS public.users (
    user_id TEXT PRIMARY KEY,
    full_name TEXT NOT NULL,
    username TEXT UNIQUE,
    email TEXT UNIQUE NOT NULL,
    phone TEXT,
    age INTEGER,
    gender TEXT,
    height_cm NUMERIC,
    weight_kg NUMERIC,
    target_weight_kg NUMERIC,
    fitness_goal TEXT,
    activity_level TEXT,
    subscription_tier TEXT DEFAULT 'FREE',
    current_streak INTEGER DEFAULT 0,
    workouts_completed_count INTEGER DEFAULT 0,
    updated_at BIGINT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);

-- Enable RLS on users
ALTER TABLE public.users ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view and update their own profile"
    ON public.users
    FOR ALL
    USING (auth.uid()::text = user_id OR auth.role() = 'authenticated')
    WITH CHECK (auth.uid()::text = user_id OR auth.role() = 'authenticated');

-- 2. WORKOUT SESSIONS TABLE
CREATE TABLE IF NOT EXISTS public.workout_sessions (
    session_id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL REFERENCES public.users(user_id) ON DELETE CASCADE,
    plan_name TEXT NOT NULL,
    start_time BIGINT NOT NULL,
    end_time BIGINT NOT NULL,
    duration_minutes INTEGER DEFAULT 0,
    total_calories_burned INTEGER DEFAULT 0,
    notes TEXT,
    completed_date TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_workout_sessions_user_date ON public.workout_sessions(user_id, completed_date);

-- Enable RLS on workout_sessions
ALTER TABLE public.workout_sessions ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can manage their own workout sessions"
    ON public.workout_sessions
    FOR ALL
    USING (auth.uid()::text = user_id OR auth.role() = 'authenticated')
    WITH CHECK (auth.uid()::text = user_id OR auth.role() = 'authenticated');

-- 3. NUTRITION & FOOD LOGS TABLE
CREATE TABLE IF NOT EXISTS public.food_logs (
    log_id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL REFERENCES public.users(user_id) ON DELETE CASCADE,
    food_name TEXT NOT NULL,
    calories INTEGER NOT NULL,
    protein_g NUMERIC DEFAULT 0,
    carbs_g NUMERIC DEFAULT 0,
    fats_g NUMERIC DEFAULT 0,
    meal_type TEXT NOT NULL,
    log_date TEXT NOT NULL,
    serving_size TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_food_logs_user_date ON public.food_logs(user_id, log_date);

-- Enable RLS on food_logs
ALTER TABLE public.food_logs ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can manage their own food logs"
    ON public.food_logs
    FOR ALL
    USING (auth.uid()::text = user_id OR auth.role() = 'authenticated')
    WITH CHECK (auth.uid()::text = user_id OR auth.role() = 'authenticated');

-- 4. AUTO PROFILE CREATION TRIGGER (Optional on Supabase Auth Signup)
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS trigger AS $$
BEGIN
  INSERT INTO public.users (user_id, full_name, email, updated_at)
  VALUES (
    NEW.id::text,
    COALESCE(NEW.raw_user_meta_data->>'full_name', 'Athlete'),
    NEW.email,
    ROUND(EXTRACT(EPOCH FROM now()) * 1000)
  )
  ON CONFLICT (user_id) DO NOTHING;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
CREATE TRIGGER on_auth_user_created
  AFTER INSERT ON auth.users
  FOR EACH ROW EXECUTE PROCEDURE public.handle_new_user();
