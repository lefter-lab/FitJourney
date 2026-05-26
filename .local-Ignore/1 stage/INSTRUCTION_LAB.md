# Smart Wallet Web App - Exercise Problem Description

## 📱 Project Overview

A **Smart Wallet Web App** is a digital platform that enables users to manage their finances, perform secure fund transfers, and monitor transactions conveniently from their browser. This is a practical university assignment combining all Spring Core concepts.

---

## 🏗️ Architecture Model

### Model Layer - Entities

The application uses a layered architecture with the following entities:

### **User**
- **id** – UUID
- **username** – String (unique identifier)
- **firstName** – String
- **lastName** – String
- **profilePicture** – String (URL)
- **email** – String
- **password** – String (encoded)
- **role** – UserRole enum (ADMIN, USER)
- **country** – Country enum (BULGARIA, GERMANY, FRANCE)
- **isActive** – Boolean (indicates active status)
- **createdOn** – LocalDateTime
- **updatedOn** – LocalDateTime
- **subscriptions** – List<Subscription> (user's subscriptions)
- **wallets** – List<Wallet> (user's wallets)

### **Wallet**
- **id** – UUID
- **owner** – User (ManyToOne relation)
- **status** – WalletStatus enum (ACTIVE, INACTIVE)
- **balance** – BigDecimal
- **currency** – Currency
- **frozenAmount** – BigDecimal
- **amountInTheWallet** – BigDecimal
- **createdOn** – LocalDateTime (wallet creation date)
- **updatedOn** – LocalDateTime (last update date)

### **Subscription**
- **id** – UUID
- **owner** – User (ManyToOne relation)
- **status** – SubscriptionStatus enum (ACTIVE, COMPLETED, TERMINATED)
- **type** – SubscriptionType enum (DEFAULT, STANDARD, PREMIUM, ULTIMATE)
- **period** – SubscriptionPeriod enum (MONTHLY, YEARLY)
- **price** – BigDecimal
- **renewalAllowed** – Boolean
- **createdOn** – LocalDateTime
- **completedOn** – LocalDateTime

### **Transaction**
- **id** – UUID
- **owner** – User (ManyToOne)
- **type** – TransactionType enum (DEPOSIT, WITHDRAWAL, TRANSFER)
- **status** – TransactionStatus enum (SUCCEEDED, FAILED)
- **amount** – BigDecimal
- **balanceLeft** – BigDecimal
- **currency** – Currency
- **description** – String
- **failureReason** – String (optional)
- **sender** – String (wallet identifier from)
- **receiver** – String (wallet identifier to)
- **createdOn** – LocalDateTime

---

## 📊 Enumerations

### UserRole
- ADMIN
- USER

### Country
- BULGARIA
- GERMANY
- FRANCE

### WalletStatus
- ACTIVE
- INACTIVE

### SubscriptionStatus
- ACTIVE
- COMPLETED
- TERMINATED

### SubscriptionType
- DEFAULT
- STANDARD
- PREMIUM
- ULTIMATE

### SubscriptionPeriod
- MONTHLY
- YEARLY

### TransactionType
- DEPOSIT
- WITHDRAWAL
- TRANSFER

### TransactionStatus
- SUCCEEDED
- FAILED

---

## 💾 Data Access Layer

The application must support basic **CRUD operations** (Create, Read, Update, Delete). Implement repositories for every entity to persist data.

### Repositories Required:
- **UserRepository** - extends JpaRepository<User, UUID>
- **WalletRepository** - extends JpaRepository<Wallet, UUID>
- **SubscriptionRepository** - extends JpaRepository<Subscription, UUID>
- **TransactionRepository** - extends JpaRepository<Transaction, UUID>

---

## 🔐 Business Logic Layer

### User Registration Flow

When a new user registers, they need:

#### 1. **Account Creation**
- Validate that username is unique
- Store user details securely (password must be encrypted with BCrypt)
- Set default role as USER
- Set isActive to true

#### 2. **Default Wallet Creation**
- Automatically create a wallet for the user
- Initial Balance: **€20**
- Status: **ACTIVE**
- Currency: **EUR**

#### 3. **Default Subscription Setup**
- Assign a free DEFAULT subscription to the user
- Type: DEFAULT
- Period: MONTHLY (from configuration)
- Price: €0 (free)
- Renewal allowed for monthly subscriptions only

#### Registration Method Structure:
```
public UserDTO registerUser(UserRegisterRequest request) {
  // 1. Check if username already exists
  // 2. Encode password using BCrypt
  // 3. Map UserRegisterRequest to User Entity
  // 4. Save User in database
  // 5. Create default Wallet for user
  // 6. Create default Subscription for user
  // 7. Return UserDTO (never Entity!)
}
```

---

### Login Functionality

The login validates a user's credentials and ensures only authorized users gain access.

#### Steps:
1. Verify the username exists in database
2. Confirm the password matches securely (compare encoded password)
3. Return the logged-in user

---

### Wallet Operations

#### **Top-Up: Adding Funds to Wallets**

Users can increase their wallet balance by performing a top-up operation.

Steps:
1. Retrieve the wallet using its ID
2. Verify wallet is **ACTIVE** (INACTIVE wallets are not eligible)
3. Add the top-up amount to wallet balance
4. Record the transaction for auditing

---

#### **Charge: Deducting Funds from Wallets**

A charge represents an outgoing payment from a wallet.

Steps:
1. Retrieve the wallet using its ID
2. Ensure wallet is **ACTIVE** and has sufficient funds
3. Deduct the specified amount from balance
4. Record the transaction and save it

⚠️ **IMPORTANT**: Always return a transaction for every charge (failed or succeeded) for tracking purposes.

---

## 🎯 Key Requirements

### Currency & Localization
- **Default Currency**: EUR (Euro)
- **Supported Countries**: Bulgaria, Germany, France

### Password Security
- ✅ Never store passwords in plain text
- ✅ Use BCrypt for password encoding
- ✅ Use Spring Security's PasswordEncoder bean

### User Status
- New users are automatically set to **isActive = true**
- Users can be deactivated but still retain their data

### Subscription Model
- **DEFAULT**: Free, provided to all new users
- **STANDARD**: Premium features
- **PREMIUM**: Advanced features
- **ULTIMATE**: Full access

### Renewal Policy
- Monthly subscriptions can be automatically renewed
- Yearly subscriptions require manual renewal

---

## 🔄 Data Flow Diagram

```
UI Registration Form (Presentation Layer)
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
User Entity (saved in DB)
         ↓
Create Default Wallet
         ↓
Create Default Subscription
         ↓
UserService returns UserDTO
         ↓
UI shows result
```

---

## 📋 Implementation Checklist

### Entities & Enums
- [ ] Create all Enums (UserRole, Country, WalletStatus, etc.)
- [ ] Create User entity with all fields and annotations
- [ ] Create Wallet entity with OneToMany relation
- [ ] Create Subscription entity with configurations
- [ ] Create Transaction entity with join columns

### Repositories
- [ ] Create UserRepository with custom methods
- [ ] Create WalletRepository
- [ ] Create SubscriptionRepository
- [ ] Create TransactionRepository

### DTOs & Mappers
- [ ] Create UserDTO
- [ ] Create UserRegisterRequest
- [ ] Create UserMapper with BCrypt encoding

### Configuration
- [ ] Create PasswordEncoderConfig bean
- [ ] Configure BCryptPasswordEncoder

### Services
- [ ] Create UserService
- [ ] Implement registerUser() method
- [ ] Implement login() method
- [ ] Create WalletService
- [ ] Create SubscriptionService
- [ ] Create TransactionService

### Database
- [ ] Update application-dev.properties with correct database name
- [ ] Configure Hibernate DDL auto: update
- [ ] Test database connection

---

## 🔧 Configuration Properties

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/SmartWalletApp_2026
spring.datasource.username=root
spring.datasource.password=[YOUR_PASSWORD]
spring.jpa.hibernate.ddl-auto=update

# Wallet Configuration
wallet.default.balance=20.00
wallet.default.currency=EUR

# Subscription Configuration
subscription.default.type=DEFAULT
subscription.default.price=0.00
subscription.default.period=MONTHLY
```

---

## 🚀 Expected Output

After successful registration and login:
- User has one active wallet with €20 balance
- User has one DEFAULT subscription (free)
- User can log in with username and password
- User can top-up wallet and be charged for subscriptions
- All transactions are recorded in database

---

*Document Date: 2026-05-21*
*Course: Spring Fundamentals*
*University Assignment*

