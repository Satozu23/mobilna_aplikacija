package com.example.komsilukconnect.post.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.komsilukconnect.home.viewmodel.HomeEvent
import com.example.komsilukconnect.home.viewmodel.HomeViewModel
import com.example.komsilukconnect.post.data.PostType
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostListScreen(
    homeViewModel: HomeViewModel = viewModel(),
    onPostClick: (String) -> Unit
) {
    val uiState by homeViewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sve objave") }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {

            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { query ->
                    homeViewModel.onEvent(HomeEvent.OnSearchQueryChanged(query))
                },
                label = { Text("Pretraga po naslovu ili opisu") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            FilterButtons(
                selectedFilter = uiState.selectedFilter,
                onFilterSelected = { filter ->
                    homeViewModel.onEvent(HomeEvent.OnFilterChanged(filter))
                }
            )

            LazyColumn {
                items(uiState.filteredPosts) { post ->
                    PostListItem(
                        title = post.title,
                        author = post.authorName,
                        date = post.createdAt,
                        onClick = { onPostClick(post.id) }
                    )
                }
            }
        }
    }
}
@Composable
fun FilterButtons(
    selectedFilter: PostType?,
    onFilterSelected: (PostType?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Button(
            onClick = { onFilterSelected(null) },
            colors = if (selectedFilter == null) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors()
        ) { Text("Sve") }

        Button(
            onClick = { onFilterSelected(PostType.REQUEST) },
            colors = if (selectedFilter == PostType.REQUEST) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors()
        ) { Text("Tražim") }

        Button(
            onClick = { onFilterSelected(PostType.OFFER) },
            colors = if (selectedFilter == PostType.OFFER) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors()
        ) { Text("Nudim") }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostListItem(
    title: String,
    author: String,
    date: Date?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Autor: $author", style = MaterialTheme.typography.bodyMedium)
            date?.let {
                Text(
                    "Objavljeno: ${formatDate(it)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatDate(date: Date): String {
    val formatter = SimpleDateFormat("dd.MM.yyyy. 'u' HH:mm", Locale.getDefault())
    return formatter.format(date)
}