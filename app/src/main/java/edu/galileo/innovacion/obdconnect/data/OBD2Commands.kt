package edu.galileo.innovacion.obdconnect.data

enum class InitCommand(val code: String) {
    RESET("ATZ"),           // Reset adapter
    ECHO_OFF("ATE0"),       // Echo off
    LINEFEEDS_OFF("ATL0"),  // Linefeeds off
    SPACES_OFF("ATS0"),     // Spaces off
    HEADERS_OFF("ATH0"),    // Headers off
    AUTO_PROTOCOL("ATSP0")  // Automatic protocol detection
}

enum class PIDCommand(val code: String, val description: String) {
    RPM("010C", "Engine RPM"),
    SPEED("010D", "Vehicle Speed"),
    COOLANT("0105", "Engine Coolant Temperature")
}