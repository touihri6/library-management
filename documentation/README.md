# Documentation – Library Management System

Index de la documentation de conception. Tous les diagrammes sont écrits en **Mermaid** et s'affichent directement sur GitHub.

## Architecture

| Diagramme | Description |
|---|---|
| [Architecture logique](diagrams/architecture/architecture-logique.md) | Couches fonctionnelles, rôle de chaque service, architecture interne d'un microservice |
| [Architecture physique](diagrams/architecture/architecture-physique.md) | Processus, ports, stockage, enregistrement Eureka |

## Diagrammes de classes

| Diagramme | Description |
|---|---|
| [Diagramme de classes global](diagrams/class/diagramme-classes-global.md) | Les 7 entités du domaine et leurs énumérations |
| [book-service](diagrams/class/book-service.md) | Livres |
| [member-service](diagrams/class/member-service.md) | Adhérents |
| [loan-service](diagrams/class/loan-service.md) | Emprunts |
| [review-service](diagrams/class/review-service.md) | Avis |
| [event-service](diagrams/class/event-service.md) | Événements |
| [author-service](diagrams/class/author-service.md) | Auteurs |
| [notification-service](diagrams/class/notification-service.md) | Notifications (Python) |

## Diagrammes de séquence

| Diagramme | Description |
|---|---|
| [Enregistrement Eureka](diagrams/sequence/eureka-registration.md) | Register, heartbeat, fetch registry, éviction |
| [Routage Gateway](diagrams/sequence/gateway-routing.md) | Client → Gateway → Eureka → microservice |
| [Création d'un livre](diagrams/sequence/create-book.md) | Parcours d'une requête dans les couches (201 / 400 / 409) |
| [Emprunt et retour](diagrams/sequence/borrow-and-return.md) | POST d'un emprunt puis PATCH de retour |
| [Gestion des erreurs](diagrams/sequence/error-handling.md) | 400 / 404 / 409 en ProblemDetail |

## Autres diagrammes

| Diagramme | Description |
|---|---|
| [Cas d'utilisation](diagrams/use-case/cas-utilisation.md) | Acteurs Adhérent, Bibliothécaire, Administrateur |
| [Composants](diagrams/component/composants.md) | Composants déployables et interfaces |
