# nutrition-svc

REST microservice за FitJourney — управление на хранителни планове към workout програми.

## Tech Stack

| Layer      | Technology              |
|------------|-------------------------|
| Language   | Java 17                 |
| Framework  | Spring Boot 3.4.0       |
| Build tool | Maven                   |
| Database   | MySQL (fitjourney_nutrition) |
| ORM        | Spring Data JPA         |
| Port       | 8081                    |

## Domain Entities

### NutritionPlan
Хранителен план свързан с конкретна workout програма.

### MealEntry
Конкретно хранене (закуска, обяд, вечеря) в рамките на хранителен план.

## API Endpoints

| Method | Endpoint                          | Description                    |
|--------|-----------------------------------|--------------------------------|
| GET    | /nutrition/programs/{programId}   | Вземи план за програма         |
| POST   | /nutrition/plans                  | Създай нов хранителен план     |
| POST   | /nutrition/plans/{id}/meals       | Добави хранене към план        |
| DELETE | /nutrition/plans/{id}             | Изтрий план                    |

## Setup

```sql
CREATE DATABASE fitjourney_nutrition;
```

Конфигурирай `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/fitjourney_nutrition
spring.datasource.username=YOUR_USERNAME
spring.datasource.password=YOUR_PASSWORD
```

```bash
mvn spring-boot:run
```

## Integration с Main App

Комуникацията с main app (FitJourney) се осъществява чрез Feign Client.
Виж `FEIGN_CLIENT_FOR_MAIN_APP.java` за инструкции.
