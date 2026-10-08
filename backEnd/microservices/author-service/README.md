# Author Service

Gestion des auteurs des livres du catalogue.

- Port : `8086`
- Nom Eureka : `AUTHOR-SERVICE`
- Via l'API Gateway : `http://localhost:8080/api/v1/authors`
- Base de données : H2 en mémoire (`jdbc:h2:mem:authordb`), 5 auteurs chargés depuis `data.sql`

## Démarrage
```bash
./mvnw spring-boot:run
```
- Swagger UI : http://localhost:8086/swagger-ui.html
- Console H2 : http://localhost:8086/h2-console
- Santé : http://localhost:8086/actuator/health

## Endpoints
| Méthode | URL | Description |
|---|---|---|
| GET | `/api/v1/authors?lastName=&nationality=&page=0&size=20` | Liste paginée avec filtres (insensibles à la casse) |
| GET | `/api/v1/authors/{id}` | Détail d'un auteur |
| POST | `/api/v1/authors` | Création d'un auteur |
| PUT | `/api/v1/authors/{id}` | Remplacement d'un auteur |
| DELETE | `/api/v1/authors/{id}` | Suppression (204) |

## Tests
```bash
./mvnw test
```
