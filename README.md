<div align="center">
<img width="1200" height="475" alt="GHBanner" src="https://ai.google.dev/static/site-assets/images/share-ais-513315318.png" />
</div>

# Run and deploy your AI Studio app

This contains everything you need to run your app locally.

View your app in AI Studio: https://ai.studio/apps/bc86dfdd-bdc7-4516-8740-ca72c9b8c54f

## Run Locally

**Prerequisites:**  [Android Studio](https://developer.android.com/studio)


1. Open Android Studio
2. Select **Open** and choose the directory containing this project
3. Allow Android Studio to fix any incompatibilities as it imports the project.
4. Create a file named `.env` in the project directory and set `GEMINI_API_KEY` in that file to your Gemini API key (see `.env.example` for an example)
5. Remove this line from the app's `build.gradle.kts` file: `signingConfig = signingConfigs.getByName("debugConfig")`
6. Run the app on an emulator or physical device
7. If you have already published your app in AI Studio, please [request upload key reset](https://support.google.com/googleplay/android-developer/answer/9842756#zippy=%2Crequest-an-upload-key-reset) in Google Play Console.

## Supabase & Vercel Integration
FitTrack AI supports authentication and database synchronization with **Supabase** and companion web apps deployed on **Vercel**.
- See [SUPABASE_VERCEL_SETUP.md](SUPABASE_VERCEL_SETUP.md) for full configuration details.
- Run [supabase_schema.sql](supabase_schema.sql) in your Supabase SQL editor to create the necessary tables and RLS policies.
