package com.ipn.firebase

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ipn.firebase.presentation.home.HomeScreen
import com.ipn.firebase.presentation.initial.InitialScreen
import com.ipn.firebase.presentation.login.LoginScreen
import com.ipn.firebase.presentation.signup.SignUpScreen

@Composable
fun NavigationWrapper(
    navHostController: NavHostController,
    auth: FirebaseAuth,
) {

    NavHost(navController = navHostController, startDestination = "initial") {
        composable("initial") {
            InitialScreen(
                navigateToLogin = {navHostController.navigate("login")},
                navigateToSignUp = {navHostController.navigate("signup")}
            )
        }
        composable("login"){
            LoginScreen(auth){ navHostController.navigate("home")}
        }
        composable("signup"){
            SignUpScreen(auth)
        }
        composable("home"){
            HomeScreen()
        }
    }
}