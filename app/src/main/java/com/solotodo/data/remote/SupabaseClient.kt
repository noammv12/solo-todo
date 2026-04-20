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
            install(Auth) {
                // Default: persist session in EncryptedSharedPreferences-backed storage
                // via the Android platform plugin baked into supabase-kt.
                alwaysAutoRefresh = true
                autoLoadFromStorage = true
            }
            install(Postgrest)
            install(Realtime)
        }
    }
}
