# TaskFlow

[![Java](https://img.shields.io/badge/Java-21-orange?logo=java)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.14-green?logo=spring)](https://spring.io/projects/spring-boot)
[![Build Status](https://img.shields.io/github/actions/workflow/status/Madapada-TarunKumar-Git/taskflow/deploy.yml?branch=main)](https://github.com/Madapada-TarunKumar-Git/taskflow/actions)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

A robust, scalable **task management and processing system** built with **Spring Boot 3.5** and **Java 21**. TaskFlow provides enterprise-grade features for managing asynchronous tasks, including user authentication with JWT, task scheduling, CSV file processing, and comprehensive audit logging.

## 🎯 Features

- **Task Management**
  - Create, read, update, and delete tasks with multiple priority levels
  - Support for diverse task types: Customer Import, Bill Processing, XML Import, Report Generation
  - Real-time task status tracking (Created, In Progress, Completed, Failed, Retry)
  - Automatic retry mechanism with configurable limits

- **File Processing**
  - CSV file upload and validation
  - Customer data import with BOM (Byte Order Mark) handling
  - Asynchronous batch processing with progress tracking
  - Success and failure record metrics

- **User Authentication & Security**
  - JWT (JSON Web Token) based authentication
  - Role-based access control (User, Admin roles)
  - Password encryption using BCrypt
  - Secure token expiry (15 minutes default)
  - Request validation and exception handling

- **Task Scheduling**
  - Configurable task scheduler with profile-specific delays
  - Automatic background job processing
  - Docker-optimized scheduler configuration

- **Audit & Monitoring**
  - Complete task audit trail with timestamps
  - Track all task actions and state changes
  - Request/response logging
  - Spring Actuator endpoints for health checks

- **API Documentation**
  - Interactive Swagger UI powered by SpringDoc OpenAPI
  - Comprehensive endpoint documentation
  - Real-time API testing capabilities

## 📋 Tech Stack

| Component | Technology | Version |
|-----------|------------|---------|
| **Language** | Java | 21 |
| **Framework** | Spring Boot | 3.5.14 |
| **Database** | PostgreSQL | Latest |
| **Security** | Spring Security + JWT | JJWT 0.13.0 |
| **API Docs** | SpringDoc OpenAPI | 2.8.16 |
| **Data Processing** | Apache Commons CSV | 1.10.0 |
| **ORM** | Spring Data JPA | Included |
| **Build Tool** | Maven | 3.9 |
| **Containerization** | Docker | Latest |

## 🚀 Installation & Setup

### Prerequisites

- **Java 21** or higher installed
- **Maven 3.9+** or use the included Maven wrapper
- **PostgreSQL 16+** running (or use Docker)
- **Git** for cloning the repository

### Step 1: Clone the Repository

```bash
git clone https://github.com/Madapada-TarunKumar-Git/taskflow.git
cd taskflow
```

### Step 2: Configure Environment

Create a `.env` file in the project root:

```bash
# Database Configuration
DB_URL=jdbc:postgresql://localhost:5432/taskflow_db
DB_USERNAME=postgres
DB_PASSWORD=your_secure_password

# JWT Secret (minimum 256 bits for HS256)
SECRET=your_super_secret_key_with_minimum_32_characters_length

# Application Profile (dev, docker, prod)
PROFILE=dev
```

### Step 3: Database Setup

Using Docker (Recommended):

```bash
# Start PostgreSQL container
docker run --name taskflow-postgres \
  -e POSTGRES_DB=taskflow_db \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=your_secure_password \
  -p 5432:5432 \
  -d postgres:16
```

Or run the initialization script manually:

```bash
psql -U postgres -d taskflow_db -f init-db.sql
```

### Step 4: Build & Run

**Using Maven:**

```bash
# Build the project
./mvnw clean package

# Run the application
./mvnw spring-boot:run
```

**Using Docker Compose (All-in-One):**

```bash
docker-compose up -d
```

This will:
- Build the TaskFlow application image
- Create and start the container
- Mount the `uploads/` directory for file storage
- Expose the API on `http://localhost:7600`

### Step 5: Verify Installation

```bash
# Check API health
curl -X GET http://localhost:7600/actuator/health

# Access Swagger UI
# Navigate to: http://localhost:7600/swagger-ui.html
```

## 📖 Usage

### 1. Authentication

**Register a New User:**

```bash
curl -X POST http://localhost:7600/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@example.com",
    "password": "SecurePass123!"
  }'
```

**Response:**
```json
{
  "message": "User registered successfully",
  "userId": "123e4567-e89b-12d3-a456-426614174000"
}
```

**Login:**

```bash
curl -X POST http://localhost:7600/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "SecurePass123!"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 900,
  "tokenType": "Bearer"
}
```

### 2. Task Management

**Create a Task:**

```bash
curl -X POST http://localhost:7600/api/v1/tasks \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "taskName": "Customer Import Q3",
    "description": "Import customer data for Q3 reporting",
    "taskType": "CUSTOMER_IMPORT",
    "priority": "HIGH"
  }'
```

**Response:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "taskName": "Customer Import Q3",
  "status": "CREATED",
  "priority": "HIGH",
  "createdAt": "2024-07-01T10:30:00Z",
  "createdBy": "john_doe"
}
```

**Fetch All Tasks (Paginated):**

```bash
curl -X GET "http://localhost:7600/api/v1/tasks?page=0&size=10&sort=createdAt,desc" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Get Task Details:**

```bash
curl -X GET http://localhost:7600/api/v1/tasks/{taskId} \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Update Task Status:**

```bash
curl -X PATCH http://localhost:7600/api/v1/tasks/{taskId}/status \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "IN_PROGRESS"
  }'
```

### 3. File Upload & Processing

**Upload CSV File for Processing:**

```bash
curl -X POST http://localhost:7600/api/v1/tasks/{taskId}/upload \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@customers.csv"
```

**Expected CSV Format:**
```csv
customerName,email,phone,country
John Smith,john@example.com,+1-555-0101,USA
Jane Doe,jane@example.com,+1-555-0102,USA
```

**Response:**
```json
{
  "taskId": "550e8400-e29b-41d4-a716-446655440000",
  "fileName": "customers.csv",
  "totalRecords": 1000,
  "successRecords": 985,
  "failedRecords": 15,
  "status": "IN_PROGRESS"
}
```

### 4. Task Audit Trail

**View Task Audit History:**

```bash
curl -X GET http://localhost:7600/api/v1/tasks/{taskId}/audit \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:**
```json
[
  {
    "id": "audit-123",
    "taskId": "550e8400-e29b-41d4-a716-446655440000",
    "action": "TASK_CREATED",
    "previousStatus": null,
    "newStatus": "CREATED",
    "timestamp": "2024-07-01T10:30:00Z"
  },
  {
    "id": "audit-124",
    "taskId": "550e8400-e29b-41d4-a716-446655440000",
    "action": "TASK_STARTED",
    "previousStatus": "CREATED",
    "newStatus": "IN_PROGRESS",
    "timestamp": "2024-07-01T10:35:00Z"
  }
]
```

## ⚙️ Configuration

### Application Properties

Create `application-dev.yml` or `application-docker.yml` in `src/main/resources/`:

```yaml
spring:
  application:
    name: taskflow
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}

server:
  port: 7600
  servlet:
    context-path: /
  compression:
    enabled: true

jwt:
  secret: ${SECRET}
  expiration: 900000  # 15 minutes in milliseconds

management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus
  endpoint:
    health:
      show-details: always

springdoc:
  swagger-ui:
    enabled: true
    path: /swagger-ui.html

app:
  scheduler:
    delay: 5000  # Task processing delay in milliseconds
    fixed-rate: 30000  # Fixed rate between task processing cycles
```

### Environment Variables

| Variable | Purpose | Default | Example |
|----------|---------|---------|---------|
| `SPRING_PROFILES_ACTIVE` | Active profile (dev, docker, prod) | dev | docker |
| `SPRING_DATASOURCE_URL` | Database connection URL | - | jdbc:postgresql://localhost:5432/taskflow_db |
| `SPRING_DATASOURCE_USERNAME` | DB username | - | postgres |
| `SPRING_DATASOURCE_PASSWORD` | DB password | - | secure_pass |
| `SECRET` | JWT secret key | - | super_secret_key_32_chars_min |

## 📁 Architecture Overview

```
taskflow/
├── src/main/java/com/example/taskflow/
│   ├── presentation/          # REST Controllers & DTOs
│   │   ├── controller/        # API endpoints
│   │   ├── request/           # Request DTOs
│   │   └── response/          # Response DTOs
│   ├── application/           # Business Logic (Use Cases)
│   │   ├── service/           # Application services
│   │   ├── mapper/            # DTO ↔ Entity mappers
│   │   ├── validation/        # Input validation
│   │   └── dto/               # Data Transfer Objects
│   ├── domain/                # Domain Layer (Core Business)
│   │   ├── model/             # Domain entities
│   │   ├── enums/             # Enumerations
│   │   ├── exception/         # Domain exceptions
│   │   └── repository/        # Repository interfaces
│   ├── infrastructure/        # Technical Implementation
│   │   ├── security/          # Auth & JWT configuration
│   │   ├── persistence/       # JPA repositories & DB config
│   │   └── config/            # Spring configurations
│   └── shared/                # Shared utilities
│       ├── exception/         # Global exceptions
│       ├── response/          # Common response types
│       └── util/              # Helper utilities
├── src/test/java/             # Unit & Integration Tests
├── src/main/resources/
│   ├── application.yml        # Default configuration
│   ├── application-dev.yml    # Development profile
│   └── application-docker.yml # Docker profile
├── pom.xml                    # Maven configuration
├── Dockerfile                 # Docker image definition
└── docker-compose.yml         # Multi-container setup
```

### Key Packages

| Package | Responsibility |
|---------|-----------------|
| **presentation** | REST API endpoints, request/response handling |
| **application** | Business logic, use cases, validation, DTOs |
| **domain** | Core business entities, rules, exceptions |
| **infrastructure** | Security, database, external integrations |
| **shared** | Global exceptions, utilities, responses |

## 🧪 Testing

### Running Tests

```bash
# Run all tests
./mvnw test

# Run tests with coverage
./mvnw test jacoco:report

# Run specific test class
./mvnw test -Dtest=TaskCommandServiceTest

# Run tests with detailed output
./mvnw test -X
```

### Test Coverage

The project includes:
- **Unit Tests**: Service layer logic, validators, mappers
- **Integration Tests**: Task scheduler, repository operations, API endpoints
- **Test Dependencies**:
  - JUnit 5
  - Mockito for mocking
  - Spring Boot Test
  - H2 Database (in-memory for tests)
  - Awaitility (for async testing)

### Example Test

```java
@SpringBootTest
@ActiveProfiles("test")
public class TaskCommandServiceTest {
    
    @Autowired
    private TaskCommandService taskCommandService;
    
    @Test
    public void testCreateTask() {
        // Arrange
        CreateTaskRequest request = new CreateTaskRequest(
            "Test Task", "Description", 
            TaskType.CUSTOMER_IMPORT, TaskPriority.HIGH
        );
        
        // Act
        TaskResponseDto result = taskCommandService.createTask(request);
        
        // Assert
        assertNotNull(result.getId());
        assertEquals("Test Task", result.getTaskName());
        assertEquals(TaskStatus.CREATED, result.getStatus());
    }
}
```

## 🐳 Deployment

### Docker Deployment

**Build Docker Image:**

```bash
docker build -t taskflow:latest .
```

**Run as Container:**

```bash
docker run -d \
  --name taskflow \
  -p 7600:7600 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/taskflow_db \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=password \
  -e SECRET=your_secret_key \
  -e SPRING_PROFILES_ACTIVE=docker \
  -v taskflow-uploads:/app/uploads \
  taskflow:latest
```

### Docker Compose (Production-Ready)

```bash
# Start services
docker-compose up -d

# View logs
docker-compose logs -f taskflow-app

# Stop services
docker-compose down

# Clean up volumes
docker-compose down -v
```

### Kubernetes Deployment (Future)

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: taskflow
spec:
  replicas: 3
  selector:
    matchLabels:
      app: taskflow
  template:
    metadata:
      labels:
        app: taskflow
    spec:
      containers:
      - name: taskflow
        image: taskflow:latest
        ports:
        - containerPort: 7600
        env:
        - name: SPRING_DATASOURCE_URL
          valueFrom:
            configMapKeyRef:
              name: taskflow-config
              key: db-url
```

### Cloud Deployment (AWS EC2)

**Via GitHub Actions:**

The project includes a GitHub Actions workflow (`.github/workflows/deploy.yml`) that:
1. Builds the application on push to `main`
2. Runs all tests
3. Deploys to AWS EC2 instance
4. Manages environment variables securely

**Manual EC2 Deployment:**

```bash
# SSH into EC2 instance
ssh -i "your-key.pem" ec2-user@your-ec2-ip

# Pull latest code
cd /app/taskflow
git pull origin main

# Build and run
docker-compose build --no-cache
docker-compose up -d
```

## 🤝 Contributing Guidelines

We welcome contributions! Please follow these steps:

### 1. Create a Feature Branch

```bash
git checkout -b feature/your-feature-name
```

Use descriptive branch names:
- `feature/add-email-notifications`
- `bugfix/fix-task-status-update`
- `docs/update-api-docs`

### 2. Make Your Changes

- Follow existing code style and patterns
- Add tests for new features
- Update documentation as needed
- Ensure all tests pass: `./mvnw test`

### 3. Commit Your Changes

```bash
git add .
git commit -m "feat: add email notifications for task completion"
```

Follow commit conventions:
- `feat:` New feature
- `fix:` Bug fix
- `docs:` Documentation
- `test:` Test additions
- `refactor:` Code refactoring
- `perf:` Performance improvements
- `chore:` Maintenance

### 4. Push and Submit a Pull Request

```bash
git push origin feature/your-feature-name
```

**PR Template:**

```markdown
## Description
Brief description of changes

## Related Issues
Closes #issue_number

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Testing
- [ ] Unit tests added
- [ ] Integration tests added
- [ ] All tests pass

## Checklist
- [ ] Code follows style guidelines
- [ ] Documentation updated
- [ ] No new warnings generated
```

### 5. Code Review & Merge

- Wait for code review
- Address feedback
- Rebase and merge to `main` once approved

### GitHub Actions CI/CD Pipeline

The project includes automated:
- **Code Building**: Maven build
- **Unit Testing**: All test suites
- **Integration Testing**: Full system tests
- **Deployment**: Auto-deploy on merge to `main`

View workflow: `.github/workflows/deploy.yml`

## 📊 Key Metrics & Monitoring

### Health Check

```bash
curl http://localhost:7600/actuator/health
```

**Response:**
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP"
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 107374182400,
        "free": 50000000000,
        "threshold": 10485760
      }
    }
  }
}
```

### Application Metrics

```bash
# View application info
curl http://localhost:7600/actuator/info

# View metrics
curl http://localhost:7600/actuator/prometheus
```

## 📝 License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

### MIT License Summary

You are free to:
- ✅ Use, modify, and distribute this software
- ✅ Use it for commercial purposes
- ✅ Include in proprietary applications

With the condition that you include the original license and copyright notice.

## 👤 Author & Contributors

**Project Owner:** [Tarun Kumar Madapada](https://github.com/Madapada-TarunKumar-Git)

### Contributing Team

We appreciate contributions from the community! If you've helped improve TaskFlow, please add yourself here.

## 🔗 Quick Links

- 📚 [API Documentation](http://localhost:7600/swagger-ui.html) - Interactive Swagger UI
- 📖 [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- 🛡️ [Spring Security Guide](https://spring.io/projects/spring-security)
- 🗄️ [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- 🐳 [Docker Documentation](https://docs.docker.com/)

## 🐛 Issue Reporting

Found a bug? Have a suggestion?

1. Check [existing issues](https://github.com/Madapada-TarunKumar-Git/taskflow/issues)
2. Create a [new issue](https://github.com/Madapada-TarunKumar-Git/taskflow/issues/new) with:
   - Clear description
   - Steps to reproduce (for bugs)
   - Expected vs actual behavior
   - Environment details (OS, Java version, etc.)

## 📞 Support

For questions or support:
- 💬 Open a GitHub Discussion
- 📧 Contact: tarunkumar.madapada@gmail.com
- 🔍 Search existing [Issues](https://github.com/Madapada-TarunKumar-Git/taskflow/issues)

---

**Made with ❤️ using Spring Boot and Java 21**

Last Updated: July 1, 2024 | Version: 0.0.1-SNAPSHOT
