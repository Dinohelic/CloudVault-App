package com.cloudvault.app.ui.screens


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cloudvault.app.viewmodel.AuthViewModel

@Composable
fun AuthScreen(vm: AuthViewModel = viewModel()) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var isLogin by remember { mutableStateOf(true) }

    val state by vm.authState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {

        if (!isLogin) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (isLogin)
                vm.login(email, password)
            else
                vm.signup(email, password, name)
        }) {
            Text(if (isLogin) "Login" else "Sign Up")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = { isLogin = !isLogin }) {
            Text(if (isLogin) "Create Account" else "Already have account?")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = state)
    }
}