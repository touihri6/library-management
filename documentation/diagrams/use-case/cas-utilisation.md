# Diagramme de cas d'utilisation

Mermaid n'a pas de type « use case » natif : le diagramme utilise un `flowchart` où les acteurs sont à gauche et les cas d'utilisation (ovales) sont regroupés par microservice.

| Acteur | Rôle |
|---|---|
| Adhérent | Consulte le catalogue, les auteurs et les événements ; donne des avis ; lit ses notifications |
| Bibliothécaire | Gère le catalogue, les auteurs, les adhérents, les emprunts, les événements ; envoie des notifications |
| Administrateur | Supervise l'infrastructure : tableau de bord Eureka, routes de la Gateway, santé des services |

```mermaid
flowchart LR
    A(["👤 Adhérent"])
    B(["👤 Bibliothécaire"])
    AD(["👤 Administrateur"])

    subgraph SYS["Library Management System"]
        direction TB
        subgraph Catalogue["book-service / author-service"]
            UC1(["Consulter / rechercher les livres"])
            UC2(["Gérer les livres"])
            UC3(["Consulter les auteurs"])
            UC4(["Gérer les auteurs"])
        end
        subgraph Adherents["member-service"]
            UC5(["Gérer les adhérents"])
        end
        subgraph Emprunts["loan-service"]
            UC6(["Enregistrer un emprunt"])
            UC7(["Enregistrer un retour"])
            UC8(["Consulter les emprunts"])
        end
        subgraph Avis["review-service"]
            UC9(["Donner un avis"])
            UC10(["Consulter la note moyenne"])
        end
        subgraph Evenements["event-service"]
            UC11(["Consulter les événements"])
            UC12(["Gérer les événements"])
        end
        subgraph Notifications["notification-service"]
            UC13(["Lire ses notifications"])
            UC14(["Envoyer une notification"])
        end
        subgraph Infra["eureka-server / api-gateway"]
            UC15(["Superviser les services"])
        end
    end

    A --- UC1 & UC3 & UC8 & UC9 & UC10 & UC11 & UC13
    B --- UC2 & UC4 & UC5 & UC6 & UC7 & UC8 & UC12 & UC14
    AD --- UC15
```
