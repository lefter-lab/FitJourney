# 🏋️ FitJourney

FitJourney is a fitness and workout tracking platform built with Spring Boot.

The application allows trainers to create workout programs, while users can enroll, track their progress, and submit reviews.

---

# Tech Stack

| Layer           | Technology                  |
| --------------- | --------------------------- |
| Language        | Java 17                     |
| Framework       | Spring Boot 3.4.0           |
| Build Tool      | Maven                       |
| Database        | MySQL                       |
| ORM             | Spring Data JPA / Hibernate |
| Backend         | Spring Framework            |
| Frontend        | Spring MVC + Thymeleaf      |
| UI              | HTML, CSS, Bootstrap        |
| Security        | Spring Security, BCrypt     |
| Scheduling      | Spring @Scheduled           |
| Caching         | Spring @Cacheable           |
| Version Control | Git / GitHub                |

---

# Features

* User registration and login with BCrypt password hashing
* Session-based authentication with `user_id` stored in session
* Role-based access control (USER, TRAINER, ADMIN)
* Trainers can create, edit, deactivate, and delete workout programs
* Users can enroll in workout programs
* Users can update enrollment progress
* Users can submit reviews and ratings
* Duplicate enrollments are prevented
* Duplicate reviews are prevented
* Trainer ownership checks are enforced
* Cached program listings
* Scheduled background tasks

---

# Functionalities

| # | Functionality              | Method | Endpoint                               | Role    |
| - | -------------------------- | ------ | -------------------------------------- | ------- |
| 1 | Create workout program     | POST   | `/programs/create`                     | TRAINER |
| 2 | Edit workout program       | POST   | `/programs/{id}/edit`                  | TRAINER |
| 3 | Deactivate workout program | POST   | `/programs/{id}/deactivate`            | TRAINER |
| 4 | Delete workout program     | POST   | `/programs/{id}/delete`                | TRAINER |
| 5 | Enroll in workout program  | POST   | `/enrollments/{programId}/enroll`      | USER    |
| 6 | Update enrollment progress | POST   | `/enrollments/{enrollmentId}/progress` | USER    |
| 7 | Submit review              | POST   | `/reviews/programs/{programId}`        | USER    |

---

# Domain Entities

### User

Represents an application user with a specific role.

### WorkoutProgram

Workout program created by a trainer.

### Enrollment

Tracks a user's participation and progress in a workout program.

### WorkoutReview

Stores ratings and reviews submitted by users.

---

# Pages

| URL                      | Description             | Access        |
| ------------------------ | ----------------------- | ------------- |
| `/`                      | Home page               | Public        |
| `/register`              | Registration page       | Guest         |
| `/login`                 | Login page              | Guest         |
| `/dashboard`             | User dashboard          | Authenticated |
| `/programs/all`          | Browse workout programs | Authenticated |
| `/programs/create`       | Create program          | TRAINER       |
| `/programs/{id}/edit`    | Edit program            | TRAINER       |
| `/enrollments/my`        | My enrollments          | USER          |
| `/reviews/programs/{id}` | Program reviews         | Authenticated |

---

# Security

* Spring Security authentication and authorization
* Session-based login
* Authenticated user's UUID stored in HTTP session
* BCrypt password hashing
* Guest users can access only public pages
* Authenticated users can access protected pages
* Role checks implemented with Spring Security and `@PreAuthorize`
* Trainer ownership validation for edit, deactivate, and delete operations
* USER-only actions restricted to USER role

### Session Authentication

After successful login:

```java
session.setAttribute("user_id", user.getId());
```

The authenticated user's UUID is stored in the HTTP session as required by the project specification.

---

# Validation and Error Handling

The application uses Jakarta Validation for server-side validation.

Validation is applied to:

* Registration form
* Workout program creation form
* Workout program edit form
* Enrollment progress update form
* Workout review submission form

Business rules:

* A user cannot enroll twice in the same program
* Progress must be between 0 and 100
* A user cannot submit multiple reviews for the same program
* Trainers cannot modify programs owned by other trainers

Custom exceptions:

* `ProgramNotFoundException`
* `UserNotFoundException`
* `EnrollmentNotFoundException`
* `DuplicateEnrollmentException`
* `DuplicateReviewException`
* `UnauthorizedProgramAccessException`

---

# Database

* Spring Data JPA
* UUID primary keys for all entities
* Relational database (MySQL)
* BCrypt hashed passwords

Relationships:

* User → WorkoutProgram
* User → Enrollment
* WorkoutProgram → Enrollment
* User → WorkoutReview
* WorkoutProgram → WorkoutReview

---

# Scheduling

### Archive Inactive Programs

Automatically archives inactive workout programs.

### Complete Finished Enrollments

Automatically updates enrollments with 100% progress to COMPLETED status.

---

# Caching

Program listings are cached using:

```java
@Cacheable("programs")
```

Cache is cleared using:

```java
@CacheEvict
```

when programs are created, edited, deactivated, deleted, or archived.

---

# Project Structure

```text
src/main/java/com/fitjourney/fitjourney/
├── config/
│   ├── PasswordEncoderConfig.java
│   └── SecurityConfig.java
├── controller/
│   ├── AuthController.java
│   ├── EnrollmentController.java
│   ├── HomeController.java
│   ├── WorkoutProgramController.java
│   └── WorkoutReviewController.java
├── dto/
│   ├── EnrollmentProgressDto.java
│   ├── RegisterDto.java
│   ├── WorkoutProgramDto.java
│   └── WorkoutReviewDto.java
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
│   ├── DuplicateEnrollmentException.java
│   ├── DuplicateReviewException.java
│   ├── EnrollmentNotFoundException.java
│   ├── ProgramNotFoundException.java
│   ├── UnauthorizedProgramAccessException.java
│   └── UserNotFoundException.java
├── repository/
│   ├── EnrollmentRepository.java
│   ├── UserRepository.java
│   ├── WorkoutProgramRepository.java
│   └── WorkoutReviewRepository.java
├── scheduler/
│   └── ScheduledTasks.java
├── security/
│   └── CustomAuthenticationSuccessHandler.java
└── service/
    ├── EnrollmentService.java
    ├── UserService.java
    ├── WorkoutProgramService.java
    └── WorkoutReviewService.java
```

---

# Getting Started

## Prerequisites

* Java 17
* Maven
* MySQL

## Setup

Clone repository:

```bash
git clone https://github.com/lefter-lab/FitJourney.git
cd FitJourney
```

Create database:

```sql
CREATE DATABASE fitjourney;
```

Configure `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/fitjourney
spring.datasource.username=YOUR_DB_USERNAME
spring.datasource.password=YOUR_DB_PASSWORD
spring.jpa.hibernate.ddl-auto=update
```

Run application:

```bash
mvn spring-boot:run
```

Open:

```text
http://localhost:8080
```

---

# GitHub

https://github.com/lefter-lab/FitJourney
