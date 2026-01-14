package edu.galileo.innovacion.obdconnect.ui.viewmodels

import androidx.lifecycle.ViewModel
import edu.galileo.innovacion.obdconnect.data.MessageType
import edu.galileo.innovacion.obdconnect.data.TerminalMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TerminalViewModel : ViewModel() {
    private val _messages = MutableStateFlow<List<TerminalMessage>>(emptyList())
    val messages: StateFlow<List<TerminalMessage>> = _messages.asStateFlow()

    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    fun addMessage(message: TerminalMessage) {
        _messages.value = _messages.value + message
    }

    fun sendMessage(text: String) {
        val message = TerminalMessage(
            text = text,
            type = MessageType.SENT
        )
        addMessage(message)
        // TODO: Implement actual sending logic to sensor
    }

    fun receiveMessage(text: String) {
        val message = TerminalMessage(
            text = text,
            type = MessageType.RECEIVED
        )
        addMessage(message)
    }

    fun addSystemMessage(text: String) {
        val message = TerminalMessage(
            text = text,
            type = MessageType.SYSTEM
        )
        addMessage(message)
    }

    fun clearMessages() {
        _messages.value = emptyList()
    }

    fun formatTimestamp(timestamp: Long): String {
        return timeFormat.format(Date(timestamp))
    }
}
