package com.solotodo.data.remote

import com.solotodo.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

/**
 * Lazily-constructed Supabase client singleton.
 *
 * Credentials live in [BuildConfig] (injected from `local.properties` with
 * a default fallback baked into `app/build.gradle.kts`). The publishable key
 * is designed to be shipped client-side — Row-Level Security on the server
 * is what actually protects data.
 *
 * Wired into Hilt via [NetworkModule].
 */
object SoloSupabase {
    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_PUBLISHABLE_KEY,
        ) {
            // The Android platform plugin baked into supabase-kt handles
            // persisted session + auto-refresh by default; no explicit config
            // needed here. Lifecycle callbacks stay on (default) so the session
            // survives background → foreground cycles without extra plumbing.
            install(Auth)
            install(Postgrest)
            install(Realtime)
        }
    }
}
