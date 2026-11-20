
import os
import traceback
import mysql.connector

print("CINEBOOK DATABASE TEST", flush=True)

try:
    print("Connecting to MySQL...", flush=True)

    conn = mysql.connector.connect(
        host="127.0.0.1",
        port=3306,
        database="cinebook_db",
        user="root",
        password=os.environ.get("DB_PASSWORD", ""),
        connection_timeout=5,
        use_pure=True
    )

    print("DATABASE CONNECTED SUCCESSFULLY!", flush=True)

    cursor = conn.cursor()
    cursor.execute("SELECT DATABASE()")

    print("Connected database:", cursor.fetchone()[0], flush=True)

    cursor.close()
    conn.close()

    print("CONNECTION CLOSED SUCCESSFULLY", flush=True)

except Exception as e:
    print("DATABASE CONNECTION ERROR:", repr(e), flush=True)
    traceback.print_exc()
