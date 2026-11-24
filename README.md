## ******Multi-Threaded Banking Transaction Processing System******

A banking application built using Java and Spring Boot with PostgreSQL as the database. the application allows users to create bank accounts, deposit money, withdraw money, and maintain
transaction records through
REST APIs.

**Project Status:** Work in Progress


## Tech Stack

| Component | Technology |
| :--- | :--- |
| Programming Language | Java 21 |
| Backend Framework | Spring Boot |
| Database | PostgreSQL 18 |
| ORM | Spring Data JPA / Hibernate |
| Build Tool | Maven |
| API Testing | Postman |
| IDE | IntelliJ IDEA |

## **Features Implemented**

* **Account Creation**: Create a new bank account with account number, initial balance, and account status.
* Deposit: Add money to an existing bank account.
* Withdrawal: Withdraw money from an account after checking the available balance.
* Transaction History: Retrieve the transaction history of an account through a REST API.
* Database Integration: Store account details, balances, and transaction records in PostgreSQL.
* REST APIs: Perform banking operations through HTTP requests.
* Layered Architecture: Organize the application using Controller, Service, Repository, Entity and DTO layers.

## Project Structure

```text
src/main/java/com/banking/system/
├── controller/
│   └── AccountController.java
│
├── service/
│   └── AccountService.java
│
├── repository/
│   ├── AccountRepository.java
│   └── TransactionRepository.java
│
├── entity/
│   ├── Account.java
│   ├── Transaction.java
│   └── FailedTransaction.java
│
└── dto/
    └── TransactionDTO.java