
import os
import mysql.connector
import traceback

print("PYTHON STARTED", flush=True)
print("PASSWORD SET:", bool(os.getenv("DB_PASSWORD")), flush=True)

try:
    print("CONNECTING TO MYSQL...", flush=True)

    conn = mysql.connector.connect(
        host="127.0.0.1",
        port=3306,
        user="root",
        password=os.getenv("DB_PASSWORD", ""),
        database="cinebook_db",
        connection_timeout=5,
        use_pure=True
    )

    print("DATABASE CONNECTED SUCCESSFULLY!", flush=True)
    conn.close()

except BaseException:
    traceback.print_exc()

print("TEST FINISHED", flush=True)
