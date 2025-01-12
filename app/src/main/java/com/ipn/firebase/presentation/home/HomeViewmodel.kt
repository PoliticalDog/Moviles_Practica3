package com.ipn.firebase.presentation.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ipn.firebase.presentation.model.Comentario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class HomeViewmodel:ViewModel(){

    private var db: FirebaseFirestore = Firebase.firestore
    private val _comentario = MutableStateFlow<List<Comentario>>(emptyList())

    val comentario: StateFlow<List<Comentario>> = _comentario
    val showPostCreationDialog = MutableStateFlow(false)

    private val _currentUser = MutableStateFlow<String?>(null)
    val currentUser: StateFlow<String?> = _currentUser

    private val _isAdmin = MutableStateFlow(false)
    val isAdmin: StateFlow<Boolean> = _isAdmin

    init {
        getComentario()
        getCurrentUser()
        getUserRole()
    }

    private fun getComentario() {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO){ getAllComentario() }
            _comentario.value = result
        }
    }

    suspend fun getAllComentario(): List<Comentario> {
        return try {
            db.collection("comentario")
                .get()
                .await()
                .documents
                .mapNotNull { snapshot ->
                    snapshot.toObject(Comentario::class.java)?.copy(id = snapshot.id)
                }
        } catch (e: Exception) {
            Log.e("HomeViewModel", "Error fetching comments: ${e.message}")
            emptyList()
        }
    }

    fun createPost(comentario: Comentario) {
        viewModelScope.launch {
            try {
                // Genera un nuevo ID manualmente
                val newDocRef = db.collection("comentario").document()
                val comentarioConId = comentario.copy(id = newDocRef.id)

                // Guarda el comentario con el ID generado
                newDocRef.set(comentarioConId)
                    .addOnSuccessListener { Log.i("HomeViewModel", "Post creado exitosamente") }
                    .addOnFailureListener { e -> Log.e("HomeViewModel", "Error al crear post: ${e.message}") }

                // Actualiza la lista de comentarios
                getComentario()
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error: ${e.message}")
            }
        }
    }


    private fun getCurrentUser() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        _currentUser.value = firebaseUser?.displayName ?: "Anónimo"
    }

    private fun checkIfAdmin() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        Firebase.firestore.collection("usuarios").document(userId).get()
            .addOnSuccessListener { document ->
                _isAdmin.value = document.getBoolean("isAdmin") ?: false
            }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            try {
                db.collection("comentario").document(postId).delete().await()
                getComentario() // Actualizar la lista de comentarios
            } catch (e: Exception) {
                Log.e("Error", "Failed to delete post: ${e.message}")
            }
        }
    }

    private fun getUserRole() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        Firebase.firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val role = document.getString("role")
                _isAdmin.value = role == "admin"
            }
            .addOnFailureListener { e ->
                Log.e("HomeViewModel", "Error fetching user role: ${e.message}")
            }
    }

    fun updatePost(post: Comentario) {
        viewModelScope.launch {
            try {
                db.collection("comentario").document(post.id!!).set(post).await()
                getComentario() // Actualiza la lista después del cambio
            } catch (e: Exception) {
                Log.e("Error", "Failed to update post: ${e.message}")
            }
        }
    }

    fun onDelete(id: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("comentario") // Asegúrate de que el nombre sea correcto

            .document(id)
            .delete()
            .addOnSuccessListener {
                Log.d("Firestore", "Comentario eliminado exitosamente")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al eliminar el comentario", e)
            }
    }

    val postToEdit = MutableStateFlow<Comentario?>(null)
    val showEditDialog = MutableStateFlow(false)

    fun editPost(post: Comentario) {
        postToEdit.value = post
        showEditDialog.value = true
    }



}