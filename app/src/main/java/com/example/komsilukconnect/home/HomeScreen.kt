package com.example.komsilukconnect.home
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.komsilukconnect.auth.data.AuthRepositoryImpl
import com.example.komsilukconnect.home.viewmodel.HomeEvent
import com.example.komsilukconnect.home.viewmodel.HomeViewModel
import com.example.komsilukconnect.post.data.PostRepositoryImpl
import com.example.komsilukconnect.post.data.PostType
import com.example.komsilukconnect.post.ui.CreatePostScreen
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import android.app.NotificationManager
import android.app.NotificationChannel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.example.komsilukconnect.utils.calculateDistance
private const val CHANNEL_ID = "nearby_posts_channel"
@SuppressLint("MissingPermission")
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = viewModel(),
    onMarkerClick: (String) -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val authRepository = remember { AuthRepositoryImpl() }
    val postRepository = remember { PostRepositoryImpl() }
    var hasLocationPermission by remember { mutableStateOf(checkLocationPermission(context)) }
    var lastKnownLocation by remember { mutableStateOf<LatLng?>(null) }
    var showCreatePostDialog by remember { mutableStateOf(false) }

    val homeUiState by homeViewModel.uiState.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> hasLocationPermission = isGranted }
    )
    val locationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> }
    )
    LaunchedEffect(Unit) {
        createNotificationChannel(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }


    DisposableEffect(key1 = hasLocationPermission) {
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.locations.lastOrNull()?.let { location ->
                    val newLocation = LatLng(location.latitude, location.longitude)
                    lastKnownLocation = newLocation
                    homeViewModel.checkForNewNearbyPosts(newLocation)
                    coroutineScope.launch {
                        authRepository.updateUserLocation(newLocation)
                    }
                }
            }
        }
        if (hasLocationPermission) {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L).build()
            locationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        onDispose {
            locationClient.removeLocationUpdates(locationCallback)
        }
    }

    LaunchedEffect(key1 = homeUiState.newNearbyPost) {
        homeUiState.newNearbyPost?.let { post ->
            showNotification(context, "Nova objava u blizini!", post.title)
            homeViewModel.onEvent(HomeEvent.NotificationShown)
        }
    }

    val defaultLocation = LatLng(43.3209, 21.8958)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 14f)
    }
    var isMapCentered by remember { mutableStateOf(false) }

    LaunchedEffect(lastKnownLocation) {
        if (lastKnownLocation != null && !isMapCentered) {
            coroutineScope.launch {
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngZoom(lastKnownLocation!!, 15f),
                    durationMs = 1500
                )
                isMapCentered = true
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = hasLocationPermission),
            uiSettings = MapUiSettings(zoomControlsEnabled = true)
        ) {
            val radiusInKm = homeUiState.searchRadiusKm.toDouble()
            val postsToDisplayOnMap = lastKnownLocation?.let { userLocation ->
                homeUiState.allPosts.filter { post ->
                    post.location?.let { postLocation ->
                        calculateDistance(userLocation, LatLng(postLocation.latitude, postLocation.longitude)) <= radiusInKm
                    } ?: false
                }
            } ?: emptyList()

            postsToDisplayOnMap.forEach { post ->
                post.location?.let { geoPoint ->
                    val markerColor = if (post.type == PostType.OFFER) {
                        BitmapDescriptorFactory.HUE_AZURE
                    } else {
                        BitmapDescriptorFactory.HUE_RED
                    }
                    Marker(
                        state = MarkerState(position = LatLng(geoPoint.latitude, geoPoint.longitude)),
                        title = post.title,
                        snippet = post.description,
                        onInfoWindowClick = { onMarkerClick(post.id) },
                        icon = BitmapDescriptorFactory.defaultMarker(markerColor)
                    )
                }
            }
        }

        Column(
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp, start = 16.dp, end = 16.dp)
        ) {
            val df = DecimalFormat("#.##")
            Text(
                text = "Radijus pretrage: ${df.format(homeUiState.searchRadiusKm)} km",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Slider(
                value = homeUiState.searchRadiusKm,
                onValueChange = { newRadius ->
                    homeViewModel.onEvent(HomeEvent.OnRadiusChanged(newRadius))
                },
                valueRange = 0.1f..5.0f
            )
        }

        FloatingActionButton(
            onClick = { if (lastKnownLocation != null) { showCreatePostDialog = true } },
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Dodaj objavu")
        }

        if (showCreatePostDialog) {
            CreatePostScreen(
                onDismiss = { showCreatePostDialog = false },
                onCreatePost = { title, description, postType ->
                    lastKnownLocation?.let { location ->
                        coroutineScope.launch {
                            val geoPoint = GeoPoint(location.latitude, location.longitude)
                            postRepository.createPost(title, description, postType, 2, geoPoint)
                        }
                    }
                    showCreatePostDialog = false
                }
            )
        }
    }
}
private fun createNotificationChannel(context: Context) {
    val name = "Objave u blizini"
    val descriptionText = "Notifikacije za nove objave u vašem komšiluku"
    val importance = NotificationManager.IMPORTANCE_DEFAULT
    val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
        description = descriptionText
    }
    val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.createNotificationChannel(channel)
}
@SuppressLint("MissingPermission")
private fun showNotification(context: Context, title: String, content: String) {
    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle(title)
        .setContentText(content)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)

    with(NotificationManagerCompat.from(context)) {
        notify(System.currentTimeMillis().toInt(), builder.build())
    }
}
private fun checkLocationPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}