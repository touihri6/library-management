# Eureka Server

Annuaire de découverte de services du Library Management System. Tous les microservices et l'API Gateway s'y enregistrent et envoient un heartbeat toutes les 30 s.

- Port : `8761`
- Dashboard : http://localhost:8761
- Adresse du registre pour les clients : `http://localhost:8761/eureka`

## Démarrage
À lancer **en premier**, avant tous les autres microservices.
```bash
./mvnw spring-boot:run
```

## Tests
```bash
./mvnw test
```
