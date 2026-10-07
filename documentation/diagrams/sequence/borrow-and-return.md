# Diagramme de séquence : emprunt puis retour d'un livre

Un adhérent emprunte un livre puis le rend. `loan-service` ne vérifie pas l'existence du livre ou de l'adhérent : il n'appelle aucun autre microservice et stocke uniquement `bookId` et `memberId`.

```mermaid
sequenceDiagram
    autonumber
    actor B as Bibliothécaire
    participant GW as API Gateway
    participant LS as loan-service
    participant DB as H2 loandb

    B->>GW: POST /api/v1/loans {bookId, memberId, loanDate, dueDate}
    GW->>LS: POST /api/v1/loans
    alt dueDate n'est pas après loanDate
        LS-->>GW: 400 ProblemDetail "Invalid dates"
    else Dates valides
        LS->>LS: status = ONGOING, returnDate = null
        LS->>DB: INSERT loan
        DB-->>LS: id
        LS-->>GW: 201 Created (status ONGOING)
    end
    GW-->>B: réponse

    Note over B,DB: Quelques jours plus tard

    B->>GW: PATCH /api/v1/loans/{id}/return
    GW->>LS: PATCH /api/v1/loans/{id}/return
    LS->>DB: SELECT loan
    alt Emprunt introuvable
        LS-->>GW: 404 ProblemDetail
    else Déjà rendu (RETURNED)
        LS-->>GW: 409 ProblemDetail "Invalid operation"
    else ONGOING ou LATE
        LS->>LS: returnDate = aujourd'hui, status = RETURNED
        LS->>DB: UPDATE loan
        LS-->>GW: 200 OK (status RETURNED)
    end
    GW-->>B: réponse
```
