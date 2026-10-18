# Architecture physique

L'architecture physique décrit **le déploiement** : quels processus tournent, sur quels ports, et où sont stockées les données.

- Chaque microservice est un **processus indépendant** (une JVM par service Spring Boot, un processus Uvicorn pour Python).
- Chaque service Spring Boot a sa **propre base MySQL** sur un serveur MySQL local (`localhost:3306`), avec des données **conservées entre les redémarrages** ; le service Python garde ses données **en mémoire**.
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
            LIB["book-service"] --- DB1[("MySQL bookdb")]
        end
        subgraph P3["JVM :8082"]
            MEM["member-service"] --- DB2[("MySQL memberdb")]
        end
        subgraph P4["JVM :8083"]
            LOAN["loan-service"] --- DB3[("MySQL loandb")]
        end
        subgraph P5["JVM :8084"]
            REV["review-service"] --- DB4[("MySQL reviewdb")]
        end
        subgraph P6["JVM :8085"]
            EVT["event-service"] --- DB5[("MySQL eventdb")]
        end
        subgraph P7["JVM :8086"]
            AUT["author-service"] --- DB6[("MySQL authordb")]
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
| book-service | Spring Boot | 8081 | BOOK-SERVICE | MySQL `bookdb` |
| member-service | Spring Boot | 8082 | MEMBER-SERVICE | MySQL `memberdb` |
| loan-service | Spring Boot | 8083 | LOAN-SERVICE | MySQL `loandb` |
| review-service | Spring Boot | 8084 | REVIEW-SERVICE | MySQL `reviewdb` |
| event-service | Spring Boot | 8085 | EVENT-SERVICE | MySQL `eventdb` |
| author-service | Spring Boot | 8086 | AUTHOR-SERVICE | MySQL `authordb` |
| notification-service | Python / FastAPI | 8087 | NOTIFICATION-SERVICE | mémoire |
