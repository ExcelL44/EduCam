package com.excell44.educam.ui.screen.auth

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.platform.LocalContext
import com.excell44.educam.data.repository.PaymentService
import kotlinx.coroutines.launch
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.Icon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    // Step control: 1 = perso, 2 = parent, 3 = review/payment
    var step by remember { mutableStateOf(1) }

    // Personal info
    var pseudo by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    val classOptions = listOf("Tle C", "Tle D", "1ère C", "1ère D")
    var classExpanded by remember { mutableStateOf(false) }
    var selectedClass by remember { mutableStateOf("") }
    var school by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordConfirm by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var neighborhood by remember { mutableStateOf("") }

    // Parent info
    var parentName by remember { mutableStateOf("") }
    var parentPhone by remember { mutableStateOf("") }
    val relationOptions = listOf("Père", "Mère", "Tuteur")
    var relationExpanded by remember { mutableStateOf(false) }
    var selectedRelation by remember { mutableStateOf("") }
    var promoCode by remember { mutableStateOf("") }

    var agreedMajor by remember { mutableStateOf(false) }
    var agreedTerms by remember { mutableStateOf(false) }
    var paymentAttempts by remember { mutableStateOf(0) }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            onRegisterSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "EduCam - Inscription",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(20.dp))

        // Progress indicator with numbered steps
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            StepIndicator(number = 1, label = "Infos perso", active = step == 1)
            Divider(modifier = Modifier.weight(1f).align(Alignment.CenterVertically).padding(horizontal = 8.dp), color = MaterialTheme.colorScheme.surfaceVariant)
            StepIndicator(number = 2, label = "Parent/Tutor", active = step == 2)
            Divider(modifier = Modifier.weight(1f).align(Alignment.CenterVertically).padding(horizontal = 8.dp), color = MaterialTheme.colorScheme.surfaceVariant)
            StepIndicator(number = 3, label = "Revue & Paiement", active = step == 3)
        }

        Spacer(modifier = Modifier.height(18.dp))

        when (step) {
            1 -> {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = pseudo,
                        onValueChange = { if (it.length <= 15) pseudo = it },
                        label = { Text("Pseudo*") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { if (it.length <= 49) fullName = it },
                        label = { Text("Nom & Prénom*") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Classe & Série dropdown (respecter la liste demandée)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedClass,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Classe & Série*") },
                            trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth()
                                .clickable { classExpanded = true }
                        )
                        DropdownMenu(expanded = classExpanded, onDismissRequest = { classExpanded = false }) {
                            classOptions.forEach { option ->
                                DropdownMenuItem(text = { Text(option) }, onClick = {
                                    selectedClass = option
                                    classExpanded = false
                                })
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = school,
                        onValueChange = { if (it.length <= 49) school = it },
                        label = { Text("Établissement*") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { if (it.length <= 4 && it.all { ch -> ch.isDigit() }) password = it },
                        label = { Text("Mot de Passe (04 chiffres)*") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = passwordConfirm,
                        onValueChange = { if (it.length <= 4 && it.all { ch -> ch.isDigit() }) passwordConfirm = it },
                        label = { Text("Confirmer Mot de Passe (04 chiffres)*") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = city,
                        onValueChange = { if (it.length <= 49) city = it },
                        label = { Text("Ville") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = neighborhood,
                        onValueChange = { if (it.length <= 49) neighborhood = it },
                        label = { Text("Quartier") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        OutlinedButton(onClick = { /* go back to login */ onNavigateToLogin() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Retour")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Retour")
                        }

                        com.excell44.educam.ui.components.PrimaryButton(
                            onClick = { step = 2 },
                            text = "Suivant",
                            modifier = Modifier.widthIn(min = 140.dp)
                        )
                    }
                }
            }

            2 -> {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Informations parent/tuteur", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = parentName,
                        onValueChange = { if (it.length <= 49) parentName = it },
                        label = { Text("Nom & Prénom") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = parentPhone,
                        onValueChange = { if (it.length <= 9 && it.all { ch -> ch.isDigit() }) parentPhone = it },
                        label = { Text("Numéro de téléphone (9 chiffres)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Relation dropdown (Père/Mère/Tuteur)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedRelation,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Relation") },
                            trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth()
                                .clickable { relationExpanded = true }
                        )
                        DropdownMenu(expanded = relationExpanded, onDismissRequest = { relationExpanded = false }) {
                            relationOptions.forEach { option ->
                                DropdownMenuItem(text = { Text(option) }, onClick = {
                                    selectedRelation = option
                                    relationExpanded = false
                                })
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = promoCode,
                        onValueChange = { if (it.length <= 7) promoCode = it },
                        label = { Text("Code Promo (07 chiffres)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = agreedMajor, onCheckedChange = { agreedMajor = it })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Je suis majeur(e) OU j'ai l'autorisation parentale")
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = agreedTerms, onCheckedChange = { agreedTerms = it })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("J'accepte les conditions (lien)")
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        OutlinedButton(onClick = { step = 1 }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Retour")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Retour")
                        }

                        com.excell44.educam.ui.components.PrimaryButton(
                            onClick = { step = 3 },
                            text = "Suivant",
                            modifier = Modifier.widthIn(min = 140.dp)
                        )
                    }
                }
            }

            3 -> {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Revue & Paiement", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Simple review
                    Text("Pseudo: $pseudo")
                    Text("Nom: $fullName")
                    Text("Classe & Série: $selectedClass")
                    Text("Établissement: $school")
                    Spacer(modifier = Modifier.height(12.dp))

                    if (uiState.errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        OutlinedButton(onClick = { step = 2 }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Retour")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Retour")
                        }

                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(36.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            com.excell44.educam.ui.components.PrimaryButton(
                                onClick = {
                                    // Basic validation
                                    if (pseudo.isBlank() || fullName.isBlank() || selectedClass.isBlank() || school.isBlank()) return@PrimaryButton
                                    if (password.length != 4 || password != passwordConfirm) return@PrimaryButton
                                    if (!agreedMajor || !agreedTerms) return@PrimaryButton

                                    // Simulate payment attempts up to 3
                                    scope.launch {
                                        val payment = PaymentService()
                                        var success = false
                                        while (paymentAttempts < 3 && !success) {
                                            paymentAttempts += 1
                                            val ok = payment.attemptPayment()
                                            if (ok) {
                                                success = true
                                                // create account locally
                                                viewModel.registerFull(
                                                    pseudo = pseudo,
                                                    password = password,
                                                    fullName = fullName,
                                                    gradeLevel = selectedClass,
                                                    school = school,
                                                    city = city,
                                                    neighborhood = neighborhood,
                                                    parentName = parentName.ifBlank { null },
                                                    parentPhone = parentPhone.ifBlank { null },
                                                    relation = selectedRelation.ifBlank { null },
                                                    promoCode = promoCode.ifBlank { null }
                                                )
                                            }
                                        }

                                        if (!success) {
                                            // Payment failed after attempts -> offer support (WhatsApp) or USSD hint
                                            val phone = "+22912345678"
                                            val uri = Uri.parse("https://wa.me/${phone.removePrefix("+")}?text=${Uri.encode("La plateforme de paiement est en panne. J'ai besoin d'aide pour l'inscription.")}")
                                            val intent = Intent(Intent.ACTION_VIEW, uri)
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            ctx.startActivity(intent)
                                        }
                                    }
                                },
                                text = "Payer & Créer le compte",
                                modifier = Modifier.widthIn(min = 180.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        TextButton(onClick = onNavigateToLogin) {
            Text("Déjà un compte ? Se connecter")
        }
    }
}

@Composable
fun StepIndicator(number: Int, label: String, active: Boolean) {
    val bg = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val content = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(bg, shape = MaterialTheme.shapes.small),
            contentAlignment = Alignment.Center
        ) {
            Text(text = number.toString(), color = content)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, color = if (active) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

