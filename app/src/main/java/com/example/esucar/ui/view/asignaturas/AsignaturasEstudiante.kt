package com.example.esucar.ui.view.asignaturas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.esucar.rutas.SubDestination
import androidx.compose.foundation.lazy.items

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsignaturasEstudiante(innerPadding: PaddingValues,
                          navController: NavController,
                          viewModel: AsignaturasViewModel
){
    val lista = viewModel.asignaturas

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = innerPadding,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(lista, key = {it.asignatura.id}) { item ->
            AsignaturaCard(
                item = item,
                mostrarProgreso = true,
                onCardClick = {
                    navController.navigate(
                        SubDestination.DetalleAsignatura.createRoute(item.asignatura.id)
                    )
                }
            )
        }
    }
}
