# Member Service

Gestion des adhérents de la bibliothèque.

- Port : `8082`
- Nom Eureka : `MEMBER-SERVICE`
- Via l'API Gateway : `http://localhost:8080/api/v1/members`
- Base de données : H2 en mémoire (`jdbc:h2:mem:memberdb`), 5 adhérents chargés depuis `data.sql`

## Démarrage
```bash
./mvnw spring-boot:run
```
- Swagger UI : http://localhost:8082/swagger-ui.html
- Console H2 : http://localhost:8082/h2-console
- Santé : http://localhost:8082/actuator/health

## Endpoints
| Méthode | URL | Description |
|---|---|---|
| GET | `/api/v1/members?lastName=&membershipType=&page=0&size=20` | Liste paginée avec filtres |
| GET | `/api/v1/members/{id}` | Détail d'un adhérent |
| POST | `/api/v1/members` | Création (201, 409 si l'email existe déjà) |
| PUT | `/api/v1/members/{id}` | Remplacement d'un adhérent |
| DELETE | `/api/v1/members/{id}` | Suppression (204) |

`membershipType` : `STUDENT`, `STANDARD`, `PREMIUM`.

## Tests
```bash
./mvnw test
```
