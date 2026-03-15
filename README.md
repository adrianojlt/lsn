# LSN - Local Social Network

A console-based Local Social Networking (LSN) application.
I thought in designing with extensibility in mind, following a phased roadmap from a standalone console app to a fully networked, 
real-time social network. The main requirement is **PHASE01**, but i want to try to evolve into a real communication 
console app (check **PHASE02** and **PHASE03** branches).

---

## Phased Roadmap

```
PHASE01   O  Console app, in-memory, standalone
PHASE02   X  Spring Boot REST API + MongoDB
PHASE03   X  WebSockets, real-time, multi-client, authentication
PHASE04   X  Kafka, Redis, scaling
```

---

## PHASE01 - Getting Started

### Requirements
- Java 21
- Gradle

### Build
```bash
./gradlew jar
```

### Run
```bash
java -jar build/libs/lsn.jar
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
$ java -jar build/libs/lsn.jar

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

## Architecture

Strict layering — each layer depends only on the one below it:

```
- Console
- CommandParser
- SocialNetworkService  -> business logic
- SocialNetworkRepository (interface)
- InMemoryRepository    -> in-memory implementation (PHASE01)
```

### Design Decisions

**Sealed Command hierarchy**
Each user action is modeled as an immutable record implementing a sealed interface.
The sealed hierarchy forces exhaustive handling in switch expressions — no accidental
missing-case bugs.

**Repository as an interface**
`SocialNetworkRepository` is an interface. `InMemoryRepository` is the only implementation
in PHASE01. In PHASE02 a MongoDB implementation will replace it without having to touch the
service layer.

**Clock injection**
`SocialNetworkService` receives a `java.time.Clock` via constructor injection.
`Instant.now()` is never called directly. This makes time fully controllable in tests.

**CommandParser is pure**
`CommandParser` has no I/O, no side effects, no static state.
It is a pure function: `String -> Optional<Command>`.
It will be reused in the HTTP client in PHASE02.

---

## Testing

```bash
./gradlew test
```

Test coverage report is generated at:
```
build/reports/jacoco/test/html/index.html
```

### Testing approach

The project follows **Test-Driven Development (TDD)** — tests were written before
implementation following the Red/Green/Refactor cycle. The commit history reflects
this process step by step.

| Layer | Approach                                              |
|---|-------------------------------------------------------|
| `CommandParser` | Unit tests - all command types and edge cases         |
| `TimeFormatter` | Unit tests - all duration thresholds, singular/plural |
| `SocialNetworkService` | Unit tests - fixed `Clock`, all scenarios             |
| Full scenario | Integration test - end-to-end replay of the spec      |
| REPL wiring | Manual smoke test                                     |

---

## Future Phases

### PHASE02 - REST API + MongoDB
- Spring Boot REST API exposing the four commands over HTTP
- MongoDB for persistence
- `InMemoryRepository` replaced by `MongoRepository` - no service changes required
- Deployable via Docker

### PHASE03 - WebSockets + Authentication
- Real-time feed updates pushed to connected clients
- User authentication
- Multi-client support

### PHASE04 - Scaling
- Kafka or Redis Streams for event-driven feed updates
- Redis caching for wall aggregation
- Horizontal scaling