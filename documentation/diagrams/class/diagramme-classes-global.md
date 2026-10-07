# Diagramme de classes global (modèle du domaine)

Ce diagramme regroupe les entités des sept microservices métier et leurs énumérations.

Les traits pointillés (`bookId`, `memberId`) sont des **références logiques** : il n'existe ni clé étrangère ni appel réseau entre services. Chaque service ne stocke que l'identifiant.

```mermaid
classDiagram
    direction LR

    class Book {
        <<book-service>>
        Long id
        String title
        String author
        String isbn
        Genre genre
        Integer publicationYear
        BigDecimal price
        int availableCopies
    }
    class Genre {
        <<enumeration>>
        FICTION
        NON_FICTION
        SCIENCE_FICTION
        FANTASY
        MYSTERY
        BIOGRAPHY
        HISTORY
        SCIENCE
        TECHNOLOGY
    }

    class Member {
        <<member-service>>
        Long id
        String firstName
        String lastName
        String email
        String phone
        MembershipType membershipType
        LocalDate joinDate
    }
    class MembershipType {
        <<enumeration>>
        STUDENT
        STANDARD
        PREMIUM
    }

    class Loan {
        <<loan-service>>
        Long id
        Long bookId
        Long memberId
        LocalDate loanDate
        LocalDate dueDate
        LocalDate returnDate
        LoanStatus status
    }
    class LoanStatus {
        <<enumeration>>
        ONGOING
        RETURNED
        LATE
    }

    class Review {
        <<review-service>>
        Long id
        Long bookId
        String reviewerName
        Integer rating
        String comment
    }

    class Event {
        <<event-service>>
        Long id
        String title
        String description
        EventType type
        LocalDateTime eventDate
        String location
        Integer capacity
    }
    class EventType {
        <<enumeration>>
        READING_CLUB
        WORKSHOP
        CONFERENCE
        BOOK_SIGNING
    }

    class Author {
        <<author-service>>
        Long id
        String firstName
        String lastName
        String nationality
        Integer birthYear
        String biography
    }

    class Notification {
        <<notification-service>>
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

    Book --> Genre
    Member --> MembershipType
    Loan --> LoanStatus
    Event --> EventType
    Notification --> NotificationType

    Loan ..> Book : bookId
    Loan ..> Member : memberId
    Review ..> Book : bookId
```

Toutes les entités Spring Boot possèdent aussi les champs techniques `createdAt`, `updatedAt` (audit JPA) et `version` (verrouillage optimiste). Ils sont omis ici pour la lisibilité.
