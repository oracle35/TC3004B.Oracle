# Backend

The **Backend** is a Java application built using the Spring Boot framework. It provides a RESTful API for the frontend application and integrates with a Telegram bot for task management. It also includes functionality for generating AI-powered KPI summaries.

## Technologies Used

- Java 17
- Spring Boot (Web, Data JPA, Security, TelegramBots)
- Maven (for dependency management and build lifecycle)
- Oracle Database (via JDBC and UCP - Universal Connection Pool)
- Hibernate (as the JPA provider)
- TelegramBots API (for Telegram bot integration)
- Java HTTP Client (for calling external APIs like Gemini)
- Nix (for reproducible builds and dependency management, including Maven dependencies via `mvn2nix`)
- Docker (for containerization)
- JUnit & Mockito (for unit/integration testing)

## How it Works

The backend follows a standard layered architecture commonly used in Spring Boot applications.

### Structure

- **`src/main/java/com/springboot/MyTodoList/`**: Contains all the Java source code.
  - **`controller/`**: Handles incoming HTTP requests (REST API) and Telegram bot updates. Maps requests to service methods. Includes `ToDoItemController`, `ProjectController`, `UserController`, `SprintController`, `KpiController`, and `ToDoItemBotController`.
  - **`service/`**: Contains the business logic. Coordinates data access and performs operations. Includes `ToDoItemService`, `ProjectService`, `UserService`, `SprintService`, and `KpiService`.
  - **`repository/`**: Defines Spring Data JPA repositories for database interaction (e.g., `ToDoItemRepository`, `ProjectRepository`). These interfaces handle CRUD operations.
  - **`model/`**: Defines JPA entity classes (`ToDoItem`, `Project`, `User`, `Sprint`, `Task`) that map to database tables.
  - **`util/`**: Contains utility classes and enums, such as `BotCommands`, `BotLabels`, `BotHelper`, `BotMessages`.
  - **`config/`**: (If present) Would contain configuration classes (e.g., for security, beans).
  - **`MyTodoListApplication.java`**: The main application class with the `main` method to run the Spring Boot application and register the Telegram bot.
- **`src/main/resources/`**: Contains non-Java resources.
  - **`application.properties`** or **`application.yaml`**: Configuration for Spring Boot, database connection, logging, Telegram bot, and custom properties like the Gemini API key.
  - **`static/`**: (Often inside `target/classes/static` after build) This is where the compiled frontend (`dist/` content) is placed to be served by Spring Boot.
- **`pom.xml`**: Maven project configuration, defining dependencies, plugins, and build settings.
- **`Dockerfile`**: Instructions for building the Docker container image for the backend application.
- **`nix-run.sh`**: A script used by the Nix derivation for local running, responsible for loading environment variables from `.env` and starting the Java application.
- **`wallet/`**: (Not tracked by Git) Contains the Oracle Wallet files required for secure database connection. Needs to be present at runtime.

### Workflow

1. **Startup**: When the application starts (via `mvn spring-boot:run`, `java -jar ...`, or `nix run .#todoapp`), Spring Boot auto-configures the application based on dependencies and properties. The `MyTodoListApplication` registers the `ToDoItemBotController` with the Telegram API.
2. **API Request Handling**:
    - Incoming HTTP requests to `/api/...` endpoints are routed by Spring Web MVC to the appropriate methods in the `@RestController` classes (e.g., `ToDoItemController`).
    - Controllers delegate the processing to `@Service` classes.
3. **Telegram Bot Handling**:
    - The `ToDoItemBotController` receives updates (messages, commands) from the Telegram API via long polling.
    - It parses the messages, identifies commands (like `/start`, `/additem`, `/aisummary`), and interacts with relevant `@Service` classes to perform actions or retrieve data.
    - It uses `BotHelper` to send responses back to the Telegram chat.
4. **Business Logic**: Service classes contain the core application logic, such as validating data, coordinating calls to repositories, and processing data before returning it to the controller. The `KpiService` handles fetching data from other services and calling the Gemini API to generate summaries.
5. **Data Access**: Service classes use `@Repository` interfaces (extending Spring Data JPA's `JpaRepository`) to interact with the Oracle database. Spring Data JPA automatically generates the necessary SQL queries for common operations.
6. **AI Summary**: The `KpiService` fetches task, user, and sprint data, constructs a prompt, and sends it to the Google Gemini API via an HTTP request. It parses the response and returns the summary text. The API key is read from configuration (`application.properties` or environment variables).
7. **Frontend Serving**: Spring Boot is configured to serve static files. During the build process ([`bin/build.sh`](bin/build.sh ) or Nix build), the compiled frontend assets are copied to `src/main/resources/static/` (or `target/classes/static/`). Spring Boot then serves the `index.html` and other frontend assets when the root URL (`/`) is accessed.
8. **Nix Build**: The Nix flake (`flake.nix` and related files like [`MtdrSpring/backend/package.nix`](MtdrSpring/backend/package.nix )) defines how to build the backend reproducibly. It uses `mvn2nix` to fetch and hash Maven dependencies, compiles the Java code using Maven (often in offline mode using the Nix-provided dependencies), and packages the application, including the pre-built frontend, into a runnable JAR or a Docker image.
