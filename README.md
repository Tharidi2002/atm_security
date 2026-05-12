# ATM Security System

ATM Security Monitoring System with Microservices Architecture (Docker-less Development)

## Architecture Overview

This system provides centralized security monitoring for standalone ATMs with real-time alert processing, user management, and comprehensive reporting.

### Microservices

- **Eureka Server** (Port 8761) - Service Discovery
- **Auth Service** (Port 8081) - Authentication & Authorization
- **ATM Management Service** (Port 8082) - ATM CRUD Operations
- **Alert Processor Service** (Port 8083) - SMS Alert Processing with Kafka
- **Notification Service** (Port 8084) - WebSocket & Email Notifications
- **Reporting Service** (Port 8085) - Report Generation
- **API Gateway** (Port 8080) - Central Entry Point

### Technology Stack

- **Backend**: Spring Boot 3.2.0, Java 17, Spring Security
- **Database**: MySQL 8.0.45
- **Cache**: Redis
- **Message Queue**: Apache Kafka
- **Communication**: WebSocket (STOMP), REST APIs
- **Security**: JWT, 2FA (TOTP), BCrypt

## Prerequisites

Before starting development, ensure you have:

1. **Java 17** installed and JAVA_HOME configured
2. **MySQL 8.0.45** running with:
   - Database: `atm_security_system`
   - Username: `root`
   - Password: `MySQL@123`
3. **Redis** installed and running on default port 6379
4. **Apache Kafka** with Zookeeper running on localhost:9092
5. **Maven 3.8+** for building

## Database Setup

```sql
CREATE DATABASE atm_security_system;
CREATE DATABASE atm_security_audit;
```

## Development Workflow

### 1. Start Services in Order

1. **Eureka Server** - Service discovery backbone
2. **MySQL** - Database must be running
3. **Redis** - For session management
4. **Kafka** - For message streaming
5. **Auth Service** - Base authentication service
6. **Other Services** - ATM Management, Alert Processor, etc.
7. **API Gateway** - Last to start (depends on all services)

### 2. Service Registration

All services (except Eureka) will auto-register with Eureka server.

### 3. Testing

- Each service has health endpoints at `/actuator/health`
- API Gateway routes requests to appropriate services
- WebSocket connections available at `ws://localhost:8084/ws-alerts`

## Security Features

- **JWT Authentication**: 15-minute tokens with 7-day refresh
- **2FA Support**: TOTP using Google Authenticator
- **Role-based Access**: SUPER_ADMIN, BANK_ADMIN, SECURITY_OFFICER
- **Rate Limiting**: 100 requests/minute per IP
- **Account Lockout**: 5 failed attempts = 15 minutes
- **Data Encryption**: AES-256 for sensitive data

## API Endpoints

### Authentication (via Gateway → Auth Service)
- `POST /api/auth/login` - User login
- `POST /api/auth/refresh` - Token refresh
- `POST /api/auth/logout` - Logout
- `POST /api/auth/enable-2fa` - Enable 2FA

### ATM Management
- `GET /api/atm/all` - List all ATMs
- `POST /api/atm/create` - Create new ATM
- `PUT /api/atm/{id}` - Update ATM
- `DELETE /api/atm/{id}` - Delete ATM (soft delete)

### Alerts
- `GET /api/alerts` - List alerts with filters
- `PUT /api/alerts/{id}/acknowledge` - Acknowledge alert
- `POST /api/alerts/escalate/{id}` - Escalate critical alert

### WebSocket
- Connect to: `ws://localhost:8084/ws-alerts`
- Subscribe to: `/user/queue/notifications` (personal)
- Subscribe to: `/topic/alerts` (broadcast)

## Development Notes

- **No Docker**: All services run locally on separate ports
- **Hot Reload**: Use Spring Boot DevTools for faster development
- **Logging**: All services log to console with structured format
- **Configuration**: Each service has its own `application.properties`

## Building & Running

```bash
# Build all services
mvn clean compile

# Run specific service (from service directory)
mvn spring-boot:run

# Or run from root with specific module
mvn spring-boot:run -pl auth-service
```

## Troubleshooting

### Common Issues

1. **Port Conflicts**: Check if ports 8080-8085, 8761 are available
2. **Database Connection**: Verify MySQL is running and credentials are correct
3. **Eureka Registration**: Ensure Eureka server starts before other services
4. **Kafka Connection**: Verify Kafka and Zookeeper are running
5. **Redis Connection**: Check Redis is running on port 6379

### Health Checks

- Eureka: http://localhost:8761
- API Gateway: http://localhost:8080/actuator/health
- Auth Service: http://localhost:8081/actuator/health

## Next Steps

1. Start with Eureka Server
2. Develop Auth Service (base for all other services)
3. Add ATM Management Service
4. Implement Alert Processor with Kafka
5. Add Notification Service with WebSocket
6. Create Reporting Service
7. Finally, configure API Gateway

## License

© 2024 ATM Security System. All rights reserved.
