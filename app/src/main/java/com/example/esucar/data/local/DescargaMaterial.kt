package com.example.esucar.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "descargas")
data class DescargaMaterial(
    @PrimaryKey val materialId: Int,
    val usuarioId: String,
    val titulo: String,
    val tipo: String,
    val nombreArchivo: String,
    val rutaLocal: String,
    val fechaDescarga: Long = System.currentTimeMillis()
)

@Dao
interface DescargaDao {
    @Query("SELECT * FROM descargas WHERE usuarioId = :usuarioId ORDER BY fechaDescarga DESC")
    fun observarTodas(usuarioId: String): Flow<List<DescargaMaterial>>

    @Query("SELECT materialId FROM descargas WHERE usuarioId = :usuarioId")
    fun observarIds(usuarioId: String): Flow<List<Int>>

    @Query("SELECT * FROM descargas WHERE materialId = :materialId AND usuarioId = :usuarioId LIMIT 1")
    suspend fun obtener(materialId: Int, usuarioId: String): DescargaMaterial?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(descarga: DescargaMaterial)

    @Query("DELETE FROM descargas WHERE materialId = :materialId AND usuarioId = :usuarioId")
    suspend fun eliminar(materialId: Int, usuarioId: String)
}