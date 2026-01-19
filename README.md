# Insurance Policy Management System

A comprehensive Spring Boot application for managing vehicle insurance policies with an approval workflow system. This system includes both admin and customer portals for managing vehicles, policies, claims, and approval requests.

## 🚀 Features

### Customer Features
- **Customer Registration & Authentication**
    - Secure login with JWT authentication
    - Customer profile management

- **Vehicle Management**
    - Request vehicle registration
    - View registered vehicles
    - Support for multiple vehicle types (Car, Bike, Truck)

- **Policy Management**
    - Request policy creation for vehicles
    - View active and expired policies
    - Automatic premium calculation based on vehicle type and age
    - Policy renewal functionality

- **Claims Management**
    - File insurance claims
    - Track claim status (Submitted, Approved, Rejected)
    - View claim history

- **Request Tracking**
    - Monitor approval status of vehicle and policy requests
    - View admin comments on requests

### Admin Features
- **Approval Workflow**
    - Review pending vehicle registration requests
    - Approve/reject policy creation requests
    - Add comments to processed requests

- **Policy Management**
    - Create policies directly
    - View all policies across customers
    - Track policy expiration dates

- **Claims Processing**
    - Review submitted claims
    - Approve/reject claims with comments
    - Track claim amounts and reasons

- **Reporting**
    - View pending request counts
    - Monitor active and expired policies
    - Customer and vehicle analytics

## 🛠️ Technology Stack

### Backend
- **Java 21**
- **Spring Boot 3.5.9**
    - Spring Web
    - Spring Data JPA
    - Spring Security
    - Spring Boot Starter Test
- **MySQL** - Database
- **Hibernate** - ORM
- **JWT (JSON Web Tokens)** - Authentication
- **Lombok** - Reduce boilerplate code
- **JaCoCo** - Code coverage

### Frontend
- **HTML5**
- **CSS3** (Modern, minimalist design)
- **JavaScript (Vanilla)**
- **Fetch API** for HTTP requests

### Testing
- **JUnit 5**
- **Mockito** - Mocking framework
- **Spring Boot Test**

## 📁 Project Structure

```
DB/
│
├── Frontend/
│   ├── admin-portal/
│   │   ├── admin-login.html
│   │   └── index.html
│   └── customer-portal
│       ├── customer-dashboard.html
│       └── customer-login.html
├── src/
│   ├── main/
│   │   ├── java/com/Policy/DB/
│   │   │   ├── config/           # Configuration classes
│   │   │   ├── controller/       # REST API controllers
│   │   │   ├── dto/              # Data Transfer Objects
│   │   │   ├── model/            # Entity classes
│   │   │   ├── repository/       # JPA repositories
│   │   │   ├── security/         # Security configurations
│   │   │   ├── service/          # Business logic
│   │   │   └── util/             # Utility classes
│   │   └── resources/
│   │       ├── application.yaml 
│   │           
│   └── test/
│       └── java/com/Policy/DB/
│           ├── controller/       # Controller tests
│           └── service/          # Service tests
├── pom.xml
└── README.md
```

## 📋 Prerequisites

Before running this application, ensure you have:

- **Java 21** or higher installed
- **Maven 3.6+** installed
- **MySQL 8.0+** installed and running
- **Git** for version control
- An IDE (IntelliJ IDEA, Eclipse, VS Code)

## 💻 Installation

### 1. Clone the Repository

```bash
git clone https://github.com/VenkatreddyPadala/Vehicle-Insurance-Policy-Claims.git
cd DB
```

### 2. Create MySQL Database

```sql
CREATE DATABASE insurance_db;
```

### 3. Configure Database Connection

Update `src/main/resources/application.properties`:

```properties
spring:
    application:
        name: DB
    datasource:
        url: jdbc:mysql://localhost:3306/Insurancepolicy?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
        username: <Your DB Name>
        password: <Your DB Password>
        driver-class-name: com.mysql.cj.jdbc.Driver
        hikari:
            maximum-pool-size: 10
            minimum-idle: 5
            connection-timeout: 30000

    jpa:
        hibernate:
            ddl-auto: update
            naming:
                physical-strategy: org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl
                implicit-strategy: org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl
        show-sql: true
        properties:
            hibernate:
                format_sql: true
                dialect: org.hibernate.dialect.MySQLDialect
                jdbc:
                    batch_size: 20
                order_inserts: true
                order_updates: true
            open-in-view: false
    server:
        port: 8099
        error:
            include-message: always
            include-binding-errors: always
            include-stacktrace: on_param
```

### 4. Build the Project

```bash
mvn clean install
```

### CORS Configuration

The application allows all origins by default. Update `CorsConfig.java` for production:

```java
@Override
public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
            .allowedOrigins("http://your-frontend-domain.com")
            .allowedMethods("GET", "POST", "PUT", "DELETE")
            .allowedHeaders("*");
}
```

## 🏃 Running the Application

### Using Maven

```bash
mvn spring-boot:run
```

### Using Java

```bash
java -jar target/DB-0.0.1.jar
```

### Access the Application

- **Backend API**: http://localhost:8099
- **Customer Dashboard**: http://localhost:8099/customer-dashboard.html
- **Admin Dashboard**: http://localhost:8099/admin-dashboard.html

## 🔌 API Endpoints

### Customer Endpoints

#### Authentication
```
POST   /login          
GET    /validate
POST   /register              
```

