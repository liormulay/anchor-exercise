# Anchor Exercise

This is a Spring Boot application for managing sheets with customizable columns and cell values. It provides a RESTful API to create sheets, set cell values, and retrieve sheet data.

## Requirements
- Java 17 or higher
- Maven 3.6+

## Running the Application

1. **Clone the repository:**
   ```sh
   git clone https://github.com/liormulay/anchor-exercise.git
   cd anchor-exercise
   ```
2. **Build the project:**
   ```sh
   mvn clean install
   ```
3. **Run the application:**
   ```sh
   mvn spring-boot:run
   ```
   The application will start on [http://localhost:8080](http://localhost:8080).

## API Endpoints

### 1. Create a Sheet
- **Endpoint:** `POST /sheets`
- **Request Body:**
  ```json
  {
    "columns": [
      { "name": "A", "type": "String" },
      { "name": "B", "type": "Integer" }
    ]
  }
  ```
- **Response:**
  ```json
  { "sheetId": "<generated-id>" }
  ```

### 2. Set Cell Value
- **Endpoint:** `POST /sheets/{sheetId}/cell`
- **Request Body:**
  ```json
  {
    "rowIndex": 0,
    "columnName": "A",
    "value": "Hello World"
  }
  ```
- **Response:**
  - `200 OK` on success
  - `400 Bad Request` or `404 Not Found` on error

### 3. Get Sheet by ID
- **Endpoint:** `GET /sheets/{sheetId}`
- **Response:**
  ```json
  {
    "columns": [
      { "name": "A", "type": "String" },
      { "name": "B", "type": "Integer" }
    ],
    "rows": [
      { "A": "Hello World", "B": null }
    ]
  }
  ```

## Notes
- The application uses an in-memory H2 database by default. For production, configure a MySQL database in `application.properties`.
- For more details, see the [Spring Boot documentation](https://spring.io/projects/spring-boot). 

## Running the Tests

To execute all tests, run:
```sh
mvn test
```
This will compile the project and run all unit and integration tests.

### Test Types

- **Context Load Test**: Ensures the Spring application context loads successfully.
  - `AnchorExerciseApplicationTests`
- **Unit Tests (Service Layer)**: Test the business logic in isolation using mocks for dependencies.
  - `SheetServiceTest`
- **Unit Tests (Controller Layer)**: Test controller logic with mocked service layer, using MockMvc for HTTP simulation.
  - `SheetControllerTest`
- **Integration Tests (Controller Layer)**: Test the full stack (HTTP, controller, service, repository) using a real application context and TestRestTemplate.
  - `SheetControllerIntegrationTest`

You can run a specific test class with:
```sh
mvn -Dtest=ClassName test
```
Replace `ClassName` with the desired test class, e.g., `SheetServiceTest`. 