package com.example.growfit1

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(onLoggedIn: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var err by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val emailOk = email.isNotBlank()
    val passOk = password.isNotBlank() && (!isSignUp || password.length >= 6)
    val formOk = emailOk && passOk && !loading

    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("GrowFit", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            isError = isSignUp && password.isNotEmpty() && password.length < 6,
            supportingText = {
                if (isSignUp && password.length in 1..5) Text("Password must be at least 6 characters")
            }
        )

        Spacer(Modifier.height(16.dp))
        err?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                scope.launch {
                    loading = true; err = null
                    try {
                        if (isSignUp) {
                            AuthRepo.signUp(email.trim(), password)
                        } else {
                            AuthRepo.signIn(email.trim(), password)
                        }
                        onLoggedIn()
                    } catch (e: Exception) {
                        err = e.message ?: "Authentication failed"
                    } finally {
                        loading = false
                    }
                }
            },
            enabled = formOk,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (loading) "Please wait..." else if (isSignUp) "Create account" else "Sign in")
        }

        Spacer(Modifier.height(8.dp))
        TextButton(
            onClick = { isSignUp = !isSignUp },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isSignUp) "I already have an account – Sign in" else "Create account")
        }
    }
}
