package com.example.esucar.ui.view.consultas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.esucar.data.Sesion
import com.example.esucar.data.model.ConsultaDetalle
import com.example.esucar.data.remote.Repositorio
import com.example.esucar.ui.view.navigation.Destination
import com.example.esucar.ui.view.perfil.cerrarSesionYSalir
import kotlinx.coroutines.launch

class ConsultasViewModel : ViewModel() {
    var consultas by mutableStateOf<List<ConsultaDetalle>>(emptyList())
        private set

    fun cargar() {
        viewModelScope.launch{
            try {
                consultas = Repositorio.consultas()
            } catch (e: Exception) {
                // sin internet u otro error: se mantiene la lista anterior
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Consultas(navController: NavController,
              viewModel: ConsultasViewModel = viewModel()
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { viewModel.cargar() }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text(
                        "Consultas",
                        overflow = TextOverflow.Ellipsis
                    )
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(Destination.CONSULTAS.route)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Consultas"
                        )
                    }

                    IconButton(onClick = {cerrarSesionYSalir(scope, navController)}) {
                        Icon(
                            Icons.AutoMirrored.Filled.Login,
                            "login"
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        if(Sesion.esDocente){
            ConsultasDocente(innerPadding,navController,viewModel)
        }
        else{
            ConsultasEstudiante(innerPadding,navController,viewModel)
        }
    }
}

@Composable
fun ConsultaCard(item: ConsultaDetalle,
                 mostrarEstudiante: Boolean,
                 onCardClick: () -> Unit
){
    val consulta = item.consulta
    OutlinedCard(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(1.dp, Color.Black),
        modifier = Modifier.fillMaxWidth(0.9f).padding(vertical = 4.dp).clickable{onCardClick()}
    ) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            Arrangement.spacedBy(6.dp)
        ) {
            //asignatura
            Text(
                item.asignatura.nombre,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.primary
            )
            //unidad y flecha para ver detalles
            Row(
                Modifier.fillMaxWidth(),
                Arrangement.SpaceBetween,
                Alignment.CenterVertically
            ) {
                Text(
                    item.unidad.nombre,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "ver detalles",
                    tint = Color.Gray
                )
            }
            //asunto y estado
            Row(
                Modifier.fillMaxWidth(),
                Arrangement.SpaceBetween,
                Alignment.CenterVertically
            ) {
                Text(
                    consulta.asunto,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f),
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    if(consulta.respondida) "Respondida" else "Sin Respuesta",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if(consulta.respondida) Color(0xFF2E7D32) else Color(0xFFC62828)
                )
            }
            //informacion de estudiante o docente
            Text(
                if(mostrarEstudiante) "Estudiante: ${consulta.estudiante}"
                else "Docente: ${consulta.docente}",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}