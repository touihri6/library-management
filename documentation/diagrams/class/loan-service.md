# Diagramme de classes : loan-service

Classes du microservice de gestion des emprunts, de la couche HTTP à la couche persistance.

Règles métier : `dueDate` doit être après `loanDate` (400) ; un emprunt est créé `ONGOING` ; un emprunt déjà rendu ne peut pas être rendu à nouveau (409).

```mermaid
classDiagram
    direction TB
    class LoanController {
        <<RestController>>
        -LoanService service
        +findAll(Long memberId, Long bookId, LoanStatus status, Pageable pageable) PageResponse~LoanResponse~
        +findById(Long id) LoanResponse
        +create(LoanRequest request) LoanResponse
        +update(Long id, LoanRequest request) LoanResponse
        +delete(Long id) void
        +returnLoan(Long id) LoanResponse
    }
    class LoanService {
        <<interface>>
        +findAll(Long memberId, Long bookId, LoanStatus status, Pageable pageable) PageResponse~LoanResponse~
        +findById(Long id) LoanResponse
        +create(LoanRequest request) LoanResponse
        +update(Long id, LoanRequest request) LoanResponse
        +delete(Long id) void
        +returnLoan(Long id) LoanResponse
    }
    class LoanServiceImpl {
        <<Service>>
        -LoanRepository repository
        -LoanMapper mapper
    }
    class LoanRepository {
        <<interface>>
        +search(Long memberId, Long bookId, LoanStatus status, Pageable pageable) Page~Loan~
    }
    class LoanMapper {
        <<MapStruct>>
        +toEntity(LoanRequest request) Loan
        +toResponse(Loan entity) LoanResponse
        +updateEntity(LoanRequest request, Loan entity) void
    }
    class Loan {
        <<Entity>>
        -Long id
        -Long bookId
        -Long memberId
        -LocalDate loanDate
        -LocalDate dueDate
        -LocalDate returnDate
        -LoanStatus status
        -Instant createdAt
        -Instant updatedAt
        -Long version
    }
    class LoanRequest {
        <<record>>
        Long bookId
        Long memberId
        LocalDate loanDate
        LocalDate dueDate
    }
    class LoanResponse {
        <<record>>
        Long id
        Long bookId
        Long memberId
        LocalDate loanDate
        LocalDate dueDate
        LocalDate returnDate
        LoanStatus status
        Instant createdAt
        Instant updatedAt
    }
    class GlobalExceptionHandler {
        <<RestControllerAdvice>>
        +handleNotFound() ProblemDetail
        +handleMethodArgumentNotValid() ProblemDetail
    }
    class LoanStatus {
        <<enumeration>>
        ONGOING
        RETURNED
        LATE
    }
    class ResourceNotFoundException
    class InvalidDatesException
    class InvalidOperationException

    LoanController --> LoanService
    LoanServiceImpl ..|> LoanService
    LoanServiceImpl --> LoanRepository
    LoanServiceImpl --> LoanMapper
    LoanRepository ..> Loan : JpaRepository
    LoanMapper ..> LoanRequest
    LoanMapper ..> LoanResponse
    Loan --> LoanStatus
    GlobalExceptionHandler ..> ResourceNotFoundException : handles
    GlobalExceptionHandler ..> InvalidDatesException : handles
    GlobalExceptionHandler ..> InvalidOperationException : handles
```
