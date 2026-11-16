package com.example.esucar.ui.view.perfil

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.esucar.data.local.DescargaMaterial
import com.example.esucar.data.model.AsignaturaConProgreso

@Composable
fun PerfilEstudiante(innerPadding: PaddingValues,
                     navController: NavController,
                     viewModel: PerfilViewModel
) {
    val lista = viewModel.asignaturas
    val descargados by viewModel.descargados.collectAsState()
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = innerPadding,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Encabezado("Estudiante")
        }
        item {
            InformacionPersonal()
        }
        item {
            MiActividadEstudiante(lista)
        }
        item {
            MisAsignaturas(lista)
        }
        item {
            MaterialesDescargados(
                descargados = descargados,
                onAbrir = { d ->
                    try {
                        viewModel.abrir(context, d)
                    } catch (e: Exception) {
                        Toast.makeText(
                            context,
                            "No hay una app para abrir este archivo",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                onEliminar = { d -> viewModel.eliminar(d) }
            )
        }
        item {
            Configuracion()
        }
        item{
            BotonCerrarSesion(navController)
        }
    }
}

@Composable
fun MiActividadEstudiante(lista: List<AsignaturaConProgreso>){
    val totalMaterial = lista.sumOf { it.totalMateriales }
    val materialesEstudiados = lista.sumOf { it.materialEstudiado }
    val progresoGeneral = if(totalMaterial > 0){
        (materialesEstudiados * 100) / totalMaterial
    }
    else{
        0
    }

    SeccionPerfil("Mi Actividad") {
        Text("Asignaturas Inscritas: ${lista.size}", fontSize = 16.sp)
        Text("Progreso General: ${progresoGeneral}%", fontSize = 16.sp)
    }
}
@Composable
fun MaterialesDescargados(
    descargados: List<DescargaMaterial>,
    onAbrir: (DescargaMaterial) -> Unit,
    onEliminar: (DescargaMaterial) -> Unit
) {
    SeccionPerfil("Materiales Descargados") {
        if (descargados.isEmpty()) {
            Text("Aun no has descargado materiales", fontSize = 14.sp, color = Color.Gray)
        }
        descargados.forEach { d ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { onAbrir(d) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Download,
                    contentDescription = "descargado",
                    tint = MaterialTheme.colorScheme.primary
                )
                Column(
                    Modifier
                        .weight(1f)
                        .padding(start = 12.dp)
                ) {
                    Text(d.titulo, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Text("Tipo: ${d.tipo}", fontSize = 12.sp, color = Color.Gray)
                }
                IconButton(onClick = { onEliminar(d) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar descarga")
                }
            }
        }
    }
}