#### Vehicles
```
POST    /add/{customerId}            # addVehicle
GET     /{vehicleId}                 # getVehicleDetails
PUT     /update/{vehicleId}          # updateVehicleDetails
GET     /customer/{customerId}       # getVehiclesByCustomerId
GET     /all                         # getAllVehicles
DELETE  /delete/{vehicleId}          # deleteVehicle
```

#### Policies
```
POST    /create/{vehicleId}          # createPolicy
GET     /{policyId}                  # getPolicyDetails
PUT     /renew/{policyId}            # renewPolicy
GET     /all                         # getAllPolicies
GET     /customer/{customerId}       # getPoliciesByCustomerId
GET     /active                      # getActivePolicies
GET     /expired                     # getExpiredPolicies
```

#### Claims
```
POST   /file/{policyId}              # File a claim
PUT    /process/{claimId}            # processClaim
GET    /{claimId}                    # getClaimStatus
GET    /all                          # getAllClaims
GET    /policy/{policyId}            # getClaimsByPolicyId
GET    /customer/{customerId}        # getClaimsByCustomerId
GET    /pending                      # getPendingClaims
GET    /status/{status}              # getClaimsByStatus   
```

#### Approval Requests
```
POST   /approvals/request/vehicle/{customerId}    # Request vehicle registration
POST   /approvals/request/policy/{customerId}     # Request policy creation
GET    /approvals/customer/{customerId}           # Get customer requests
```

### Admin Endpoints

#### Approval Management
```
GET    /approvals/pending                  # Get pending requests
GET    /approvals/all                      # Get all requests
PUT    /approvals/process/{requestId}      # Process request (approve/reject)
GET    /approvals/pending/count            # Get pending count
```

#### Policy Management
```
POST   /policies/create/{vehicleId}        # Create policy
GET    /policies/all                       # Get all policies
GET    /policies/active                    # Get active policies
GET    /policies/expired                   # Get expired policies
```

#### Claims Management
```
GET    /claims/all                         # Get all claims
PUT    /claims/process/{claimId}           # Process claim
```

## 🧪 Testing

### Run All Tests

```bash
mvn test
```

### Run Specific Test Class

```bash
mvn test -Dtest=ApprovalServiceTest
```

### Test Coverage

The project includes comprehensive unit tests for:
- ✅ Controllers (ApprovalController)
- ✅ Services (ApprovalService, PolicyService)

### Current Test Coverage

- **Controller Layer**: ~91% coverage
- **Service Layer**: ~92% coverage
- **Overall**: Targeting 80%+ coverage for business logic

## 📊 Code Coverage

### Generate Coverage Report

```bash
mvn clean test
```

### View Coverage Report

Open `target/site/jacoco/index.html` in a browser.

### Coverage Exclusions

The following packages are excluded from coverage:
- `config/` - Configuration classes
- `dto/` - Data Transfer Objects
- `model/` - Entity classes
- `repository/` - JPA repositories
- `security/` - Security configurations
- `util/` - Utility classes

## 🗄️ Database Schema

### Main Tables

#### Customer
```sql
- customerId (PK)
- name
- email
- phone
- address
```

#### Vehicle
```sql
- vehicleId (PK)
- customerId (FK)
- registrationNumber
- make
- model
- yearOfManufacture
- vehicleType (CAR, BIKE, TRUCK)
```

#### Policy
```sql
- policyId (PK)
- vehicleId (FK)
- policyNumber
- coverageAmount
- premiumAmount
- startDate
- endDate
- policyStatus (ACTIVE, EXPIRED)
```

#### Claim
```sql
- claimId (PK)
- policyId (FK)
- claimAmount
- claimDate
- claimReason
- claimStatus (SUBMITTED, APPROVED, REJECTED)
```

#### ApprovalRequest
```sql
- requestId (PK)
- customerId (FK)
- requestType (VEHICLE_REGISTRATION, POLICY_CREATION)
- requestData (JSON)
- status (PENDING, APPROVED, REJECTED)
- adminComments
- createdAt
- processedAt
- processedBy
```

## User Interfaces

### Customer Dashboard

**Features:**
- Overview statistics (vehicles, policies, claims, requests)
- Quick actions for common tasks
- Vehicle management
- Policy management
- Claims filing and tracking
- Request status monitoring
- Profile management

**Login Requirements:**
- Email address
- Customer ID

### Admin Dashboard

**Features:**
- Approval request management
- Policy administration
- Claims processing
- Customer analytics
- Real-time statistics

**Access:**
- Requires admin authentication
- Role-based access control

## Security

### Authentication

- JWT-based authentication
- Token expiration: 24 hours (configurable)
- Secure password storage with BCrypt

### Authorization

- Role-based access control (Customer, Admin)
- Protected endpoints with Spring Security
- CORS configuration for cross-origin requests

## Premium Calculation Logic

The system automatically calculates premiums based on:

1. **Base Premium by Vehicle Type:**
    - Car: ₹1,500
    - Bike: ₹800
    - Truck: ₹2,500

2. **Age Factor:**
    - Vehicle > 10 years: 1.5x multiplier
    - Vehicle > 5 years: 1.2x multiplier
    - Vehicle ≤ 5 years: No multiplier

3. **Coverage Factor:**
    - 2% of coverage amount

**Formula:**
```
Premium = (Base Premium × Age Multiplier) + (Coverage Amount × 0.02)
```

## Troubleshooting

### Common Issues

**Issue: Port 8099 already in use**
```bash
# Solution: Change port in application.properties
server.port=8080
```

**Issue: Database connection failed**
```bash
# Solution: Verify MySQL is running
sudo systemctl status mysql

# Check credentials in application.properties
```
**Built with ❤️ using Spring Boot**
