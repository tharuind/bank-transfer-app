# Bank Transfer System (Assignment 01)

Spring Boot + Thymeleaf + Spring Data JPA + MySQL + Lombok.

Demonstrates:
* `@Transactional` - a money transfer between two customers either fully succeeds or is fully rolled back
* `@Query` - JPQL, native SQL and UPDATE queries written in the repository interfaces

## Run
1. Start MySQL (XAMPP -> MySQL -> Start). The database `bank_db` is created automatically.
2. `./mvnw spring-boot:run`  (Windows: `mvnw.cmd spring-boot:run`)
3. Open http://localhost:8080  (3 sample customers are created on first start)

If your MySQL root user has a password, copy `secrets.properties.example` to `secrets.properties` and edit it.

## JSON transfer API
Send a `POST` request to `http://localhost:8080/transfer` with `Content-Type: application/json`:

```json
{
  "fromAccount": "100001",
  "toAccount": "100002",
  "amount": 5000.00,
  "simulateFailure": false
}
```

In Postman, choose **Body -> raw -> JSON**. A successful transfer returns JSON with the transaction ID, status, account numbers, and amount. Invalid fields or transfer failures return a JSON error response with HTTP `400`. The existing browser form continues to use `application/x-www-form-urlencoded` at the same URL.
"# Assignment_Grading" 
"# Bank_Tranfer_App" 
