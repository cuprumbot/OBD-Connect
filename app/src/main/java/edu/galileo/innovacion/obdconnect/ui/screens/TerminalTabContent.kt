package edu.galileo.innovacion.obdconnect.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.galileo.innovacion.obdconnect.data.MessageType
import edu.galileo.innovacion.obdconnect.data.TerminalMessage
import kotlinx.coroutines.launch

@Composable
fun TerminalTabContent(
    isConnected: Boolean = false
) {
    var messageInput by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<TerminalMessage>() }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Terminal header
        Text(
            text = "Terminal (Debug)",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Terminal display
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF1E1E1E),
            shadowElevation = 2.dp
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(messages) { message ->
                    TerminalMessageItem(message)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Input section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = messageInput,
                onValueChange = { messageInput = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Enter command...") },
                singleLine = true,
                enabled = isConnected
            )

            Button(
                onClick = {
                    if (messageInput.isNotBlank()) {
                        // Add sent message to terminal
                        messages.add(
                            TerminalMessage(
                                text = messageInput,
                                type = MessageType.SENT
                            )
                        )
                        
                        // Scroll to bottom
                        coroutineScope.launch {
                            listState.animateScrollToItem(messages.size - 1)
                        }
                        
                        // TODO: Actually send the message to the sensor
                        
                        // Clear input
                        messageInput = ""
                    }
                },
                enabled = isConnected && messageInput.isNotBlank(),
                modifier = Modifier.height(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send"
                )
            }
        }

        // Connection status hint
        if (!isConnected) {
            Text(
                text = "Connect to OBD2 sensor to send commands",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun TerminalMessageItem(message: TerminalMessage) {
    val textColor = when (message.type) {
        MessageType.SENT -> Color(0xFF4CAF50)      // Green for sent
        MessageType.RECEIVED -> Color(0xFF2196F3)  // Blue for received
        MessageType.SYSTEM -> Color(0xFFFFC107)    // Yellow for system
    }

    val prefix = when (message.type) {
        MessageType.SENT -> ">> "
        MessageType.RECEIVED -> "<< "
        MessageType.SYSTEM -> "[*] "
    }

    Text(
        text = "$prefix${message.text}",
        color = textColor,
        fontFamily = FontFamily.Monospace,
        fontSize = 13.sp,
        lineHeight = 18.sp
    )
}
