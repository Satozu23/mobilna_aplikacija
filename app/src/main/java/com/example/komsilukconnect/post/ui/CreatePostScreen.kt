package com.example.komsilukconnect.post.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.Alignment
import com.example.komsilukconnect.post.data.PostType

@Composable
fun CreatePostScreen(
    onDismiss: () -> Unit,
    onCreatePost: (String, String,PostType) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var postType by remember { mutableStateOf(PostType.REQUEST) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Nova objava", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Usluga") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Opis") },
                    modifier = Modifier.fillMaxWidth().height(100.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text("Tip objave:", style = MaterialTheme.typography.bodyLarge)
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = postType == PostType.REQUEST,
                        onClick = { postType = PostType.REQUEST }
                    )
                    Text("Tražim")
                    Spacer(Modifier.width(16.dp))
                    RadioButton(
                        selected = postType == PostType.OFFER,
                        onClick = { postType = PostType.OFFER }
                    )
                    Text("Nudim")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Otkaži")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        onCreatePost(title, description, postType)
                    }) {
                        Text("Objavi")
                    }
                }
            }
        }
    }
}