# Diagramme de séquence : enregistrement dans Eureka

Cycle de vie d'une instance dans l'annuaire Eureka : enregistrement au démarrage, renouvellement par heartbeat, récupération du registre par la Gateway, puis retrait de l'instance.

```mermaid
sequenceDiagram
    autonumber
    participant MS as Microservice (ex. member-service :8082)
    participant EU as Eureka Server :8761
    participant GW as API Gateway :8080

    MS->>EU: POST /eureka/apps/MEMBER-SERVICE (register : host, port, healthCheckUrl)
    EU-->>MS: 204 No Content
    Note over EU: Instance UP dans le registre

    loop Toutes les 30 s
        MS->>EU: PUT /eureka/apps/MEMBER-SERVICE/{instanceId} (heartbeat)
        EU-->>MS: 200 OK
    end

    loop Toutes les 30 s
        GW->>EU: GET /eureka/apps/delta (fetch registry)
        EU-->>GW: liste des instances UP
        Note over GW: Cache local utilisé par lb://
    end

    alt Arrêt propre du service
        MS->>EU: DELETE /eureka/apps/MEMBER-SERVICE/{instanceId}
        EU-->>MS: 200 OK
    else Plus de heartbeat pendant 90 s
        Note over EU: Éviction de l'instance
    end
```
