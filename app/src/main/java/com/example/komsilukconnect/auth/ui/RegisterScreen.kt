package com.example.komsilukconnect.auth.ui

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.komsilukconnect.R
import com.example.komsilukconnect.auth.viewmodel.AuthEvent
import com.example.komsilukconnect.auth.viewmodel.AuthViewModel
import java.io.File
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.Manifest

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val uiState by authViewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showImageSourceDialog by remember { mutableStateOf(false) }


    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        authViewModel.onEvent(AuthEvent.ImageUriChanged(uri))
    }

    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            authViewModel.onEvent(AuthEvent.ImageUriChanged(tempCameraUri))
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            if (isGranted) {
                val newUri = createImageUri(context)
                tempCameraUri = newUri
                cameraLauncher.launch(newUri)
            }
        }
    )

    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("Izaberi izvor") },
            text = { Text("Odakle želite da dodate sliku?") },

            confirmButton = {
                Button(onClick = {
                    galleryLauncher.launch("image/*")
                    showImageSourceDialog = false
                }) { Text("Galerija") }
            },

            dismissButton = {
                Button(onClick = {
                    showImageSourceDialog = false
                    val permission = Manifest.permission.CAMERA
                    if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                        val newUri = createImageUri(context)
                        tempCameraUri = newUri
                        cameraLauncher.launch(newUri)
                    } else {
                        cameraPermissionLauncher.launch(permission)
                    }
                }) { Text("Kamera") }
            }
        )
    }

    LaunchedEffect(key1 = uiState.isSuccess) {
        if (uiState.isSuccess) {
            onRegisterSuccess()
            authViewModel.onEvent(AuthEvent.NavigationHandled)
        }
    }

    Box(modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Registruj se", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))

            Image(
                painter = rememberAsyncImagePainter(
                    model = uiState.imageUri ?: R.drawable.ic_launcher_background
                ),
                contentDescription = "Profilna slika",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .clickable { showImageSourceDialog = true },
                contentScale = ContentScale.Crop
            )
            Text("Dodirni za izmenu slike", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(16.dp))


            OutlinedTextField(value = uiState.firstName,
                onValueChange = { authViewModel.onEvent(AuthEvent.FirstNameChanged(it)) },
                label = { Text("Ime") },
                modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(value = uiState.lastName,
                onValueChange = { authViewModel.onEvent(AuthEvent.LastNameChanged(it)) },
                label = { Text("Prezime") },
                modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(value = uiState.phoneNumber,
                onValueChange = { authViewModel.onEvent(AuthEvent.PhoneNumberChanged(it)) },
                label = { Text("Broj telefona") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(value = uiState.email,
                onValueChange = { authViewModel.onEvent(AuthEvent.EmailChanged(it)) },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = uiState.errorMessage != null)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(value = uiState.password,
                onValueChange = { authViewModel.onEvent(AuthEvent.PasswordChanged(it)) },
                label = { Text("Lozinka") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = uiState.errorMessage != null)
            Spacer(modifier = Modifier.height(8.dp))

            uiState.errorMessage?.let {
                Text(text = it, color = Color.Red)
            }
            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { authViewModel.onEvent(AuthEvent.Register) },
                modifier = Modifier.fillMaxWidth()) {
                Text("Registruj se")
            }
            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = onNavigateToLogin,
                modifier = Modifier.fillMaxWidth()) {
                Text("Imaš nalog? Prijavi se")
            }
        }

        if (uiState.isLoading) {
            CircularProgressIndicator()
        }
    }
}

private fun createImageUri(context: Context): Uri {
    val imageFile = File(context.cacheDir, "images/${System.currentTimeMillis()}.jpg")
    imageFile.parentFile?.mkdirs()
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        imageFile
    )
}