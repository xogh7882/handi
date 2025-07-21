# Backend API

Spring Boot 기반의 백엔드 REST API 프로젝트입니다.

## 기술 스택

- **Java**: 17
- **Spring Boot**: 3.4.5
- **Database**: H2 (개발용), PostgreSQL (운영용)
- **Cache**: Redis
- **Documentation**: Swagger UI (OpenAPI 3.0)
- **Build Tool**: Gradle

## 실행 방법

### 로컬 실행
```bash
./gradlew bootRun
```

### 빌드
```bash
./gradlew build
```

## API 문서

애플리케이션 실행 후 다음 URL에서 API 문서를 확인할 수 있습니다:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs

## 헬스체크 API

### GET /api/health
애플리케이션 상태를 확인합니다.

**응답 예시:**
```json
{
  "status": "UP",
  "message": "Backend application is running successfully",
  "timestamp": "2025-07-17T21:59:51.558468"
}
```

### GET /api/info
애플리케이션 기본 정보를 반환합니다.

**응답 예시:**
```json
{
  "name": "Backend API",
  "version": "1.0.0",
  "description": "Spring Boot Backend Application",
  "timestamp": "2025-07-17T22:00:01.928544"
}
```

## 데이터베이스 설정

### 개발환경 (H2)
현재 H2 인메모리 데이터베이스로 설정되어 있습니다.
- H2 Console: http://localhost:8080/h2-console

### 운영환경 (PostgreSQL)
PostgreSQL을 사용하려면 `application.yml`에서 다음 설정을 주석 해제하세요:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/backend_db
    username: postgres
    password: password
    driver-class-name: org.postgresql.Driver
```

## Redis 설정

Redis를 사용하려면 `application.yml`에서 다음 설정을 주석 해제하세요:
```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      timeout: 2000ms
```

## 포트

- **애플리케이션**: 8080
- **H2 Console**: 8080/h2-console
- **Swagger UI**: 8080/swagger-ui.html 