package com.example.esucar.ui.view.asignaturas

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.esucar.rutas.SubDestination
import androidx.compose.material3.TextButton

@Composable
fun DetalleAsignaturaEstudiante(
    innerPadding: PaddingValues,
    asignaturaId: Int,
    navController: NavController,
    viewModel: DetalleAsignaturaViewModel
) {
    val unidadesAsignatura = viewModel.unidades
    val descargas by viewModel.descargas.collectAsState()
    val descargando = viewModel.descargando
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(unidadesAsignatura, key = {it.unidad.id}){ item ->
            UnidadItemAcordeon(item){material ->
                if (material.storagePath != null) {
                    val descarga = descargas.find { it.materialId == material.id }

                    when {
                        material.id in descargando -> {
                            CircularProgressIndicator(
                                Modifier.padding(12.dp).size(24.dp),
                                strokeWidth = 2.dp
                            )
                        }

                        descarga != null -> {
                            TextButton(onClick = {
                                try {
                                    viewModel.abrir(context, descarga)
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        context,
                                        "No hay una app para abrir este archivo",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp).padding(end = 4.dp)
                                )
                                Text("Abrir")
                            }
                        }

                        else -> {
                            IconButton(onClick = {
                                viewModel.descargar(material) { ok ->
                                    Toast.makeText(
                                        context,
                                        if (ok) "Material descargado" else "No se pudo descargar",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }) {
                                Icon(
                                    Icons.Default.Download,
                                    "Descargar Material"
                                )
                            }
                        }
                    }
                }

                Checkbox(
                    checked = material.estudiado,
                    onCheckedChange = { viewModel.marcarEstudiado(asignaturaId, material.id, it) }
                )
            }
        }

        item{
            Button(
                onClick = {navController.navigate(SubDestination.FormularioConsulta.createRoute(asignaturaId))},
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
            ){
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 10.dp)
                )
                Text("Consultar sobre la asignatura")
            }
        }
    }
}
