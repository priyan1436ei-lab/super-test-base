# FitTrack AI - Supabase & Vercel Integration Guide

This guide walks you through connecting **Supabase** directly to your **FitTrack AI Android app**, and explains how to integrate **Vercel** with Supabase if you host serverless APIs or a companion web dashboard.

---

## Part 1: Setting Up Your Supabase Project

1. Go to [Supabase](https://supabase.com) and sign in or create a free account.
2. Click **New Project** and name it `fittrack-ai`.
3. Choose a strong database password and select a region close to your users.
4. Once the project is created, navigate to:
   - **Project Settings** (gear icon in sidebar) -> **API**
   - Copy your **Project URL** (e.g. `https://yourprojectid.supabase.co`)
   - Copy your **anon / public key** (under *Project API keys*)

---

## Part 2: Configuring FitTrack AI Android App

### 1. Update `.env`
Open or edit the [.env](file:///c:/Users/priya/Downloads/fittrack-ai%20(1)/.env) file in the root of the project:

```bash
# Gemini AI Key
GEMINI_API_KEY=your_gemini_api_key_here

# Supabase API Credentials
SUPABASE_URL=https://your-project-id.supabase.co
SUPABASE_ANON_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

> **Note:** The Gradle Secrets plugin (`com.google.android.libraries.mapsplatform.secrets-gradle-plugin`) automatically reads `.env` and exposes these as `BuildConfig.SUPABASE_URL` and `BuildConfig.SUPABASE_ANON_KEY`.

### 2. Run Database Migrations in Supabase
1. Open your Supabase Dashboard -> **SQL Editor**.
2. Click **New query**.
3. Copy and paste the contents of [supabase_schema.sql](file:///c:/Users/priya/Downloads/fittrack-ai%20(1)/supabase_schema.sql).
4. Click **Run**.
5. This creates:
   - `users`: athlete profile, streak, subscriptions, and biometric targets
   - `workout_sessions`: completed workout logs and burned calories
   - `food_logs`: nutrition and macro breakdowns
   - **Row Level Security (RLS)**: ensures users can only read/write their own records
   - **Auth Trigger**: automatically initializes an athlete profile when a user registers.

---

## Part 3: Architecture in the Android App

- **Authentication**: [`SupabaseAuthService`](file:///c:/Users/priya/Downloads/fittrack-ai%20(1)/app/src/main/java/com/example/data/auth/SupabaseAuthService.kt)
  - Sign up via Supabase GoTrue Auth (`/auth/v1/signup`)
  - Email OTP verification (`/auth/v1/verify`)
  - Password login (`/auth/v1/token?grant_type=password`)
  - Automatic fallback simulation for offline testing or demo mode
- **Cloud Database Sync**: [`SupabaseDataService`](file:///c:/Users/priya/Downloads/fittrack-ai%20(1)/app/src/main/java/com/example/data/network/SupabaseDataService.kt)
  - Synchronizes user profiles, workout sessions, and food logs with Supabase PostgREST tables.

---

## Part 4: Connecting Supabase to Vercel (Web / Serverless Backend)

If you have a companion web dashboard, admin portal, or serverless API hosted on **Vercel**:

### 1. Native Vercel Supabase Integration
1. In the Vercel Dashboard, select your project.
2. Go to **Integrations** tab -> Search for **Supabase**.
3. Click **Add Integration** and select your Supabase project (`fittrack-ai`).
4. Vercel automatically populates the following environment variables:
   - `NEXT_PUBLIC_SUPABASE_URL`
   - `NEXT_PUBLIC_SUPABASE_ANON_KEY`
   - `SUPABASE_SERVICE_ROLE_KEY`
   - `POSTGRES_URL`

### 2. Sharing the Same Database
Because both the Android app and your Vercel deployment point to the same Supabase project URL and database:
- An athlete logging a workout on the Android app will immediately see it reflected in your Vercel web portal.
- Authentication tokens from Supabase Auth work identically across both platforms.
