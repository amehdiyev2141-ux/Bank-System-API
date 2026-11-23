# Banking System API

A REST API for managing customers, bank accounts, account transactions, transfers, and payment cards. The application is built with Java 21, Spring Boot, Spring Data JPA, and PostgreSQL.

## Features

- Create, list, retrieve, update, and change the status of customers.
- Create bank accounts and retrieve them by ID, customer, or as a list.
- Deposit to and withdraw from active accounts; view account transaction history.
- Transfer funds between active accounts with balance and currency checks.
- Create cards for active accounts, retrieve cards, and update card status. Card numbers are masked in API responses.
- Validate request payloads and return consistent JSON errors.
- Lock account rows during money operations to protect balance updates from concurrent requests.

## Technology

- Java 21
- Spring Boot 4.1.2
- Gradle
- Spring Web MVC, Bean Validation, Spring Data JPA / Hibernate
- PostgreSQL
- Lombok
- springdoc-openapi and Swagger UI
- JUnit 5, Spring test support, Mockito

## Architecture

The application uses a layered structure under `com.example.banksystemapi`:

- `controller` exposes REST endpoints and validates requests.
- `service` contains business rules and transaction boundaries.
- `repository` provides Spring Data JPA persistence operations.
- `entity` maps customers, accounts, transactions, transfers, and cards to database tables.
- `dto` defines API request and response models; controllers do not expose entities.
- `enums` contains customer, account, transaction, transfer, card, and currency states.
- `exception` contains domain exceptions and the shared JSON error handler.

Customers own bank accounts. Accounts are referenced by transaction records, transfer source/destination relations, and cards. Money is represented using `BigDecimal`; account rows are pessimistically locked for transaction and transfer balance changes.

## Requirements

- JDK 21
- PostgreSQL 15 or newer (or Docker)
- Docker, if using the optional container command below

## Configuration

Database settings are read from environment variables. `DB_PASSWORD` is required; do not commit real credentials. Copy `.env.example` to `.env` for reference, then set the values in your shell or IDE run configuration. Spring Boot does not automatically load `.env` files.

Defaults are provided for `DB_URL` (`jdbc:postgresql://localhost:5432/banking_db`) and `DB_USERNAME` (`postgres`). The current JPA schema mode is `update`; the application does not create the PostgreSQL database itself.

### Local PostgreSQL

Create a database named `banking_db`, set `DB_PASSWORD` to the password for the configured database user, and run the application:

```powershell
$env:DB_PASSWORD = "your-local-postgres-password"
./gradlew.bat bootRun
```

On macOS/Linux, use `export DB_PASSWORD=...` and `./gradlew bootRun`.

### Optional PostgreSQL Docker container

The repository does not include a Docker Compose configuration. To start a local PostgreSQL container for development:

```bash
docker run --name banking-postgres \
  -e POSTGRES_DB=banking_db \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=change-this-local-password \
  -p 5432:5432 -d postgres:16
```

Set `DB_PASSWORD` to the same local value before starting the API. This command is for local development only; use your own secret rather than committing it.

## Run and test

```bash
./gradlew bootRun
./gradlew clean test
./gradlew build
```

On Windows, use `gradlew.bat` instead of `./gradlew`.

## API documentation

With the application running, open Swagger UI at [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html). The OpenAPI JSON is available at [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs). Endpoints are grouped as Customers, Bank Accounts, Transactions, Transfers, and Cards.

## API endpoints

| Method | Path | Description |
|---|---|---|
| POST | `/api/customers` | Create a customer |
| GET | `/api/customers` | List customers |
| GET | `/api/customers/{id}` | Get a customer |
| PUT | `/api/customers/{id}` | Update customer details |
| PATCH | `/api/customers/{id}/status` | Update customer status |
| POST | `/api/bank-accounts` | Create a bank account |
| GET | `/api/bank-accounts` | List bank accounts |
| GET | `/api/bank-accounts/{id}` | Get a bank account |
| GET | `/api/bank-accounts/customer/{customerId}` | List a customer's accounts |
| POST | `/api/transactions/deposit` | Deposit to an account |
| POST | `/api/transactions/withdraw` | Withdraw from an account |
| GET | `/api/transactions/account/{accountId}` | List account transactions |
| POST | `/api/transfers` | Transfer between accounts |
| GET | `/api/transfers/{id}` | Get a transfer |
| GET | `/api/transfers/account/{accountId}` | List transfers involving an account |
| POST | `/api/cards` | Create a card for an account |
| GET | `/api/cards` | List cards (numbers are masked) |
| GET | `/api/cards/{id}` | Get a card (number is masked) |
| GET | `/api/cards/account/{accountId}` | List cards for an account |
| PATCH | `/api/cards/{id}/status` | Update card status |

## Request examples

Create a customer:

```json
{
  "firstName": "Ada",
  "lastName": "Lovelace",
  "email": "ada@example.com",
  "phoneNumber": "+12025550123"
}
```

Create an account:

```json
{
  "customerId": 1,
  "currency": "USD"
}
```

Deposit or withdraw (use the corresponding endpoint):

```json
{
  "accountId": 1,
  "amount": 25.50
}
```

Transfer funds:

```json
{
  "sourceAccountId": 1,
  "destinationAccountId": 2,
  "amount": 10.00
}
```

Create a card:

```json
{
  "bankAccountId": 1
}
```

Change a customer or card status using the respective status endpoint:

```json
{
  "status": "ACTIVE"
}
```

Status and currency values must match the enums shown by Swagger. Customer IDs, account numbers, initial balances, statuses, timestamps, card numbers, and transfer/transaction metadata are managed by the application where applicable. Card responses expose only a masked number.
