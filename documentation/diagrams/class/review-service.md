# Diagramme de classes : review-service

Classes du microservice de gestion des avis, de la couche HTTP à la couche persistance.

Règle métier : la note est comprise entre 1 et 5 ; la moyenne est arrondie à une décimale et vaut 0.0 sans avis.

```mermaid
classDiagram
    direction TB
    class ReviewController {
        <<RestController>>
        -ReviewService service
        +findAll(Long bookId, Pageable pageable) PageResponse~ReviewResponse~
        +findById(Long id) ReviewResponse
        +create(ReviewRequest request) ReviewResponse
        +update(Long id, ReviewRequest request) ReviewResponse
        +delete(Long id) void
        +averageForBook(Long bookId) RatingSummary
    }
    class ReviewService {
        <<interface>>
        +findAll(Long bookId, Pageable pageable) PageResponse~ReviewResponse~
        +findById(Long id) ReviewResponse
        +create(ReviewRequest request) ReviewResponse
        +update(Long id, ReviewRequest request) ReviewResponse
        +delete(Long id) void
        +averageForBook(Long bookId) RatingSummary
    }
    class ReviewServiceImpl {
        <<Service>>
        -ReviewRepository repository
        -ReviewMapper mapper
    }
    class ReviewRepository {
        <<interface>>
        +findByBookId(Long bookId, Pageable pageable) Page~Review~
        +countByBookId(Long bookId) long
        +averageRating(Long bookId) Double
    }
    class ReviewMapper {
        <<MapStruct>>
        +toEntity(ReviewRequest request) Review
        +toResponse(Review entity) ReviewResponse
        +updateEntity(ReviewRequest request, Review entity) void
    }
    class Review {
        <<Entity>>
        -Long id
        -Long bookId
        -String reviewerName
        -Integer rating
        -String comment
        -Instant createdAt
        -Instant updatedAt
        -Long version
    }
    class ReviewRequest {
        <<record>>
        Long bookId
        String reviewerName
        Integer rating
        String comment
    }
    class ReviewResponse {
        <<record>>
        Long id
        Long bookId
        String reviewerName
        Integer rating
        String comment
        Instant createdAt
        Instant updatedAt
    }
    class GlobalExceptionHandler {
        <<RestControllerAdvice>>
        +handleNotFound() ProblemDetail
        +handleMethodArgumentNotValid() ProblemDetail
    }
    class ResourceNotFoundException
    class RatingSummary {
        <<record>>
        Long bookId
        double average
        long count
    }

    ReviewController --> ReviewService
    ReviewServiceImpl ..|> ReviewService
    ReviewServiceImpl --> ReviewRepository
    ReviewServiceImpl --> ReviewMapper
    ReviewRepository ..> Review : JpaRepository
    ReviewMapper ..> ReviewRequest
    ReviewMapper ..> ReviewResponse
    GlobalExceptionHandler ..> ResourceNotFoundException : handles
    ReviewServiceImpl ..> RatingSummary
```
