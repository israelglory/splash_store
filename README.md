# SplashStore Authentication

This project includes JWT-based authentication with Spring Security.

## Endpoints

- `POST /api/auth/signup`
  - Body: `username`, `email`, `password`, `phone`, `fullname`
  - Returns: JWT token and user info
- `POST /api/auth/login`
  - Body: `email`, `password`
  - Returns: JWT token and user info

## Configuration

JWT settings live in `src/main/resources/application.properties`:

- `jwt.secret`
- `jwt.expiration-ms`

## Quick Try

```zsh
./mvnw test
./mvnw spring-boot:run
```

