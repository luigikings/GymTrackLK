package com.example.gymapplktrack.ui.features.exercises

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.gymapplktrack.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExerciseScreen(
    onBack: () -> Unit,
    onSave: (String, String?, String?, String?) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var name by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue("")) }
    var category by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    var imageUri by rememberSaveable { mutableStateOf<String?>(null) }

    val pickImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        imageUri = uri?.toString()
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            pickImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    fun requestImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pickImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        } else {
            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agregar ejercicio") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            ExerciseImagePreview(imageUri = imageUri)
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = ::requestImage) {
                Icon(Icons.Default.Image, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Seleccionar imagen")
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nombre del ejercicio") },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Categoría (opcional)") }
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Notas") },
                minLines = 3
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    if (name.text.isBlank()) {
                        coroutineScope.launch { snackbarHostState.showSnackbar("El nombre es obligatorio") }
                    } else {
                        onSave(name.text.trim(), category.takeIf { it.isNotBlank() }, imageUri, notes.takeIf { it.isNotBlank() })
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar")
            }
        }
    }
}

@Composable
private fun ExerciseImagePreview(imageUri: String?) {
    val shape = RoundedCornerShape(16.dp)
    if (imageUri != null) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUri)
                .crossfade(true)
                .build(),
            contentDescription = "Imagen del ejercicio",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(shape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentScale = ContentScale.Crop
        )
    } else {
        Image(
            painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "Sin imagen",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(shape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentScale = ContentScale.Crop
        )
    }
}
