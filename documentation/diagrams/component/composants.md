# Diagramme de composants

Vue des composants déployables et de leurs interfaces. Chaque microservice expose une API REST (`/api/v1/...`), une documentation OpenAPI et un endpoint de santé ; il consomme uniquement l'interface d'enregistrement d'Eureka.

```mermaid
flowchart LR
    Client["Client"]

    subgraph GW["«component» api-gateway"]
        Routes["Routes Web MVC<br/>Path predicates"]
        LB["LoadBalancer client"]
        GWE["Eureka client"]
    end

    subgraph EU["«component» eureka-server"]
        Reg["Registre<br/>/eureka/apps"]
        Dash["Dashboard /"]
    end

    subgraph SB["«component» microservice Spring Boot ×6"]
        API["REST API /api/v1/..."]
        Docs["OpenAPI /v3/api-docs<br/>Swagger UI"]
        Act["Actuator /actuator/health"]
        Core["Controller → Service → Repository"]
        EC["Eureka client"]
        H2[("H2")]
    end

    subgraph PY["«component» notification-service (FastAPI)"]
        PAPI["REST API /api/v1/notifications"]
        PDocs["OpenAPI /v3/api-docs<br/>Swagger UI"]
        PH["/health"]
        PS["NotificationService<br/>dict en mémoire"]
        PEC["py-eureka-client"]
    end

    Client --> Routes
    Routes --> LB
    LB --> GWE
    GWE -->|fetch registry| Reg
    LB -->|HTTP| API
    LB -->|HTTP| PAPI
    API --> Core --> H2
    PAPI --> PS
    EC -->|register / heartbeat| Reg
    PEC -->|register / heartbeat| Reg
    Reg -.->|health check| PH
```

| Composant | Interfaces fournies | Interfaces requises |
|---|---|---|
| api-gateway | `:8080/api/v1/**`, `/actuator/health` | registre Eureka, API REST des services |
| eureka-server | `/eureka/apps`, dashboard `/` | – |
| microservice Spring Boot | `/api/v1/<ressource>`, `/v3/api-docs`, `/swagger-ui.html`, `/actuator/health` | registre Eureka |
| notification-service | `/api/v1/notifications`, `/v3/api-docs`, `/swagger-ui`, `/health` | registre Eureka |
