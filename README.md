# 🏋️ FitJourney

A fitness and workout tracking platform built with Spring Boot, where trainers publish workout programs and users enroll, track progress, and leave reviews.

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 3.4.0 |
| Build Tool | Maven |
| Database | MySQL |
| ORM | Spring Data JPA |
| Frontend | Thymeleaf + HTML/CSS |
| Security | Session-based authentication |
| Password Hashing | BCrypt |
| Scheduling | Spring `@Scheduled` |
| Caching | Spring `@Cacheable` |
| Version Control | Git / GitHub |

---

## Features

### Authentication & Authorization
- User registration with hashed passwords (BCrypt)
- Session-based login (stores `user_id` in session)
- Role-based access control: `GUEST`, `USER`, `TRAINER`, `ADMIN`

### Workout Programs
- Trainers can create, edit, and deactivate workout programs
- Programs have difficulty levels, duration (weeks), and price
- Active/inactive status management
- Cached program listings for performance

### Enrollments
- Users can enroll in available workout programs
- Track progress percentage per enrollment
- View all personal enrollments with status

### Reviews
- Users can leave a rating and comment on enrolled programs
- Reviews displayed on program detail page

### Admin
- View and manage all users
- Change user roles

### Scheduling
- Daily cron job archives inactive programs
- Every 10 minutes: updates expired enrollments

---

## Functionalities

| # | Functionality | Method | Endpoint | Role |
|---|--------------|--------|----------|------|
| 1 | Create workout program | POST | `/programs` | TRAINER |
| 2 | Edit workout program | POST | `/programs/{id}/edit` | TRAINER |
| 3 | Deactivate workout program | POST | `/programs/{id}/deactivate` | TRAINER |
| 4 | Enroll in program | POST | `/enrollments` | USER |
| 5 | Update progress | POST | `/enrollments/{id}/progress` | USER |
| 6 | Add review | POST | `/reviews` | USER |

---

## Domain Entities

- **User** — platform account with role assignment
- **WorkoutProgram** — fitness program created by a trainer
- **Enrollment** — link between a user and a program, tracks progress
- **WorkoutReview** — rating and comment left by an enrolled user

---

## Pages

| URL | Description | Access |
|-----|-------------|--------|
| `/` | Home page | Public |
| `/register` | Register new account | Guest |
| `/login` | Login | Guest |
| `/dashboard` | Personal dashboard | USER |
| `/programs` | Browse all programs | Public |
| `/programs/{id}` | Program details and reviews | Public |
| `/programs/create` | Create new program | TRAINER |
| `/programs/{id}/edit` | Edit program | TRAINER |
| `/enrollments` | My enrollments | USER |
| `/enrollments/{id}` | Enrollment details and progress | USER |
| `/reviews/new` | Add review | USER |
| `/admin/users` | User management | ADMIN |
| `/profile` | View profile | USER |
| `/profile/edit` | Edit profile | USER |

---

## Getting Started

### Prerequisites
- Java 17+
- MySQL 8+
- Maven 3.8+

### Setup

1. Clone the repository:
```bash
git clone https://github.com/YOUR_USERNAME/fitjourney-app.git
cd fitjourney-app
```

2. Create the database:
```sql
CREATE DATABASE fitjourney;
```

3. Configure `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/fitjourney
spring.datasource.username=YOUR_DB_USERNAME
spring.datasource.password=YOUR_DB_PASSWORD
spring.jpa.hibernate.ddl-auto=update
```

4. Run the application:
```bash
mvn spring-boot:run
```

5. Open in browser: `http://localhost:8080`

---

## Project Structure

```
src/main/java/com/fitjourney/
├── entity/
│   ├── User.java
│   ├── WorkoutProgram.java
│   ├── Enrollment.java
│   └── WorkoutReview.java
├── repository/
│   ├── UserRepository.java
│   ├── WorkoutProgramRepository.java
│   ├── EnrollmentRepository.java
│   └── WorkoutReviewRepository.java
├── service/
│   ├── UserService.java
│   ├── WorkoutProgramService.java
│   ├── EnrollmentService.java
│   └── WorkoutReviewService.java
├── controller/
│   ├── HomeController.java
│   ├── AuthController.java
│   ├── ProgramController.java
│   ├── EnrollmentController.java
│   ├── ReviewController.java
│   └── AdminController.java
├── dto/
│   ├── RegisterDto.java
│   ├── ProgramDto.java
│   ├── EnrollmentDto.java
│   └── ReviewDto.java
└── exception/
    ├── ProgramNotFoundException.java
    ├── AlreadyEnrolledException.java
    └── UnauthorizedAccessException.java
```

---

## License

This project was developed as part of the **Spring Fundamentals – Regular Exam @ SoftUni, May 2026**.
