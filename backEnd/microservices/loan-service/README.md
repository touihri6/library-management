# Loan Service

Gestion des emprunts : emprunt et retour des livres. Un emprunt stocke `bookId` et `memberId` comme de simples identifiants. Le livre et l'adhérent sont lus dans `book-service` et `member-service` avec **OpenFeign** (voir plus bas).

- Port : `8083`
- Nom Eureka : `LOAN-SERVICE`
- Via l'API Gateway : `http://localhost:8080/api/v1/loans`
- Base de données : MySQL (`jdbc:mysql://localhost:3306/loandb`, créée automatiquement au démarrage ; utilisateur `root`, mot de passe vide). Les données sont conservées entre les redémarrages.

## Démarrage
```bash
./mvnw spring-boot:run
```
- Swagger UI : http://localhost:8083/swagger-ui.html
- Santé : http://localhost:8083/actuator/health

## Endpoints
| Méthode | URL | Description |
|---|---|---|
| GET | `/api/v1/loans?memberId=&bookId=&status=&page=0&size=20` | Liste paginée avec filtres |
| GET | `/api/v1/loans/{id}` | Détail d'un emprunt |
| GET | `/api/v1/loans/{id}/details` | Emprunt avec le livre et l'adhérent, lus via OpenFeign |
| POST | `/api/v1/loans` | Emprunter un livre : le statut est toujours `ONGOING` (400 si `dueDate` n'est pas après `loanDate`, 404 si le livre ou l'adhérent n'existe pas, 409 si le livre n'a plus d'exemplaire disponible) |
| PUT | `/api/v1/loans/{id}` | Modifier le livre, l'adhérent et les dates (404 si le livre ou l'adhérent n'existe pas) |
| PATCH | `/api/v1/loans/{id}/return` | Rendre le livre : `returnDate` = aujourd'hui, statut `RETURNED` (409 si déjà rendu) |
| DELETE | `/api/v1/loans/{id}` | Suppression (204) |

`status` : `ONGOING`, `RETURNED`, `LATE`. Le statut `LATE` n'est pas calculé automatiquement : il provient des données initiales.

## Communication avec OpenFeign

`loan-service` appelle `book-service` et `member-service` avec deux clients OpenFeign déclarés dans `com.example.loan.client` :

| Client | Nom Eureka | Appel |
|---|---|---|
| `BookClient` | `BOOK-SERVICE` | `GET /api/v1/books/{id}` |
| `MemberClient` | `MEMBER-SERVICE` | `GET /api/v1/members/{id}` |

- `@EnableFeignClients` sur `LoanServiceApplication` active les clients ; Eureka résout le nom du service et Spring Cloud LoadBalancer choisit l'instance (aucune URL en dur, la Gateway n'est pas utilisée).
- Utilisé pour **valider** un emprunt (`POST` / `PUT`) et pour **enrichir** `GET /api/v1/loans/{id}/details`.
- Erreurs : un 404 distant devient `404 "Book … not found"` / `"Member … not found"` ; un service arrêté ou absent d'Eureka donne `503 Service unavailable` (`ProblemDetail`).
- `book-service` et `member-service` doivent donc être démarrés et enregistrés dans Eureka. Chaque appel est tracé dans la console (`logger-level=basic`).

## Tests
```bash
./mvnw test
```
Les clients Feign sont remplacés par des mocks (`@MockitoBean`) : les tests ne nécessitent ni Eureka ni les autres services.
