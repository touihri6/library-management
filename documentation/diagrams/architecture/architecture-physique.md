# Architecture physique

L'architecture physique décrit **le déploiement** : quels processus tournent, sur quels ports, et où sont stockées les données.

- Chaque microservice est un **processus indépendant** (une JVM par service Spring Boot, un processus Uvicorn pour Python).
- Chaque service Spring Boot embarque sa **propre base H2 en mémoire** ; le service Python garde ses données **en mémoire**.
- Au démarrage, chaque service **s'enregistre** dans Eureka puis envoie un **heartbeat toutes les 30 s**.
- La Gateway récupère le registre Eureka et **répartit la charge** entre les instances (`lb://`).

```mermaid
flowchart TB
    Client["Client HTTP"]

    subgraph Host["Poste local (localhost)"]
        subgraph P0["JVM :8080"]
            GW["api-gateway"]
        end
        subgraph P1["JVM :8761"]
            EU["eureka-server"]
        end
        subgraph P2["JVM :8081"]
            LIB["book-service"] --- H1[("H2 bookdb")]
        end
        subgraph P3["JVM :8082"]
            MEM["member-service"] --- H2[("H2 memberdb")]
        end
        subgraph P4["JVM :8083"]
            LOAN["loan-service"] --- H3[("H2 loandb")]
        end
        subgraph P5["JVM :8084"]
            REV["review-service"] --- H4[("H2 reviewdb")]
        end
        subgraph P6["JVM :8085"]
            EVT["event-service"] --- H5[("H2 eventdb")]
        end
        subgraph P7["JVM :8086"]
            AUT["author-service"] --- H6[("H2 authordb")]
        end
        subgraph P8["Python Uvicorn :8087"]
            NOT["notification-service"] --- M[("dict en mémoire")]
        end
    end

    Client -->|":8080"| GW
    GW -->|"fetch registry"| EU
    LIB & MEM & LOAN & REV & EVT & AUT & NOT -.->|"register + heartbeat 30 s"| EU
    GW -->|"lb://"| LIB & MEM & LOAN & REV & EVT & AUT & NOT
```

## Ports et noms

| Composant | Technologie | Port | Nom Eureka | Stockage |
|---|---|---|---|---|
| api-gateway | Spring Cloud Gateway | 8080 | API-GATEWAY | – |
| eureka-server | Spring Cloud Netflix Eureka | 8761 | – | registre en mémoire |
| book-service | Spring Boot | 8081 | BOOK-SERVICE | H2 `bookdb` |
| member-service | Spring Boot | 8082 | MEMBER-SERVICE | H2 `memberdb` |
| loan-service | Spring Boot | 8083 | LOAN-SERVICE | H2 `loandb` |
| review-service | Spring Boot | 8084 | REVIEW-SERVICE | H2 `reviewdb` |
| event-service | Spring Boot | 8085 | EVENT-SERVICE | H2 `eventdb` |
| author-service | Spring Boot | 8086 | AUTHOR-SERVICE | H2 `authordb` |
| notification-service | Python / FastAPI | 8087 | NOTIFICATION-SERVICE | mémoire |
