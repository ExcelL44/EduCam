package com.excell44.educam.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.excell44.educam.core.session.DeviceAccount
import java.text.SimpleDateFormat
import java.util.*

/**
 * Sélecteur de compte pour switcher entre les comptes enregistrés sur l'appareil.
 */
@Composable
fun AccountSwitcher(
    accounts: List<DeviceAccount>,
    currentUserId: String?,
    onAccountSelected: (String) -> Unit,
    onAddAccount: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Comptes sur cet appareil (${accounts.size}/3)",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            accounts.forEach { account ->
                AccountItem(
                    account = account,
                    isSelected = account.userId == currentUserId,
                    onClick = { onAccountSelected(account.userId) }
                )
                
                if (account != accounts.last()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }
            
            // Bouton ajouter un compte
            if (accounts.size < 3) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onAddAccount,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("+ Ajouter un compte")
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Limite de 3 comptes atteinte",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun AccountItem(
    account: DeviceAccount,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Account",
            modifier = Modifier.size(48.dp),
            tint = if (isSelected) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = account.userName,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = account.userEmail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (account.isOfflineAccount) {
                Text(
                    text = "Compte hors ligne",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
        
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
