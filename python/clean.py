import socket
import time

# --- Configuration ---
HOST = "192.168.0.10"  # OBD-II adapter IP
PORT = 35000            # OBD-II adapter port
RETRY_DELAY = 0.5       # seconds between retries

# --- Helper functions ---
def send_command(sock, cmd):
    """Send a command to the OBD-II adapter and return the raw response."""
    sock.sendall((cmd + "\r").encode())
    time.sleep(0.2)  # small delay to let adapter respond
    data = sock.recv(1024).decode()
    return data

def clean_response(response):
    """
    Remove SEARCHING..., > prompts, empty lines, and extra whitespace.
    Returns a single-line cleaned string, or None if no valid data.
    """
    lines = response.splitlines()
    cleaned_lines = []
    for line in lines:
        line = line.strip()
        if not line or line.startswith("SEARCHING"):
            continue
        # Remove trailing '>' prompt
        if line.endswith(">"):
            line = line[:-1].strip()
        cleaned_lines.append(line)
    if not cleaned_lines:
        return None
    return " ".join(cleaned_lines)

def send_until_valid(sock, cmd):
    """Keep sending the command until a valid response is received."""
    while True:
        raw = send_command(sock, cmd)
        #sprint("raw...", cmd, "->", raw)
        cleaned = clean_response(raw)
        if cleaned:
            return cleaned
        time.sleep(RETRY_DELAY)

# --- Main script ---
with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
    print("Connecting to OBD-II adapter...")
    s.connect((HOST, PORT))
    time.sleep(1)

    # Initialization sequence
    for init_cmd in ["ATZ", "ATE0", "ATL0", "ATS0", "ATH0", "ATSP0"]:
        response = send_until_valid(s, init_cmd)
        # optional: print(f"{init_cmd} -> {response}")

    print("Adapter initialized.")

    # Clear trouble codes
    response = send_until_valid(s, "04")
    print("Clear DTC response:", response)

    print("All trouble codes cleared (if any).")
