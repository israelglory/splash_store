# SplashStore API

This project includes JWT-based authentication, product management, addresses, orders, Swagger/OpenAPI docs, and a Postman collection.

## API Docs

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Postman collection: `docs/postman/SplashStore.postman_collection.json`
- Postman environment: `docs/postman/SplashStore.postman_environment.json`

## Main Endpoints

- `POST /api/auth/signup`
  - Body: `username`, `email`, `password`, `phone`, `fullname`
  - Returns: JWT token and user info
- `POST /api/auth/login`
  - Body: `email`, `password`
  - Returns: JWT token and user info

- `POST /api/addresses`
  - Body: `label`, `street`, `city`, `state`, `postalCode`, `country`, `phone`, `isDefault`
- `POST /api/orders`
  - Body: `addressId`, `items[]` with `productId` and `quantity`
- `POST /api/products`
  - Body: `name`, `description`, `categoryId`, `price`, `image`, `status`
- `POST /api/category`
  - Body: `categoryName`

## Configuration

JWT settings live in `src/main/resources/application.properties`:

- `jwt.secret`
- `jwt.expiration-ms`

## Quick Try

```zsh
./mvnw test
./mvnw spring-boot:run
```

## Importing Postman

1. Open Postman.
2. Import `docs/postman/SplashStore.postman_collection.json`.
3. Import `docs/postman/SplashStore.postman_environment.json`.
4. Set `token` after login.

