                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            value = pseudo,
            onValueChange = { if (it.length <= 15 && !isReadOnly) pseudo = it },
            label = { Text("Pseudo") },
            singleLine = true,
            enabled = !isReadOnly,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))
        
        // Mode-specific UI
        when (userMode) {
            UserMode.ACTIVE -> {
                Button(
                    onClick = { /* Navigate to BetaT registration */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF800080) // Violet
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "BetaTesteur",
                        color = Color(0xFFFFD700) // Gold
                    )
                }
                Text(
                    text = "Contribue au développement de l'application et devient Beta Testeur !!!",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            UserMode.BETA_T -> {
                // Promo code progress bar
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Code Promo: BETA123", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = 0.3f, // TODO: Fetch from backend
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("3/10 inscriptions", style = MaterialTheme.typography.bodySmall)
                }
            }
            else -> {
                Text(text = "Statut du compte: $accountType", style = MaterialTheme.typography.bodyMedium)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onNavigateToBilan,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Icon(Icons.Filled.Assessment, contentDescription = "Bilan des activités")
        }

        Spacer(modifier = Modifier.height(12.dp))
        if (!isReadOnly) {
            OutlinedButton(onClick = { /* Edit profile save logic could go here */ }, modifier = Modifier.fillMaxWidth()) {
                Text("Enregistrer")
            }
        }
    }
}
