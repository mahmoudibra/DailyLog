package com.booking.worktracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.booking.worktracker.ui.designsystem.DSTheme

@Composable
fun SignInScreen(
    onSignIn: (email: String, password: String, rememberMe: Boolean) -> Unit,
    onNavigateToSignUp: () -> Unit,
    isLoading: Boolean,
    error: String?
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(DSTheme.spacing.screenPadding),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.widthIn(max = 400.dp),
            colors = CardDefaults.cardColors(
                containerColor = DSTheme.colors.surface
            ),
            shape = DSTheme.shapes.large,
            elevation = CardDefaults.cardElevation(defaultElevation = DSTheme.elevation.medium)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(DSTheme.spacing.space8),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(DSTheme.spacing.medium)
            ) {
                Text(
                    text = "DailyTracker",
                    style = MaterialTheme.typography.headlineLarge,
                    color = DSTheme.colors.primary
                )

                Text(
                    text = "Sign in to your account",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DSTheme.colors.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(DSTheme.spacing.small))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = "Email")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = "Password")
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { rememberMe = it },
                        enabled = !isLoading
                    )
                    Text(
                        text = "Remember me",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DSTheme.colors.onSurface
                    )
                }

                if (error != null) {
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = DSTheme.colors.error
                    )
                }

                Button(
                    onClick = { onSignIn(email, password, rememberMe) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(DSTheme.sizes.inputHeight),
                    enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
                    shape = DSTheme.shapes.small
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(DSTheme.sizes.iconMedium),
                            color = DSTheme.colors.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Sign In")
                    }
                }

                TextButton(onClick = onNavigateToSignUp) {
                    Text("Don't have an account? Sign Up")
                }
            }
        }
    }
}
