# Workflow: Spring Core - Smart Wallet Application - Лекция 1

## Преглед на курса и Архитектура

### Фаза 1: Въведение и Преглед
- [ ] Запознаване с учителя - Кавян Костадинов
- [ ] Преглед на предходни курсове (Java OOP, Java Advanced)
- [ ] Обяснение на практичния подход - писане на реална апликация вместо типични упражнения
- [ ] Представяне на Smart Wallet Application концепция

### Фаза 2: Разбиране на Layered Architecture
- [ ] Изучаване на Layered Architecture диаграма
- [ ] Разбиране на трите слоя:
  - **Data Access Layer (DAL)** - Repositories + Entities
  - **Business Logic Layer (BLL)** - Services
  - **Presentation Layer (PL)** - Controllers + View
- [ ] Разбиране на Data Transfer Objects (DTO) концепция
- [ ] Разбиране на взаимодействието между слоевете:
  - DTO се движи нагоре към Presentation Layer
  - Entity се движи между DAL и BLL
  - DTO пътува надолу от Presentation Layer

### Фаза 3: Преглед на Smart Wallet Функционалности
- [ ] Разбиране на потребителски функции:
  - Регистрация
  - Логин
  - Wallet управление (множество портфейли)
  - Subscriptions (Free Default, Standard, Premium)
  - Transactions (преводи между потребители)
  - Upgrade subscriptions
- [ ] Преглед на дефолтните баланси и функции

### Фаза 4: Преглед на Entity-Relationship диаграма
- [ ] Разбиране на таблици:
  - **User** (основен потребител)
  - **Wallet** (портфейли на потребител)
  - **Subscription** (абонаментни нива)
  - **Transaction** (транзакции между потребители)
- [ ] Разбиране на връзките:
  - User → Wallet (One-to-Many)
  - User → Subscription (One-to-Many)
  - User → Transaction (One-to-Many)
  - Transaction → User (Many-to-One)

---

## Раздел 1: Настройка и Конфигурация

### Стъпка 1: Подготовка на Проекта
- [ ] Изтегляне на Smart Wallet Application ресурсите
- [ ] Отваряне на папката в IntelliJ IDEA
- [ ] Проверка на pom.xml за необходимите dependencies
- [ ] Потвърждаване на Spring версия (3.4.0 или подобна)
- [ ] Включване на необходимите библиотеки:
  - Spring Data JPA
  - Thymeleaf
  - Spring Web
  - MySQL Connector
  - Spring Configuration
  - Lombok (за @Data, @Getter, @Setter, @Builder, @NoArgsConstructor, @AllArgsConstructor)

### Стъпка 2: Конфигурация на Database Профил
- [ ] Отваряне на Run Configuration в IntelliJ
- [ ] Добавяне на Active Profile: **dev**
- [ ] Отваряне на application-dev.properties файл
- [ ] Конфигуриране на database настройки:
  - Server: localhost
  - Port: 3306
  - Username: root (по дефолт)
  - Password: [вашата парола]
  - Database name: SmartWalletApp_2026
  - DDL auto: update (за автоматично създаване на таблици)

### Стъпка 3: Проверка на MySQL Connection в IntelliJ
- [ ] Отваряне на Database Tool в IntelliJ
- [ ] Добавяне на ново MySQL connection
- [ ] Въвеждане на connection детайли
- [ ] Клик на "Test Connection" - потвърждение на успешна връзка
- [ ] Включване на Annotation Processing в IntelliJ (за Lombok)

---

## Раздел 2: Създаване на Структура на Пакетите

### Стъпка 4: Организиране на Пакетите по Слоеве
- [ ] В `app` пакет създаване на основна структура:

```
app/
├── model/
│   ├── entity/
│   │   ├── user/
│   │   ├── wallet/
│   │   ├── subscription/
│   │   └── transaction/
│   ├── dto/
│   │   └── user/
│   └── mapper/
│       └── user/
├── repository/
│   ├── user/
│   ├── wallet/
│   ├── subscription/
│   └── transaction/
├── service/
│   ├── user/
│   ├── wallet/
│   ├── subscription/
│   └── transaction/
├── config/
│   └── bean/
└── web/
    └── (за следваща лекция)
```

---

## Раздел 3: Дефиниране на Entities

### Стъпка 5: Създаване на User Entity
- [ ] Навигация: `model/entity/user/`
- [ ] Създаване на новия пакет `enum` в `user`
- [ ] Създаване на enum `UserRole` с стойности:
  - ADMIN
  - USER
- [ ] Създаване на enum `Country` с стойности:
  - Bulgaria
  - Germany
  - France
- [ ] Създаване на нов Java class `User` със следните поля:

```
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

Private UUID id
Private String username (Unique, NotNull)
Private String password
Private String firstName
Private String lastName
Private String email
Private String profilePicture
Private UserRole role (Default: USER)
Private Country country
Private Boolean isActive (Default: true)
Private LocalDateTime createdOn
Private LocalDateTime updatedOn
Private List<Wallet> wallets
Private List<Subscription> subscriptions
Private List<Transaction> transactions
```

