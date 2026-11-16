package com.example.esucar.ui.view.consultas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.esucar.data.Sesion
import com.example.esucar.data.model.Asignatura
import com.example.esucar.data.model.Consulta
import com.example.esucar.data.model.Unidad
import com.example.esucar.data.remote.Repositorio
import com.example.esucar.ui.view.navigation.Destination
import com.example.esucar.ui.view.perfil.cerrarSesionYSalir
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FormularioConsultaViewModel : ViewModel() {
    var asignaturas by mutableStateOf<List<Asignatura>>(emptyList())
        private set
    var unidades by mutableStateOf<List<Unidad>>(emptyList())
        private set

    init {
        viewModelScope.launch {
            try {
                asignaturas = Repositorio.asignaturasConProgreso().map { it.asignatura }
            } catch (e: Exception) {
                // sin internet u otro error
            }
        }
    }

    fun cargarUnidades(asignaturaId: Int) {
        unidades = emptyList()
        viewModelScope.launch {
            try {
                unidades = Repositorio.unidadesConMateriales(asignaturaId).map { it.unidad }
            } catch (e: Exception) {
                // sin internet u otro error
            }
        }
    }

    fun guardar(consulta: Consulta, alTerminar: () -> Unit, alFallar: () -> Unit) {
        viewModelScope.launch {
            try {
                val docente = Repositorio.primerDocente()?.nombre
                    ?: "Sin Docente Registrado"
                Repositorio.crearConsulta(
                    asignaturaId = consulta.asignaturaId,
                    unidadId = consulta.unidadId,
                    estudiante = consulta.estudiante,
                    docente = docente,
                    asunto = consulta.asunto,
                    contenido = consulta.contenido,
                    fecha = consulta.fecha
                )
                alTerminar()
            } catch (e: Exception) {
                alFallar()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioConsulta(navController: NavController,
                       viewModel: FormularioConsultaViewModel = viewModel()
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val scope = rememberCoroutineScope()
    var asignatura by remember { mutableStateOf<Asignatura?>(null) }
    var unidad by remember { mutableStateOf<Unidad?>(null) }
    var asunto by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    var expandirAsignatura by remember { mutableStateOf(false) }
    var expandirUnidad by remember {mutableStateOf(false) }

    val asignaturas = viewModel.asignaturas
    val unidades = viewModel.unidades

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
                        "Nueva Consulta",
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
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
        LazyColumn(
            Modifier.fillMaxSize()
                .imePadding()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            item {
                Column(
                    Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ExposedDropdownMenuBox(
                        expandirAsignatura,
                        {expandirAsignatura = !expandirAsignatura}
                    ) {
                        OutlinedTextField(
                            value = asignatura?.nombre ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Asignatura") },
                            placeholder = {Text("Seleccionar")},
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expandirAsignatura)
                            },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expandirAsignatura,
                            {expandirAsignatura = false}
                        ) {
                            asignaturas.forEach { opcion ->
                                DropdownMenuItem(
                                    {Text(opcion.nombre)},
                                    {
                                        asignatura = opcion
                                        unidad = null
                                        viewModel.cargarUnidades(opcion.id)
                                        expandirAsignatura = false}
                                )
                            }
                        }
                    }

                    ExposedDropdownMenuBox(
                        expandirUnidad,
                        {if(asignatura != null) expandirUnidad = !expandirUnidad}
                    ) {
                        OutlinedTextField(
                            value = unidad?.nombre ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Unidad") },
                            placeholder = {
                                Text(
                                    if(asignatura == null) "Primero elige una asignatura"
                                    else "Seleccionar"
                                )
                            },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expandirUnidad)
                            },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expandirUnidad,
                            {expandirUnidad = false}
                        ) {
                            unidades.forEach { opcion ->
                                DropdownMenuItem(
                                    {Text(opcion.nombre)},
                                    {
                                        unidad = opcion
                                        expandirUnidad = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = asunto,
                        onValueChange = { asunto = it },
                        label = { Text("Asunto") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = mensaje,
                        onValueChange = {mensaje = it},
                        label = {Text("Consulta")},
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 6
                    )

                    error?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
                    }

                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = {
                            val a = asignatura
                            val u = unidad
                            if (a != null && u != null && asunto.isNotBlank() && mensaje.isNotBlank()) {
                                error = null
                                viewModel.guardar(
                                    Consulta(
                                        asignaturaId = a.id,
                                        unidadId = u.id,
                                        estudiante = Sesion.usuario?.nombre ?: "Estudiante",
                                        docente = "",
                                        asunto = asunto,
                                        contenido = mensaje,
                                        fecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                            .format(Date()),
                                        respondida = false,
                                        respuesta = null
                                    ),
                                    alTerminar = { navController.popBackStack() },
                                    alFallar = { error = "No se pudo enviar la consulta. Revisa tu conexion." }
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Enviar Consulta", fontSize = 16.sp)
                    }
                }
            }
        }
    }
}



