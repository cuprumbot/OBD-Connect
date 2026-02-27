import socket
import time

# --- Configuration ---
HOST = "192.168.0.10"  # OBD-II adapter IP
PORT = 35000            # OBD-II adapter port
DELAY = 1               # seconds between full cycle

# Commands we want to read (PID requests)
COMMANDS = {
    "RPM": "010C",        # Engine RPM
    "SPEED": "010D",      # Vehicle speed
    "COOLANT": "0105",    # Engine coolant temperature
}

# --- Helper functions ---
def send_command(sock, cmd):
    """Send a command to the OBD-II adapter and return the response."""
    sock.sendall((cmd + "\r").encode())
    time.sleep(0.2)  # allow adapter to respond
    data = sock.recv(1024)
    return data.decode().strip()

def safe_parse_response(response, pid):
    """Parse ELM327 response safely, ignoring 'SEARCHING...' lines."""
    response = response.strip().upper()
    response = response.replace(">", "")
    response = response.replace(" ", "")

    if "SEARCHING" in response or "NO DATA" in response:
        return None

    if response.startswith("41") and response[2:4] == pid:
        if len(response) == 6:
            return [ response[0:2], response[2:4], response[4:6] ]
        elif len(response) == 8:
            return [ response[0:2], response[2:4], response[4:6], response[6:8] ]
    else:
        return None

def parse_rpm(response):
    parts = safe_parse_response(response, "010C")
    if not parts:
        return None
    try:
        A = int(parts[2], 16)
        B = int(parts[3], 16)
        return ((A * 256) + B) / 4
    except:
        return None

def parse_speed(response):
    parts = safe_parse_response(response, "010D")
    if not parts:
        return None
    try:
        A = int(parts[2], 16)
        return A
    except:
        return None

def parse_coolant(response):
    parts = safe_parse_response(response, "0105")
    if not parts:
        return None
    try:
        A = int(parts[2], 16)
        return A - 40
    except:
        return None

# Map PIDs to parser functions
PARSERS = {
    "RPM": parse_rpm,
    "SPEED": parse_speed,
    "COOLANT": parse_coolant,
}

# --- Main script ---
with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
    print("Connecting to OBD-II adapter...")
    s.connect((HOST, PORT))
    time.sleep(1)

    # Initialization sequence
    for init_cmd in ["ATZ", "ATE0", "ATL0", "ATS0", "ATH0", "ATSP0"]:
        response = send_command(s, init_cmd)
        print(init_cmd, "->", response)

    print("Adapter initialized. Reading live data... (press Ctrl+C to stop)")

    try:
        while True:
            results = {}
            # Query each PID sequentially
            for name, cmd in COMMANDS.items():
                while True:
                    response = send_command(s, cmd)
                    value = PARSERS[name](response)
                    if value is not None:
                        results[name] = value
                        break
                    else:
                        # skip SEARCHING... or invalid lines
                        print("skipping...", cmd, "->", response)
                        time.sleep(0.1)
            # Print all results together
            print(" | ".join(f"{k}: {v}" for k, v in results.items()))
            time.sleep(DELAY)
    except KeyboardInterrupt:
        print("Stopped.")
