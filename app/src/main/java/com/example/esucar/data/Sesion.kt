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

    fun iniciar(u: Usuario) { usuario = u }

    suspend fun cerrar() {
        try {
            Repositorio.cerrarSesion()
        } finally {
            usuario = null
        }
    }

    suspend fun restaurar() {
        usuario = Repositorio.perfilActual()
    }
}