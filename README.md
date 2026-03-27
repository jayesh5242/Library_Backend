<div align="center">

# 📚 Multi-Branch College Library Network

### A Complete Digital Library Management System

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.3-6DB33F?style=for-the-badge&logo=spring&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-18.3-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![React](https://img.shields.io/badge/React-18.x-61DAFB?style=for-the-badge&logo=react&logoColor=black)](https://reactjs.org/)
[![JWT](https://img.shields.io/badge/JWT-Security-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)](https://jwt.io/)
[![Railway](https://img.shields.io/badge/Deployed-Railway-0B0D0E?style=for-the-badge&logo=railway&logoColor=white)](https://railway.app/)

**A centralized web-based library management system connecting all department libraries of a college under one unified platform.**

[🌐 Live Demo](#-live-demo) · [📖 API Docs](#-api-documentation) · [🚀 Quick Start](#-quick-start) · [📋 Features](#-features)

</div>

---

## 🌐 Live Demo

| Service | URL |
|---------|-----|
| 🚀 **Live API** | `https://librarybackend-production-67b6.up.railway.app` |
| 📖 **Swagger UI** | `https://librarybackend-production-67b6.up.railway.app/swagger-ui/index.html` |
| 🗄️ **Database** | PostgreSQL hosted on Railway |

---

## 📋 Table of Contents

- [About the Project](#-about-the-project)
- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [System Architecture](#-system-architecture)
- [Database Design](#-database-design)
- [API Modules](#-api-modules)
- [User Roles](#-user-roles)
- [Quick Start](#-quick-start)
- [Environment Variables](#-environment-variables)
- [Deployment](#-deployment)
- [Project Structure](#-project-structure)
- [Team](#-team)

---

## 🎯 About the Project

The **Multi-Branch College Library Network** solves the problems of traditional paper-based library management by providing a fully digital, automated system.

**Problems it solves:**

| Before (Manual System) | After (This System) |
|------------------------|---------------------|
| Students physically visit to check availability | Real-time online search across all branches |
| No advance reservation system | Online reservation with automated waitlist |
| Manual fine calculation with errors | Automated daily fine calculation |
| No connection between department branches | Inter-branch transfer request system |
| Paper records — lost or damaged | Digital PostgreSQL — permanent audit trail |
| Admin has zero visibility | Real-time analytics dashboard |
| No email communication | Automated email reminders via scheduler |

---

## ✨ Features

### 🔐 Authentication & Security
- JWT stateless authentication with Access Token (24h) + Refresh Token (7d)
- BCrypt password hashing — never store plain text
- Token blacklisting on logout — immediate session invalidation
- Email verification with UUID token
- Forgot password with 6-digit OTP (60-minute expiry)
- Role-based access control with `@PreAuthorize`

### 📚 Book & Inventory Management
- Complete book catalog with search, filter, and pagination
- Multi-branch inventory — same book tracked across all branches
- Real-time availability status per branch
- Book reviews and ratings (1–5 stars)
- New arrivals and trending books endpoints

### 🔄 Borrowing & Transactions
- Librarian issues and returns books with automated due date
- Maximum 2 renewals per book
- Automatic overdue detection (daily scheduler at 6 AM)
- Overdue books list per branch

### 📋 Reservation System
- Online reservation with waitlist queue management
- Status flow: PENDING → APPROVED → READY → COLLECTED
- Automatic expiry after 3 days if not collected
- Email notification when book is ready for pickup

### 🔀 Inter-Branch Transfers
- Request books from any department library
- Approval and dispatch workflow
- Automatic inventory adjustment between branches

### 💰 Fine Management
- Automatic fine calculation: Days Overdue × Rs. 2/day
- Fine status: PENDING → PAID / PARTIAL / WAIVED
- Librarian can waive fines with reason
- Complete fine history per student

### 🔔 Notifications
- In-app notification inbox per user
- Unread count for bell icon badge
- 8 notification types: DUE_REMINDER, FINE_ALERT, BOOK_READY, RETURN_SUCCESS, TRANSFER_UPDATE, PURCHASE_UPDATE, ACCOUNT_BLOCKED, GENERAL
- Admin broadcast to all users
- Librarian can send to specific users

### 📊 Analytics & Reports (Admin Only)
- Real-time dashboard: users, books, borrowings, fines, alerts
- Monthly borrowing trend (line chart data)
- Popular books ranking
- Branch comparison with percentages
- Fine collection by month
- Active users leaderboard
- Category breakdown
- 5 printable HTML reports: Inventory, Borrowing, Fines, Semester, Overdue

### ⏰ Automated Schedulers (4 Daily Jobs)
| Job | Time | Action |
|-----|------|--------|
| OverdueCheckScheduler | 6:00 AM | Marks overdue transactions |
| FineCalculationScheduler | 6:05 AM | Creates/updates fine records |
| DueDateReminderScheduler | 9:00 AM | Sends email reminders (3 days before) |
| ReservationExpiryScheduler | 8:00 AM | Expires uncollected reservations |

### 📧 Email Notifications
- Account verification email
- Role-specific welcome email
- Password reset OTP
- Due date reminder (3 days before)
- Book ready for pickup notification
- Fine alert notification
- Account blocked/unblocked notification

---

## 🛠️ Tech Stack

### Backend
| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 21 | Core language |
| Spring Boot | 4.0.3 | Application framework |
| Spring Security | Included | Auth & authorization |
| Spring Data JPA | Included | ORM / database layer |
| Spring Mail | Included | Email via Gmail SMTP |
| Spring Scheduler | Included | Automated daily jobs |
| JWT (jjwt) | 0.11.5 | Token authentication |
| BCrypt | Included | Password hashing |
| Lombok | 1.18.x | Reduce boilerplate |
| SpringDoc OpenAPI | 2.8.5 | Swagger documentation |
| PostgreSQL Driver | Latest | Database connector |
| Maven | 3.x | Build tool |

### Frontend
| Technology | Purpose |
|------------|---------|
| React.js 18 | UI framework |
| Tailwind CSS | Styling |
| Recharts | Analytics charts |
| Axios | HTTP API calls |
| React Router v6 | Navigation |
| React Query | Data fetching & caching |

### Infrastructure
| Service | Purpose |
|---------|---------|
| Railway.app | Cloud deployment |
| PostgreSQL 18 | Production database |
| GitHub | Version control |
| Docker | Containerization |

---

## 🏗️ System Architecture

```
┌──────────────────────────────────────────────────────────┐
│              FRONTEND — React.js 18 + Tailwind CSS        │
│   Student Portal | Faculty Portal | Librarian | Admin     │
└────────────────────────┬─────────────────────────────────┘
                         │ HTTP REST API (JSON + JWT Token)
┌────────────────────────▼─────────────────────────────────┐
│              BACKEND — Spring Boot 4.x                    │
│  Controllers → Services → Repositories → JPA Entities    │
│  Spring Security | JWT Filter | Email Service | Scheduler │
└────────────────────────┬─────────────────────────────────┘
                         │ Spring Data JPA / Hibernate
┌────────────────────────▼─────────────────────────────────┐
│              DATABASE — PostgreSQL 18                     │
│  12 Tables | JPA Relationships | Auto-created by Hibernate│
└──────────────────────────────────────────────────────────┘
         │                              │
┌────────▼────────┐          ┌──────────▼────────────┐
│  Gmail SMTP     │          │  Spring Scheduler      │
│  Email service  │          │  4 daily automated jobs│
└─────────────────┘          └───────────────────────┘
```

---

## 🗄️ Database Design

**12 tables** with complex JPA relationships:

| Table | Description |
|-------|-------------|
| `users` | All 4 role types: Student, Faculty, Librarian, Super Admin |
| `branches` | Department library branches |
| `books` | Book catalog with metadata |
| `book_inventory` | Copies per branch with availability tracking |
| `borrow_transactions` | Every issue and return record |
| `reservations` | Reservation queue with waitlist |
| `inter_branch_transfers` | Cross-branch transfer requests |
| `fines` | Overdue fine tracking and payment status |
| `book_reviews` | User ratings and reviews |
| `notifications` | In-app notification inbox |
| `token_blacklist` | Invalidated JWT tokens for security |
| `reading_lists` | Faculty course reading lists |

### Key Relationships
```
users ──< borrow_transactions >── books
users ──< reservations >── books
users ──< notifications
users >── branches (librarian)
borrow_transactions ──── fines (1:1)
books ──< book_inventory >── branches
```

---

## 📡 API Modules

**Total: 41+ REST APIs across 8 modules**

### Module 1 — Authentication (10 APIs)
```
POST   /api/auth/register              Register new user
POST   /api/auth/login                 Login → JWT tokens
POST   /api/auth/logout                Logout + blacklist token
POST   /api/auth/refresh-token         Rotate refresh token
POST   /api/auth/forgot-password       Send OTP to email
POST   /api/auth/reset-password        Reset with OTP
GET    /api/auth/profile               Get own profile
PUT    /api/auth/profile               Update profile
PUT    /api/auth/change-password       Change password
GET    /api/auth/verify-email/{token}  Verify email address
```

### Module 2 — Books & Branches (16 APIs)
```
GET    /api/books                      All books (paginated + filters)
GET    /api/books/{id}                 Book details
GET    /api/books/search               Search by title/author/ISBN
GET    /api/books/popular              Top borrowed books
GET    /api/books/new-arrivals         Recently added
GET    /api/books/{id}/availability    Availability across branches
POST   /api/books                      Add book (Librarian/Admin)
PUT    /api/books/{id}                 Update book
POST   /api/books/{id}/reviews         Submit review + rating
GET    /api/branches                   All branches
GET    /api/branches/{id}              Branch details
POST   /api/branches                   Create branch (Admin)
PUT    /api/branches/{id}/librarian    Assign librarian
POST   /api/inventory                  Add copies to branch
PUT    /api/inventory/{id}             Update copies
GET    /api/inventory/low-stock        Low stock alert
```

### Module 3 — Borrowing (11 APIs)
```
POST   /api/borrow/issue               Issue book to student
POST   /api/borrow/return              Process book return
POST   /api/borrow/renew/{id}          Renew loan (max 2x)
GET    /api/borrow/my                  My current borrows
GET    /api/borrow/my/history          My borrow history
GET    /api/borrow/all                 All transactions (Admin)
GET    /api/borrow/overdue             All overdue books
GET    /api/borrow/overdue/branch/{id} Overdue by branch
GET    /api/borrow/{id}                Transaction details
PUT    /api/borrow/{id}/lost           Mark book as lost
GET    /api/borrow/user/{userId}       User borrow history
```

### Module 4 — Reservations & Transfers (20 APIs)
```
POST   /api/reservations               Reserve a book
GET    /api/reservations/my            My reservations
DELETE /api/reservations/{id}          Cancel reservation
GET    /api/reservations/pending       Pending (Librarian)
PUT    /api/reservations/{id}/approve  Approve reservation
PUT    /api/reservations/{id}/ready    Mark book ready → email
PUT    /api/reservations/{id}/collected Mark collected
POST   /api/transfers                  Request inter-branch transfer
GET    /api/transfers/my               My transfers
PUT    /api/transfers/{id}/approve     Approve transfer
PUT    /api/transfers/{id}/dispatch    Dispatch book
PUT    /api/transfers/{id}/receive     Confirm received
PUT    /api/transfers/{id}/reject      Reject transfer
GET    /api/transfers/all              All transfers (Admin)
```

### Module 5 — Fines (10 APIs)
```
GET    /api/fines/my                   My pending fines
GET    /api/fines/my/total             Total amount owed
GET    /api/fines/all                  All fines (Admin)
GET    /api/fines/pending              Unpaid fines
GET    /api/fines/branch/{id}          Fines by branch
PUT    /api/fines/{id}/pay             Mark fine paid
PUT    /api/fines/{id}/waive           Waive fine
PUT    /api/fines/{id}/partial-pay     Partial payment
GET    /api/fines/user/{userId}        User fine history
GET    /api/fines/summary              Fine summary (Admin)
```

### Module 6 — User Management (11 APIs)
```
GET    /api/users                      All users (paginated)
GET    /api/users/{id}                 User details
POST   /api/users                      Create user (Admin)
PUT    /api/users/{id}                 Update user
DELETE /api/users/{id}                 Deactivate user
PUT    /api/users/{id}/role            Change role
PUT    /api/users/{id}/block           Block user
PUT    /api/users/{id}/unblock         Unblock user
GET    /api/users/search               Search users
GET    /api/users/{id}/activity        User activity stats
POST   /api/users/bulk-import          Bulk import (max 500)
```

### Module 7 — Notifications (7 APIs)
```
GET    /api/notifications/my           My notifications
PUT    /api/notifications/{id}/read    Mark one as read
PUT    /api/notifications/read-all     Mark all as read
DELETE /api/notifications/{id}         Delete notification
GET    /api/notifications/unread-count Bell badge count
POST   /api/notifications/broadcast    Send to all users (Admin)
POST   /api/notifications/send         Send to specific user
```

### Module 8 — Analytics & Reports (13 APIs)
```
GET    /api/analytics/dashboard        Complete stats overview
GET    /api/analytics/borrowing-trends Monthly trend data
GET    /api/analytics/popular-books    Top N books ranked
GET    /api/analytics/branch-comparison Branch activity %
GET    /api/analytics/overdue-summary  Overdue statistics
GET    /api/analytics/fine-collection  Monthly fine amounts
GET    /api/analytics/active-users     Top active users
GET    /api/analytics/category-breakdown Books by category
GET    /api/reports/inventory          HTML inventory report
GET    /api/reports/borrowing          HTML borrowing report
GET    /api/reports/fines              HTML fine report
GET    /api/reports/semester           HTML semester summary
GET    /api/reports/overdue            HTML overdue list
```

---

## 👥 User Roles

| Role | Access Level | Key Capabilities |
|------|-------------|------------------|
| **STUDENT** | Basic | Browse, reserve, view history, pay fines, rate books |
| **FACULTY** | Extended | Reading lists, book purchase requests, extended loan limits |
| **LIBRARIAN** | Operational | Issue/return, approve reservations, manage inventory, collect fines |
| **SUPER_ADMIN** | Full System | Analytics, user management, all branches, reports |

---

## 🚀 Quick Start

### Prerequisites
- Java 21+
- Maven 3.x
- PostgreSQL 15+
- Git

### Local Setup

**1. Clone the repository**
```bash
git clone https://github.com/jayesh5242/Library_Backend.git
cd Library_Backend
```

**2. Create PostgreSQL database**
```sql
CREATE DATABASE library_db;
```

**3. Configure application.properties**

The project uses environment variables with sensible defaults. No changes needed for local development — defaults connect to `localhost:5432/library_db`.

```properties
# Default local values are already set — just run the app!
spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/library_db}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME:postgres}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:your_password}
```

**4. Run the application**
```bash
mvn spring-boot:run
```

**5. Access Swagger UI**
```
http://localhost:9090/swagger-ui/index.html
```

**6. Test with sample credentials**

After first run, register users via Swagger or Postman:
```json
POST /api/auth/register
{
  "fullName": "Admin User",
  "email": "admin@college.edu",
  "password": "admin123",
  "role": "SUPER_ADMIN"
}
```

### Docker Setup

```bash
# Build and run with Docker Compose
docker-compose up --build

# Access at http://localhost:9090/swagger-ui/index.html
```

---

## ⚙️ Environment Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `SPRING_DATASOURCE_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://host:5432/railway` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `yourpassword` |
| `SPRING_JPA_DDL` | Hibernate DDL mode | `update` |
| `JWT_SECRET` | JWT signing secret (40+ chars) | `MySecretKey123...` |
| `JWT_EXPIRATION` | Access token TTL (ms) | `86400000` (24h) |
| `JWT_REFRESH_EXPIRATION` | Refresh token TTL (ms) | `604800000` (7d) |
| `MAIL_USERNAME` | Gmail address | `yourapp@gmail.com` |
| `MAIL_PASSWORD` | Gmail App Password (16 chars) | `abcdefghijklmnop` |
| `APP_FRONTEND_URL` | Frontend URL for email links | `https://yourapp.com` |
| `PORT` | Server port | `8080` |

> **Gmail App Password:** Go to Google Account → Security → 2-Step Verification → App Passwords → Generate

---

## 🚢 Deployment

### Deploy to Railway (Recommended — Free)

**1. Fork/clone this repo to your GitHub**

**2. Go to [railway.app](https://railway.app) → Login with GitHub**

**3. Create new project → Add PostgreSQL database**

**4. Deploy from GitHub repo → Select this repository**

**5. Set environment variables in Railway dashboard:**
```
SPRING_DATASOURCE_URL    = jdbc:postgresql://${{Postgres.PGHOST}}:${{Postgres.PGPORT}}/${{Postgres.PGDATABASE}}
SPRING_DATASOURCE_USERNAME = ${{Postgres.PGUSER}}
SPRING_DATASOURCE_PASSWORD = ${{Postgres.PGPASSWORD}}
SPRING_JPA_DDL           = update
JWT_SECRET               = YourLongProductionSecretKey1234567890
JWT_EXPIRATION           = 86400000
MAIL_USERNAME            = your@gmail.com
MAIL_PASSWORD            = your16charapppassword
APP_FRONTEND_URL         = https://your-app.up.railway.app
```

**6. Generate domain in Settings → Networking → Generate Domain**

**7. Access Swagger UI at:** `https://your-app.up.railway.app/swagger-ui/index.html`

### Deploy with Docker

```bash
# Build image
docker build -t library-backend .

# Run with environment variables
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host:5432/library_db \
  -e SPRING_DATASOURCE_PASSWORD=yourpassword \
  -e JWT_SECRET=YourSecretKey \
  -e MAIL_USERNAME=your@gmail.com \
  -e MAIL_PASSWORD=apppassword \
  library-backend
```

---

## 📁 Project Structure

```
Library_Backend/
├── src/
│   ├── main/
│   │   ├── java/com/example/Library_backend/
│   │   │   ├── controller/          # REST API endpoints (8 controllers)
│   │   │   ├── service/             # Business logic (15+ services)
│   │   │   ├── repository/          # JPA data access (13 repositories)
│   │   │   ├── entity/              # JPA entities (12 tables)
│   │   │   ├── dto/
│   │   │   │   ├── request/         # Incoming request DTOs
│   │   │   │   └── response/        # Outgoing response DTOs
│   │   │   ├── security/            # JWT filter, UserDetails, SecurityConfig
│   │   │   ├── scheduler/           # 4 automated daily jobs
│   │   │   ├── enums/               # Role, TransactionStatus, FineStatus, etc.
│   │   │   ├── exception/           # GlobalExceptionHandler
│   │   │   └── config/              # Security, CORS, Swagger config
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       ├── java/                    # Unit + Integration tests
│       └── resources/
│           └── application-test.properties  # H2 in-memory for tests
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── README.md
```

---

## 🧪 Testing

```bash
# Run all tests
mvn test

# Run with coverage report
mvn test jacoco:report
# Report at: target/site/jacoco/index.html

# Run specific test class
mvn test -Dtest=AuthServiceTest
```

**Test coverage includes:**
- Unit tests: `AuthServiceTest`, `FineServiceTest`
- Integration tests: `AuthControllerTest`
- 500+ Postman test cases across all 41 APIs

---

## 📊 Project Stats

| Metric | Value |
|--------|-------|
| Total REST APIs | 41+ |
| Database Tables | 12 |
| User Roles | 4 |
| Modules | 8 |
| Automated Schedulers | 4 |
| Email Notification Types | 8 |
| Postman Test Cases | 500+ |
| JPA Repository Interfaces | 13 |

---

## 👨‍💻 Team

This project was built as a Final Year Project by a team of 3 students from the Computer Science Department.

| Module | Developer |
|--------|-----------|
| Authentication APIs (10) | Jayesh |
| User Management APIs (11) | Jayesh |
| Notification APIs (7) | Jayesh |
| Analytics & Reports APIs (13) | Jayesh |
| Books, Branches & Inventory APIs (16) | Teammate 2 |
| Borrowing & Transactions APIs (11) | Teammate 3 |
| Reservation & Transfer APIs (20) | Teammate 3 |
| Fine Management APIs (10) | Teammate 2 |

---

## 📄 License

This project is developed for academic purposes as a Final Year Project.

---

## 🙏 Acknowledgements

- [Spring Boot](https://spring.io/projects/spring-boot) — Application framework
- [Railway](https://railway.app) — Free cloud deployment
- [SpringDoc OpenAPI](https://springdoc.org) — Swagger documentation
- [JWT.io](https://jwt.io) — JSON Web Tokens

---

<div align="center">

**Built with ☕ Java Spring Boot · 🐘 PostgreSQL · ⚛️ React.js · 🔐 JWT**

*Multi-Branch College Library Network — Final Year Project*

</div>