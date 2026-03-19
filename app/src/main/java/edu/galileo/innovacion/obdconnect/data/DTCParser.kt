package edu.galileo.innovacion.obdconnect.data

data class ParsedDTC(
    val systemDesignator: String,
    val codeSelection: String,
    val subSystemIdentifier: String,
    val faultIdentifier: String,
    val description: String,
    val troubleCode: String
)

data class Pair(
    val character: String,
    val description: String
)

object DTCParser {
    fun parse(code: String): ParsedDTC {
        // here we receive a code that looks like this
        // 0111

        val systemDesignator = systemDesignator(code)
        val codeSelection = codeSelection(code)
        val subSystemIdentifier = subSystemIdentifier(code)
        val faultIdentifier = faultIdentifier(code)
        val description = description(code)
        val troubleCode = troubleCode(code)

        return ParsedDTC(
            systemDesignator = systemDesignator.description,
            codeSelection = codeSelection.description,
            subSystemIdentifier = subSystemIdentifier.description,
            faultIdentifier = faultIdentifier,
            description = description,
            troubleCode = troubleCode
        )
    }

    fun systemDesignator(code: String): Pair {
        val firstChar = code[0]
        val asNumber = firstChar.toString().toInt(radix = 16)
        val bits = (asNumber shr 2) and 0x03

        val systems = arrayOf("P", "C", "B", "U")
        val systemNames = arrayOf("Powertrain", "Chassis", "Body", "Network")

        val character = systems[bits]
        val description = systemNames[bits]

        return Pair(character, description)
    }

    fun codeSelection(code: String): Pair {
        // 1. Get the first character (e.g., '0' from "010C")
        val firstChar = code[0]
        val asNumber = firstChar.toString().toInt(radix = 16)

        // 2. Extract the letter (System Designator) to know the context
        // These are bits 3 and 2 of the first hex digit (bits 7 and 6 of the full 16-bit DTC)
        val systemBits = (asNumber shr 2) and 0x03
        val systemLetter = arrayOf("P", "C", "B", "U")[systemBits]

        // 3. Extract the Code Type digit (the "Scope")
        // These are bits 1 and 0 of the first hex digit (bits 5 and 4 of the full 16-bit DTC)
        val digit = asNumber and 0x03

        // 4. Determine the description based on the System + Digit combination
        val description = when (systemLetter) {
            "P" -> when (digit) {
                0 -> "Generic (Standard)"
                1 -> "Manufacturer Specific"
                2 -> "Generic (Standard)"
                3 -> "Split (P30x-P33x: Manuf / P34x-P39x: Generic)"
                else -> "Error: non existent code"
            }
            "U" -> when (digit) {
                0 -> "Generic (Standard)"
                1 -> "Manufacturer Specific"
                2 -> "Manufacturer Specific"
                3 -> "Generic (Standard)"
                else -> "Error: non existent code"
            }
            "C", "B" -> when (digit) {
                0 -> "Generic (Standard)"
                else -> "Manufacturer Specific"
            }
            else -> "Error: non existent code"
        }

        return Pair(digit.toString(), description)
    }

    fun subSystemIdentifier(code: String): Pair {
        // 1. Extract the first character to check the System and Type
        val firstHex = code[0].toString().toInt(radix = 16)
        val systemBits = (firstHex shr 2) and 0x03
        val typeBits = firstHex and 0x03

        val systemLetter = arrayOf("P", "C", "B", "U")[systemBits]

        // 2. Extract the second character (The Sub-System Digit)
        val subSystemDigit = code[1].toString().uppercase()

        // 3. Determine if it's a "Well Defined" Generic Powertrain range
        // P0, P2, and the P34-P39 range are standardized.
        val isGenericPowertrain = (systemLetter == "P") && (typeBits == 0 || typeBits == 2)

        // Handle the P3 edge case (P3400 - P3999 are generic)
        val isGenericP3 =
            (systemLetter == "P") && (typeBits == 3) && ( (subSystemDigit.toIntOrNull(16) ?: 0) >= 4 )

        if (!isGenericPowertrain && !isGenericP3) {
            return Pair(subSystemDigit, "")
        }

        // 4. Map the standardized Powertrain sub-systems
        val description = when (subSystemDigit) {
            "0", "1", "2" -> "Fuel and Air Metering"
            "3"           -> "Ignition System or Misfire"
            "4"           -> "Auxiliary Emissions Controls"
            "5"           -> "Vehicle Speed & Idle Control"
            "6"           -> "Computer Output Circuit"
            "7", "8", "9" -> "Transmission"
            "A", "B", "C" -> "Hybrid Propulsion"
            else          -> "" // Reserved or undefined
        }

        return Pair(subSystemDigit, description)
    }

    fun faultIdentifier(code: String): String {
        // Returns the last two hex characters (the 3rd and 4th digits of your input)
        // Example: Input "010C" -> Returns "0C"
        return code.substring(2).uppercase()
    }

    fun description(code: String): String {
        // To be implemented
        return ""
    }

    fun troubleCode(code: String): String {
        val system = systemDesignator(code).character
        val selection = codeSelection(code).character
        // substring(1) gets the remaining 3 characters of the hex
        return "$system$selection${code.substring(1)}".uppercase()
    }
}