# Diagramme de séquence : gestion des erreurs

Dans chaque microservice Spring Boot, les exceptions sont interceptées par `GlobalExceptionHandler` et transformées en réponses **ProblemDetail** (RFC 9457) : `type`, `title`, `status`, `detail`, et `errors` pour les erreurs de validation.

```mermaid
sequenceDiagram
    autonumber
    actor C as Client
    participant CT as Controller
    participant SV as Service
    participant EH as GlobalExceptionHandler

    C->>CT: requête HTTP

    alt Validation @Valid échouée
        CT->>EH: MethodArgumentNotValidException
        EH-->>C: 400 {title: "Validation failed", errors: {champ: message}}
    else Ressource absente
        CT->>SV: findById(999)
        SV->>EH: ResourceNotFoundException
        EH-->>C: 404 {title: "Resource not found"}
    else Conflit métier
        CT->>SV: create(request)
        SV->>EH: DuplicateResourceException / InvalidOperationException
        EH-->>C: 409 {title: "Duplicate resource" / "Invalid operation"}
    else Modification concurrente
        CT->>SV: update(id, request)
        SV->>EH: ObjectOptimisticLockingFailureException
        EH-->>C: 409 {title: "Concurrent modification"}
    else Succès
        SV-->>CT: DTO
        CT-->>C: 200 / 201 / 204
    end
```

Exemple de réponse 400 :

```json
{
  "type": "about:blank",
  "title": "Validation failed",
  "status": 400,
  "detail": "One or more fields are invalid",
  "instance": "/api/v1/members",
  "errors": { "email": "must be a well-formed email address" }
}
```

Le microservice Python renvoie le format FastAPI : `404 {"detail": "Notification 999 not found"}` et `422` pour un payload invalide.
