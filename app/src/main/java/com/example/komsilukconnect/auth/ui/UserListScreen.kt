package com.example.komsilukconnect.auth.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.komsilukconnect.R
import com.example.komsilukconnect.auth.data.UserModel
import com.example.komsilukconnect.auth.viewmodel.UserListViewModel
import androidx.compose.foundation.Image
import androidx.compose.material3.HorizontalDivider

@Composable
fun UserListScreen(
    userListViewModel: UserListViewModel = viewModel(),
    onUserClick: (String) -> Unit
) {
    val uiState by userListViewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp)) {
                items(uiState.users) { user ->
                    UserListItem(user = user, onClick = { onUserClick(user.uid) })
                    HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
                }
            }
        }
    }
}

@Composable
fun UserListItem(user: UserModel, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberAsyncImagePainter(
                model = user.profilePictureUrl ?: R.drawable.ic_launcher_background
            ),
            contentDescription = "Profilna slika",
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text("${user.firstName} ${user.lastName}", style = MaterialTheme.typography.titleMedium)
    }
}