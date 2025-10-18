# Library Management System - Microservices Architecture

A modern, cloud-native library management system built with Spring Boot and microservices architecture. This system provides a comprehensive solution for managing library operations including book lending, customer management, and librarian workflows.

## 🏗️ Architecture

### Microservices
1. **API Gateway** - Single entry point for all client requests
2. **Library Service** - Manages books, authors, and library resources (MySQL)
3. **Customer Service** - Handles customer information and accounts (PostgreSQL)
4. **Loan Service** - Manages book loans and returns (MongoDB)
5. **Library Worker Service** - Handles librarian operations and staff management

### Database Technologies
- **MySQL**: Library and worker data
- **PostgreSQL**: Customer information
- **MongoDB**: Loan and transaction records

## 🚀 Getting Started

### Prerequisites
- Java 17 or higher
- Docker and Docker Compose
- Maven
- MySQL, PostgreSQL, and MongoDB (or use Docker containers)

### Running with Docker Compose
```bash
docker-compose up --build
```

### Manual Setup
1. Start required databases
2. Build each service:
   ```bash
   cd <service-directory>
   ./mvnw clean package
   ```
3. Run each service with the appropriate profile

## 🔧 Configuration

### Environment Variables
- `SPRING_PROFILES_ACTIVE`: Set to `docker` for containerized deployment
- Database connection strings
- Service discovery and API gateway configurations

## 🧪 Testing

### Running Tests
```bash
# Run tests for all services
./mvnw test

# Run tests for a specific service
cd <service-directory>
./mvnw test
```

### Test Coverage
JaCoCo is configured for code coverage reporting. Reports are generated in:
```
<service-directory>/target/site/jacoco/
```

## 📦 Deployment

### Building Docker Images
```bash
# Build all services
docker-compose build

# Build a specific service
docker-compose build <service-name>
```

### Kubernetes (Optional)
Kubernetes deployment manifests are available in the `k8s/` directory.

## 📚 API Documentation

API documentation is available through Swagger UI when services are running:
- API Gateway: `http://localhost:8080/swagger-ui.html`
- Individual services: `http://<service-host>:<port>/swagger-ui.html`

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request
