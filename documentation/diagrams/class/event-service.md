# Diagramme de classes : event-service

Classes du microservice de gestion des événements, de la couche HTTP à la couche persistance.

Règles métier : la date doit être dans le futur et la capacité strictement positive (400 sinon).

```mermaid
classDiagram
    direction TB
    class EventController {
        <<RestController>>
        -EventService service
        +findAll(EventType type, Pageable pageable) PageResponse~EventResponse~
        +findById(Long id) EventResponse
        +create(EventRequest request) EventResponse
        +update(Long id, EventRequest request) EventResponse
        +delete(Long id) void
    }
    class EventService {
        <<interface>>
        +findAll(EventType type, Pageable pageable) PageResponse~EventResponse~
        +findById(Long id) EventResponse
        +create(EventRequest request) EventResponse
        +update(Long id, EventRequest request) EventResponse
        +delete(Long id) void
    }
    class EventServiceImpl {
        <<Service>>
        -EventRepository repository
        -EventMapper mapper
    }
    class EventRepository {
        <<interface>>
        +search(EventType type, Pageable pageable) Page~Event~
    }
    class EventMapper {
        <<MapStruct>>
        +toEntity(EventRequest request) Event
        +toResponse(Event entity) EventResponse
        +updateEntity(EventRequest request, Event entity) void
    }
    class Event {
        <<Entity>>
        -Long id
        -String title
        -String description
        -EventType type
        -LocalDateTime eventDate
        -String location
        -Integer capacity
        -Instant createdAt
        -Instant updatedAt
        -Long version
    }
    class EventRequest {
        <<record>>
        String title
        String description
        EventType type
        LocalDateTime eventDate
        String location
        Integer capacity
    }
    class EventResponse {
        <<record>>
        Long id
        String title
        String description
        EventType type
        LocalDateTime eventDate
        String location
        Integer capacity
        Instant createdAt
        Instant updatedAt
    }
    class GlobalExceptionHandler {
        <<RestControllerAdvice>>
        +handleNotFound() ProblemDetail
        +handleMethodArgumentNotValid() ProblemDetail
    }
    class EventType {
        <<enumeration>>
        READING_CLUB
        WORKSHOP
        CONFERENCE
        BOOK_SIGNING
    }
    class ResourceNotFoundException

    EventController --> EventService
    EventServiceImpl ..|> EventService
    EventServiceImpl --> EventRepository
    EventServiceImpl --> EventMapper
    EventRepository ..> Event : JpaRepository
    EventMapper ..> EventRequest
    EventMapper ..> EventResponse
    Event --> EventType
    GlobalExceptionHandler ..> ResourceNotFoundException : handles
```
