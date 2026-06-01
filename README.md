# 🏋️ FitJourney
A fitness and workout tracking platform built with Spring Boot,
where trainers publish workout programs and users enroll and track progress.

## Tech Stack
| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.4.0 |
| Build Tool | Maven |
| Database | MySQL |
| ORM | Spring Data JPA / Hibernate |
| Frontend | Thymeleaf + HTML/CSS |
| Security | Spring Security (BCrypt) |
| Scheduling | Spring @Scheduled |
| Caching | Spring @Cacheable |
| Version Control | Git / GitHub |

## Features
- User registration and login with BCrypt password hashing
- Role-based access control: USER, TRAINER, ADMIN
- Trainers can create, edit and deactivate workout programs
- Users can enroll in workout programs
- Users can track and update their enrollment progress
- Cached program listings for performance
- Scheduled tasks running automatically in the background

## Functionalities
| # | Functionality | Method | Endpoint | Role |
|---|---|---|---|---|
| 1 | Create workout program | POST | /programs/create | TRAINER |
| 2 | Edit workout program | POST | /programs/{id}/edit | TRAINER |
| 3 | Deactivate workout program | POST | /programs/{id}/deactivate | TRAINER |
| 4 | Enroll in program | POST | /enrollments/{programId}/enroll | USER |
| 5 | Update progress | POST | /enrollments/{id}/progress | USER |

## Domain Entities
- **User** — platform account with role assignment
- **WorkoutProgram** — fitness program created by a trainer
- **Enrollment** — link between user and program, tracks progress
- **WorkoutReview** — rating and comment left by a user

## Pages
| URL | Description | Access |
|---|---|---|
| / | Home page | Public |
| /register | Register new account | Guest |
| /login | Login | Guest |
| /dashboard | Personal dashboard | Authenticated |
| /programs/all | Browse all active programs | Authenticated |
| /programs/create | Create new program | TRAINER |
| /programs/{id}/edit | Edit program | TRAINER |
| /enrollments/my | My enrollments | USER |

## Scheduling
- **Cron job** (daily at midnight) — archives inactive workout programs
- **Fixed rate** (every 10 min) — updates completed enrollments to COMPLETED status

## Caching
- `@Cacheable("programs")` — caches the active programs list
- `@CacheEvict` — clears cache on create, edit, deactivate and archive

## Project Structure
```
src/main/java/com/fitjourney/fitjourney/
├── config/
│   ├── PasswordEncoderConfig.java
│   └── SecurityConfig.java
├── controller/
│   ├── AuthController.java
│   ├── EnrollmentController.java
│   ├── HomeController.java
│   └── WorkoutProgramController.java
├── dto/
│   ├── RegisterDto.java
│   └── WorkoutProgramDto.java
├── entity/
│   ├── Enrollment.java
│   ├── User.java
│   ├── WorkoutProgram.java
│   └── WorkoutReview.java
├── enums/
│   ├── DifficultyLevel.java
│   ├── EnrollmentStatus.java
│   └── UserRole.java
├── exception/
│   ├── ProgramNotFoundException.java
│   └── UserNotFoundException.java
├── repository/
│   ├── EnrollmentRepository.java
│   ├── UserRepository.java
│   ├── WorkoutProgramRepository.java
│   └── WorkoutReviewRepository.java
├── scheduler/
│   └── ScheduledTasks.java
└── service/
├── EnrollmentService.java
├── UserService.java
├── WorkoutProgramService.java
└── WorkoutReviewService.java
```

## Getting Started

### Prerequisites
- Java 17+
- MySQL 8+
- Maven 3.8+

### Setup
1. Clone the repository:
git clone https://github.com/lefter-lab/FitJourney.git
cd FitJourney

2. Create the database:
```sql
CREATE DATABASE fitjourney;
```

3. Configure `src/main/resources/application.properties`:
spring.datasource.url=jdbc:mysql://localhost:3306/fitjourney
spring.datasource.username=YOUR_DB_USERNAME
spring.datasource.password=YOUR_DB_PASSWORD
spring.jpa.hibernate.ddl-auto=update

4. Run the application:
mvn spring-boot:run

5. Open in browser: http://localhost:8080

## GitHub
https://github.com/lefter-lab/FitJourney
