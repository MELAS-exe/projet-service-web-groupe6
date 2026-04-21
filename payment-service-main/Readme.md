# 💳 Payment Microservice

## 🧾 Présentation

Le **Payment Service** est un microservice indépendant d’un système de type ride-hailing.  
Il est responsable de la gestion des paiements, du traitement des transactions et de la publication d’événements métier.

---

## 🧱 Architecture

Le service suit une architecture en couches :

- **Controller** → Exposition des API REST
- **Service** → Logique métier de paiement
- **Repository** → Accès base de données
- **Entity** → Modèle de données
- **DTO + Mapper** → Séparation API / modèle interne
- **Event Producer** → Publication d’événements métier
- **Exception Handler** → Gestion centralisée des erreurs

---

## ⚙️ Technologies utilisées

- Java 17
- Spring Boot
- Spring Data JPA
- PostgreSQL
- Maven
- REST API
- Architecture event-driven (simulation Kafka)

---

## 🗄️ Base de données

Base de données : `payment_db`

### Table : payment

| Champ | Type | Description |
|------|------|-------------|
| id | Long | Identifiant |
| rideId | Long | ID de la course |
| userId | Long | ID utilisateur |
| amount | Double | Montant |
| currency | String | Devise |
| method | String | Méthode de paiement |
| status | String | SUCCESS / FAILED |
| transactionRef | String | Référence transaction |
| createdAt | LocalDateTime | Date création |

---

## 🚀 API Endpoints

### 🔹 Créer un paiement

```http
POST /api/payments
{
  "rideId": 101,
  "userId": 20,
  "amount": 2500.0,
  "currency": "XOF",
  "method": "MOBILE_MONEY"
}


🔹 Récupérer un paiement
GET /api/payments/{id}
