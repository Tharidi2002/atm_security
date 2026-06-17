# ATM Security System

Enterprise-grade centralized ATM security monitoring platform with microservices architecture.

## Architecture

| Service | Port | Description |
|---------|------|-------------|
| Eureka Server | 8761 | Service discovery |
| API Gateway | 8080 | JWT validation, rate limiting, routing |
| Auth Service | 8081 | RBAC, JWT, user/bank management |
| Alert Service | 8082 | SMS webhook, alert aggregation (with native Java AI analysis) |
| Station Service | 8083 | ATM station CRUD |
| Notification Service | 8084 | WebSocket, push notifications |
| React Frontend | 5173 | Web dashboard |

## Prerequisites

- Java 17+
- Maven 3.9+
- MySQL 8.x
- Node.js 18+

## Database Setup

Initialize the databases using the provided non-Python scripts:

**Option 1: Windows Batch File**
Double click `database/init_db.bat` or run:
```bash
database/init_db.bat
```

**Option 2: PowerShell**
Run the initializer script directly:
```powershell
powershell -File database/init_db.ps1
```

**Credentials:** `root` / `Ijse@123`

## Backend Startup (Order Matters)

```bash
# 1. Eureka Server
cd backend/eureka-server && mvn spring-boot:run

# 2. Auth Service
cd backend/auth-service && mvn spring-boot:run

# 3. API Gateway
cd backend/api-gateway && mvn spring-boot:run
```

Build all modules:

```bash
cd backend && mvn clean install -DskipTests
```

## Frontend

```bash
cd frontend
npm install
npm run dev
```

Open http://localhost:5173

## Default Admin Login

- **Username:** `admin`
- **Password:** `Admin@123`

## Auth API Endpoints

| Method | Endpoint | Auth |
|--------|----------|------|
| POST | `/api/auth/login` | Public |
| POST | `/api/auth/register` | Public |
| POST | `/api/auth/refresh` | Public |
| POST | `/api/auth/logout` | Public |
| GET | `/api/auth/profile` | JWT |
| GET | `/api/banks` | Public |

## Security Features (Step 1 & 2)

- BCrypt password hashing (strength 12)
- JWT access tokens (15 min) + refresh tokens (7 days)
- Role-based permissions (ADMIN, BANK_MANAGER, SECURITY_PERSONNEL)
- Bank-wise tenant isolation in JWT claims
- AES-256-GCM encryption utility for sensitive fields
- API Gateway JWT validation + rate limiting (120 req/min)
- Account lockout after 5 failed login attempts
- Audit logging for login/register events

## Testing with ESP32
To check the system with ESP32, upload the sketch located in `esp32/esp32_sec_system/esp32_sec_system.ino` to the ESP32. Set your Wi-Fi SSID and PASSWORD, and update `gatewayUrl` to point to your computer's IP address.
When sensors on the ESP32 trigger, they send alerts directly to the centralized gateway. The alerts are processed and displayed on the web dashboard in real-time.
