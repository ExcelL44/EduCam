package com.excell44.educam.ui.screen.auth

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.excell44.educam.ui.viewmodel.AuthViewModel
import com.excell44.educam.ui.viewmodel.AuthAction
import androidx.compose.ui.platform.LocalContext
import com.excell44.educam.ui.util.screenPadding

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val ctx = LocalContext.current
    var pseudo by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    // ✅ SUPPRIMÉ: LaunchedEffect(uiState.isLoggedIn) - Navigation gérée par NavGraph uniquement
    
    LaunchedEffect(uiState.guestAttemptsRemaining) {
        android.util.Log.d("LoginScreen", "Guest Attempts Display: ${uiState.guestAttemptsRemaining}")
    }

    // Memoize callbacks to prevent recreation on recomposition
    val onLoginClick = remember(pseudo, code) {
        { viewModel.submitAction(AuthAction.Login("${pseudo.lowercase()}@local.excell", code)) }
    }
    
    val onGuestClick = remember {
        {
            viewModel.submitAction(AuthAction.ClearError)
            viewModel.submitAction(AuthAction.GuestMode)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .screenPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Bac-X_237",
            style = MaterialTheme.typography.displayMedium.copy(
                fontFamily = com.excell44.educam.ui.theme.BacXBrandFont
            ),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Révision rapide et intelligente",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(48.dp))

        OutlinedTextField(
            value = pseudo,
            onValueChange = { if (it.length <= 15) pseudo = it },
            label = { Text("Pseudo") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = code,
            onValueChange = { if (it.length <= 4 && it.all { ch -> ch.isDigit() }) code = it },
            label = { Text("Code (4 chiffres)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        if (uiState.errorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = uiState.errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(36.dp),
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            com.excell44.educam.ui.components.PrimaryButton(
                onClick = onLoginClick,
                text = "Se connecter",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = pseudo.isNotBlank() && code.length == 4
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onNavigateToRegister) {
            Text("Pas encore de compte ? S'inscrire")
        }

        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onGuestClick) {
            Text("Continuer en tant qu'invité (${uiState.guestAttemptsRemaining} essais max)")
        }

        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = {
            val phone = "+22912345678"
            val uri = Uri.parse("https://wa.me/${phone.removePrefix("+")}?text=${Uri.encode("Bonjour, j'ai besoin d'aide pour Bac-X_237.")}")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ctx.startActivity(intent)
        }) {
            Text("Mot de passe oublié ? Contacter le support")
        }
    }
}
