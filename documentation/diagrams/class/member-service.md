# Diagramme de classes : member-service

Classes du microservice de gestion des adhérents, de la couche HTTP à la couche persistance.

Règle métier : l'email est unique (409 sinon) ; un adhérent peut conserver son propre email lors d'une mise à jour.

```mermaid
classDiagram
    direction TB
    class MemberController {
        <<RestController>>
        -MemberService service
        +findAll(String lastName, MembershipType membershipType, Pageable pageable) PageResponse~MemberResponse~
        +findById(Long id) MemberResponse
        +create(MemberRequest request) MemberResponse
        +update(Long id, MemberRequest request) MemberResponse
        +delete(Long id) void
    }
    class MemberService {
        <<interface>>
        +findAll(String lastName, MembershipType membershipType, Pageable pageable) PageResponse~MemberResponse~
        +findById(Long id) MemberResponse
        +create(MemberRequest request) MemberResponse
        +update(Long id, MemberRequest request) MemberResponse
        +delete(Long id) void
    }
    class MemberServiceImpl {
        <<Service>>
        -MemberRepository repository
        -MemberMapper mapper
    }
    class MemberRepository {
        <<interface>>
        +existsByEmail(String email) boolean
        +existsByEmailAndIdNot(String email, Long id) boolean
        +search(String lastName, MembershipType membershipType, Pageable pageable) Page~Member~
    }
    class MemberMapper {
        <<MapStruct>>
        +toEntity(MemberRequest request) Member
        +toResponse(Member entity) MemberResponse
        +updateEntity(MemberRequest request, Member entity) void
    }
    class Member {
        <<Entity>>
        -Long id
        -String firstName
        -String lastName
        -String email
        -String phone
        -MembershipType membershipType
        -LocalDate joinDate
        -Instant createdAt
        -Instant updatedAt
        -Long version
    }
    class MemberRequest {
        <<record>>
        String firstName
        String lastName
        String email
        String phone
        MembershipType membershipType
        LocalDate joinDate
    }
    class MemberResponse {
        <<record>>
        Long id
        String firstName
        String lastName
        String email
        String phone
        MembershipType membershipType
        LocalDate joinDate
        Instant createdAt
        Instant updatedAt
    }
    class GlobalExceptionHandler {
        <<RestControllerAdvice>>
        +handleNotFound() ProblemDetail
        +handleMethodArgumentNotValid() ProblemDetail
    }
    class MembershipType {
        <<enumeration>>
        STUDENT
        STANDARD
        PREMIUM
    }
    class ResourceNotFoundException
    class DuplicateResourceException

    MemberController --> MemberService
    MemberServiceImpl ..|> MemberService
    MemberServiceImpl --> MemberRepository
    MemberServiceImpl --> MemberMapper
    MemberRepository ..> Member : JpaRepository
    MemberMapper ..> MemberRequest
    MemberMapper ..> MemberResponse
    Member --> MembershipType
    GlobalExceptionHandler ..> ResourceNotFoundException : handles
    GlobalExceptionHandler ..> DuplicateResourceException : handles
```
