# Event Service

Gestion des événements organisés par la bibliothèque : clubs de lecture, ateliers, conférences et séances de dédicaces.

- Port : `8085`
- Nom Eureka : `EVENT-SERVICE`
- Via l'API Gateway : `http://localhost:8080/api/v1/events`
- Base de données : H2 en mémoire (`jdbc:h2:mem:eventdb`), 5 événements chargés depuis `data.sql`

## Démarrage
```bash
./mvnw spring-boot:run
```
- Swagger UI : http://localhost:8085/swagger-ui.html
- Console H2 : http://localhost:8085/h2-console
- Santé : http://localhost:8085/actuator/health

## Endpoints
| Méthode | URL | Description |
|---|---|---|
| GET | `/api/v1/events?type=&page=0&size=20` | Liste paginée triée par date, filtre optionnel par type |
| GET | `/api/v1/events/{id}` | Détail d'un événement |
| POST | `/api/v1/events` | Création (400 si `eventDate` est dans le passé ou si `capacity` ≤ 0) |
| PUT | `/api/v1/events/{id}` | Remplacement d'un événement |
| DELETE | `/api/v1/events/{id}` | Suppression (204) |

`type` : `READING_CLUB`, `WORKSHOP`, `CONFERENCE`, `BOOK_SIGNING`.

## Tests
```bash
./mvnw test
```
