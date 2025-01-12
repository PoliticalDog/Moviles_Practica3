package com.ipn.firebase.presentation.home

import android.os.Parcelable.Creator
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.FirebaseFirestore
import com.ipn.firebase.presentation.model.Comentario
import com.ipn.firebase.ui.theme.Black

@Composable
fun HomeScreen(viewmodel: HomeViewmodel = HomeViewmodel()) {

    val comentario: State<List<Comentario>> = viewmodel.comentario.collectAsState()

    Column(Modifier.fillMaxSize().background(Black)) {
        Text("Comentarios Destacados",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp,
            modifier = Modifier.padding(16.dp)
        )

        Button(
            onClick = { viewmodel.showPostCreationDialog.value = true },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Crear Post")
        }

        LazyRow {
            items(comentario.value) { item ->
                ComentarioItem(
                    comentario = item,
                    isAdmin = viewmodel.isAdmin.collectAsState().value,
                    onEdit = { viewmodel.editPost(it) },
                    onDelete = { postId -> viewmodel.deletePost(postId) }
                )
            }
        }


        // Diálogo para crear post

        val postToEdit = viewmodel.postToEdit.collectAsState()
        val showEditDialog = viewmodel.showEditDialog.collectAsState()
        val showDialog = viewmodel.showPostCreationDialog.collectAsState()

        if (showEditDialog.value && postToEdit.value != null) {
            PostEditDialog(
                comentario = postToEdit.value!!,
                onPostUpdated = { updatedPost ->
                    viewmodel.updatePost(updatedPost)
                    viewmodel.showEditDialog.value = false
                },
                onClose = { viewmodel.showEditDialog.value = false }
            )
        }

        if (showDialog.value) {
            PostCreationDialog(
                onPostCreated = { post ->
                    viewmodel.createPost(post)
                },
                onClose = { viewmodel.showPostCreationDialog.value = false }
            )
        }
    }
}

@Composable
fun PostCreationDialog(
    viewmodel: HomeViewmodel = HomeViewmodel(),
    onPostCreated: (Comentario) -> Unit,
    onClose: () -> Unit
) {
    Dialog(onDismissRequest = { onClose() }) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .background(Color.White)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Crear Nuevo Post",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            var descripcion by remember { mutableStateOf("") }
            var estado by remember { mutableStateOf("") }
            var imageUrl by remember { mutableStateOf("") }

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = estado,
                onValueChange = { estado = it },
                label = { Text("Estado") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = imageUrl,
                onValueChange = { imageUrl = it },
                label = { Text("URL de la Imagen (opcional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val newPost = Comentario(
                        descripcion = descripcion,
                        estado = estado,
                        image = imageUrl
                    )
                    onPostCreated(newPost)
                    onClose()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Publicar")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val newPost = Comentario(
                        descripcion = descripcion,
                        estado = estado,
                        image = imageUrl,
                        usuario = viewmodel.currentUser.value ?: "Anónimo"
                    )
                    onPostCreated(newPost)
                    onClose()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Publicar Anonimo")
            }


            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { onClose() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Cancelar")
            }
        }
    }
}


@Composable
fun ComentarioItem(
    comentario: Comentario,
    isAdmin: Boolean,
    onEdit: (Comentario) -> Unit,
    onDelete: (String) -> Unit
) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(text = "Usuario: ${comentario.usuario.orEmpty()}", color = Color.White)
        Text(text = comentario.descripcion.orEmpty(), color = Color.White)
        Text(
            text = "Estado:",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Text(text = comentario.estado.orEmpty(), color = Color.White)
        AsyncImage(
            modifier = Modifier.size(450.dp),
            model = comentario.image,
            contentDescription = null,
        )

        if (isAdmin) {
            Row {
                Button(onClick = { onEdit(comentario) }) {
                    Text("Editar")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    comentario.id?.let { id ->
                        onDelete(id)
                    } ?: Log.e("Firestore", "ID del comentario es nulo")
                }) {
                    Text(text = "Eliminar")
                }
            }
        }
    }
}


@Composable
fun PostEditDialog(
    comentario: Comentario,
    onPostUpdated: (Comentario) -> Unit,
    onClose: () -> Unit
) {
    Dialog(onDismissRequest = { onClose() }) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .background(Color.White)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Editar Post",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            var descripcion by remember { mutableStateOf(comentario.descripcion.orEmpty()) }
            var estado by remember { mutableStateOf(comentario.estado.orEmpty()) }
            var imageUrl by remember { mutableStateOf(comentario.image.orEmpty()) }

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = estado,
                onValueChange = { estado = it },
                label = { Text("Estado") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = imageUrl,
                onValueChange = { imageUrl = it },
                label = { Text("URL de la Imagen (opcional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val updatedPost = comentario.copy(
                        descripcion = descripcion,
                        estado = estado,
                        image = imageUrl
                    )
                    onPostUpdated(updatedPost)
                    onClose()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Guardar Cambios")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { onClose() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Cancelar")
            }
        }
    }
}





