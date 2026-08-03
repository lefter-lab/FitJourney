# FitJourney

## 1. Project title and overview

FitJourney is a fitness and workout tracking platform composed of two independent Spring Boot applications:

- `fitjourney-app` - the main Spring MVC + Thymeleaf web application
- `nutrition-svc` - a REST microservice for nutrition plans and meals

The platform supports workout program management, user enrollments, progress tracking, reviews, profile editing, and nutrition plan coordination across the two applications.

## 2. Course context

This project was developed for **Spring Advanced - June 2026 @ SoftUni**.

## 3. Architecture

- The main application runs on port `8080`
- The nutrition service runs on port `8081`
- The applications communicate through **OpenFeign**
- Each application uses its own MySQL database
- The main application can continue to operate when the nutrition service is unavailable through graceful degradation in the integration layer

Textual flow:

```text
Browser -> fitjourney-app -> OpenFeign -> nutrition-svc
```

The two applications run as separate processes, use separate Maven projects, and do not share a database schema.

## 4. Tech stack

- Java 17
- Spring Boot 3.4.0
- Spring MVC
- Thymeleaf
- Spring Security
- Spring Data JPA
- Hibernate
- OpenFeign
- Jakarta Validation
- MySQL
- H2 for automated tests
- Maven
- Lombok
- Bootstrap
- Spring Caching
- Spring Scheduling
- JUnit 5
- Mockito
- MockMvc
- JaCoCo

## 5. Main application features

- Registration and authentication
- USER, TRAINER, and ADMIN roles
- Profile viewing and editing
- Workout program creation, editing, deactivation, and deletion
- Enrollment and progress tracking
- Reviews and ratings
- Nutrition plan integration and meal management through OpenFeign
- Validation and controlled error pages
- Cron and fixed-rate scheduled jobs
- Cached workout program listing
- Graceful degradation when the nutrition microservice is unavailable

## 6. Nutrition microservice features

- Create nutrition plans for workout programs
- Retrieve nutrition plans
- Add meals with calories and macronutrients
- Display meals in the main application
- Status endpoint for manual health checks
- Validation and structured JSON errors

## 7. REST integration

| HTTP method | Endpoint | Purpose | Consuming application |
| --- | --- | --- | --- |
| GET | `/nutrition/programs/{programId}` | Retrieve the nutrition plan for a workout program | `fitjourney-app` |
| POST | `/nutrition/programs` | Create a nutrition plan for a workout program | `fitjourney-app` |
| POST | `/nutrition/plans/{planId}/meals` | Add a meal to an existing nutrition plan | `fitjourney-app` |
| GET | `/nutrition/status` | Return a simple service status response | Browser / operator |

When `nutrition-svc` is stopped or unreachable, nutrition-specific calls return a controlled fallback result instead of an uncontrolled server error. Core workout, enrollment, profile, and review functionality remains available. Creating or retrieving nutrition plans and adding meals still require the microservice to be running.

## 8. Domain entities

### Main app

- `User`
- `WorkoutProgram`
- `Enrollment`
- `WorkoutReview`

### Nutrition service

- `NutritionPlan`
- `MealEntry`

## 9. Valid domain functionalities

| Action | Endpoint | Role |
| --- | --- | --- |
| Create workout program | `POST /programs/create` | TRAINER |
| Edit workout program | `POST /programs/{id}/edit` | TRAINER |
| Deactivate workout program | `POST /programs/{id}/deactivate` | TRAINER |
| Delete workout program | `POST /programs/{id}/delete` | TRAINER |
| Enroll in program | `POST /enrollments/{programId}/enroll` | USER |
| Update enrollment progress | `POST /enrollments/{enrollmentId}/progress` | USER |
| Submit review | `POST /reviews/programs/{programId}` | USER |
| Update own profile | `POST /profile` | Authenticated user |
| Create nutrition plan | `POST /programs/{id}/nutrition-plan` | TRAINER or ADMIN |
| Add meal | `POST /programs/{programId}/nutrition-plan/{planId}/meals` | TRAINER or ADMIN |

## 10. Security

