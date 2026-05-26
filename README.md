# Instructions for candidates

This is the Java version of the Payment Gateway challenge. If you haven't already read this [README.md](https://github.com/cko-recruitment/) on the details of this exercise, please do so now.

## Requirements
- JDK 17
- Docker

## Template structure

src/ - A skeleton SpringBoot Application

test/ - Some simple JUnit tests

imposters/ - contains the bank simulator configuration. Don't change this

.editorconfig - don't change this. It ensures a consistent set of rules for submissions when reformatting code

docker-compose.yml - configures the bank simulator


## API Documentation
For documentation openAPI is included, and it can be found under the following url: **http://localhost:8090/swagger-ui/index.html**

**Feel free to change the structure of the solution, use a different library etc.**

## Implementation Summary
This solution uses a lightweight hexagonal-style structure to separate:
- `controller` for HTTP endpoints
- `service` for business orchestration
- `domain` for validation and request rules
- `ports` for external contracts (payment repository, bank client)
- `adapters` for the in-memory repository and bank simulator HTTP client

Payment requests are validated before calling the bank simulator. Valid requests are persisted in the provided in-memory repository, and retrieval is supported by payment ID.

## Supported Endpoints
- `POST /payment`
  - Accepts a payment request with `card_number`, `expiry_month`, `expiry_year`, `currency`, `amount`, and `cvv`
  - Returns `201 Created` with payment details including `id`, `status`, `cardNumberLastFour`, `expiryMonth`, `expiryYear`, `currency`, and `amount`
  - Returns `400 Bad Request` for validation failure
  - Returns `503 Service Unavailable` if the bank simulator is unreachable or returns server errors
- `GET /payment/{id}`
  - Returns `200 OK` with stored payment details
  - Returns `404 Not Found` with `{"message":"Page not found"}` for missing payments

## How to run
1. Start the bank simulator:
```bash
docker-compose up
```
2. Start the application:
```bash
./gradlew bootRun
```
3. Open API docs at:
```bash
http://localhost:8090/swagger-ui/index.html
```

## How to test
Run the Gradle test suite:
```bash
./gradlew test --no-daemon
```

## Assumptions and Notes
- The repository remains in-memory as allowed by the exercise.
- The bank simulator request body is mapped to the provided `card_number`, `expiry_date`, `currency`, `amount`, and `cvv` fields.
- Validation covers card number length/format, expiry month/year, future expiry date, currency whitelist (`USD`, `EUR`, `GBP`), positive minor-unit amount, and 3-4 digit CVV.
- If the bank simulator returns HTTP 5xx, the client retries once before returning `503 Service Unavailable`.
