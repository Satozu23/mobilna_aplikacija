package com.example.komsilukconnect.ranking.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.komsilukconnect.auth.data.UserModel
import com.example.komsilukconnect.ranking.viewmodel.RankingViewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.foundation.background

@Composable
fun RankingScreen(
    rankingViewModel: RankingViewModel = viewModel()
) {
    val uiState by rankingViewModel.uiState.collectAsState()

    val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp)) {
                itemsIndexed(uiState.users) { index, user ->
                    UserRankItem(
                        rank = index + 1,
                        user = user,
                        isCurrentUser = user.uid == currentUserUid
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
fun UserRankItem(rank: Int, user: UserModel,isCurrentUser: Boolean) {
    val backgroundColor = if (isCurrentUser) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$rank.",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.width(40.dp)
        )
        Column {
            Text(
                text = "${user.firstName} ${user.lastName}",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "${user.points} poena",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}