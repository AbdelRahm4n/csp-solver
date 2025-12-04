# CSP Solver

A production-grade Constraint Satisfaction Problem (CSP) Solver implemented as a Spring Boot microservice. All algorithms are implemented from scratch without external CSP libraries.

## Features

- **Backtracking Search** with configurable heuristics
- **Arc Consistency (AC-3)** preprocessing for domain reduction
- **Forward Checking** propagation during search
- **Variable Ordering Heuristics**: MRV, Degree, Dom/WDeg, MRV+Degree
- **Value Ordering Heuristics**: LCV (Least Constraining Value)
- **Built-in Problems**: N-Queens, Sudoku, Graph Coloring, Map Coloring, Cryptarithmetic
- **WebSocket Support** for real-time solving progress
- **REST API** with OpenAPI/Swagger documentation
- **Docker Support** for containerized deployment

## Performance

| Problem | Time |
|---------|------|
| 8-Queens | < 1ms |
| 100-Queens | < 5ms |
| 1000-Queens | < 100ms |
| Sudoku (hard) | < 50ms |

## Quick Start

### Prerequisites

- Java 21+
- Maven 3.8+

### Run with Maven

```bash
# Clone and build
mvn clean package -DskipTests

# Run the application
mvn spring-boot:run
```

### Run with Docker

```bash
# Build and run with Docker Compose
docker-compose up --build
```

The API will be available at `http://localhost:8080`

## API Reference

### Swagger UI

Interactive API documentation is available at:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI Spec**: http://localhost:8080/api-docs

### Endpoints

#### Problems

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/problems` | List available problems |
| POST | `/api/v1/problems/nqueens` | Solve N-Queens problem |
| GET | `/api/v1/problems/nqueens/{n}` | Quick solve N-Queens |
| POST | `/api/v1/problems/sudoku` | Solve Sudoku puzzle |

#### Benchmarks

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/benchmark/nqueens` | Benchmark N-Queens solver |
| GET | `/api/v1/benchmark/nqueens/{n}` | Benchmark specific size |
| GET | `/api/v1/benchmark/compare-heuristics` | Compare heuristics |

### Example Requests

#### Solve N-Queens

```bash
curl -X POST http://localhost:8080/api/v1/problems/nqueens \
  -H "Content-Type: application/json" \
  -d '{"n": 8}'
```

Response:
```json
{
  "status": "SATISFIABLE",
  "satisfiable": true,
  "solutionCount": 1,
  "solutions": [{"Q0": 0, "Q1": 4, "Q2": 7, "Q3": 5, "Q4": 2, "Q5": 6, "Q6": 1, "Q7": 3}],
  "metrics": {
    "nodesExplored": 113,
    "backtracks": 37,
    "elapsedTimeMs": 2
  }
}
```

#### Solve Sudoku

```bash
curl -X POST http://localhost:8080/api/v1/problems/sudoku \
  -H "Content-Type: application/json" \
  -d '{
    "grid": [
      [5,3,0,0,7,0,0,0,0],
      [6,0,0,1,9,5,0,0,0],
      [0,9,8,0,0,0,0,6,0],
      [8,0,0,0,6,0,0,0,3],
      [4,0,0,8,0,3,0,0,1],
      [7,0,0,0,2,0,0,0,6],
      [0,6,0,0,0,0,2,8,0],
      [0,0,0,4,1,9,0,0,5],
      [0,0,0,0,8,0,0,7,9]
    ]
  }'
```

## Algorithms

### Backtracking Search

The core solver uses depth-first backtracking search with:
- **Checkpoint/Rollback**: Efficient state restoration using domain checkpoints
- **Constraint Propagation**: Forward checking removes inconsistent values early
- **Heuristic Ordering**: Smart variable and value selection reduces search space

### Variable Ordering Heuristics

| Heuristic | Description |
|-----------|-------------|
| **MRV** | Minimum Remaining Values - choose variable with smallest domain |
| **Degree** | Choose variable involved in most constraints |
| **Dom/WDeg** | Domain size divided by weighted degree (learned from failures) |
| **MRV+Degree** | MRV with Degree as tiebreaker (default) |

### AC-3 Arc Consistency

Before search begins, AC-3 enforces arc consistency:
1. For each arc (Xi, Xj), remove values from Xi that have no support in Xj
2. Propagate changes to affected arcs
3. Repeat until no changes or domain wipeout

### Min-Conflicts (for large N-Queens)

For N >= 50, the solver uses the min-conflicts local search algorithm:
1. Initialize with a greedy assignment
2. Pick a conflicted variable
3. Move to value with minimum conflicts
4. Repeat until solution or max iterations

## Built-in Problems

### N-Queens
Place N queens on an NxN chessboard so no two queens attack each other.

### Sudoku
Fill a 9x9 grid so each row, column, and 3x3 box contains digits 1-9.

### Graph Coloring
Color nodes of a graph with k colors so no adjacent nodes share a color.

### Map Coloring
Color regions of a map so no adjacent regions share a color.

### Cryptarithmetic
Solve puzzles like SEND + MORE = MONEY where letters represent digits.

## WebSocket

Real-time solving progress is available via WebSocket:

```javascript
const socket = new SockJS('/ws/solver');
const stompClient = Stomp.over(socket);

stompClient.connect({}, () => {
  stompClient.subscribe('/topic/solver/progress', (message) => {
    console.log('Progress:', JSON.parse(message.body));
  });
});
```

## Configuration

Application configuration in `application.yml`:

```yaml
server:
  port: 8080

solver:
  default-timeout-ms: 60000
  max-solutions: 1000

springdoc:
  swagger-ui:
    path: /swagger-ui.html
```

## Project Structure

```
src/main/java/com/cspsolver/
├── core/
│   ├── model/          # CSP, Variable, Domain, Assignment
│   ├── constraint/     # Constraint interfaces and implementations
│   └── propagation/    # AC-3, Forward Checking
├── solver/
│   ├── backtracking/   # BacktrackingSolver
│   └── heuristics/     # MRV, Degree, LCV selectors
├── problems/           # Built-in problem factories
├── api/
│   ├── controller/     # REST controllers
│   └── dto/            # Request/Response DTOs
└── websocket/          # WebSocket configuration
```

## Development

### Run Tests

```bash
mvn test
```

### Build JAR

```bash
mvn clean package
java -jar target/csp-solver-1.0.0-SNAPSHOT.jar
```

### Code Coverage

```bash
mvn verify
# Reports in target/site/jacoco/
```

## Docker

### Build Image

```bash
docker build -t csp-solver .
```

### Run Container

```bash
docker run -p 8080:8080 csp-solver
```

### Docker Compose

```bash
docker-compose up -d
```

## API Health Check

```bash
curl http://localhost:8080/actuator/health
```

## License

MIT License
