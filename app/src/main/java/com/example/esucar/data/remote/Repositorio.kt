package com.example.esucar.data.remote

import com.example.esucar.data.model.*
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import java.util.UUID

object Repositorio {
    private val db get() = SupabaseProvider.client
    private val uid: String
        get() = db.auth.currentUserOrNull()?.id ?: error("No hay sesión iniciada")

    suspend fun idSesionLocal(): String? {
        db.auth.awaitInitialization()
        return db.auth.currentUserOrNull()?.id
    }

    suspend fun iniciarSesion(correo: String, contrasena: String): Usuario? {
        return try {
            db.auth.signInWith(Email) {
                email = correo.trim()
                password = contrasena
            }
            perfilActual()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun registrar(nombre: String, correo: String, contrasena: String, rol: String): Usuario {
        db.auth.signUpWith(Email) {
            email = correo.trim()
            password = contrasena
        }
        val id = db.auth.currentUserOrNull()?.id
            ?: error("Registro creado pero sin sesión: desactiva 'Confirm email' en Supabase")
        db.from("perfiles").insert(PerfilNuevo(id, nombre, correo.trim(), rol))
        return Usuario(id, nombre, correo.trim(), rol)
    }

    suspend fun perfilActual(): Usuario? {
        db.auth.awaitInitialization()
        val id = db.auth.currentUserOrNull()?.id ?: return null
        return db.from("perfiles").select { filter { eq("id", id) } }
            .decodeSingleOrNull<Usuario>()
    }

    suspend fun cerrarSesion() = db.auth.signOut()

    suspend fun primerDocente(): Usuario? =
        db.from("perfiles").select { filter { eq("rol", Roles.DOCENTE) }; limit(1) }
            .decodeSingleOrNull<Usuario>()


    private suspend fun idsEstudiados(): Set<Int> =
        db.from("material_estudiado").select { filter { eq("usuario_id", uid) } }
            .decodeList<EstudiadoFila>().map { it.materialId }.toSet()

    suspend fun asignaturasConProgreso(): List<AsignaturaConProgreso> {
        val asignaturas = db.from("asignaturas").select { order("nombre", Order.ASCENDING) }
            .decodeList<Asignatura>()
        val unidades = db.from("unidades").select().decodeList<Unidad>()
        val materiales = db.from("materiales").select().decodeList<Material>()
        val estudiados = idsEstudiados()

        return asignaturas.map { a ->
            val unidadIds = unidades.filter { it.asignaturaId == a.id }.map { it.id }.toSet()
            val deAsignatura = materiales.filter { it.unidadId in unidadIds }
            AsignaturaConProgreso(
                asignatura = a,
                totalMateriales = deAsignatura.size,
                materialEstudiado = deAsignatura.count { it.id in estudiados }
            )
        }
    }

    suspend fun asignatura(id: Int): Asignatura? =
        db.from("asignaturas").select { filter { eq("id", id) } }.decodeSingleOrNull<Asignatura>()

    suspend fun unidadesConMateriales(asignaturaId: Int): List<UnidadConMateriales> {
        val unidades = db.from("unidades").select {
            filter { eq("asignatura_id", asignaturaId) }
            order("id", Order.ASCENDING)
        }.decodeList<Unidad>()
        if (unidades.isEmpty()) return emptyList()

        val materiales = db.from("materiales").select {
            filter { isIn("unidad_id", unidades.map { it.id }) }
            order("id", Order.ASCENDING)
        }.decodeList<Material>()
        val estudiados = idsEstudiados()

        return unidades.map { u ->
            UnidadConMateriales(
                unidad = u,
                materiales = materiales.filter { it.unidadId == u.id }
                    .map { it.copy(estudiado = it.id in estudiados) }
            )
        }
    }

    suspend fun marcarEstudiado(materialId: Int, estudiado: Boolean) {
        if (estudiado) {
            db.from("material_estudiado").upsert(EstudiadoFila(uid, materialId))
        } else {
            db.from("material_estudiado").delete {
                filter {
                    eq("usuario_id", uid)
                    eq("material_id", materialId)
                }
            }
        }
    }

    suspend fun publicarMaterial(
        unidadId: Int, titulo: String, tipo: String,
        nombreArchivo: String, bytes: ByteArray
    ) {
        val seguro = nombreArchivo.replace(Regex("[^A-Za-z0-9._-]"), "_")
        val path = "unidad_$unidadId/${UUID.randomUUID()}_$seguro"
        db.storage.from(SupabaseProvider.BUCKET).upload(path, bytes)
        db.from("materiales").insert(
            MaterialNuevo(unidadId, titulo, tipo, path, nombreArchivo, uid)
        )
    }

    suspend fun descargarBytes(storagePath: String): ByteArray =
        db.storage.from(SupabaseProvider.BUCKET).downloadAuthenticated(storagePath)

    private suspend fun armarDetalle(consultas: List<Consulta>): List<ConsultaDetalle> {
        val asignaturas = db.from("asignaturas").select().decodeList<Asignatura>().associateBy { it.id }
        val unidades = db.from("unidades").select().decodeList<Unidad>().associateBy { it.id }
        return consultas.mapNotNull { c ->
            val a = asignaturas[c.asignaturaId] ?: return@mapNotNull null
            val u = unidades[c.unidadId] ?: return@mapNotNull null
            ConsultaDetalle(c, a, u)
        }
    }

    suspend fun consultas(): List<ConsultaDetalle> {
        val lista = db.from("consultas").select { order("id", Order.DESCENDING) }
            .decodeList<Consulta>()
        return armarDetalle(lista)
    }

    suspend fun consulta(id: Int): ConsultaDetalle? {
        val c = db.from("consultas").select { filter { eq("id", id) } }
            .decodeSingleOrNull<Consulta>() ?: return null
        return armarDetalle(listOf(c)).firstOrNull()
    }

    suspend fun crearConsulta(
        asignaturaId: Int, unidadId: Int, estudiante: String, docente: String,
        asunto: String, contenido: String, fecha: String
    ) {
        db.from("consultas").insert(
            ConsultaNueva(asignaturaId, unidadId, estudiante, docente, asunto, contenido, fecha)
        )
    }

    suspend fun contarPendientes(): Int =
        db.from("consultas").select(Columns.list("id")) { filter { eq("respondida", false) } }
            .decodeList<IdFila>().size

    suspend fun responder(id: Int, respuesta: String) {
        db.from("consultas").update({
            set("respuesta", respuesta)
            set("respondida", true)
        }) { filter { eq("id", id) } }
    }
}