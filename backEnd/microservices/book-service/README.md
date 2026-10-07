# Book Service — Spring Boot 4 / Java 25 CRUD example

A layered REST API that manages the books of a library.

```
HTTP ─► BookController ─► BookService (interface) ─► BookServiceImpl ─► BookRepository ─► DB
           (DTOs only)            │                       │                (JPA entity)
                                  └──── BookMapper (MapStruct: DTO ⇄ Entity) ┘
```

| Layer | Package | Responsibility |
|---|---|---|
| Controller | `controller` | HTTP mapping, `@Valid` input, status codes. Only knows DTOs and the service interface. |
| Service | `service`, `service.impl` | Business rules (unique ISBN, not found), transactions, DTO ⇄ entity conversion through the mapper. |
| Repository | `repository` | Spring Data JPA: built-in CRUD, derived queries, one JPQL search query. |
| Models | `model.entity`, `model.enums` | JPA entity `Book` (auditing, optimistic locking) and the `Genre` enum. |
| DTOs | `dto` | `BookRequest` (input + validation), `BookResponse` (output), `PageResponse<T>` (pagination). |
| Mapper | `mapper` | `BookMapper`: MapStruct generates `BookMapperImpl` at compile time. |
| Errors | `exception` | `GlobalExceptionHandler` returns RFC 9457 `ProblemDetail` responses (400/404/409). |
| Config | `config` | OpenAPI metadata and JPA auditing. |

## Microservice
- Port: `8081`
- Eureka name: `BOOK-SERVICE` (registers in `http://localhost:8761/eureka`)
- Through the API Gateway: `http://localhost:8080/api/v1/books`

## Requirements
- JDK 25
- Nothing else: the Maven wrapper (`mvnw`) is included, and H2 runs in memory.

## Run
```bash
./mvnw spring-boot:run
```
- Swagger UI: http://localhost:8081/swagger-ui.html
- OpenAPI JSON: http://localhost:8081/v3/api-docs
- H2 console: http://localhost:8081/h2-console (JDBC URL `jdbc:h2:mem:bookdb`, user `sa`, empty password)
- Health check: http://localhost:8081/actuator/health

On startup, `data.sql` adds 6 sample books.

### With PostgreSQL instead of H2
Requires a running PostgreSQL on `localhost:5432` with a database `library` (user `library`, password `library`).
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres
```

## Endpoints
| Method | URL | Description |
|---|---|---|
| GET | `/api/v1/books?author=&genre=&page=0&size=20&sort=title,asc` | Paginated list with filters |
| GET | `/api/v1/books/{id}` | Get one book |
| POST | `/api/v1/books` | Create a book (201 + `Location` header) |
| PUT | `/api/v1/books/{id}` | Replace a book |
| DELETE | `/api/v1/books/{id}` | Delete a book (204) |

## Tests
```bash
./mvnw test
```
- `BookServiceImplTest`: unit tests with a Mockito-mocked repository and the real generated mapper.
- `BookControllerIntegrationTest`: full-stack tests through MockMvc and H2, plus a check that the OpenAPI docs are exposed.
