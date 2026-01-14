package edu.galileo.innovacion.obdconnect.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun ConnectionTabContent(
    onConnectionChanged: (Boolean) -> Unit
) {
    var ipAddress by remember { mutableStateOf("192.168.0.10") }
    var port by remember { mutableStateOf("35000") }
    var delay by remember { mutableStateOf("500") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon header
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "OBD2 Connection Settings",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        // IP Address field
        OutlinedTextField(
            value = ipAddress,
            onValueChange = { ipAddress = it },
            label = { Text("IP Address") },
            placeholder = { Text("192.168.0.10") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Port field
        OutlinedTextField(
            value = port,
            onValueChange = { port = it },
            label = { Text("Port") },
            placeholder = { Text("35000") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Delay field
        OutlinedTextField(
            value = delay,
            onValueChange = { delay = it },
            label = { Text("Delay (ms)") },
            placeholder = { Text("500") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Connect button
        Button(
            onClick = { 
                // TODO: Implement real connection logic
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Connect")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mock Connect button (for testing)
        OutlinedButton(
            onClick = { 
                onConnectionChanged(true)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Mock Connect (Debug)")
        }
    }
}
