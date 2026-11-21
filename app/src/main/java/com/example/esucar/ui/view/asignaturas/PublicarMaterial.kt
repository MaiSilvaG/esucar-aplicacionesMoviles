package com.example.esucar.ui.view.asignaturas

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.esucar.data.Sesion
import com.example.esucar.data.local.ArchivoElegido
import com.example.esucar.data.local.leerArchivo
import com.example.esucar.data.model.Asignatura
import com.example.esucar.data.model.Unidad
import com.example.esucar.data.remote.Repositorio
import com.example.esucar.ui.view.navigation.Destination
import com.example.esucar.ui.view.perfil.cerrarSesionYSalir
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PublicarMaterialViewModel : ViewModel() {
    var asignatura by mutableStateOf<Asignatura?>(null)
        private set
    var unidades by mutableStateOf<List<Unidad>>(emptyList())
        private set
    var subiendo by mutableStateOf(false)
        private set

    fun cargar(asignaturaId: Int) {
        viewModelScope.launch {
            try {
                asignatura = Repositorio.asignatura(asignaturaId)
                unidades = Repositorio.unidadesConMateriales(asignaturaId).map { it.unidad }
            } catch (e: Exception) {
                //sin internet u otro error
            }
        }
    }

    fun publicar(
        unidadId: Int,
        titulo: String,
        tipo: String,
        archivo: ArchivoElegido,
        alTerminar: () -> Unit,
        alFallar: () -> Unit
    ) {
        subiendo = true
        viewModelScope.launch {
            try {
                Repositorio.publicarMaterial(unidadId, titulo, tipo, archivo.nombre, archivo.bytes)
                subiendo = false
                alTerminar()
            } catch (e: Exception) {
                subiendo = false
                alFallar()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicarMaterial(asignaturaId: Int,
                     navController: NavController,
                     viewModel: PublicarMaterialViewModel  = viewModel()
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(asignaturaId) { viewModel.cargar(asignaturaId) }
    val asignatura = viewModel.asignatura
    val unidades = viewModel.unidades


    var unidad by remember { mutableStateOf<Unidad?>(null) }
    var titulo by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("") }
    var archivo by remember { mutableStateOf<ArchivoElegido?>(null) }
    var expandirUnidad by remember { mutableStateOf(false) }

    val formularioCompleto = unidad != null && titulo.isNotBlank() && tipo.isNotBlank() &&
            archivo != null && !viewModel.subiendo

    val selector = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            scope.launch {
                val elegido = withContext(Dispatchers.IO) { leerArchivo(context, uri) }
                if (elegido != null) {
                    archivo = elegido
                    if (tipo.isBlank()) tipo = elegido.nombre.substringAfterLast('.', "").uppercase()
                } else {
                    Toast.makeText(context, "No se pudo leer el archivo", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LaunchedEffect(Unit) { if (!Sesion.esDocente) navController.popBackStack() }

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
                        "Publicar Material",
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
            Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Column(
                    Modifier.fillMaxWidth(),
                    Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = asignatura?.nombre ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Asignatura") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    ExposedDropdownMenuBox(
                        expandirUnidad,
                        { expandirUnidad = !expandirUnidad }
                    ) {
                        OutlinedTextField(
                            value = unidad?.nombre ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Unidad") },
                            placeholder = { Text("Seleccionar") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expandirUnidad)
                            },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expandirUnidad,
                            { expandirUnidad = false }
                        ) {
                            unidades.forEach { opcion ->
                                DropdownMenuItem(
                                    { Text(opcion.nombre) },
                                    {
                                        unidad = opcion
                                        expandirUnidad = false
                                    }
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = titulo,
                        onValueChange = { titulo = it },
                        label = { Text("Titulo Material") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = tipo,
                        onValueChange = { tipo = it },
                        label = { Text("Tipo") },
                        placeholder = { Text("Ej: PDF, Video, Guia") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    //boton para elegir el archivo
                    OutlinedButton(
                        onClick = { selector.launch("*/*") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.AttachFile,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(archivo?.nombre ?: "Elegir archivo")
                    }

                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val u = unidad
                            val a = archivo
                            if (u != null && a != null && formularioCompleto) {
                                viewModel.publicar(
                                    unidadId = u.id,
                                    titulo = titulo.trim(),
                                    tipo = tipo.trim(),
                                    archivo = a,
                                    alTerminar = { navController.popBackStack() },
                                    alFallar = {
                                        Toast.makeText(
                                            context,
                                            "No se pudo publicar. Revisa tu conexion.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                )
                            }
                        },
                        enabled = formularioCompleto,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (viewModel.subiendo) "Subiendo..." else "Publicar", fontSize = 16.sp)
                    }
                }
            }
        }
    }
}