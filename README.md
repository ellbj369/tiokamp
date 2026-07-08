# 🏆 10 Kamp — Spring Boot Web App

A leaderboard web application for a 10-event competition.

## Features
- User registration (username + email)
- Score entry for 10 events per user
- Live leaderboard (auto-refreshes every 10 seconds)
- REST API (`/api/leaderboard`, `/api/scores`)
- H2 in-memory database for development; easy to swap to PostgreSQL

---

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+

### Run

```bash
./mvnw spring-boot:run
```

Then open: http://localhost:8080

### URLs
| URL | Description |
|-----|-------------|
| `/` | Redirects to register |
| `/users/register` | Registration form |
| `/users/{id}/scores` | Score entry for user {id} |
| `/leaderboard` | Leaderboard page |
| `/api/leaderboard` | Leaderboard as JSON |
| `/api/scores` | POST endpoint to submit scores |
| `/h2-console` | H2 database console (dev only) |

---

## Project Structure

```
src/main/java/com/tiokamp/
├── tiokampApplication.java     # Entry point
├── config/
│   └── WebConfig.java           # CORS config
├── controller/
│   ├── UserController.java      # Registration + score form
│   └── ScoreController.java     # Score API + leaderboard
├── service/
│   ├── UserService.java         # Registration logic
│   └── ScoreService.java        # Score saving + leaderboard query
├── repository/
│   ├── UserRepository.java      # JPA for users
│   └── ScoreRepository.java     # JPA for scores
├── model/
│   ├── User.java                # User entity
│   └── Score.java               # Score entity (10 events + total)
└── dto/
    ├── RegistrationRequest.java # Input validation for registration
    ├── ScoreRequest.java        # Score submission payload
    └── LeaderboardEntry.java    # Leaderboard row DTO

src/main/resources/
├── application.properties       # DB + server config
├── templates/
│   ├── register.html            # Registration page
│   ├── scores.html              # Score entry page
│   └── leaderboard.html        # Leaderboard page
└── static/css/
    └── style.css                # Shared styles
```

---

## Customising Events

1. Open `Score.java` and rename `event1`–`event10` fields to your actual events.
2. Update the labels in `scores.html` to match.
3. Update the column headers in `leaderboard.html`.

## Switch to PostgreSQL

1. Start a PostgreSQL database and create a `tiokamp` schema.
2. In `application.properties`, comment out the H2 block and uncomment the PostgreSQL block.
3. Fill in your credentials.
