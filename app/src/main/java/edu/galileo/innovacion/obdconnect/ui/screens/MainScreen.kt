
package edu.galileo.innovacion.obdconnect.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun MainScreen(innerPadding: PaddingValues) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var isConnected by remember { mutableStateOf(false) }

    val tabs = listOf(
        TabItem("Connection", Icons.Default.Settings),
        TabItem("Car Status", Icons.Default.Build),
        TabItem("Error Codes", Icons.Default.Warning),
        TabItem("Terminal", Icons.Default.List)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        // Status Bar
        ConnectionStatusBar(isConnected = isConnected)

        // Tab Row
        TabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(tab.title) },
                    icon = { Icon(tab.icon, contentDescription = tab.title) }
                )
            }
        }

        // Tab Content
        when (selectedTabIndex) {
            0 -> ConnectionTabContent(
                onConnectionChanged = { connected ->
                    isConnected = !isConnected // Toggle connection state
                }
            )
            1 -> CarStatusTabContent()
            2 -> ErrorCodesTabContent()
            3 -> TerminalTabContent(isConnected = isConnected)
        }
    }
}

@Composable
private fun ConnectionStatusBar(isConnected: Boolean) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (isConnected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.errorContainer
        },
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isConnected) Icons.Default.CheckCircle else Icons.Default.Close,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (isConnected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onErrorContainer
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isConnected) "Connected to OBD2 Sensor" else "Not Connected",
                style = MaterialTheme.typography.bodySmall,
                color = if (isConnected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onErrorContainer
                }
            )
        }
    }
}

private data class TabItem(
    val title: String,
    val icon: ImageVector
)