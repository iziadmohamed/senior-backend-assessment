# 🏠 Property Rental Platform

A multi-tenant property rental system built with **Spring Boot**, **PostgreSQL**, and **Keycloak**. Designed to support property listings, bookings, search, and secure access control.

---

## 🚀 Features

- 🔐 **Role-based Authentication & Authorization** with Keycloak  
- 🏘️ **Multi-tenant Property Management**  
- 📅 **Booking System** with overlap prevention  
- 🔍 **Search & Filter** by availability, location, and amenities  
- 📄 **RESTful APIs** with DTO mapping, pagination, and validation  
- 🧪 **Integration Tests** with Testcontainers ( PostgreSQL, Keycloak, kafka, elasticsearch)  
- 🐳 **Dockerized** environment using Docker Compose  
- 📦 **Flyway** for database migration  
- ⚙️ Clean architecture with SOLID principles  

---

## 🛠️ Tech Stack

| Layer            | Technology                   |
|------------------|------------------------------|
| Backend          | Spring Boot (Java)           |
| Auth             | Keycloak (OIDC)              |
| Database         | PostgreSQL                   |
| Migrations       | Flyway                       |
| Testing          | Testcontainers               |
| Containerization | Docker, Docker Compose       |
| Build Tool       | Maven                        |

---

## 📌 Roles

- **Admin**: Full access (users, properties, bookings)  
- **Owner**: Manage their own properties  
- **User**: Book and browse available listings  

---

## 🔄 Booking Rules

- Overlapping bookings for the same property are **prevented**  
- Free slots and availability are calculated based on existing bookings  

---

## 📦 Setup

### Prerequisites
- Docker & Docker Compose  
- Java 17+  
- Maven 3.8+  

### Run the App

```bash
# Clone the project
git clone https://github.com/your-username/property-rental-app.git
cd property-rental-app

./mvnw clean package -DskipTests

# Start all services
docker-compose up --build
```
App will be available at: http://localhost:8080

Keycloak: http://localhost:8081 

### Get Access Token
```bash
curl --location 'http://localhost:8081/realms/propertyrental/protocol/openid-connect/token' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode 'grant_type=password' \
--data-urlencode 'client_id=springboot-client' \
--data-urlencode 'client_secret=secret' \
--data-urlencode 'username=admin' \
--data-urlencode 'password=admin'
```
🧪 Running Tests
```bash
./mvnw clean test
```
Testcontainers will spin up PostgreSQL, Keycloak , kafka and elasticsearch


🛠 Realm import: Configure users, roles, and clients via realm-export.json
