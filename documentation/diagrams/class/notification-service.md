# Diagramme de classes : notification-service

Le microservice Python (FastAPI) n'a pas de base de données : les notifications sont conservées dans un dictionnaire en mémoire par `NotificationService`. Le routeur reçoit le service par injection de dépendance (`Depends(get_service)`), ce qui permet de le remplacer dans les tests.

```mermaid
classDiagram
    direction TB

    class NotificationRouter {
        <<APIRouter /api/v1/notifications>>
        +find_all(recipient, read) list~Notification~
        +find_by_id(id) Notification
        +create(data) Notification
        +mark_as_read(id) Notification
        +delete(id) None
    }
    class HealthRouter {
        <<APIRouter>>
        +health() dict
    }
    class NotificationService {
        -dict~int, Notification~ _store
        -int _next_id
        +find_all(recipient, read) list~Notification~
        +find_by_id(id) Notification
        +create(data) Notification
        +mark_as_read(id) Notification
        +delete(id) bool
    }
    class NotificationCreate {
        <<BaseModel>>
        str recipient
        str message
        NotificationType type
    }
    class Notification {
        <<BaseModel>>
        int id
        str recipient
        str message
        NotificationType type
        bool read
        datetime createdAt
    }
    class NotificationType {
        <<enumeration>>
        LOAN_REMINDER
        EVENT_ANNOUNCEMENT
        GENERAL
    }
    class EurekaModule {
        <<module eureka.py>>
        +start_eureka(port) None
        +stop_eureka() None
    }
    class FastAPIApp {
        <<main.py>>
        +lifespan()
    }

    FastAPIApp --> NotificationRouter
    FastAPIApp --> HealthRouter
    FastAPIApp --> EurekaModule : lifespan
    NotificationRouter --> NotificationService : Depends
    NotificationService --> Notification : stores
    NotificationRouter ..> NotificationCreate
    Notification --> NotificationType
    NotificationCreate --> NotificationType
```
