# GameFlix Login and Registration API

This Spring Boot service registers GameFlix members, authenticates them, issues signed JSON Web Tokens, and uses those tokens to protect the current-member profile endpoint.

## Technology

- Java 21
- Spring Boot 3.5
- Spring Web
- Spring Security 6
- Spring Data JPA
- OAuth2 Resource Server JWT support
- MySQL
- Maven

## Required endpoints

| Method | Endpoint | Purpose | Expected responses |
|---|---|---|---|
| POST | `/api/auth/register` | Register a member and hash the password | 201, 400, or 409 |
| POST | `/api/auth/login` | Verify credentials and issue a JWT | 200 or 401 |
| GET | `/api/users/me` | Return the authenticated member's profile | 200 or 401 |

## MySQL setup

1. Start MySQL.
2. Run `schema.sql` in MySQL Workbench. It creates the `gameflix` database and the `users` table.
3. Create the expected local database user, replacing the example password with your own local password:

```sql
CREATE USER IF NOT EXISTS 'gameflix_app'@'localhost' IDENTIFIED BY 'choose-a-local-password';
GRANT ALL PRIVILEGES ON gameflix.* TO 'gameflix_app'@'localhost';
FLUSH PRIVILEGES;
```

The expected database is `gameflix`, and the expected application username is `gameflix_app`.
4. Copy `src/main/resources/application.properties.example` to `src/main/resources/application-local.properties`.
5. Enter the local MySQL username and password in `application-local.properties`.
6. Replace the JWT placeholder with a random value containing at least 32 characters.

`application-local.properties` is ignored by Git because it contains local credentials and the signing secret. The committed example file documents all required keys without exposing their values.

## Running the service

1. Confirm that Java 21 and MySQL are installed.
2. Open the project in IntelliJ IDEA as a Maven project.
3. Allow Maven to download the dependencies.
4. Confirm that `application-local.properties` contains valid local settings.
5. Run `GameflixAuthApplication`.
6. The service starts at `http://localhost:8080`.

From a terminal, the service can also be started with:

```bash
./mvnw spring-boot:run
```

On Windows, use `mvnw.cmd spring-boot:run` when the Maven wrapper is present.

## Schema

The complete reference schema is committed as `schema.sql`. The `users` table contains `id`, `email`, `password`, `first_name`, and `last_name`. The email column has a database-level unique constraint. The password column is `VARCHAR(255)` so the complete encoded password, including its algorithm prefix, is not truncated.

## Hibernate DDL choice

The project uses `spring.jpa.hibernate.ddl-auto=update`. I selected `update` because it preserves registered members between application runs while allowing Hibernate to keep the development schema aligned with the entity. I did not use `create-drop` because it would erase the test data each time the application stops.

## Password hashing decision

Passwords are encoded with Spring Security's delegating password encoder, which selects BCrypt with strength 10 by default and stores the `{bcrypt}` prefix with each hash. BCrypt is an adaptive, salted, one-way algorithm and strength 10 meets the course minimum while remaining practical on a development computer. Registration restricts passwords to 64 characters, which also stays below BCrypt's 72-byte input limit for the assignment's test passwords.

## JWT design and token lifetime

Successful login creates an HS256-signed JWT containing the member ID as the subject, the member email, the issue time, the issuer, and an expiration claim. No password or other confidential information is included because a JWT payload is encoded but not encrypted. The token lasts 60 minutes, which is long enough for ordinary development testing but limits the useful lifetime of a stolen token. The signing secret is loaded from the untracked local configuration rather than committed source code.

## CORS

CORS permits only `http://localhost:3000`, the named development front-end origin. The configuration does not use a wildcard origin.

## Test evidence

Postman was used to demonstrate all required success and failure paths:

1. Successful registration returns 201.
2. Reusing the same email returns 409.
3. Successful login returns 200 and a signed token.
4. Login with a wrong password returns 401 with the generic message `Invalid email or password`.
5. `GET /api/users/me` with a valid bearer token returns 200 and the member profile without the password hash.
6. `GET /api/users/me` with a missing or tampered token returns 401.
7. The query `SELECT id, email, password FROM users;` shows that MySQL stores a BCrypt hash rather than the plaintext password.

The exported collection is stored at `postman/GameFlix Authentication.postman_collection.json`. Annotated screenshots should be added to the separate Word evidence document required for the Canvas submission.

## Module 3 design-change note

The Module 3 design used a Member Service to manage member accounts and profiles. The implementation keeps that responsibility but uses the table name `users` and the Java class name `User` to match the terminology in the Module 4 assignment. Authentication and token creation are implemented inside this service rather than as a separate service because the current assignment builds one small Spring Boot application.

## AI-assisted development

I used ChatGPT to help outline the Spring Security configuration, identify the request and response DTOs, and create a checklist for the six required Postman tests. I reviewed the suggestions against the Module 4 materials before using them. For example, I kept the suggested `SecurityFilterChain` because it matches Spring Security 6, but rejected older examples based on `WebSecurityConfigurerAdapter` because the course materials state that it has been removed.

AI also suggested checking for an existing email before saving a new member. I changed that approach so the database unique constraint is the final authority and the controller catches `DataIntegrityViolationException`, because an application-only check could fail during two simultaneous registrations. I also removed suggestions to place the password or sensitive member details in the JWT payload because signed JWT claims are readable and the assignment specifically prohibits confidential token data.

## AI-use examples for the change log

- **Prompt:** "Help me configure Spring Security 6 so registration and login are public but `/api/users/me` requires a JWT."
  **Result used:** A lambda-based `SecurityFilterChain` with stateless sessions and OAuth2 Resource Server JWT support.
- **Prompt:** "How should I prevent duplicate GameFlix registration emails?"
  **Result changed:** AI suggested an application lookup; I retained a lookup-free registration flow and enforced uniqueness through the MySQL constraint, returning 409 when that constraint is violated.
- **Prompt:** "What information should be included in the GameFlix JWT?"
  **Result changed:** I limited the token to identifying and timing claims and excluded passwords and other confidential information.
- **Prompt:** "Give me the Postman tests required for registration, login, and the protected profile route."
  **Result used:** A six-test checklist covering both successful requests and required failure cases.

## Important security behavior

- Passwords are never returned by an endpoint or written to application logs.
- Login always uses the same failure message whether the email is unknown or the password is wrong.
- Missing, expired, or modified tokens are rejected with 401.
- The JWT signing secret and MySQL credentials are not committed.
