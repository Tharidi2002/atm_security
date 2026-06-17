import os
import subprocess
import sys

# Ensure pymysql is installed
try:
    import pymysql
except ImportError:
    print("Installing pymysql for database initialization...")
    subprocess.check_call([sys.executable, "-m", "pip", "install", "pymysql"])
    import pymysql

DB_HOST = "localhost"
DB_USER = "root"
DB_PASSWORD = "Ijse@123"

SQL_FILES = [
    "01_create_schemas.sql",
    "02_auth_schema.sql",
    "03_station_schema.sql",
    "04_alert_schema.sql",
    "05_notification_schema.sql"
]

def run_sql_file(cursor, file_path):
    print(f"Executing: {file_path}")
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Simple statement splitter by semicolon (ignoring comments/strings simplified)
    statements = []
    current_statement = []
    
    for line in content.split('\n'):
        # Skip comment lines
        if line.strip().startswith('--') or not line.strip():
            continue
        current_statement.append(line)
        if line.strip().endswith(';'):
            statements.append(' '.join(current_statement))
            current_statement = []
            
    for statement in statements:
        if statement.strip():
            cursor.execute(statement)

def main():
    script_dir = os.path.dirname(os.path.abspath(__file__))
    sql_dir = os.path.join(script_dir, "sql")
    
    try:
        # Connect to MySQL (no DB specified initially to run create database scripts)
        conn = pymysql.connect(
            host=DB_HOST,
            user=DB_USER,
            password=DB_PASSWORD,
            autocommit=True
        )
        cursor = conn.cursor()
        
        for sql_file in SQL_FILES:
            file_path = os.path.join(sql_dir, sql_file)
            run_sql_file(cursor, file_path)
            
        print("\nDatabase initialization completed successfully!")
        cursor.close()
        conn.close()
    except Exception as e:
        print(f"\nError initializing database: {e}")
        sys.exit(1)

if __name__ == "__main__":
    main()
