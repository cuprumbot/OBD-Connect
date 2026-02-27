package edu.galileo.innovacion.obdconnect.data

object OBD2Parser {

    /**
     * Parse ELM327 response safely, ignoring 'SEARCHING...' lines
     * Returns list of hex byte strings or null if invalid
     */
    private fun safeParseResponse(response: String, pidCode: String): List<String>? {
        var cleaned = response.trim().uppercase()
        cleaned = cleaned.replace(">", "")
        cleaned = cleaned.replace(" ", "")
        cleaned = cleaned.replace("\r", "")
        cleaned = cleaned.replace("\n", "")

        if ("SEARCHING" in cleaned || "NODATA" in cleaned || cleaned.isEmpty()) {
            return null
        }

        // Response format: 41 [PID] [DATA]
        // We're looking for "41" + last 2 chars of pidCode (e.g., "0C" from "010C")
        val pidSuffix = pidCode.takeLast(2)

        if (cleaned.startsWith("41") && cleaned.length >= 4) {
            val responsePid = cleaned.substring(2, 4)
            if (responsePid == pidSuffix) {
                // Parse bytes: mode (41) + PID + data bytes
                val bytes = mutableListOf<String>()
                var i = 0
                while (i < cleaned.length - 1) {
                    bytes.add(cleaned.substring(i, i + 2))
                    i += 2
                }
                return bytes
            }
        }

        return null
    }

    /**
     * Parse RPM from response
     * Formula: ((A * 256) + B) / 4
     */
    fun parseRPM(response: String): Float? {
        val parts = safeParseResponse(response, "010C") ?: return null
        return try {
            if (parts.size >= 4) {
                val a = parts[2].toInt(16)
                val b = parts[3].toInt(16)
                ((a * 256) + b) / 4f
            } else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Parse vehicle speed from response
     * Formula: A (km/h)
     */
    fun parseSpeed(response: String): Float? {
        val parts = safeParseResponse(response, "010D") ?: return null
        return try {
            if (parts.size >= 3) {
                parts[2].toInt(16).toFloat()
            } else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Parse coolant temperature from response
     * Formula: A - 40 (°C)
     */
    fun parseCoolant(response: String): Float? {
        val parts = safeParseResponse(response, "0105") ?: return null
        return try {
            if (parts.size >= 3) {
                (parts[2].toInt(16) - 40).toFloat()
            } else null
        } catch (e: Exception) {
            null
        }
    }
}