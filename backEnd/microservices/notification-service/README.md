# Notification Service (Python / FastAPI)

Gestion des notifications envoyées aux adhérents : rappels d'emprunt, annonces d'événements, messages généraux. Les notifications sont conservées **en mémoire** : il n'y a pas de base de données, et 3 notifications d'exemple sont créées au démarrage.

- Port : `8087`
- Nom Eureka : `NOTIFICATION-SERVICE` (enregistrement avec `py-eureka-client`, health check sur `/health`)
- Via l'API Gateway : `http://localhost:8080/api/v1/notifications`

## Prérequis
- Python 3.10+

## Installation et démarrage
```bash
python -m pip install -r requirements.txt
python -m uvicorn app.main:app --port 8087
```
- Swagger UI : http://localhost:8087/swagger-ui
- OpenAPI JSON : http://localhost:8087/v3/api-docs
- Santé : http://localhost:8087/health

Démarrez le serveur Eureka **avant** ce service : si Eureka n'est pas joignable au démarrage, l'enregistrement n'est pas retenté.

Variables d'environnement :

| Variable | Valeur par défaut | Rôle |
|---|---|---|
| `PORT` | `8087` | Port déclaré dans Eureka (doit correspondre au `--port` d'uvicorn) |
| `EUREKA_SERVER` | `http://localhost:8761/eureka` | Registre Eureka |
| `EUREKA_ENABLED` | `true` | `false` désactive l'enregistrement (utilisé par les tests) |

## Endpoints
| Méthode | URL | Description |
|---|---|---|
| GET | `/api/v1/notifications?recipient=&read=` | Liste avec filtres optionnels |
| GET | `/api/v1/notifications/{id}` | Détail d'une notification (404 si inconnue) |
| POST | `/api/v1/notifications` | Création (201, non lue) |
| PATCH | `/api/v1/notifications/{id}/read` | Marquer comme lue |
| DELETE | `/api/v1/notifications/{id}` | Suppression (204) |
| GET | `/health` | `{"status": "UP"}` |

`type` : `LOAN_REMINDER`, `EVENT_ANNOUNCEMENT`, `GENERAL`.

## Tests
```bash
python -m pip install -r requirements-dev.txt
python -m pytest
```