- [ ] Добавяне на анотации:
  - @Id за id
  - @GeneratedValue(strategy = GenerationType.UUID)
  - @Enumerated(EnumType.STRING) за role и country
  - @Column(unique = true, nullable = false) за username
  - @OneToMany за wallets, subscriptions, transactions

### Стъпка 6: Създаване на Wallet Entity
- [ ] Навигация: `model/entity/wallet/`
- [ ] Създаване на enum `WalletStatus` с стойности:
  - ACTIVE
  - INACTIVE
- [ ] Създаване на нов Java class `Wallet`:

```
@Entity
@Table(name = "wallets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

Private UUID id
Private User owner (ManyToOne relation)
Private WalletStatus status (Default: ACTIVE)
Private BigDecimal balance
Private Currency currency
Private BigDecimal frozenAmount
Private BigDecimal amountInTheWallet
Private LocalDateTime createdOn
Private LocalDateTime updatedOn
```

- [ ] Добавяне на анотации:
  - @ManyToOne с @JoinColumn(name = "owner_id")

### Стъпка 7: Създаване на Subscription Entity
- [ ] Навигация: `model/entity/subscription/`
- [ ] Създаване на enum `SubscriptionStatus` с стойности:
  - ACTIVE
  - COMPLETED
  - TERMINATED
- [ ] Създаване на enum `SubscriptionPeriod` с стойности:
  - MONTHLY
  - YEARLY
- [ ] Създаване на enum `SubscriptionType` с стойности:
  - DEFAULT
  - STANDARD
  - PREMIUM
- [ ] Създаване на нов Java class `Subscription`:

```
@Entity
@Table(name = "subscriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

Private UUID id
Private User owner (ManyToOne relation)
Private SubscriptionStatus status
Private SubscriptionType type
Private SubscriptionPeriod period
Private BigDecimal price
Private Boolean renewalAllowed
Private LocalDateTime createdOn
Private LocalDateTime completedOn
```

- [ ] Добавяне на анотации @Column(nullable = false) за основни полета

### Стъпка 8: Създаване на Transaction Entity
- [ ] Навигация: `model/entity/transaction/`
- [ ] Създаване на enum `TransactionType` с стойности:
  - DEPOSIT
  - WITHDRAWAL
  - TRANSFER
- [ ] Създаване на enum `TransactionStatus` с стойности:
  - SUCCEED
  - FAILED
- [ ] Създаване на нов Java class `Transaction`:

```
@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

Private UUID id
Private User owner (ManyToOne relation)
Private TransactionType type
Private TransactionStatus status
Private BigDecimal amount
Private BigDecimal balanceLeft
Private Currency currency
Private String description
Private String failureReason (nullable)
Private User sender
Private User receiver
Private LocalDateTime createdOn
```

---

## Раздел 4: Създаване на Repository слой

### Стъпка 9: Создаване на User Repository
- [ ] Навигация: `repository/user/`
- [ ] Създаване на interface `UserRepository` extends `JpaRepository<User, UUID>`
- [ ] Добавяне на анотация @Repository
- [ ] Дефиниране на custom методи:
  - `Optional<User> findByUsername(String username)`

### Стъпка 10: Создаване на останалите Repositories
- [ ] Навигация: `repository/wallet/`
- [ ] Създаване на `WalletRepository` extends `JpaRepository<Wallet, UUID>`
- [ ] Добавяне на анотация @Repository
- [ ] Повтаряне за:
  - `repository/subscription/SubscriptionRepository`
  - `repository/transaction/TransactionRepository`

---

## Раздел 5: Създаване на DTO слой

### Стъпка 11: Създаване на User DTOs
- [ ] Навигация: `model/dto/user/`
- [ ] Създаване на `UserDTO` (за връщане към Presentation Layer):

```
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

Private UUID id
Private String username
Private String firstName
Private String lastName
Private String email
Private String profilePicture
Private UserRole role
Private Country country
Private Boolean isActive
Private LocalDateTime createdOn
Private LocalDateTime updatedOn
```

- [ ] Създаване на `UserRegisterRequest` (за получаване от Presentation Layer):

```
@Value
(или @Data ако има проблеми)

Private String username
Private String password
Private Country country
```

---

## Раздел 6: Създаване на Mapper слой

### Стъпка 12: Създаване на User Mapper
- [ ] Навигация: `model/mapper/user/`
- [ ] Създаване на class `UserMapper`:

```
@Component
@Data

Private PasswordEncoder passwordEncoder
```

- [ ] Добавяне на метод:

```
Public UserEntity toEntity(UserRegisterRequest request) {
  return User.builder()
    .username(request.getUsername())
    .password(passwordEncoder.encode(request.getPassword()))
    .country(request.getCountry())
    .role(UserRole.USER)
    .isActive(true)
    .createdOn(LocalDateTime.now())
    .updatedOn(LocalDateTime.now())
    .build();
}
```

