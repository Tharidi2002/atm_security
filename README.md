# ATM Security System

Enterprise-grade centralized ATM security monitoring platform with microservices architecture.

## Architecture

| Service | Port | Description |
|---------|------|-------------|
| Eureka Server | 8761 | Service discovery |
| API Gateway | 8080 | JWT validation, rate limiting, routing |
| Auth Service | 8081 | RBAC, JWT, user/bank management |
| Alert Service | 8082 | SMS webhook, alert aggregation *(Step 3)* |
| Station Service | 8083 | ATM station CRUD *(Step 3)* |
| Notification Service | 8084 | WebSocket, push notifications *(Step 4)* |
| AI Service (Python) | 8000 | Anomaly detection *(Step 3)* |
| React Frontend | 5173 | Web dashboard |

## Prerequisites

- Java 17+
- Maven 3.9+
- MySQL 8.x
- Node.js 18+

## Database Setup

```bash
mysql -u root -p < database/sql/01_create_schemas.sql
mysql -u root -p < database/sql/02_auth_schema.sql
mysql -u root -p < database/sql/03_station_schema.sql
mysql -u root -p < database/sql/04_alert_schema.sql
mysql -u root -p < database/sql/05_notification_schema.sql
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

## Next Steps

Reply with: **"Now generate STEP 3 (Alert Aggregator + Station Service) and STEP 4 (React Station Manager + Python AI)"**
