# Payment Gateway — Java

This repository is a Spring Boot implementation of a Payment Gateway interview challenge.

Summary of the structural decision
---------------------------------
The codebase follows a Hexagonal (Ports & Adapters) architecture to separate the core application logic from delivery and infrastructure concerns. This improves testability and makes it easier to swap adapters (e.g., replace the in-memory store with JPA or swap the bank simulator for a real integration).

Hexagonal structure (intended)
-----------------------------
The canonical layout used by this project is:

├── domain (Entities, Value Objects)
├── application
│   ├── ports
│   │   ├── in      (Input / Driving interfaces — controllers -> application)
│   │   └── out     (Output / Driven interfaces — repositories, external API clients)
│   └── services    (Use case implementations)
├── infrastructure
│   ├── adapters
   │   ├── in      (Controllers, MQ listeners)
   │   └── out     (Persistence adapters, external API clients)
│   └── configuration (Spring configuration / DI)

Mapping to current code
-----------------------
- `domain`: `src/main/java/com/checkout/payment/gateway/model` — DTOs and validation rules (`PaymentRequest`, `PaymentResponse`).
- `application.ports.out`: `src/main/java/com/checkout/payment/gateway/ports` — `BankClient` and `PaymentRepository` interfaces.
- `infrastructure.adapters.out`: `src/main/java/com/checkout/payment/gateway/adapters/bank` and `.../adapters` — `HttpBankClient`, repository implementation.
- `infrastructure.adapters.in`: `src/main/java/com/checkout/payment/gateway/controller` — `PaymentGatewayController`.
- `infrastructure.configuration`: `src/main/java/com/checkout/payment/gateway/configuration` — DI and beans (`RestTemplate`).

API
---
- `POST /payment` — submit a payment. Returns `201 Created` with payment details including `status` (Authorized, Declined, Rejected). When `Rejected`, the response includes `violations` with validation messages.
- `GET /payment/{id}` — fetch stored payment response by id.

Running locally
---------------
1. Start the bank simulator:
```bash
docker-compose up
```
2. Start the application:
```bash
./gradlew bootRun
```
3. Open API docs:
```text
http://localhost:8090/swagger-ui/index.html
```

Testing
-------
Run the Gradle test suite:
```bash
./gradlew test --no-daemon
```

Notes & next steps
------------------
- The in-memory `PaymentsRepository` is used for simplicity in the exercise. Migrating to JPA is straightforward by implementing a JPA adapter that implements the `PaymentRepository` port.
- The bank endpoint is configurable via `src/main/resources/application.properties` (`bank.endpoint`).
- If you want me to physically move Java classes into the exact package layout above (update package declarations and imports), I can do that in a follow-up change.
