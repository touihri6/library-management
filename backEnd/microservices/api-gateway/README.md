# API Gateway

Point d'entrée unique du backend (Spring Cloud Gateway Server Web MVC, sur Tomcat). La Gateway s'enregistre dans Eureka et route chaque requête vers une instance disponible du microservice cible grâce à `lb://<NOM-EUREKA>` (répartition de charge côté client).

- Port : `8080`
- Nom Eureka : `API-GATEWAY`
- Santé : http://localhost:8080/actuator/health

## Routes
| Chemin | Cible |
|---|---|
| `/api/v1/books/**` | `lb://BOOK-SERVICE` |
| `/api/v1/notifications/**` | `lb://NOTIFICATION-SERVICE` |

Les en-têtes `X-Forwarded-*` ajoutés par la Gateway (activés par `trusted-proxies`, limité à localhost) permettent aux microservices de renvoyer un en-tête `Location` qui pointe vers la Gateway (`http://localhost:8080/...`).

## Démarrage
À lancer **en dernier**, après le serveur Eureka et les microservices.
```bash
./mvnw spring-boot:run
```

## Tests
```bash
./mvnw test
```
