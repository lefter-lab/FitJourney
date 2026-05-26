# FitJourney
## Fitness & Workout Tracking Platform
### Requirements Document — University Project
> Spring Boot | REST Microservice | Thymeleaf | MySQL
> GitHub: [github.com/lefter-lab/FitJourney](https://github.com/lefter-lab/FitJourney)

---

## 1. Project Overview

FitJourney is a web platform for managing and tracking fitness workouts. The platform connects trainers and users — trainers publish workout programs, while users enroll in them, track their progress, and leave reviews. The project is implemented as a Spring Boot monolithic application with a separate REST microservice for subscription management.

---

## 2. Architecture

### 2.1 Main Application
**Module:** `fitjourney-app`

The main Spring Boot application handles the entire user interface and business logic — user management, programs, enrollments, and reviews.

### 2.2 REST Microservice
**Module:** `fitjourney-subscription-service`

A separate Spring Boot microservice responsible for:
- Subscription plans
- Billing periods
- Renewals
- Membership management

---

## 3. Core Entities

### 3.1 Main Application Entities

| Entity | Fields | Description |
|---|---|---|
| User | id, username, email, password, role, firstName, lastName, profilePicture | Platform users with different roles |
| WorkoutProgram | id, title, description, difficulty, durationWeeks, price, active, trainer | A workout program created by a trainer |
| Enrollment | id, user, workoutProgram, enrolledAt, status, progressPercentage | A user's enrollment in a program with progress tracking |
| WorkoutReview | id, user, workoutProgram, rating, comment, createdAt | A user's review and rating of a workout program |

### 3.2 Microservice Entity

| Entity | Fields | Description |
|---|---|---|
| SubscriptionPlan | id, name, monthlyPrice, durationMonths, active, renewalEnabled | Subscription plan in the microservice — manages memberships and renewals |

---

## 4. Roles & Permissions

| Role | Permissions & Actions |
|---|---|
| USER | Enroll in programs, track progress, add reviews, manage personal profile |
| TRAINER | Create workout programs, edit and deactivate own programs |
| ADMIN | Manage users, change roles, moderate content |

---

## 5. Functionalities

### 5.1 Main Application — REST Endpoints

| # | Description | Method | Endpoint | Role |
|---|---|---|---|---|
| 1 | Trainer creates a workout program | POST | /programs | TRAINER |
| 2 | Trainer edits a workout program | PUT | /programs/{id} | TRAINER |
| 3 | Trainer deactivates a workout program | DELETE | /programs/{id} | TRAINER |
| 4 | User enrolls in a program | POST | /enrollments | USER |
| 5 | User updates progress | PUT | /enrollments/{id}/progress | USER |
| 6 | User adds a review | POST | /reviews | USER |

### 5.2 Microservice — REST Endpoints

| # | Description | Method | Endpoint |
|---|---|---|---|
| 1 | Create a subscription | POST | /api/subscriptions |
| 2 | Renew a subscription | PUT | /api/subscriptions/{id}/renew |
| 3 | Get subscription by ID | GET | /api/subscriptions/{id} |

---

## 6. Pages (UI)

The application includes the following Thymeleaf pages:

| Page | URL | Access |
|---|---|---|
| Home | / | All |
| Register | /register | Anonymous |
| Login | /login | Anonymous |
| Dashboard | /dashboard | Authenticated |
| Programs list | /programs | All |
| Program details | /programs/{id} | All |
| Create program | /programs/new | TRAINER |
| Edit program | /programs/{id}/edit | TRAINER |
| My enrollments | /enrollments/my | USER |
| Enrollment details | /enrollments/{id} | USER |
| Add review | /reviews/new | USER |
| Admin users | /admin/users | ADMIN |
| Profile | /profile | Authenticated |
| Edit profile | /profile/edit | Authenticated |

---

## 7. Scheduling

### 7.1 Cron Job
Runs every day at midnight.
- Archives inactive workout programs
- Annotation: `@Scheduled(cron = "0 0 0 * * *")`

### 7.2 Fixed Rate Job
Runs every 10 minutes.
- Updates expired enrollments
- Annotation: `@Scheduled(fixedRate = 600000)`

---

## 8. Caching

Spring Cache (`@EnableCaching`) is applied to improve performance:
- `@Cacheable("programs")` — caches the list of workout programs
- `@Cacheable("programs")` — caches the details of a specific program
- `@CacheEvict` — clears the cache when a program is created, edited, or deactivated

---

## 9. Technology Stack

| Component | Technology |
|---|---|
| Backend Framework | Spring Boot |
| Template Engine | Thymeleaf |
| Database | MySQL |
| ORM | Spring Data JPA / Hibernate |
| Security | Spring Security |
| Scheduling | Spring @Scheduled |
| Caching | Spring Cache |
| Build Tool | Maven |
| Version Control | Git / GitHub (lefter-lab/FitJourney) |
| Microservice Communication | REST (HTTP) |

---

## 10. Implementation Steps

Following the university guidelines (Step 1 — Step 6):

1. **Define the idea and sketch the UI**
   - Main goal, entities, user flows
   - Wireframes for the core pages

2. **Initialize the Spring Boot project**
   - Spring Initializr — Web, JPA, MySQL, Security, Thymeleaf, DevTools
   - Configure `application.properties` and run the app

3. **Define entities and database schema**
   - Create User, WorkoutProgram, Enrollment, WorkoutReview
   - JPA relations (`@OneToMany`, `@ManyToOne`, `@ManyToMany`)

4. **Implement basic functionality (CRUD)**
   - Repository, Service, Controller layers
   - User registration and login

5. **Build the UI (Thymeleaf + HTML/CSS)**
   - Home, Login, Register, Dashboard, Programs pages

6. **Spring MVC — Controllers and Endpoints**
   - Connect UI to backend
   - Form handling and validation

7. **End-to-end testing**
   - Manually test all features

8. **Add additional functionalities**
   - Scheduling, Cache, Microservice integration
   - Admin panel, roles, profile management
