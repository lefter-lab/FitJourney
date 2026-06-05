> # 🏋️ FitJourney
>
> A fitness and workout tracking platform built with Spring Boot, where trainers publish workout programs and users enroll, track progress, and leave reviews.
>
> ## Tech Stack
>
> Layer
>
> Technology
>
> Language
>
> Java 17
>
> Framework
>
> Spring Boot 3.4.0
>
> Build Tool
>
> Maven
>
> Database
>
> MySQL
>
> ORM
>
> Spring Data JPA / Hibernate
>
> Backend
>
> Spring Framework
>
> Frontend
>
> Spring MVC + Thymeleaf
>
> UI
>
> HTML, CSS, Bootstrap
>
> Security
>
> Spring Security, BCrypt
>
> Scheduling
>
> Spring @Scheduled
>
> Caching
>
> Spring @Cacheable
>
> Version Control
>
> Git / GitHub
>
> ## Features
>
> -   User registration and login with BCrypt password hashing
>
> -   Session-based authentication with `user_id` stored in session
>
> -   Role-based access control: USER, TRAINER, ADMIN
>
> -   Trainers can create, edit, deactivate, and delete workout programs
>
> -   Users can enroll in workout programs
>
> -   Users can track and update their enrollment progress
>
> -   Users can submit reviews and ratings for workout programs
>
> -   Duplicate enrollments are prevented
>
> -   Duplicate reviews are prevented
>
> -   Cached program listings for performance
>
> -   Scheduled tasks running automatically in the background
>
>
> ## Functionalities
>
> #
>
> Functionality
>
> Method
>
> Endpoint
>
> Role
>
> 1
>
> Create workout program
>
> POST
>
> `/programs/create`
>
> TRAINER
>
> 2
>
> Edit workout program
>
> POST
>
> `/programs/{id}/edit`
>
> TRAINER
>
> 3
>
> Deactivate workout program
>
> POST
>
> `/programs/{id}/deactivate`
>
> TRAINER
>
> 4
>
> Delete workout program
>
> POST
>
> `/programs/{id}/delete`
>
> TRAINER
>
> 5
>
> Enroll in program
>
> POST
>
> `/enrollments/{programId}/enroll`
>
> USER
>
> 6
>
> Update progress
>
> POST
>
> `/enrollments/{id}/progress`
>
> USER
>
> 7
>
> Submit workout review
>
> POST
>
> `/reviews/programs/{programId}`
>
> USER
>
> ## Domain Entities
>
> -   **User** — platform account with role assignment
>
> -   **WorkoutProgram** — fitness program created by a trainer
>
> -   **Enrollment** — link between user and program, tracks progress
>
> -   **WorkoutReview** — rating and comment left by a user for a workout program
>
>
> ## Pages
>
> URL
>
> Description
>
> Access
>
> `/`
>
> Home page
>
> Public
>
> `/register`
>
> Register new account
>
> Guest
>
> `/login`
>
> Login
>
> Guest
>
> `/dashboard`
>
> Personal dashboard
>
> Authenticated
>
> `/programs/all`
>
> Browse all active programs
>
> Authenticated
>
> `/programs/create`
>
> Create new program
>
> TRAINER
>
> `/programs/{id}/edit`
>
> Edit program
>
> TRAINER
>
> `/enrollments/my`
>
> My enrollments
>
> USER
>
> `/reviews/programs/{id}`
>
> Program reviews
>
> Authenticated
>
> ## Security
>
> -   Spring Security is used for authentication and authorization
>
> -   Login is session-based
>
> -   The authenticated user's `user_id` is stored in the HTTP session
>
> -   Passwords are hashed with BCrypt
>
> -   Guest users can access only public pages such as home, login, and register
>
> -   Logged users can access the protected application pages
>
> -   Role checks are applied for trainer and admin functionality
>
>
> ## Validation and Error Handling
>
> The application uses server-side validation with Jakarta Validation.
>
> Validation is applied to:
>
> -   User registration form
>
> -   Workout program create/edit forms
>
> -   Enrollment progress update
>
> -   Workout review submission
>
>
> Business constraints include:
>
> -   A user cannot enroll twice in the same workout program
>
> -   Progress must be between 0 and 100
>
> -   A user cannot review the same workout program twice
>
> -   Missing resources are handled with custom exceptions
>
>
> ## Database
>
> -   Spring Data JPA is used for database access
>
> -   All entities use UUID primary keys
>
> -   The application uses a relational database: MySQL
>
> -   Entity relationships are defined between:
      >
      >     -   User and WorkoutProgram
>
>     -   User and Enrollment
>
>     -   WorkoutProgram and Enrollment
>
>     -   User and WorkoutReview
>
>     -   WorkoutProgram and WorkoutReview
>
>
> ## Scheduling
>
> -   **Cron job** — archives inactive workout programs
>
> -   **Fixed rate task** — updates completed enrollments to COMPLETED status
>
>
> ## Caching
>
> -   `@Cacheable("programs")` — caches the active programs list
>
> -   `@CacheEvict` — clears cache when workout programs are created, edited, deactivated, deleted, or archived
>
>
> ## Project Structure
>
>     src/main/java/com/fitjourney/fitjourney/
>     ├── config/
>     │   ├── PasswordEncoderConfig.java
>     │   └── SecurityConfig.java
>     ├── controller/
>     │   ├── AuthController.java
>     │   ├── EnrollmentController.java
>     │   ├── HomeController.java
>     │   ├── WorkoutProgramController.java
>     │   └── WorkoutReviewController.java
>     ├── dto/
>     │   ├── RegisterDto.java
>     │   ├── WorkoutProgramDto.java
>     │   └── WorkoutReviewDto.java
>     ├── entity/
>     │   ├── Enrollment.java
>     │   ├── User.java
>     │   ├── WorkoutProgram.java
>     │   └── WorkoutReview.java
>     ├── enums/
>     │   ├── DifficultyLevel.java
>     │   ├── EnrollmentStatus.java
>     │   └── UserRole.java
>     ├── exception/
>     │   ├── ProgramNotFoundException.java
>     │   └── UserNotFoundException.java
>     ├── repository/
>     │   ├── EnrollmentRepository.java
>     │   ├── UserRepository.java
>     │   ├── WorkoutProgramRepository.java
>     │   └── WorkoutReviewRepository.java
>     ├── scheduler/
>     │   └── ScheduledTasks.java
>     ├── security/
>     │   └── CustomAuthenticationSuccessHandler.java
>     └── service/
>         ├── EnrollmentService.java
>         ├── UserService.java
>         ├── WorkoutProgramService.java
>         └── WorkoutReviewService.java
>
> ## Getting Started
>
> ### Prerequisites
>
> -   Java 17
>
> -   Maven
>
> -   MySQL
>
>
> ### Setup
>
> 1.  Clone the repository:
>
>
>     git clone https://github.com/lefter-lab/FitJourney.git
>     cd FitJourney
>
> 2.  Create the database:
>
>
>     CREATE DATABASE fitjourney;
>
> 3.  Configure `src/main/resources/application.properties`:
>
>
>     spring.datasource.url=jdbc:mysql://localhost:3306/fitjourney
>     spring.datasource.username=YOUR_DB_USERNAME
>     spring.datasource.password=YOUR_DB_PASSWORD
>     spring.jpa.hibernate.ddl-auto=update
>
> 4.  Run the application:
>
>
>     mvn spring-boot:run
>
> 5.  Open in browser:
>
>
>     http://localhost:8080
>
> ## GitHub
>
> [https://github.com/lefter-lab/FitJourney](https://github.com/lefter-lab/FitJourney)