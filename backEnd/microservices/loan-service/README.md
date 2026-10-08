# Loan Service

Gestion des emprunts : emprunt et retour des livres. Un emprunt stocke `bookId` et `memberId` comme de simples identifiants ; ce service n'appelle jamais les services des livres ou des adhérents.

- Port : `8083`
- Nom Eureka : `LOAN-SERVICE`
- Via l'API Gateway : `http://localhost:8080/api/v1/loans`
- Base de données : H2 en mémoire (`jdbc:h2:mem:loandb`), 5 emprunts chargés depuis `data.sql`

## Démarrage
```bash
./mvnw spring-boot:run
```
- Swagger UI : http://localhost:8083/swagger-ui.html
- Console H2 : http://localhost:8083/h2-console
- Santé : http://localhost:8083/actuator/health

## Endpoints
| Méthode | URL | Description |
|---|---|---|
| GET | `/api/v1/loans?memberId=&bookId=&status=&page=0&size=20` | Liste paginée avec filtres |
| GET | `/api/v1/loans/{id}` | Détail d'un emprunt |
| POST | `/api/v1/loans` | Emprunter un livre : le statut est toujours `ONGOING` (400 si `dueDate` n'est pas après `loanDate`) |
| PUT | `/api/v1/loans/{id}` | Modifier le livre, l'adhérent et les dates |
| PATCH | `/api/v1/loans/{id}/return` | Rendre le livre : `returnDate` = aujourd'hui, statut `RETURNED` (409 si déjà rendu) |
| DELETE | `/api/v1/loans/{id}` | Suppression (204) |

`status` : `ONGOING`, `RETURNED`, `LATE`. Le statut `LATE` n'est pas calculé automatiquement : il provient des données initiales.

## Tests
```bash
./mvnw test
```
