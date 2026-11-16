package com.example.esucar.data.local

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import com.example.esucar.data.model.Material
import com.example.esucar.data.remote.Repositorio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File

class DescargasRepositorio(context: Context) {
    private val appContext = context.applicationContext
    private val dao = AppDatabase.getInstance(appContext).descargaDao()

    fun observarDescargas(usuarioId: String): Flow<List<DescargaMaterial>> =
        dao.observarTodas(usuarioId)

    fun observarIdsDescargados(usuarioId: String): Flow<List<Int>> =
        dao.observarIds(usuarioId)

    suspend fun descargar(material: Material, usuarioId: String): DescargaMaterial =
        withContext(Dispatchers.IO) {
            val path = material.storagePath ?: error("Este material no tiene archivo adjunto")
            val nombre = material.nombreArchivo ?: "material_${material.id}"

            val bytes = Repositorio.descargarBytes(path)// Supabase Storage
            val carpeta = File(appContext.filesDir, "materiales").apply { mkdirs() }
            val archivo = File(carpeta, "${material.id}_$nombre")
            archivo.writeBytes(bytes)// memoria del telefono

            val registro = DescargaMaterial(
                materialId = material.id,
                usuarioId = usuarioId,
                titulo = material.titulo,
                tipo = material.tipo,
                nombreArchivo = nombre,
                rutaLocal = archivo.absolutePath
            )
            dao.insertar(registro)                                         // SQLite (Room)
            registro
        }

    suspend fun eliminar(materialId: Int, usuarioId: String) = withContext(Dispatchers.IO) {
        dao.obtener(materialId, usuarioId)?.let { File(it.rutaLocal).delete() }
        dao.eliminar(materialId, usuarioId)
    }

    fun abrir(context: Context, descarga: DescargaMaterial) {
        val archivo = File(descarga.rutaLocal)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
        val mime = MimeTypeMap.getSingleton()
            .getMimeTypeFromExtension(archivo.extension.lowercase()) ?: "*/*"
        context.startActivity(
            Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mime)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    }
}

data class ArchivoElegido(val nombre: String, val bytes: ByteArray)

fun leerArchivo(context: Context, uri: Uri): ArchivoElegido? {
    val nombre = context.contentResolver.query(uri, null, null, null, null)?.use { c ->
        val i = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (c.moveToFirst() && i >= 0) c.getString(i) else null
    } ?: "archivo"
    val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
    return ArchivoElegido(nombre, bytes)
}