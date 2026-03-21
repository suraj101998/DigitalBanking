# Digital Banking Application

A modern, secure REST API for digital banking operations built with Spring Boot 3.4.4 and Java 17. This application provides comprehensive customer management, transaction processing, and balance tracking capabilities with JWT-based authentication and role-based access control.

---

## 📋 Table of Contents

1. [Project Overview](#project-overview)
2. [Technology Stack](#technology-stack)
3. [Project Structure](#project-structure)
4. [Prerequisites](#prerequisites)
5. [Installation & Setup](#installation--setup)
6. [Database Configuration](#database-configuration)
7. [Running the Application](#running-the-application)
8. [API Endpoints](#api-endpoints)
9. [Authentication & Security](#authentication--security)
10. [Working with the API](#working-with-the-api)
11. [Architecture Overview](#architecture-overview)
12. [Development Guidelines](#development-guidelines)
13. [Troubleshooting](#troubleshooting)
14. [Future Enhancements](#future-enhancements)

---

## Project Overview

The Digital Banking Application is a production-ready REST API designed for modern banking systems. It handles:

- **User Authentication** - Secure login and registration with JWT tokens
- **Customer Management** - CRUD operations for bank customers
- **Transaction Processing** - Create, track, and filter financial transactions
- **Balance Management** - Real-time balance tracking and calculations
- **Role-Based Access Control** - Admin and User roles with specific permissions
- **Transaction History** - Comprehensive transaction queries with pagination
- **Data Caching** - Performance optimization for frequently accessed data

### Key Features

- ✅ JWT-based authentication
- ✅ Automatic balance calculation
- ✅ Comprehensive transaction validation
- ✅ Pagination support (page size: 5 items)
- ✅ Caching for performance optimization
- ✅ Global exception handling
- ✅ RESTful API design
- ✅ Spring Security integration
- ✅ MySQL database with optimized indices

---

## Technology Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| **Language** | Java | 17 |
| **Framework** | Spring Boot | 3.4.4 |
| **Database** | MySQL | 8.0.28 |
| **ORM** | JPA/Hibernate | 6.x (bundled with Spring Boot) |
| **Security** | Spring Security + JWT | JJWT 0.12.3 |
| **Build Tool** | Maven | 3.x |
| **Server** | Embedded Tomcat | 10.x |
| **JSON Processing** | Jackson | 2.x (bundled) |
| **Utilities** | Project Lombok | Latest |

### Dependencies

See `pom.xml` for complete dependency list:

```xml
<!-- Core Spring Boot -->
- spring-boot-starter-web
- spring-boot-starter-data-jpa
- spring-boot-starter-security

<!-- JWT -->
- jjwt-api, jjwt-impl, jjwt-jackson (v0.12.3)

<!-- Database -->
- mysql-connector-j

<!-- Tools -->
- lombok
- spring-boot-devtools
```

---

## Project Structure

```
src/main/java/com/example/pi/
├── controller/
│   ├── AuthenticationController.java      # Login & Registration endpoints
│   └── CustomerController.java            # Customer & Transaction endpoints
│
├── service/
│   ├── CustomerService.java               # Customer business logic (interface)
│   ├── TransactionService.java            # Transaction business logic (interface)
│   └── impl/
│       ├── CustomerServiceImpl.java        # Customer service implementation
│       └── TransactionServiceImpl.java     # Transaction service implementation
│
├── repository/
│   ├── CustomerRepository.java            # Customer data access (Spring Data)
│   ├── TransactionsRepository.java        # Transaction data access (Spring Data)
│   └── UserRepository.java                # User authentication data access
│
├── entity/
│   ├── Customers.java                     # Customer JPA entity
│   ├── Transactions.java                  # Transaction JPA entity
│   └── User.java                          # User authentication entity
│
├── dto/
│   ├── request/
│   │   ├── LoginRequest.java             # Login credentials
│   │   ├── UserRegisterRequest.java      # Registration details
│   │   └── PaymentRequest.java           # Transaction request
│   │
│   └── response/
│       ├── LoginResponse.java            # Login response with token
│       ├── BalanceResponse.java          # Balance information
│       ├── TransactionResponse.java      # Transaction details
│       └── PaginationResponse.java       # Paginated list wrapper
│
├── security/
│   ├── JwtUtils.java                     # JWT generation & validation
│   ├── JwtAuthenticationFilter.java      # JWT filter chain
│   └── SecurityConfiguration.java        # Spring Security configuration
│
├── exception/
│   ├── APIException.java                 # Custom exception class
│   ├── ErrorDetails.java                 # Error response wrapper
│   ├── ResourceNotFoundException.java     # 404 exception
│   └── GlobalExceptionHandler.java       # Centralized exception handling
│
├── util/
│   └── PaginationUtil.java              # Pagination helper methods
│
├── dao/ (Legacy - deprecated)
│   ├── UserDao.java
│   └── impl/
│       └── UserDaoImpl.java
│
└── PiApplication.java                    # Spring Boot main class

src/main/resources/
├── application.properties                # Application configuration
├── banner.txt                           # Application startup banner
└── db/migration/                        # Database migration scripts (if using Flyway)
```

---

## Prerequisites

Before setting up the project, ensure you have the following installed:

### System Requirements

- **OS**: Windows, macOS, or Linux
- **RAM**: Minimum 4GB (8GB recommended)
- **Disk Space**: 2GB free space

### Software Requirements

1. **Java Development Kit (JDK) 17+**
   ```bash
   # Verify installation
   java -version
   # Should output: Java version 17.x.x
   ```

2. **Apache Maven 3.8.0+**
   ```bash
   # Verify installation
   mvn -version
   # Should output: Apache Maven 3.8.x
   ```

3. **MySQL Server 8.0+**
   ```bash
   # Verify installation (MySQL CLI)
   mysql --version
   # Should output: MySQL version 8.0.x
   ```

4. **Git** (for version control)
   ```bash
   # Verify installation
   git --version
   ```

### IDE Recommendation

- **IntelliJ IDEA** (Ultimate or Community Edition)
- **Visual Studio Code** (with Java extensions)
- **Eclipse IDE**

---

## Installation & Setup

### Step 1: Clone the Repository

```bash
# Clone the repository
git clone <repository-url>

# Navigate to project directory
cd DigitalBanking
```

### Step 2: Verify Java and Maven Installation

```bash
# Check Java version (should be 17+)
java -version

# Check Maven version (should be 3.8.0+)
mvn -version
```

### Step 3: Build the Project

```bash
# Navigate to project root
cd DigitalBanking

# Clean and build
mvn clean install

# Or skip tests during development
mvn clean install -DskipTests
```

This command will:
- Download all dependencies
- Compile the source code
- Create the WAR/JAR file in `target/` directory

### Step 4: Install MySQL and Create Database

#### Option A: Using MySQL Command Line

```bash
# Start MySQL server
mysql -u root -p

# Create database
CREATE DATABASE digitalbanking;

# Use the database
USE digitalbanking;

# Import the schema
SOURCE path/to/Database_dump/pi.sql;

# Verify tables created
SHOW TABLES;
```

#### Option B: Using MySQL Workbench

1. Open MySQL Workbench
2. Create new connection to localhost:3306
3. Execute the SQL file: `Database_dump/pi.sql`
4. Verify tables: `customers`, `transactions`, `user`

### Step 5: Configure Application Properties

Edit `src/main/resources/application.properties`:

```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/digitalbanking
spring.datasource.username=root
spring.datasource.password=root
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA/Hibernate Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.hibernate.naming-strategy=org.hibernate.cfg.ImprovedNamingStrategy
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# Pagination Configuration
spring.data.web.pageable.default-page-size=5
spring.data.web.pageable.one-indexed-parameters=false

# Caching Configuration
spring.cache.type=simple
spring.cache.cache-names=Customer,Transactions,AllTransactions,LatestTransactions,Balance,TransactionsByMode

# JWT Configuration (IMPORTANT: Change in production!)
jwt.secret=mySecretKeyForJwtTokenGenerationAndValidationPleaseKeepItSafeAndSecure12345
jwt.expiration=3600000

# Logging Configuration
server.tomcat.accesslog.enabled=true
server.tomcat.basedir=tomcat
server.tomcat.accesslog.directory=./logs
```

**⚠️ Security Note**: Change the `jwt.secret` value in production to a strong, random string!

---

## Database Configuration

### Database Schema

The application uses three main tables:

#### 1. **users** Table
```sql
CREATE TABLE user (
  id INT PRIMARY KEY AUTO_INCREMENT,
  user_name VARCHAR(50) UNIQUE NOT NULL,
  password MEDIUMTEXT NOT NULL,
  roles VARCHAR(50) NOT NULL,
  active VARCHAR(100) NOT NULL
);
```

**Sample Data:**
```
| id | user_name | password     | roles     | active |
|----|-----------|------|----------|--------|
| 1  | user      | pass | ROLE_USER | 2 |
| 2  | Suraj     | Suraj@101998 | ROLE_ADMIN | 1 |
```

#### 2. **customers** Table
```sql
CREATE TABLE customers (
  customer_id INT PRIMARY KEY AUTO_INCREMENT,
  customer_name VARCHAR(100) NOT NULL,
  account_number MEDIUMTEXT NOT NULL,
  identity_type VARCHAR(50) NOT NULL,
  identity_number VARCHAR(50) NOT NULL,
  date_of_birth DATE,
  mobile_number MEDIUMTEXT NOT NULL,
  email_id VARCHAR(50) NOT NULL,
  address VARCHAR(50) NOT NULL,
  sex VARCHAR(20) NOT NULL
);
```

#### 3. **transactions** Table
```sql
CREATE TABLE transactions (
  serial_number INT PRIMARY KEY AUTO_INCREMENT,
  customer_id INT NOT NULL,
  initial_deposit INT NOT NULL,
  available_balance INT NOT NULL,
  transaction_type VARCHAR(45) NOT NULL,
  transaction_mode VARCHAR(45) NOT NULL,
  transaction_id VARCHAR(45) NOT NULL,
  transaction_amount INT NOT NULL,
  transaction_to VARCHAR(45) NOT NULL,
  transaction_date DATE NOT NULL
);
```

### Key Indices (for Performance)

The following indices are created automatically or manually:

```sql
-- For fast customer lookups
CREATE INDEX idx_customer_id ON transactions(customer_id);

-- For date-based queries
CREATE INDEX idx_transaction_date ON transactions(transaction_date);

-- For transaction mode filtering
CREATE INDEX idx_transaction_mode ON transactions(transaction_mode);
```

### Import Sample Data

Sample data is included in `Database_dump/pi.sql`:

```bash
# Import using MySQL CLI
mysql -u root -p digitalbanking < Database_dump/pi.sql

# Or in MySQL console
SOURCE /path/to/Database_dump/pi.sql;
```

---

## Running the Application

### Option 1: Using Maven

```bash
# Navigate to project directory
cd DigitalBanking

# Run the application
mvn spring-boot:run

# Output should show:
# [main] c.e.p.PiApplication: Started PiApplication in X.XXX seconds
```

### Option 2: Using JAR file

```bash
# Build JAR (if not already done)
mvn clean package

# Run JAR
java -jar target/pi-0.0.1-SNAPSHOT.jar

# Application will start on http://localhost:8080
```

### Option 3: Using IDE

- **IntelliJ IDEA**: Right-click `PiApplication.java` → Run
- **Eclipse**: Right-click project → Run As → Spring Boot Application
- **VS Code**: Open terminal and run `mvn spring-boot:run`

### Verify Application is Running

```bash
# Check if server is listening on port 8080
curl http://localhost:8080/

# Try login endpoint
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"userName":"Suraj","password":"Suraj@101998"}'
```

**Expected Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userName": "Suraj",
  "roles": "ROLE_ADMIN",
  "customerId": 1
}
```

---

## API Endpoints

### Base URL
```
http://localhost:8080
```

### 1. Authentication Endpoints

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "userName": "Suraj",
  "password": "Suraj@101998"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userName": "Suraj",
  "roles": "ROLE_ADMIN",
  "customerId": 1
}
```

#### Register
```http
POST /api/auth/register
Content-Type: application/json

{
  "userName": "john_doe",
  "password": "password123",
  "email": "john@example.com"
}
```

### 2. Customer Management Endpoints

#### Get All Customers (Paginated)
```http
GET /customers/DisplayAllCustomers?page=0
Authorization: Bearer {token}
```

**Response:**
```json
{
  "content": [
    {
      "customer_id": 1,
      "customer_name": "SURAJ CHAKRABORTY",
      "account_number": "2271467230249",
      "identity_type": "ADHAAR",
      "identity_number": "949998822905"
    }
  ],
  "pageNumber": 0,
  "pageSize": 5,
  "totalElements": 5,
  "totalPages": 1,
  "isFirst": true,
  "isLast": true
}
```

#### Get Customer by ID
```http
GET /customers/FindByCustomerID/{customerId}
Authorization: Bearer {token}
```

#### Add Customer (ADMIN only)
```http
POST /customers/AddCustomers
Authorization: Bearer {admin_token}
Content-Type: application/json

{
  "customer_name": "Jane Smith",
  "account_number": "ACC002",
  "identity_type": "Passport",
  "identity_number": "P123456",
  "mobile_number": "9876543210",
  "email_id": "jane@example.com",
  "address": "New York, USA",
  "sex": "FEMALE"
}
```

#### Update Customer (ADMIN only)
```http
PUT /customers/UpdateCustomer
Authorization: Bearer {admin_token}
Content-Type: application/json

{
  "customer_id": 1,
  "customer_name": "SURAJ CHAKRABORTY Updated",
  "account_number": "2271467230249",
  ...
}
```

#### Delete Customer (ADMIN only)
```http
DELETE /customers/DeleteCustomer/{customerId}
Authorization: Bearer {admin_token}
```

### 3. Transaction Endpoints

#### Create Transaction (ADMIN)
```http
POST /customers/Banking?customerId=1
Authorization: Bearer {admin_token}
Content-Type: application/json

{
  "transaction_type": "debit",
  "transaction_to": "Another Account",
  "transaction_amount": 1000,
  "transaction_mode": "transfer"
}
```

**Response (201 Created):**
```json
{
  "serialNumber": 123,
  "transactionType": "debit",
  "transactionId": "TXN123",
  "transactionTo": "Another Account",
  "transactionDate": "2024-03-20",
  "transactionAmount": 1000,
  "customerId": 1,
  "initialDeposit": 100000,
  "availableBalance": 99000,
  "transactionMode": "transfer"
}
```

#### Check Balance
```http
GET /customers/CheckBalance/{customerId}
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "customerId": 1,
  "availableBalance": 99000,
  "initialDeposit": 100000
}
```

#### Get Transaction History (Ministatement)
```http
GET /customers/Ministatement/{customerId}?page=0
Authorization: Bearer {token}
```

#### Filter Transactions by Mode
```http
GET /customers/CheckTransactionMode/{transactionMode}?page=0
Authorization: Bearer {token}
```

#### Get All Transactions History (ADMIN)
```http
GET /customers/AllTransactionsHistory?page=0
Authorization: Bearer {admin_token}
```

#### Get Latest Transactions (ADMIN)
```http
GET /customers/LatestTransactions?page=0
Authorization: Bearer {admin_token}
```

### Error Responses

#### 400 Bad Request - Invalid Amount
```json
{
  "message": "Invalid transaction amount"
}
```

#### 400 Bad Request - Insufficient Balance
```json
{
  "message": "Insufficient balance. Current: 1000, Requested: 2000"
}
```

#### 404 Not Found
```json
{
  "message": "Customer not found"
}
```

#### 401 Unauthorized
```json
{
  "message": "Invalid or expired token"
}
```

#### 403 Forbidden
```json
{
  "message": "Access Denied"
}
```

---

## Authentication & Security

### JWT Token Workflow

1. **User logs in** with credentials
2. **Server generates JWT token** valid for 1 hour (3600000ms)
3. **Client stores token** (usually in localStorage or sessionStorage)
4. **For each request**, client sends token in Authorization header
5. **Server validates token** before processing request
6. **If expired or invalid**, server returns 401 Unauthorized

### Authorization Header Format

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJTdXJhaiIsImlhdCI6MTcwNjc...
```

### Roles & Permissions

| Role | Permissions |
|------|------------|
| **ROLE_ADMIN** | ✅ All endpoints (customers, transactions, admin operations) |
| **ROLE_USER** | ✅ View own balance and transactions only |

### Security Considerations

- ✅ Passwords are hashed using Spring Security
- ✅ JWT tokens are signed with a secret key
- ✅ CSRF protection enabled
- ✅ SQL injection prevention (using parameterized queries)
- ✅ XSS protection enabled
- ✅ Role-based access control (RBAC)

### Changing JWT Secret (Production)

```properties
# In application.properties
jwt.secret=your-very-long-random-secure-key-change-this-in-production
jwt.expiration=3600000  # 1 hour in milliseconds
```

---

## Working with the API

### Complete Workflow Example

#### 1. Register a New User

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "userName": "john_doe",
    "password": "SecurePass123!",
    "email": "john@example.com"
  }'
```

#### 2. Login to Get Token

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "userName": "john_doe",
    "password": "SecurePass123!"
  }'
```

**Save the token from response:**
```
TOKEN=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### 3. Check Account Balance

```bash
curl -X GET http://localhost:8080/customers/CheckBalance/1 \
  -H "Authorization: Bearer $TOKEN"
```

#### 4. View Transaction History

```bash
curl -X GET "http://localhost:8080/customers/Ministatement/1?page=0" \
  -H "Authorization: Bearer $TOKEN"
```

#### 5. Make a Payment (if ADMIN)

```bash
curl -X POST "http://localhost:8080/customers/payment/1" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "transaction_type": "debit",
    "transaction_to": "Friend Account",
    "transaction_amount": 500,
    "transaction_mode": "transfer"
  }'
```

### Using Postman

1. **Create a new collection** "Digital Banking"
2. **Add variable** `baseUrl = http://localhost:8080`
3. **Add variable** `token = ` (leave empty, fill after login)
4. **Create requests**:
   - POST `/api/auth/login` - Save token to variable
   - GET `/customers/CheckBalance/1` - Use token in Authorization
   - POST `/customers/payment/1` - Create transaction
   - GET `/customers/Ministatement/1?page=0` - View history

**Import Postman Collection:**
Use the provided `DigitalBanking.postman_collection.json` file.

---

## Architecture Overview

### Layered Architecture

```
┌─────────────────────────────────────┐
│  REST Client (Browser/Postman/App)  │
└────────────────┬────────────────────┘
                 │ HTTP Request
                 ▼
┌─────────────────────────────────────┐
│  Controllers (HTTP handling)         │
│  - AuthenticationController          │
│  - CustomerController               │
└────────────────┬────────────────────┘
                 │ Method calls
                 ▼
┌─────────────────────────────────────┐
│  Services (Business Logic)          │
│  - CustomerService                 │
│  - TransactionService              │
└────────────────┬────────────────────┘
                 │ Queries/Updates
                 ▼
┌─────────────────────────────────────┐
│  Repositories (Data Access)         │
│  - CustomerRepository               │
│  - TransactionsRepository           │
│  - UserRepository                   │
└────────────────┬────────────────────┘
                 │ SQL
                 ▼
┌─────────────────────────────────────┐
│  MySQL Database                     │
│  - customers table                  │
│  - transactions table               │
│  - user table                       │
└─────────────────────────────────────┘
```

### Request Flow Example: Create Transaction

```
1. Client sends POST /customers/Banking?customerId=1
                    ↓
2. JwtAuthenticationFilter validates token
                    ↓
3. CustomerController receives request
                    ↓
4. TransactionService.createTransaction() called
   - Validates customer exists
   - Validates transaction amount > 0
   - Checks balance for debit transactions
   - Calculates new balance
                    ↓
5. TransactionsRepository saves to database
                    ↓
6. TransactionResponse returned to client
```

### Key Design Patterns Used

1. **MVC Pattern** - Controllers, Services, Repositories
2. **DTO Pattern** - Request/Response objects for clean API contracts
3. **Repository Pattern** - Data access abstraction using Spring Data
4. **Singleton Pattern** - Services as Spring beans
5. **Dependency Injection** - Autowiring of services and repositories

### Caching Strategy

```
Cached Endpoints:
├── /CheckBalance/{id} - cached by customer ID
├── /Ministatement/{id} - cached by customer + page
├── /CheckTransactionMode/{mode} - cached by mode + page
└── /AllTransactionsHistory - cached by page

Cache Type: Simple (In-memory)
Invalidation: Automatic on application restart
```

---

## Development Guidelines

### Code Style and Conventions

1. **Naming Conventions**
   - Classes: PascalCase (e.g., `CustomerService`)
   - Methods: camelCase (e.g., `createTransaction()`)
   - Constants: UPPER_SNAKE_CASE (e.g., `MAX_PAGE_SIZE`)
   - Database columns: snake_case (e.g., `customer_id`)

2. **File Organization**
   - One class per file
   - Related classes in same package
   - Follow package structure strictly

3. **Java Best Practices**
   - Use `@Autowired` for dependency injection
   - Annotate methods with `@Transactional` for database operations
   - Use `@RequestMapping` annotations for controller methods
   - Handle exceptions with `@ControllerAdvice`

### Adding New Endpoints

#### Step 1: Create DTO Classes (if needed)

```java
// src/main/java/com/example/pi/dto/request/MyRequest.java
package com.example.pi.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MyRequest {
    private String field1;
    private int field2;
}
```

#### Step 2: Create Service Interface

```java
// src/main/java/com/example/pi/service/MyService.java
package com.example.pi.service;

import com.example.pi.dto.response.MyResponse;

public interface MyService {
    MyResponse processData(String input);
}
```

#### Step 3: Implement Service

```java
// src/main/java/com/example/pi/service/impl/MyServiceImpl.java
package com.example.pi.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MyServiceImpl implements MyService {

    @Autowired
    private MyRepository repository;

    @Override
    @Transactional
    public MyResponse processData(String input) {
        // Business logic here
        return new MyResponse();
    }
}
```

#### Step 4: Create Controller Endpoint

```java
// src/main/java/com/example/pi/controller/MyController.java
package com.example.pi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/my")
public class MyController {

    @Autowired
    private MyService service;

    @PostMapping("/endpoint")
    public ResponseEntity<?> create(@RequestBody MyRequest request) {
        MyResponse response = service.processData(request.getField1());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
```

### Adding New Database Entities

#### Step 1: Create Entity Class

```java
// src/main/java/com/example/pi/entity/MyEntity.java
package com.example.pi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "my_table")
@Getter
@Setter
public class MyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "field_name", nullable = false)
    private String fieldName;

    @Column(name = "field_value")
    private Integer fieldValue;
}
```

#### Step 2: Create Repository

```java
// src/main/java/com/example/pi/repository/MyRepository.java
package com.example.pi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.pi.entity.MyEntity;

public interface MyRepository extends JpaRepository<MyEntity, Long> {
    MyEntity findByFieldName(String fieldName);
}
```

### Testing Endpoints

Use Postman or cURL:

```bash
# Test with cURL
curl -X GET http://localhost:8080/customers/CheckBalance/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"

# Pretty print JSON response
curl -s http://localhost:8080/... | jq .
```

### Debugging Tips

1. **View Application Logs**
   ```bash
   # Logs are in ./logs/access_log.YYYY-MM-DD.log
   tail -f ./logs/access_log.2024-03-21.log
   ```

2. **Enable Debug Mode**
   ```properties
   # In application.properties
   logging.level.com.example.pi=DEBUG
   ```

3. **Database Query Logging**
   ```properties
   # In application.properties
   spring.jpa.show-sql=true
   spring.jpa.properties.hibernate.format_sql=true
   ```

4. **Use IDE Debugger**
   - Set breakpoints in code
   - Run in Debug mode
   - Inspect variables at runtime

---

## Troubleshooting

### Common Issues and Solutions

#### 1. Error: "Class 'PiApplication' not found in module 'pi-0.0.1-SNAPSHOT'"

**Solution:**
```bash
# Rebuild the project
mvn clean install

# If using IDE, rebuild and invalidate cache
# IntelliJ: File → Invalidate Caches
# Eclipse: Project → Clean
```

#### 2. Error: "Connection refused: localhost:3306"

**Solution:**
```bash
# Check if MySQL is running
# Windows:
Get-Service MySQL80

# Linux:
sudo systemctl status mysql

# Start MySQL if not running
# Windows: services.msc → MySQL80 → Start
# Linux: sudo systemctl start mysql

# Verify connection
mysql -u root -p -h localhost
```

#### 3. Error: "Access denied for user 'root'@'localhost'"

**Solution:**
```properties
# Check credentials in application.properties
spring.datasource.username=root
spring.datasource.password=root

# Update with your MySQL credentials
# Test connection:
mysql -u root -p -h localhost
```

#### 4. Error: "No application.properties file found"

**Solution:**
- Ensure file exists at: `src/main/resources/application.properties`
- Right-click project → Maven → Update Project
- Rebuild: `mvn clean install`

#### 5. JWT Token Expired Error

**Solution:**
```bash
# Token expires after 1 hour by default
# Get a new token by logging in again
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"userName":"Suraj","password":"Suraj@101998"}'

# Or increase expiration in application.properties:
jwt.expiration=7200000  # 2 hours
```

#### 6. 404 Not Found on Request

**Solution:**
- Verify correct endpoint path
- Check authorization header format: `Authorization: Bearer {token}`
- Check request method (GET, POST, etc.)
- Verify custom ID exists in database

#### 7. Access Denied (403 Forbidden)

**Solution:**
- Ensure user has required role:
  - ROLE_ADMIN for admin operations
  - ROLE_USER for user operations
- Check token is valid and not expired

#### 8. Insufficient Balance Error

**Solution:**
- Check customer's available balance first:
  ```bash
  curl -X GET http://localhost:8080/customers/CheckBalance/1 \
    -H "Authorization: Bearer $TOKEN"
  ```
- Use debit transactions only when sufficient balance
- Use credit transactions to add funds

#### 9. Database Indices Not Created

**Solution:**
```bash
# Create indices manually in MySQL
mysql> USE digitalbanking;
mysql> CREATE INDEX idx_customer_id ON transactions(customer_id);
mysql> CREATE INDEX idx_transaction_date ON transactions(transaction_date);
mysql> CREATE INDEX idx_transaction_mode ON transactions(transaction_mode);
```

#### 10. Port 8080 Already in Use

**Solution:**
```bash
# Option 1: Change port in application.properties
server.port=8081

# Option 2: Kill process using port 8080
# Windows:
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Linux:
lsof -i :8080
kill -9 <PID>
```

### Logs and Debugging

#### View Application Logs
```bash
# Tomcat access logs
tail -f ./logs/access_log.2024-03-21.log

# Application debug logs (if enabled)
tail -f ./logs/application.log
```

#### Enable Detailed Logging
```properties
# In application.properties
logging.level.root=INFO
logging.level.com.example.pi=DEBUG
logging.level.org.springframework.web=DEBUG
logging.level.org.hibernate.SQL=DEBUG
```

---

## Future Enhancements

### Planned Features

1. **Advanced Authentication**
   - Two-factor authentication (2FA)
   - OAuth2 integration
   - Biometric login support

2. **Transaction Features**
   - Scheduled transactions
   - Transaction notifications
   - Email/SMS alerts

3. **Analytics & Reporting**
   - Transaction analytics dashboard
   - Spending reports
   - Real-time monitoring

4. **Performance**
   - Redis caching (distributed)
   - Database query optimization
   - Async transaction processing

5. **Security**
   - AES encryption for sensitive data
   - API rate limiting
   - IP whitelisting
   - Audit logging

6. **Integration**
   - Payment gateway integration (Stripe, PayPal)
   - SMS notifications (Twilio)
   - Email notifications
   - Third-party API integration

7. **Mobile Application**
   - React Native mobile app
   - iOS and Android support
   - Offline transaction queue

### Contribution Guidelines

1. Fork the repository
2. Create feature branch: `git checkout -b feature/feature-name`
3. Commit changes: `git commit -m "Add feature"`
4. Push to branch: `git push origin feature/feature-name`
5. Open pull request

---

## Support & Documentation

### Additional Resources

- **API Guide**: See `API_GUIDE.md` for detailed endpoint documentation
- **Architecture**: See `ARCHITECTURE.md` for system design details
- **Quick Reference**: See `QUICK_REFERENCE.md` for quick lookup

### Database Files

- **SQL Dump**: `Database_dump/pi.sql`
- **Schema**: MySQL 8.0.28 compatible

### Configuration Files

- **Application Properties**: `src/main/resources/application.properties`
- **POM XML**: `pom.xml`
- **Postman Collection**: `DigitalBanking.postman_collection.json`

---

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 0.0.1 | 2024-03-21 | Initial release with JWT auth, transaction management, and balance tracking |

---

## License

This project is proprietary and confidential. Unauthorized copying, modification, or distribution is strictly prohibited.

---

## Contact & Support

For issues, questions, or support:
- Email: support@digitalbanking.local
- Issue Tracker: [GitHub Issues](https://github.com/your-repo/issues)

---

**Last Updated**: March 21, 2024
**Documentation Version**: 1.0
**Application Version**: 0.0.1-SNAPSHOT

---

## Quick Checklist for First-Time Setup

- [ ] Install Java 17+
- [ ] Install Maven 3.8.0+
- [ ] Install MySQL 8.0+
- [ ] Clone repository
- [ ] Run `mvn clean install`
- [ ] Create database and import SQL script
- [ ] Update `application.properties` with your credentials
- [ ] Run `mvn spring-boot:run`
- [ ] Verify with login endpoint
- [ ] Import Postman collection
- [ ] Start testing APIs!

**Happy Banking! 🏦**
