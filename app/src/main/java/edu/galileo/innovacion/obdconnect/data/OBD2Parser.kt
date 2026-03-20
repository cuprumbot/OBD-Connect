package edu.galileo.innovacion.obdconnect.data

import android.util.Log

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

        val pidSuffix = pidCode.takeLast(2)

        if (cleaned.startsWith("41") && cleaned.length >= 4) {
            val responsePid = cleaned.substring(2, 4)
            if (responsePid == pidSuffix) {
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

    /**
     * Parse DTC response from mode 03 command.
     *
     * Formats:
     *   43 00              → no error codes
     *   43 xx xx           → one error code
     *   43 xx xx yy yy     → two error codes
     *   43 xx xx yy yy zz zz → three error codes (max supported)
     *
     * Each code is two bytes represented as a raw 4-char hex string e.g. "0101", "0113".
     * Returns empty list if no codes, null if the response is invalid.
     */
    fun parseDTCs(response: String): List<ParsedDTC>? {

        Log.d("parseDTCs", response)
        Log.d("parseDTCs", response.length.toString())

        var cleaned = response.trim().uppercase()
        cleaned = cleaned.replace(">", "")
        cleaned = cleaned.replace(" ", "")
        cleaned = cleaned.replace("\r", "")
        cleaned = cleaned.replace("\n", "")

        if ("SEARCHING" in cleaned || "NODATA" in cleaned || cleaned.isEmpty()) {
            return null
        }

        // Must start with 43 (mode 03 response)
        if (!cleaned.startsWith("43")) return null

        return try {
            // Strip the leading "43" mode byte
            val data = cleaned.substring(2)

            // "00" means no codes
            if (data == "00") return emptyList()

            // Each code is 4 hex chars (2 bytes); read up to 3 codes
            val codes = mutableListOf<String>()
            val parsedCodes = mutableListOf<ParsedDTC>()
            var i = 0
            while (i + 3 < data.length && codes.size < 3) {
                codes.add(data.substring(i, i + 4))
                parsedCodes.add(DTCParser.parse(data.substring(i, i + 4)))

                i += 4
            }

            parsedCodes
        } catch (e: Exception) {
            null
        }
    }
}