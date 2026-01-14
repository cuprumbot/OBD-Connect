
package edu.galileo.innovacion.obdconnect.data

data class TerminalMessage(
    val text: String,
    val type: MessageType,
    val timestamp: Long = System.currentTimeMillis()
)

enum class MessageType {
    SENT,      // Messages sent to the sensor
    RECEIVED,  // Messages received from the sensor
    SYSTEM     // System messages (connection status, errors, etc.)
}
