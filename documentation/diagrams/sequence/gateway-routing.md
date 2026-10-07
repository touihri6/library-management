# Diagramme de séquence : routage par l'API Gateway

Le client appelle uniquement la Gateway. Celle-ci choisit la route grâce au prédicat `Path`, résout le nom logique `lb://MEMBER-SERVICE` en une adresse réelle à partir du registre Eureka, puis transmet la requête.

```mermaid
sequenceDiagram
    autonumber
    actor C as Client
    participant GW as API Gateway :8080
    participant LB as Load balancer (Gateway)
    participant EU as Eureka Server :8761
    participant MS as member-service :8082

    C->>GW: GET /api/v1/members/1
    GW->>GW: Prédicat Path=/api/v1/members/** → route member-service
    GW->>LB: résoudre lb://MEMBER-SERVICE
    LB->>EU: instances de MEMBER-SERVICE (cache rafraîchi toutes les 30 s)
    EU-->>LB: [localhost:8082]
    LB-->>GW: http://localhost:8082
    GW->>MS: GET /api/v1/members/1
    MS-->>GW: 200 OK + MemberResponse (JSON)
    GW-->>C: 200 OK + MemberResponse (JSON)

    opt Aucune instance disponible
        LB-->>GW: aucune instance
        GW-->>C: 503 Service Unavailable
    end
```
