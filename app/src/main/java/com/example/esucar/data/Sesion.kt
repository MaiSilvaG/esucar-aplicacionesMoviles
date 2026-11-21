package com.example.esucar.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.esucar.data.model.Roles
import com.example.esucar.data.model.Usuario
import com.example.esucar.data.remote.Repositorio

object Sesion {
    var usuario by mutableStateOf<Usuario?>(null)
        private set

    val esDocente: Boolean
        get() = usuario?.rol == Roles.DOCENTE

    private var appContext: android.content.Context? = null
    fun init(context: android.content.Context) { appContext = context.applicationContext }

    private fun prefs(context: android.content.Context) =
        context.applicationContext.getSharedPreferences("sesion", android.content.Context.MODE_PRIVATE)
    private fun guardar(context: android.content.Context, u: Usuario) {
        prefs(context).edit()
            .putString("id", u.id).putString("nombre", u.nombre)
            .putString("correo", u.correo).putString("rol", u.rol)
            .apply()
    }

    fun iniciar(context: android.content.Context, u: Usuario) {
        usuario = u
        guardar(context, u)
    }

    suspend fun cerrar() {
        try {
            Repositorio.cerrarSesion()
        } finally {
            usuario = null
            appContext?.let { prefs(it).edit().clear().apply() }
        }
    }

    suspend fun restaurar(context: android.content.Context) {
        val p = prefs(context)
        usuario = try {
            Repositorio.perfilActual()?.also { guardar(context, it) }
        } catch (e: Exception) {
            val id = Repositorio.idSesionLocal()
            if (id != null && p.getString("id", null) == id) {
                Usuario(
                    id,
                    p.getString("nombre", "") ?: "",
                    p.getString("correo", "") ?: "",
                    p.getString("rol", "") ?: ""
                )
            } else null
        }
    }
}