# Architecture logique

L'architecture logique décrit **l'organisation fonctionnelle** du système, indépendamment des machines qui l'exécutent.

- Le **client** (navigateur, Postman, futur frontEnd) ne connaît qu'une seule adresse : l'**API Gateway**.
- La Gateway interroge l'**annuaire Eureka** pour trouver une instance disponible du microservice cible, puis lui transmet la requête.
- Chaque **microservice métier** est autonome : il possède son propre modèle et ses propres données (*database per service*).
- **Aucun appel entre microservices** : les références entre domaines (`bookId`, `memberId`) sont de simples identifiants.

```mermaid
flowchart TB
    Client["Client<br/>navigateur / Postman / frontEnd"]

    subgraph Edge["Couche d'accès"]
        GW["API Gateway<br/>Spring Cloud Gateway"]
    end

    subgraph Discovery["Couche découverte"]
        EU["Eureka Server<br/>annuaire des services"]
    end

    subgraph Business["Couche métier"]
        LIB["book-service<br/>Livres"]
        MEM["member-service<br/>Adhérents"]
        LOAN["loan-service<br/>Emprunts"]
        REV["review-service<br/>Avis"]
        EVT["event-service<br/>Événements"]
        AUT["author-service<br/>Auteurs"]
        NOT["notification-service<br/>Notifications"]
    end

    subgraph Data["Couche données"]
        DB1[("bookdb")]
        DB2[("memberdb")]
        DB3[("loandb")]
        DB4[("reviewdb")]
        DB5[("eventdb")]
        DB6[("authordb")]
        MEMO[("mémoire")]
    end

    Client -->|HTTP REST| GW
    GW -.->|découverte lb://| EU
    GW --> LIB & MEM & LOAN & REV & EVT & AUT & NOT
    LIB --> DB1
    MEM --> DB2
    LOAN --> DB3
    REV --> DB4
    EVT --> DB5
    AUT --> DB6
    NOT --> MEMO
```

## Architecture interne d'un microservice Spring Boot

Les six microservices Spring Boot suivent la même architecture en couches :

```mermaid
flowchart LR
    HTTP["Requête HTTP"] --> C["Controller<br/>DTO + @Valid"]
    C --> S["Service<br/>interface + impl"]
    S --> M["Mapper<br/>MapStruct"]
    S --> R["Repository<br/>Spring Data JPA"]
    R --> DB[("H2")]
    C -. erreurs .-> H["GlobalExceptionHandler<br/>ProblemDetail RFC 9457"]
```

| Couche | Responsabilité |
|---|---|
| Controller | Mapping HTTP, validation des entrées, codes de statut |
| Service | Règles métier, transactions, conversion DTO ⇄ entité |
| Repository | Accès aux données (CRUD, requêtes JPQL) |
| Mapper | Conversion DTO ⇄ entité générée à la compilation |
| Exception handler | Réponses d'erreur normalisées 400 / 404 / 409 |
