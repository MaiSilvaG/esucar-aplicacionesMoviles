package com.example.esucar.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

object Roles {
    const val ESTUDIANTE = "ESTUDIANTE"
    const val DOCENTE = "DOCENTE"
}

@Serializable
data class Asignatura(
    val id: Int = 0,
    val nombre: String,
    val codigo: String,
    val periodo: String
)

@Serializable
data class Unidad(
    val id: Int = 0,
    @SerialName("asignatura_id") val asignaturaId: Int,
    val nombre: String
)

@Serializable
data class Material(
    val id: Int = 0,
    @SerialName("unidad_id") val unidadId: Int,
    val titulo: String,
    val tipo: String,
    @SerialName("storage_path") val storagePath: String? = null,
    @SerialName("nombre_archivo") val nombreArchivo: String? = null,
    @Transient val estudiado: Boolean = false
)

@Serializable
data class Consulta(
    val id: Int = 0,
    @SerialName("asignatura_id") val asignaturaId: Int,
    @SerialName("unidad_id") val unidadId: Int,
    val estudiante: String,
    val docente: String,
    val asunto: String,
    val contenido: String,
    val fecha: String,
    val respondida: Boolean = false,
    val respuesta: String? = null
)

@Serializable
data class Usuario(
    val id: String,
    val nombre: String,
    val correo: String,
    val rol: String
)

data class AsignaturaConProgreso(
    val asignatura: Asignatura,
    val totalMateriales: Int,
    val materialEstudiado: Int
)

data class UnidadConMateriales(
    val unidad: Unidad,
    val materiales: List<Material> = emptyList()
)

data class ConsultaDetalle(
    val consulta: Consulta,
    val asignatura: Asignatura,
    val unidad: Unidad
)

@Serializable
internal data class PerfilNuevo(
    val id: String, val nombre: String, val correo: String, val rol: String
)

@Serializable
internal data class MaterialNuevo(
    @SerialName("unidad_id") val unidadId: Int,
    val titulo: String,
    val tipo: String,
    @SerialName("storage_path") val storagePath: String?,
    @SerialName("nombre_archivo") val nombreArchivo: String?,
    @SerialName("subido_por") val subidoPor: String
)

@Serializable
internal data class ConsultaNueva(
    @SerialName("asignatura_id") val asignaturaId: Int,
    @SerialName("unidad_id") val unidadId: Int,
    val estudiante: String,
    val docente: String,
    val asunto: String,
    val contenido: String,
    val fecha: String
)

@Serializable
internal data class IdFila(val id: Int)

@Serializable
internal data class EstudiadoFila(
    @SerialName("usuario_id") val usuarioId: String,
    @SerialName("material_id") val materialId: Int
)