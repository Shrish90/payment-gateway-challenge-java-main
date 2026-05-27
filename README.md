# Payment Gateway — Java

A production-ready Spring Boot implementation of a Payment Gateway with comprehensive validation, error handling, and hexagonal architecture for maintainability and testability.

## Architecture Overview

The codebase follows a **Hexagonal (Ports & Adapters) architecture** to separate core business logic from delivery mechanisms and external integrations. This design enables:

- **Testability**: Ports are abstracted, enabling mock implementations for testing
- **Maintainability**: Core logic remains independent of framework details
- **Extensibility**: Easy to swap adapters (e.g., add JPA without changing service logic)

### Hexagonal Structure

```
src/main/java/com/checkout/payment/gateway/
├── model/                              (Domain Layer)
│   ├── PaymentRequest                 (Input DTO with validation)
│   ├── PaymentResponse                (Output DTO)
│   └── ErrorResponse
├── enums/
│   └── PaymentStatus                  (AUTHORIZED, DECLINED, REJECTED)
├── ports/                              (Application Layer - Interfaces)
│   ├── BankClient                     (Output port for bank communication)
│   └── PaymentsCRUDRepository          (Output port for persistence)
├── service/
│   └── PaymentGatewayService          (Use case orchestration)
├── adapters/                           (Infrastructure Layer - Implementations)
│   └── bank/
│       ├── HttpBankClient             (HTTP adapter for bank simulator)
│       └── BankResponse
├── repository/
│   └── PaymentsRepository             (In-memory adapter for storage)
├── controller/
│   └── PaymentGatewayController       (REST adapter)
├── exception/
│   ├── CommonExceptionHandler         (Global error handling)
│   ├── EventProcessingException
│   └── BankUnavailableException
└── configuration/
    └── ApplicationConfiguration       (Spring beans and DI)
```

## Technology Stack

- **Java 17** with Spring Boot 3.1.5
- **Spring Web**: REST API handling
- **Jakarta Bean Validation**: Input validation with cross-field constraints
- **Lombok 1.18.32**: Eliminates boilerplate code (@Data, @Builder, etc.)
- **SpringDoc OpenAPI 2.2.0**: API documentation and Swagger UI
- **JUnit 5 + Mockito**: Unit testing

## Payment Processing Flow

### Authorized Flow
1. Client submits valid `PaymentRequest`
2. Service validates all constraints (card number format, expiry date, CVV)
3. Service calls bank simulator API
4. Bank returns `authorized=true`
5. Service persists payment with `AUTHORIZED` status
6. Returns `201 Created` with full payment details

### Declined Flow
1. Client submits valid `PaymentRequest`
2. All validations pass
3. Service calls bank simulator API
4. Bank returns `authorized=false`
5. Service persists payment with `DECLINED` status
6. Returns `201 Created` with declined payment details

### Rejected Flow
1. Client submits `PaymentRequest` with validation errors (invalid card, expired, etc.)
2. Service validates constraints using Jakarta Bean Validation
3. Violations detected → service returns immediately without calling bank
4. **Payment is NOT persisted** for rejected requests
5. Returns `201 Created` with `REJECTED` status and list of violation messages

## API Specification

### POST /api/v1/payment — Process Payment

**Request:**
```json
{
  "card_number": "4111111111111111",
  "expiry_month": 12,
  "expiry_year": 2026,
  "currency": "USD",
  "amount": 100,
  "cvv": "123"
}
```

**Validation Rules:**
- `card_number`: Must be numeric, 14-19 characters
- `expiry_month`: 1-12, must be non-null
- `expiry_year`: 4-digit year, must be in future (via `@AssertTrue`)
- `currency`: Required, non-empty
- `amount`: Required, positive
- `cvv`: 3-4 digits

**Success Response (201 Created):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "status": "Authorized",
  "amount": 100,
  "currency": "USD",
  "expiryMonth": 12,
  "expiryYear": 2026,
  "cardNumberLastFour": 1111
}
```

**Rejection Response (201 Created):**
```json
{
  "status": "Rejected",
  "violations": [
    "cardNumber: Card number must be numeric and between 14 and 19 characters"
  ]
}
```

**Error Response (e.g., 503 Service Unavailable):**
```json
{
  "message": "Bank unavailable"
}
```

### GET /api/v1/payment/{id} — Retrieve Payment

**Success Response (200 OK):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "status": "Authorized",
  "amount": 100,
  "currency": "USD",
  "expiryMonth": 12,
  "expiryYear": 2026,
  "cardNumberLastFour": 1111
}
```

1**Start the application**:
```bash
./gradlew bootRun
```
The application starts on `http://localhost:8090`

2**Access API documentation**:
Open [http://localhost:8090/swagger-ui/index.html](http://localhost:8090/swagger-ui/index.html)

### Configuration

Edit `src/main/resources/application.properties`:
```properties
bank.endpoint=http://localhost:8080/payments    # Bank simulator endpoint
```

## Key Design Decisions

### 1. Hexagonal Architecture
- **Why**: Separates business logic from framework concerns
- **Benefit**: Easy to test in isolation; simple to swap implementations
- **Example**: Replacing in-memory storage with JPA requires only a new adapter, no service changes

### 2. Jakarta Bean Validation
- **Why**: Declarative validation; cleaner than imperative checks scattered in code
- **Benefit**: Centralized, reusable validation rules; clear error messages
- **Example**: `@CardNumber` on card fields; `@Size` on CVV; `@AssertTrue` for cross-field expiry logic

### 3. Rejected Payments Not Persisted
- **Why**: Rejected payments are validation errors, not legitimate business events
- **Benefit**: Keeps repository clean; rejected payments are request/response artifacts
- **Note**: This differs from **Declined** payments which are legitimate bank-initiated rejections

### 4. Lombok for Boilerplate
- **Why**: Reduces code verbosity (getters, setters, constructors, equals/hashCode)
- **Benefit**: Improves readability; less maintenance; generated code is IDE-friendly
