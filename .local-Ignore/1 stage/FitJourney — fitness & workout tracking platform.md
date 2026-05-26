- > # Идея 3: **FitJourney — fitness & workout tracking platform**
  
  Платформа, в която:  
- треньори публикуват тренировъчни програми
- потребители купуват/записват програми
- следят прогрес
- оставят ревюта
  
  Това е много добър domain за Spring project, защото:  
- има естествени CRUD операции
- има роли
- има scheduling
- има microservice логика
- лесно се правят functionalities
  
---
- # Архитектура
- # Main Application
  
  ```
  fitjourney-app
  ```
  
---
- # REST Microservice
  
  ```
  fitjourney-subscription-service
  ```
  
  Отговаря за:  
- subscriptions
- membership plans
- billing periods
- renewals
  
---
- # Main App Entities
- ## 1. User
  
  ```
  id
  username
  email
  password
  role
  firstName
  lastName
  profilePicture
  ```
  
---
- ## 2. WorkoutProgram
  
  ```
  id
  title
  description
  difficulty
  durationWeeks
  price
  active
  trainer
  ```
  
---
- ## 3. Enrollment
  
  ```
  id
  user
  workoutProgram
  enrolledAt
  status
  progressPercentage
  ```
  
---
- ## 4. WorkoutReview
  
  ```
  id
  user
  workoutProgram
  rating
  comment
  createdAt
  ```
  
---
- # Microservice Entity
- ## SubscriptionPlan
  
  ```
  id
  name
  monthlyPrice
  durationMonths
  active
  renewalEnabled
  ```
  
---
- # Main App Functionalities
- ## 1. Trainer creates workout program
  
  ```
  POST /programs
  ```
  
---
- ## 2. Trainer edits workout program
  
  ```
  PUT /programs/{id}
  ```
  
---
- ## 3. Trainer deactivates workout program
  
  ```
  DELETE /programs/{id}
  ```
  
---
- ## 4. User enrolls in program
  
  ```
  POST /enrollments
  ```
  
---
- ## 5. User updates progress
  
  ```
  PUT /enrollments/{id}/progress
  ```
  
---
- ## 6. User adds review
  
  ```
  POST /reviews
  ```
  
---
- # Microservice Functionalities
- ## 1. Create subscription
  
  ```
  POST /api/subscriptions
  ```
  
---
- ## 2. Renew subscription
  
  ```
  PUT /api/subscriptions/{id}/renew
  ```
  
---
- ## GET endpoint
  
  ```
  GET /api/subscriptions/{id}
  ```
  
---
- # Roles
- ## USER
- enrolls
- tracks progress
- reviews
  
---
- ## TRAINER
- creates workout programs
- edits own programs
  
---
- ## ADMIN
- manages users
- changes roles
- moderates content
  
---
- # Pages
- Home
- Register
- Login
- Dashboard
- Programs list
- Program details
- Create program
- Edit program
- My enrollments
- Enrollment details
- Add review
- Admin users
- Profile
- Edit profile
  
---
- # Scheduling идеи
- ## Cron job
  
  Всеки ден:  
  
  ```
  archive inactive workout programs
  ```
  
---
- ## Fixed rate job
  
  На всеки 10 мин:  
  
  ```
  update expired enrollments
  ```
  
---
- # Cache идеи
  
  ```
  Java
  
  ```
  @Cacheable("programs")  
  ```
  ```
  
  Кеширай:  
- workout list
- workout details
  
---