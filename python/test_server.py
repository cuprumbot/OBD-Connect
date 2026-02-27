import socket
import time
import random

def start_obd_server():
    server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    server.bind(('0.0.0.0', 35000))
    server.listen(1)
    
    print("OBD-II Mock Server (Dynamic Data Mode) Live.")
    print("Simulating linked RPM/Speed and steady Coolant Temp.")

    # Shared state for linked values
    current_rpm = 2000

    while True:
        conn, addr = server.accept()
        print(f"--- Connected to: {addr} ---")
        
        try:
            while True:
                raw = conn.recv(1024).decode('utf-8')
                if not raw: break
                
                cmd = raw.strip().replace(" ", "").upper()
                if not cmd: continue

                # 1. Update dynamic state
                # We fluctuate RPM slightly, and Speed will follow
                current_rpm += random.randint(-150, 150)
                current_rpm = max(2000, min(4000, current_rpm))
                
                # Logic: Speed (km/h) = RPM / 60 (Simplified "Gear" ratio)
                # Results in ~33km/h at 2000 RPM and ~66km/h at 4000 RPM
                current_speed = int(current_rpm / 60) 
                
                # Random coolant between 85-95
                current_temp = random.randint(85, 95)

                # 2. Prepare responses
                if cmd == "010C": # RPM
                    # Formula: (A*256 + B) / 4 = RPM  -> Hex = RPM * 4
                    hex_val = hex(current_rpm * 4)[2:].zfill(4).upper()
                    answer = f"41 0C {hex_val[:2]} {hex_val[2:]}"
                
                elif cmd == "010D": # Speed
                    # Formula: A = km/h
                    answer = f"41 0D {hex(current_speed)[2:].zfill(2).upper()}"
                
                elif cmd == "0105": # Coolant Temp
                    # Formula: A - 40 = Temp C -> Hex = Temp + 40
                    answer = f"41 05 {hex(current_temp + 40)[2:].zfill(2).upper()}"
                
                elif cmd == "ATZ":
                    answer = "ELM327 v2.1"
                elif cmd.startswith("AT"):
                    answer = "OK"
                else:
                    answer = "NO DATA"

                # 3. Hardware-accurate transmission
                full_payload = f"{answer}\r>"
                time.sleep(0.01) # Faster response for testing
                conn.sendall(full_payload.encode('utf-8'))
                
        except Exception as e:
            print(f"Session ended: {e}")
        finally:
            conn.close()
            print("Connection closed. Waiting for next...")

if __name__ == "__main__":
    start_obd_server()