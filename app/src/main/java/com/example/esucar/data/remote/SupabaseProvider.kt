package com.example.esucar.data.remote

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

object SupabaseProvider {
    private const val SUPABASE_URL = "https://zmcnudzzlygflxujpryh.supabase.co"
    private const val SUPABASE_KEY = "sb_publishable_FjnPjTGNyvqG8Cw_-5eF8g_sLY_F3gn"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Auth)
        install(Postgrest)
        install(Storage)
    }

    const val BUCKET = "materiales"
}