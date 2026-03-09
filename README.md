# Car Sales Backend API

Backend API cho hệ thống bán xe ô tô, xây dựng với Spring Boot.

## Công nghệ sử dụng

- Java 17
- Spring Boot 4.0.3
- Spring Security + JWT
- MySQL 8.0
- Redis (Cache)
- Flyway (Database Migration)
- Docker & Docker Compose

## Yêu cầu hệ thống

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 7+
- Docker (optional)

## Cài đặt và chạy

### 1. Clone project và cấu hình

```bash
cd BE_oto/java-web-oto
cp .env.example .env
```

### 2. Chạy với Docker (Khuyến nghị)

```bash
cd docker
docker-compose up -d
```

### 3. Chạy local

```bash
# Khởi động MySQL và Redis trước
mvnw.cmd spring-boot:run
```

## API Documentation

Sau khi chạy ứng dụng, truy cập Swagger UI tại:

```
http://localhost:8080/api/swagger-ui.html
```

## Cấu trúc dự án

- `config/` - Cấu hình hệ thống
- `controller/` - REST API endpoints
- `service/` - Business logic
- `repository/` - Database access
- `entity/` - JPA entities
- `dto/` - Data transfer objects
- `security/` - JWT & Authentication
- `exception/` - Exception handling
