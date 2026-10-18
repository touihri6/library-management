# Review Service

Gestion des avis sur les livres : note de 1 à 5 et commentaire. Un avis stocke `bookId` comme un simple identifiant ; ce service n'appelle jamais le service des livres.

- Port : `8084`
- Nom Eureka : `REVIEW-SERVICE`
- Via l'API Gateway : `http://localhost:8080/api/v1/reviews`
- Base de données : MySQL (`jdbc:mysql://localhost:3306/reviewdb`, créée automatiquement au démarrage ; utilisateur `root`, mot de passe vide). Les données sont conservées entre les redémarrages.

## Démarrage
```bash
./mvnw spring-boot:run
```
- Swagger UI : http://localhost:8084/swagger-ui.html
- Santé : http://localhost:8084/actuator/health

## Endpoints
| Méthode | URL | Description |
|---|---|---|
| GET | `/api/v1/reviews?bookId=&page=0&size=20` | Liste paginée, filtre optionnel par livre |
| GET | `/api/v1/reviews/{id}` | Détail d'un avis |
| GET | `/api/v1/reviews/books/{bookId}/average` | `{ bookId, average, count }`, moyenne arrondie à une décimale (0.0 sans avis) |
| POST | `/api/v1/reviews` | Création (400 si la note n'est pas entre 1 et 5) |
| PUT | `/api/v1/reviews/{id}` | Remplacement d'un avis |
| DELETE | `/api/v1/reviews/{id}` | Suppression (204) |

## Tests
```bash
./mvnw test
```