- BCrypt password hashing is used for stored passwords
- CSRF protection is enabled by Spring Security
- Public pages are available only where explicitly permitted
- Authenticated pages require login
- Role-restricted endpoints are enforced with Spring Security
- Method-level authorization is used with `@PreAuthorize`
- Users can view and edit only their own profile data
- ADMIN users can manage user roles through the admin panel
- Enrollment progress can be updated only by the owner of the enrollment
- Enrollment progress is also validated at service level and must be between `0` and `100`
- TRAINER users can manage nutrition plans only for workout programs they own
- ADMIN users can manage nutrition data independently of trainer ownership where the current endpoints permit it
- Before a meal is added, the route `planId` and `programId` are checked against the nutrition plan returned by the microservice
- Feign create and add-meal calls are not executed when ownership is denied
- The add-meal Feign call is not executed when the nutrition plan does not match the route identifiers
- Authorization and ownership failures are not converted into nutrition service fallback results

## 11. Validation and error handling

### Main app

- Jakarta Validation is used on DTOs and form inputs
- Service-level rules protect against invalid business operations
- Enrollment progress is defensively validated at service level before repository lookup or entity mutation
- Nutrition ownership is validated before state-changing microservice calls
- Nutrition plan and workout program consistency is validated before adding a meal
- Custom exceptions are used for domain-specific error cases
- HTML error pages are available for controlled failures

### Nutrition service

- DTO and entity validation are used for nutrition requests
- Structured JSON error responses are returned through a global exception handler
- Domain-specific exceptions are handled centrally

## 12. Scheduling and caching

### Scheduled jobs

- A cron job marks overdue active enrollments as `EXPIRED`
- A fixed-rate job marks active enrollments with 100% progress as `COMPLETED`

### Caching

- Workout program listings are cached with `@Cacheable("programs")`
- Cache entries are cleared with `@CacheEvict` after create, update, deactivate, and delete operations

## 13. Databases

The project uses two separate MySQL databases:

- `fitjourney`
- `fitjourney_nutrition`

Primary keys are UUID-based in both applications.

Local evaluation configuration:

- MySQL username: `root`
- MySQL password: `1234`
- MySQL port: `3306`
- Main application port: `8080`
- Nutrition microservice port: `8081`

## 14. Setup and startup

1. Clone the repository.
2. Create the two MySQL databases.
3. Verify that the local MySQL server is available with username `root` and password `1234`, which are already configured in both `application.properties` files.
4. Start `nutrition-svc`.
5. Start `fitjourney-app`.
6. Open the main application at `http://localhost:8080`.
7. Check the microservice status at `http://localhost:8081/nutrition/status`.

Create the databases:

```sql
CREATE DATABASE fitjourney;
CREATE DATABASE fitjourney_nutrition;
```

Main application configuration:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/fitjourney
spring.datasource.username=root
spring.datasource.password=1234
nutrition.service.url=http://localhost:8081/nutrition
```

Nutrition microservice configuration:

```properties
server.port=8081
spring.datasource.url=jdbc:mysql://localhost:3306/fitjourney_nutrition?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=1234
```

Start the nutrition microservice from the repository root:

```powershell
cd nutrition-svc
mvn spring-boot:run
```

Start the main application in a separate terminal:

```powershell
cd fitjourney-app
mvn spring-boot:run
```

The recommended startup order is MySQL, `nutrition-svc`, and then `fitjourney-app`. The main application can still start when `nutrition-svc` is unavailable, but nutrition-specific operations require the microservice.

## 15. Testing

The following results were confirmed for commit `3f9c89e`.

### Main application

```powershell
cd fitjourney-app
mvn clean verify
```

- Tests: `144`
- Failures: `0`
- Errors: `0`
- Skipped: `0`
- Line coverage: `89.54%`
- Covered lines: `411`
- Missed lines: `48`
- Total lines: `459`

JaCoCo reports:

- `fitjourney-app/target/site/jacoco/index.html`
- `fitjourney-app/target/site/jacoco/jacoco.csv`

### Nutrition microservice

```powershell
cd nutrition-svc
mvn clean verify
```

- Tests: `26`
- Failures: `0`
- Errors: `0`
- Line coverage: `97.87%`
- Covered lines: `92`
- Missed lines: `2`
- Total lines: `94`

JaCoCo reports:

- `nutrition-svc/target/site/jacoco/index.html`
- `nutrition-svc/target/site/jacoco/jacoco.csv`

Total automated tests: `170`.

JaCoCo reports are generated by `mvn clean verify`.

## 16. Repository structure

```text
fitjourney-app/
nutrition-svc/
README.md
```

## 17. Git

Commit messages follow Conventional Commits.
