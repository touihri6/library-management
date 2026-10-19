# Diagramme de séquence : emprunt puis retour d'un livre

Un adhérent emprunte un livre puis le rend. Avant d'enregistrer l'emprunt, `loan-service` vérifie l'adhérent et le livre en appelant `member-service` et `book-service` avec **OpenFeign** (résolution des noms via Eureka). Il stocke uniquement `bookId` et `memberId`.

```mermaid
sequenceDiagram
    autonumber
    actor B as Bibliothécaire
    participant GW as API Gateway
    participant LS as loan-service
    participant MS as member-service
    participant BS as book-service
    participant DB as MySQL loandb

    B->>GW: POST /api/v1/loans {bookId, memberId, loanDate, dueDate}
    GW->>LS: POST /api/v1/loans
    alt dueDate n'est pas après loanDate
        LS-->>GW: 400 ProblemDetail "Invalid dates"
    else Dates valides
        LS->>MS: GET /api/v1/members/{memberId} (OpenFeign)
        MS-->>LS: adhérent ou 404
        LS->>BS: GET /api/v1/books/{bookId} (OpenFeign)
        BS-->>LS: livre ou 404
        alt Adhérent ou livre introuvable
            LS-->>GW: 404 ProblemDetail "Resource not found"
        else Service indisponible
            LS-->>GW: 503 ProblemDetail "Service unavailable"
        else Aucun exemplaire disponible
            LS-->>GW: 409 ProblemDetail "Invalid operation"
        else Livre disponible
            LS->>LS: status = ONGOING, returnDate = null
            LS->>DB: INSERT loan
            DB-->>LS: id
            LS-->>GW: 201 Created (status ONGOING)
        end
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
