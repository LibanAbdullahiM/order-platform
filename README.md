# Order Management Platform

A Spring Boot microservices project built with Java 21, Maven, PostgreSQL, and Docker Compose.

---

## Prerequisite

Make sure you have the following installed:

- **Java 21** — required to run the application.
- **Maven** — required to build the application.
- **Docker Desktop or Docker Engine**.
- **IntelliJ IDEA** — recommended IDE for running and developing the services.
- **Git** — for cloning and managing the repository.

Verify your installations:

    java -version
    mvn -version
    docker --version
    docker compose version

---

## Project Structure

```
order-platform/
├── .env.example
├── .gitignore
├── docker-compose.yml
├── README.md
├── postgres/
│   └── init.sql
└── auth-service/
    ├── pom.xml
    └── src/
```

The exact structure may evolve as additional services are added.

---

## 1. Configure Environment Variables

The project uses environment variables to configure database connections and avoid hardcoding credentials.

From the repository root, create your local .env file using the example template:

    cp .env.example .env

Open .env and configure the values for your local environment.

For example:

    DB_HOST=localhost
    DB_PORT=5432
    DB_NAME=authdb
    DB_USER=order_platform_user
    DB_PASSWORD=your_local_password

---

## 2. Start PostgreSQL with Docker Compose

From the repository root, run:

    docker compose up -d

This starts the infrastructure defined in docker-compose.yml in the background.

Docker Compose automatically reads the root .env file for variable substitution and configuration.

Check the running containers:

    docker compose ps

## 3. Configure Environment Variables in IntelliJ IDEA

Docker Compose reads .env automatically, but Spring Boot does not automatically load an arbitrary .env file when you run the application directly from IntelliJ IDEA.

To pass the variables to auth-service:

1. Open IntelliJ IDEA and open the auth-service run configuration.
2. Go to **Run → Edit Configurations**.
3. Select the run configuration for the Spring Boot application.
4. Click **Modify options**.
5. Select **Environment variables**.
6. Add the environment variables defined in .env file.
7. **Apply** the changes and click **OK**.

---

## 4. Start auth-service

Ensure Docker Compose is running and the PostgreSQL database is ready.

Start auth-service using the Start button in IntelliJ IDEA..

---

## 5. Verify the Health Endpoint

Once auth-service has started, open the following URL:

http://localhost:8081/actuator/health

Or run:

    curl http://localhost:8081/actuator/health

Expected response:

    {"status":"UP"}

The health endpoint should be accessible without authentication. Other endpoints should remain protected by Spring Security.

---

## 6. Stop the Application

Stop auth-service using the Stop button in IntelliJ IDEA.

To stop the Docker Compose services without deleting the database data, run this from the repository root:

    docker compose down -v

PostgreSQL initialization script only run on a fresh volume.

---

## Author

**Liban A M**
