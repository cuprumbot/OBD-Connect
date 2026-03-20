package edu.galileo.innovacion.obdconnect.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.galileo.innovacion.obdconnect.data.MessageType
import edu.galileo.innovacion.obdconnect.data.PIDCommand
import edu.galileo.innovacion.obdconnect.data.ParsedDTC
import edu.galileo.innovacion.obdconnect.data.TerminalMessage
import edu.galileo.innovacion.obdconnect.network.OBD2Connection
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ConnectionViewModel : ViewModel() {
    // private val _ipAddress = MutableStateFlow("192.168.0.10")
    private val _ipAddress = MutableStateFlow("10.0.2.2")
    val ipAddress: StateFlow<String> = _ipAddress.asStateFlow()

    private val _port = MutableStateFlow("35000")
    val port: StateFlow<String> = _port.asStateFlow()

    private val _delay = MutableStateFlow("500")
    val delay: StateFlow<String> = _delay.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _isConnecting = MutableStateFlow(false)
    val isConnecting: StateFlow<Boolean> = _isConnecting.asStateFlow()

    private val _connectionError = MutableStateFlow<String?>(null)
    val connectionError: StateFlow<String?> = _connectionError.asStateFlow()

    private val _rpmValue = MutableStateFlow(0f)
    val rpmValue: StateFlow<Float> = _rpmValue.asStateFlow()

    private val _speedValue = MutableStateFlow(0f)
    val speedValue: StateFlow<Float> = _speedValue.asStateFlow()

    private val _coolantValue = MutableStateFlow(0f)
    val coolantValue: StateFlow<Float> = _coolantValue.asStateFlow()

    private val _dtcCodes = MutableStateFlow<List<ParsedDTC>>(emptyList())
    val dtcCodes: StateFlow<List<ParsedDTC>> = _dtcCodes.asStateFlow()

    private val _isReadingDTCs = MutableStateFlow(false)
    val isReadingDTCs: StateFlow<Boolean> = _isReadingDTCs.asStateFlow()

    private val _dtcError = MutableStateFlow<String?>(null)
    val dtcError: StateFlow<String?> = _dtcError.asStateFlow()

    private val obd2Connection = OBD2Connection()
    private var readingJob: Job? = null
    private var shouldReadData = false

    var onTerminalLog: ((TerminalMessage) -> Unit)? = null

    init {
        obd2Connection.onLog = { message ->
            onTerminalLog?.invoke(TerminalMessage(text = message, type = MessageType.SYSTEM))
        }
        obd2Connection.onMessageReceived = { command, response ->
            onTerminalLog?.invoke(
                TerminalMessage(text = "$command -> $response", type = MessageType.RECEIVED)
            )
        }
    }

    fun updateIpAddress(newIp: String) { _ipAddress.value = newIp }
    fun updatePort(newPort: String) { _port.value = newPort }
    fun updateDelay(newDelay: String) { _delay.value = newDelay }

    fun connect() {
        viewModelScope.launch {
            _isConnecting.value = true
            _connectionError.value = null
            val result = obd2Connection.connect(
                host = _ipAddress.value,
                port = _port.value.toIntOrNull() ?: 35000
            )
            _isConnecting.value = false
            if (result.isSuccess) {
                _isConnected.value = true
                logTerminal("Connection established successfully", MessageType.SYSTEM)
            } else {
                _isConnected.value = false
                _connectionError.value = result.exceptionOrNull()?.message ?: "Connection failed"
                logTerminal("Connection failed: ${_connectionError.value}", MessageType.SYSTEM)
            }
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            stopReadingData()
            obd2Connection.disconnect()
            _isConnected.value = false
            _rpmValue.value = 0f
            _speedValue.value = 0f
            _coolantValue.value = 0f
            _dtcCodes.value = emptyList()
            logTerminal("Disconnected from OBD2 adapter", MessageType.SYSTEM)
        }
    }

    fun startReadingData() {
        if (readingJob?.isActive == true) return
        shouldReadData = true
        readingJob = viewModelScope.launch {
            logTerminal("Starting continuous data reading...", MessageType.SYSTEM)
            while (shouldReadData && _isConnected.value) {
                try {
                    val rpm = obd2Connection.readPID(PIDCommand.RPM)
                    if (rpm != null) _rpmValue.value = rpm

                    val speed = obd2Connection.readPID(PIDCommand.SPEED)
                    if (speed != null) _speedValue.value = speed

                    val coolant = obd2Connection.readPID(PIDCommand.COOLANT)
                    if (coolant != null) _coolantValue.value = coolant

                    delay(_delay.value.toLongOrNull() ?: 500L)
                } catch (e: Exception) {
                    logTerminal("Error reading data: ${e.message}", MessageType.SYSTEM)
                    delay(1000)
                }
            }
            logTerminal("Stopped data reading", MessageType.SYSTEM)
        }
    }

    fun stopReadingData() {
        shouldReadData = false
        readingJob?.cancel()
        readingJob = null
    }

    fun readDTCs() {
        viewModelScope.launch {
            _isReadingDTCs.value = true
            _dtcError.value = null
            logTerminal("Reading Diagnostic Trouble Codes...", MessageType.SYSTEM)

            val codes = obd2Connection.readDTCs()
            when {
                codes == null -> {
                    _dtcError.value = "Failed to read error codes"
                    logTerminal("Failed to read DTC codes", MessageType.SYSTEM)
                }
                codes.isEmpty() -> {
                    _dtcCodes.value = emptyList()
                    logTerminal("No trouble codes found", MessageType.SYSTEM)
                }
                else -> {
                    _dtcCodes.value = codes
                    logTerminal("Found ${codes.size} trouble code(s): ${codes.joinToString()}", MessageType.SYSTEM)
                }
            }

            _isReadingDTCs.value = false
        }
    }

    suspend fun sendCustomCommand(command: String): String {
        logTerminal(command, MessageType.SENT)
        return obd2Connection.sendCommand(command)
    }

    private fun logTerminal(message: String, type: MessageType) {
        onTerminalLog?.invoke(TerminalMessage(text = message, type = type))
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch { disconnect() }
    }
}