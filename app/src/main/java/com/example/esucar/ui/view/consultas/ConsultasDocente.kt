package com.example.esucar.ui.view.consultas

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import com.example.esucar.rutas.SubDestination
import androidx.compose.foundation.lazy.items

@Composable
fun ConsultasDocente(innerPadding: PaddingValues,
                     navController: NavController,
                     viewModel: ConsultasViewModel
) {
    val lista = viewModel.consultas

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = innerPadding,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(lista, key = {it.consulta.id}) { item ->
            ConsultaCard(
                item = item,
                mostrarEstudiante = true,
                onCardClick = {
                    navController.navigate(
                        SubDestination.DetalleConsulta.createRoute(item.consulta.id)
                    )
                }
            )
        }
    }
}