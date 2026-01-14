package edu.galileo.innovacion.obdconnect.ui.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ConnectionViewModel : ViewModel() {
    private val _ipAddress = MutableStateFlow("192.168.0.10")
    val ipAddress: StateFlow<String> = _ipAddress.asStateFlow()

    private val _port = MutableStateFlow("35000")
    val port: StateFlow<String> = _port.asStateFlow()

    private val _delay = MutableStateFlow("500")
    val delay: StateFlow<String> = _delay.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    fun updateIpAddress(newIp: String) {
        _ipAddress.value = newIp
    }

    fun updatePort(newPort: String) {
        _port.value = newPort
    }

    fun updateDelay(newDelay: String) {
        _delay.value = newDelay
    }

    fun connect() {
        // TODO: Implement actual connection logic
        _isConnected.value = true
    }

    fun disconnect() {
        // TODO: Implement actual disconnection logic
        _isConnected.value = false
    }

    fun mockToggleConnection() {
        _isConnected.value = !_isConnected.value
    }
}