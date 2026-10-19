--- GameFlix Login and Registration API - Implement and Test Login & Registration Endpoints ---

This Spring Boot application allows GameFlix members to register, log in, and view their account information through a protected endpoint.

--- How to Run ---

This project requires Java 21, MySQL, and IntelliJ IDEA.

Run `schema.sql` in MySQL Workbench to create the `gameflix` database and `users` table. The application expects a MySQL user named `gameflix_app`.


CREATE USER IF NOT EXISTS 'gameflix_app'@'localhost'
IDENTIFIED BY 'YOUR_MYSQL_PASSWORD';

GRANT ALL PRIVILEGES ON gameflix.* TO 'gameflix_app'@'localhost';

FLUSH PRIVILEGES;

Copy application.properties.example and rename the copy application-local.properties. Replace the placeholders with your MySQL password and a JWT secret containing at least 32 characters. This local file is excluded from Git because it contains private information.

Open the project in IntelliJ as a Maven project and run GameflixAuthApplication. The service will start at http://localhost:8080.

--- Database Schema ---

CREATE DATABASE IF NOT EXISTS gameflix;
USE gameflix;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(191) NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email)
);

The email column is unique to prevent duplicate accounts. The password column is large enough to store the complete encoded password.

--- Password Hashing ---

The application uses BCrypt with strength 10. I chose this setting because it meets the course requirement and provides strong password protection without making login testing too slow. Passwords are encoded before they are stored.

--- DDL choice ---

spring.jpa.hibernate.ddl-auto=update

I chose update because it keeps registered members in the database after the application restarts. Using create-drop would delete the test data whenever the application stopped.

--- Token Lifetime ---

JWTs remain valid for 60 minutes. This provides enough time for normal testing while limiting how long a stolen token could be used. Passwords are not included in the token.

--- Test Evidence ---

I used Postman to test:
1. Successful registration - 201 Created
2. Duplicate email - 409 Conflict
3. Successful login - 200 OK
4. Incorrect password - 401 Unauthorized
5. Protected request with a valid token - 200 OK
6. Protected request without a valid token - 401 Unauthorized

I also used the following MySQL query to confirm that the password was stored as an encoded value:

SELECT id, email, password FROM users;

The Postman collection is included in the postman folder. Screenshots of the tests are included in the separate Word document. 

--- Module 3 Design Change ---

My Module 3 design used a Member Service for accounts and profiles. This project keeps that responsibility but uses the class name User and table name users to match the Module 4 instructions. Login and JWT creation are included in the same service because this assignment uses one small Spring Boot application.

--- AI Use ---

I used ChatGPT while creating and testing this project. I reviewed its suggestions before deciding what to keep or change.

1. ChatGPT suggested organizing the project with separate controller, service, repository, and request classes. I kept this structure because it clearly separated the responsibilities of each part of the application.
2. For the Spring Security configuration, ChatGPT suggested using a lambda-based SecurityFilterChain. I kept this approach because it works with Spring Security 6 and matches the concepts covered in the course materials.
3. ChatGPT originally suggested checking for an existing email through the application before registering a member. I changed this approach by also using a unique database constraint because the database should provide the final protection against duplicate registrations.
4. AI suggested storing the MySQL password and JWT secret in a local properties file. I kept the separate configuration file but excluded it from Git because publishing these private values would create a security risk.
5. ChatGPT helped organize the README around the required assignment sections. I kept the suggested headings because they made the setup instructions, security choices, test evidence, and AI-use details easier to find.
6. To make sure I covered the required testing, I asked ChatGPT to create a checklist of the Postman tests. I kept the checklist because it helped me verify the successful and failed requests for all three endpoints.
7. While troubleshooting the application, I provided ChatGPT with the errors I received. Its responses helped me identify incorrect MySQL credentials and a JWT secret that was too short. I made the recommended corrections and reran the application to confirm that they worked.
8. When I encountered generated blocks of code that I did not fully understand, I asked ChatGPT to explain them in simpler terms. I compared the explanations with the course materials and kept the code when it used concepts we had covered. I left out anything that introduced unfamiliar or unnecessary concepts.
9. ChatGPT helped me write and understand the MySQL commands used to create the GameFlix database, users table, and gameflix_app database user. I compared the suggested SQL with the assignment requirements and changed the table fields to match the GameFlix design. I also kept the unique email constraint because the assignment requires duplicate registrations to be prevented at the database level.

 `application.properties.example` contains:

spring.datasource.url=jdbc:mysql://localhost:3306/gameflix
spring.datasource.username=gameflix_app
spring.datasource.password=YOUR_MYSQL_PASSWORD
app.jwt.secret=REPLACE_WITH_A_RANDOM_SECRET_AT_LEAST_32_CHARACTERS_LONG
