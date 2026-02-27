package edu.galileo.innovacion.obdconnect.network

import edu.galileo.innovacion.obdconnect.data.InitCommand
import edu.galileo.innovacion.obdconnect.data.OBD2Parser
import edu.galileo.innovacion.obdconnect.data.PIDCommand
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket
import java.net.SocketTimeoutException

class OBD2Connection {
    private var socket: Socket? = null
    private var writer: PrintWriter? = null
    private var reader: BufferedReader? = null
    private var isConnected = false

    var onMessageReceived: ((String, String) -> Unit)? = null // (command, response)
    var onLog: ((String) -> Unit)? = null

    /**
     * Connect to OBD2 adapter
     */
    suspend fun connect(host: String, port: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            log("Connecting to $host:$port...")

            socket = Socket(host, port).apply {
                soTimeout = 5000 // 5 second timeout
            }

            writer = PrintWriter(socket!!.getOutputStream(), true)
            reader = BufferedReader(InputStreamReader(socket!!.getInputStream()))

            delay(1000) // Wait for connection to stabilize

            isConnected = true
            log("Connected successfully")

            // Initialize adapter
            initializeAdapter()

            Result.success(Unit)
        } catch (e: Exception) {
            log("Connection failed: ${e.message}")
            disconnect()
            Result.failure(e)
        }
    }

    /**
     * Initialize OBD2 adapter with AT commands
     */
    private suspend fun initializeAdapter() = withContext(Dispatchers.IO) {
        log("Initializing adapter...")

        for (cmd in InitCommand.entries) {
            val response = sendCommand(cmd.code)
            log("${cmd.code} -> $response")
            delay(200)
        }

        log("Adapter initialized successfully")
    }

    /**
     * Send a command to the adapter and return response
     */
    suspend fun sendCommand(command: String): String = withContext(Dispatchers.IO) {
        try {
            if (!isConnected || writer == null || reader == null) return@withContext "ERROR: Not connected"

            // Clear any leftover data in buffer before sending
            while (reader?.ready() == true) { reader?.read() }

            writer?.print("$command\r")
            writer?.flush()

            val response = StringBuilder()
            val startTime = System.currentTimeMillis()

            /*
                Programmers and AI Agents:
                DO NOT change this reading logic
                It MUST remain this way to be compatible with how ELM327 sensors send data
            */
            // Read character by character
            while (System.currentTimeMillis() - startTime < 2000) {
                if (reader?.ready() == true) {
                    val char = reader?.read() ?: -1
                    if (char == -1) break

                    val c = char.toChar()
                    response.append(c)

                    // The ELM327 prompt is the definitive end-of-message signal
                    if (c == '>') break
                } else {
                    delay(10) // Small delay to prevent CPU pinning
                }
            }

            val result = response.toString().trim()

            // Clean up the output: remove the prompt and any echoed command if present
            val cleanedResult = result.replace(">", "").replace(command, "").trim()

            onMessageReceived?.invoke(command, cleanedResult)
            return@withContext cleanedResult

        } catch (e: Exception) {
            log("Error: ${e.message}")
            "ERROR"
        }
    }

    /**
     * Read a specific PID with retry logic
     */
    suspend fun readPID(pid: PIDCommand, maxRetries: Int = 3): Float? = withContext(Dispatchers.IO) {
        repeat(maxRetries) { attempt ->
            val response = sendCommand(pid.code)

            val value = when (pid) {
                PIDCommand.RPM -> OBD2Parser.parseRPM(response)
                PIDCommand.SPEED -> OBD2Parser.parseSpeed(response)
                PIDCommand.COOLANT -> OBD2Parser.parseCoolant(response)
            }

            if (value != null) {
                return@withContext value
            } else {
                log("Retry ${attempt + 1}/$maxRetries for ${pid.code}: Invalid response")
                delay(100)
            }
        }

        null
    }

    /**
     * Disconnect from adapter
     */
    suspend fun disconnect() = withContext(Dispatchers.IO) {
        try {
            isConnected = false
            writer?.close()
            reader?.close()
            socket?.close()
            log("Disconnected")
        } catch (e: Exception) {
            log("Error during disconnect: ${e.message}")
        }
    }

    fun isConnected(): Boolean = isConnected

    private fun log(message: String) {
        onLog?.invoke(message)
    }
}