---

## Раздел 7: Конфигурация на Bean-и

### Стъпка 13: Създаване на Bean Configuration
- [ ] Навигация: `config/bean/`
- [ ] Създаване на class `PasswordEncoderConfig`:

```
@Configuration

@Bean
Public PasswordEncoder passwordEncoder() {
  return new BCryptPasswordEncoder();
}
```

---

## Раздел 8: Създаване на Service слой

### Стъпка 14: Създаване на User Service
- [ ] Навигация: `service/user/`
- [ ] Създаване на class `UserService`:

```
@Service
@RequiredArgsConstructor
@Data

Private UserRepository userRepository
Private UserMapper userMapper
Private PasswordEncoder passwordEncoder
```

- [ ] Добавяне на методи:

#### Метод 1: Register User
```
Public UserDTO registerUser(UserRegisterRequest request) {
  // 1. Проверка далиUsername вече съществува
  if (userRepository.findByUsername(request.getUsername()).isPresent()) {
    throw new RuntimeException("User with this username already exists");
    // TODO: Създаване на Custom Exception
  }
  
  // 2. Енкодиране на паролата
  String encodedPassword = passwordEncoder.encode(request.getPassword());
  request.setPassword(encodedPassword);
  
  // 3. Мап UserRegisterRequest към User Entity
  User user = userMapper.toEntity(request);
  
  // 4. Запазване на User в база
  User savedUser = userRepository.save(user);
  
  // 5. Върнете UserDTO (не Entity!)
  return convertToDTO(savedUser);
}
```

#### Метод 2: Convert to DTO
```
Private UserDTO convertToDTO(User user) {
  return UserDTO.builder()
    .id(user.getId())
    .username(user.getUsername())
    .firstName(user.getFirstName())
    .lastName(user.getLastName())
    .email(user.getEmail())
    .profilePicture(user.getProfilePicture())
    .role(user.getRole())
    .country(user.getCountry())
    .isActive(user.getIsActive())
    .createdOn(user.getCreatedOn())
    .updatedOn(user.getUpdatedOn())
    .build();
}
```

---

## Раздел 9: Стартиране и Тестване

### Стъпка 15: Подготовка на Стартиране
- [ ] Проверка че всички зависимости са конфигурирани
- [ ] Проверка че Active Profile е зададен като "dev"
- [ ] Проверка че MySQL сървърът работи
- [ ] Проверка че Annotation Processing е включен

### Стъпка 16: Первия Старт на Апликацията
- [ ] Старт на Application.java
- [ ] Очакване за успешен startup без грешки
- [ ] Проверка на DDL логове за създаване на таблици
- [ ] Проверка на MySQL че таблиците са създадени в базата

---

## Забележки и Принципи

### Архитектурни Принципи
- Никога не пращаме Entity нагоре към Presentation Layer - използваме DTO
- Паролите се криптират с BCrypt - никога не се запазват в plain text
- Mapper слоят отговаря за конвертирането между DTO и Entity
- Bean Configuration се използва за외ни biblioteki зависимости

### Следваща Лекция
- Создаване на Controllers (Presentation Layer)
- Работа с Thymeleaf шаблоните
- Интеграция на Services с Controllers
- Обработка на HTTP Request/Response

### Важни Ремаркования
- **Feature-based vs Layer-based архитектура**: За напреднали проекти се препоръчва Feature-based, но за начинаещи Layer-based е по-добро за разбиране
- **OneBlock Lombok**: Използваме за спестяване на boilerplate код
- **TODO**: Създаване на Custom Exception вместо RuntimeException за по-добра обработка на грешки

---

## Диаграма на Флоу на Регистрация

```
UI Form (Presentation Layer)
         ↓
UserRegisterRequest (DTO)
         ↓
UserService.registerUser()
         ↓
UserMapper.toEntity()
         ↓
PasswordEncoder.encode()
         ↓
UserRepository.save()
         ↓
User Entity (сохранена в БД)
         ↓
UserService връща UserDTO
         ↓
UI показва резултат
```

---

## Чек-лист за Завършване

- [ ] Всички Entities създадени със правилни анотации
- [ ] Всички Repositories създадени със методи
- [ ] User DTOs создадени
- [ ] UserMapper работещ със Password Encoding
- [ ] PasswordEncoderConfig Bean конфигуриран
- [ ] UserService регистрираща методи работещи
- [ ] Апликацията стартира без грешки
- [ ] Таблиците се създават в базата

---

## Команди за Запомняне

```
// Ако трябва да пресъздам базата:
// 1. Отворете MySQL Workbench
// 2. DROP DATABASE SmartWalletApp_2026;
// 3. Рестартирайте апликацията - тя ще пересъздаде базата

// За проверка на логове:
// Погледнете Application Console за SQL statements
```

---

*Документ дата: 21.05.2026*
*Курс: Spring Fundamentals*
*Преподавател: Кавян Костадинов*
