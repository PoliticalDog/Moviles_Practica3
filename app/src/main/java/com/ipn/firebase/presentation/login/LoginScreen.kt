package com.ipn.firebase.presentation.login

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
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ipn.firebase.R
import com.ipn.firebase.ui.theme.Black
import com.ipn.firebase.ui.theme.SelectedField
import com.ipn.firebase.ui.theme.UnselectedField


@Composable
fun LoginScreen(auth: FirebaseAuth, navigateToHome:() -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column (modifier = Modifier
        .fillMaxSize()
        .background(Black)
        .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Spacer(Modifier.height(48.dp))
        Row (){
            Icon(painter = painterResource(id = R.drawable.back),
                contentDescription = "",
                tint = White,
                modifier = Modifier.padding(vertical = 24.dp).size(24.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(48.dp))
        Text("Email", color = White, fontWeight = FontWeight.Bold, fontSize = 40.sp)
        TextField(value = email,
            onValueChange = { email = it},
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = UnselectedField,
                focusedContainerColor = SelectedField
            )

        )
        Spacer(Modifier.height(48.dp))

        Text("Password", color = White, fontWeight = FontWeight.Bold, fontSize = 40.sp)
        TextField(value = password, onValueChange = { password = it} ,
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = UnselectedField,
                focusedContainerColor = SelectedField
            )
        )
        Spacer(Modifier.height(48.dp))
        Button(onClick = {
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Dentro
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        val db = FirebaseFirestore.getInstance()
                        db.collection("users").document(userId).get()
                            .addOnSuccessListener { document ->
                                if (document.exists()) {
                                    val role = document.getString("role") ?: "user"
                                    val name = document.getString("name") ?: "Usuario"
                                    Log.i("Login", "Rol: $role, Nombre: $name")
                                    navigateToHome()
                                } else {
                                    Log.i("Login", "Documento no encontrado")
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e("Login", "Error al obtener datos del usuario: ${e.message}")
                            }
                    }
                } else {
                    // Error en el inicio de sesión
                    Log.i("Login", "Error en inicio de sesión: ${task.exception?.message}")
                }
            }
        }) {
            Text(text = "Entrar")
        }

    }
}
