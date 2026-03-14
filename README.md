# LSN — Local Social Network

A console-based Local Social Networking (LSN) application.
I thought in designing with extensibility in mind, following a phased roadmap from a standalone console app to a fully networked,
real-time social network. The main requirement is **PHASE01**, but i want to try to evolve into a real communication
console app.
---

## Phased Roadmap

```
PHASE01   O  Console app, in-memory, standalone
PHASE02   O  Spring Boot REST API + MongoDB
PHASE03   O  WebSockets, real-time, multi-client, authentication
PHASE04   X  Kafka, Redis, scaling
```

---

## Project Structure

Multi-module Gradle project:

```
lsn/
  api/      — shared domain contract  (com.lsn.api)
  client/   — console app             (com.lsn.client)
  server/   — REST API                (com.lsn.server)
```

---

## PHASE01 — Console Client

### Requirements
- Java 21
- Gradle

### Build
```bash
./gradlew :client:jar
```

### Run
```bash
java -jar client/build/libs/lsn.jar
```

### Commands

| Command | Example | Output                                     |
|---|---|--------------------------------------------|
| Post | `Alice -> I love the weather today` | *(nothing)*                                |
| Read | `Alice` | `I love the weather today (5 minutes ago)` |
| Follow | `Charlie follows Alice` | *(nothing)*                                |
| Wall | `Charlie wall` | `Charlie - <message> (X minutes ago)`      |
| Exit | `exit` | *(exits the application)*                  |

Unknown commands print: `Unknown command: <input>`

### Example session
```
$ java -jar client/build/libs/lsn.jar

Alice -> I love the weather today
Bob -> Damn! We lost!
Bob -> Good game though.

Alice
I love the weather today (5 minutes ago)

Bob
Good game though. (1 minute ago)
Damn! We lost! (2 minutes ago)

Charlie -> I'm in New York today! Anyone want to have a coffee?
Charlie follows Alice
Charlie wall
Charlie - I'm in New York today! Anyone want to have a coffee? (2 seconds ago)
Alice - I love the weather today (5 minutes ago)

Charlie follows Bob
Charlie wall
Charlie - I'm in New York today! Anyone want to have a coffee? (15 seconds ago)
Bob - Good game though. (1 minute ago)
Bob - Damn! We lost! (2 minutes ago)
Alice - I love the weather today (5 minutes ago)

exit
```

---

## PHASE02 — REST API Server

### Requirements
- Java 21
- Gradle
- MongoDB (or Docker)

### Build
```bash
./gradlew :server:bootJar
```

### Run
```bash
MONGO_URI=mongodb://localhost:27017/lsn java -jar server/build/libs/server.jar
```

### Docker
```bash
docker build -t lsn-server ./server
docker run --rm -e MONGO_URI=mongodb://host.docker.internal:27017/lsn -p 8080:8080 lsn-server
```

### Endpoints

| Method | Path | Description |
|---|---|---|
| `GET` | `/health` | Health check → `{"status":"ok"}` |
| `POST` | `/users/{username}/posts` | Publish a post |
| `GET` | `/users/{username}/posts` | Read a user's timeline (newest first) |
| `POST` | `/users/{username}/following` | Follow another user |
| `GET` | `/users/{username}/wall` | Aggregated wall (own + followed, newest first) |

### Example
```bash
curl -X POST http://localhost:8080/users/alice/posts \
     -H 'Content-Type: application/json' \
     -d '{"content":"I love the weather today"}'

curl http://localhost:8080/users/alice/posts
# [{"content":"I love the weather today","postedAt":"2026-03-13T10:00:00Z"}]

curl -X POST http://localhost:8080/users/charlie/following \
     -H 'Content-Type: application/json' \
     -d '{"target":"alice"}'

curl http://localhost:8080/users/charlie/wall
# [{"username":"alice","content":"I love the weather today","postedAt":"2026-03-13T10:00:00Z"}]
```

---

## Architecture

### Design Decisions

**Sealed Command hierarchy**
Each user action is modeled as an immutable record implementing a sealed interface.
The sealed hierarchy forces exhaustive handling in switch expressions — no accidental
missing-case bugs.

**Repository as an interface**
`SocialNetworkRepository` is defined once in the `api` module and implemented by both
`client` (via `InMemoryRepository`) and `server` (via `MongoSocialNetworkRepository`).
The service layer in both modules is coded against the interface — no implementation details leak up.

**Clock injection**
`SocialNetworkService` receives a `java.time.Clock` via constructor injection.
`Instant.now()` is never called directly. This makes time fully controllable in tests.

**CommandParser is pure**
`CommandParser` has no I/O, no side effects, no static state.
It is a pure function: `String -> Optional<Command>`.

---

## Testing

```bash
# all modules
./gradlew test

# individual
./gradlew :client:test
./gradlew :server:test
```

### Client test coverage

| Layer | Approach |
|---|---|
| `CommandParser` | Unit tests — all command types and edge cases |
| `TimeFormatter` | Unit tests — all duration thresholds, singular/plural |
| `SocialNetworkService` | Unit tests — fixed `Clock`, all scenarios |
| Full scenario | Integration test — end-to-end replay of the spec |
| REPL wiring | Manual smoke test |

### Server test coverage

| Layer | Approach |
|---|---|
| `HealthController` | MockMvc — status 200, body |
| `PostController` | MockMvc — valid/invalid POST, GET newest-first with ISO timestamps |
| `FollowController` | MockMvc — valid/invalid follow, wall aggregation |
| `MongoSocialNetworkRepository` | `@DataMongoTest` + embedded Mongo — save, timeline, follow, wall |

---

## Future Phases

### PHASE03 — WebSockets + Authentication
- Real-time feed updates pushed to connected clients
- User authentication
- Multi-client support

### PHASE04 — Scaling
- Kafka or Redis Streams for event-driven feed updates
- Redis caching for wall aggregation
- Horizontal scaling
