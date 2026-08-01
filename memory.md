# Efficiency Improver Memory — matsurigoto/agentic-workflow-demo

## Last Updated
2026-08-01

## Discovered Commands
- **Build**: `mvn -Dmaven.repo.local=/tmp/gh-aw/agent/m2 package -DskipTests`
- **Test**: `mvn -Dmaven.repo.local=/tmp/gh-aw/agent/m2 test`
- **Note**: Default m2 repo at `~/.m2` is not writable; always use `-Dmaven.repo.local=/tmp/gh-aw/agent/m2`
- **Pre-existing test failures** (on main): `DateUtilsTest.testGetQuarter` (date boundary), `TaskServiceTest.testGetTaskStatistics` (ArithmeticException divide-by-zero)

## Efficiency Notes
- DatabaseHelper uses raw JDBC with shared static Connection (not thread-safe)
- Spring JPA repositories also exist; prefer them over DatabaseHelper for new code
- `SELECT *` used extensively in DatabaseHelper — over-fetches columns
- TaskService is a 700+ line god class

## Optimisation Backlog

| Priority | Focus Area | Opportunity | Estimated Impact |
|---|---|---|---|
| HIGH | Data | `TaskService.getAllTasks()` / `findAll()` called without pagination in multiple places (lines 276, 317, 442, 543, 641, 655) | HIGH — unbounded full-table loads |
| HIGH | Data | `DatabaseHelper.searchTasks()` and `getTasksByUser()` use `SELECT *` + SQL injection | HIGH |
| HIGH | Data | `UserService.getActiveUsersByRole()` loads all users by role then filters in Java (push filter to DB) | MEDIUM |
| MEDIUM | Code | `StringUtils` duplicates Apache Commons StringUtils already on classpath | MEDIUM — dead code / class loading |
| MEDIUM | Data | `DatabaseHelper` resource leaks (ResultSet/Statement never closed) | MEDIUM — memory/GC pressure |
| LOW | Code | `System.out/err.println` used for audit/logging throughout — no-op in prod but wasteful in high-throughput paths | LOW |

## Backlog Cursor
- Next scan: check controllers for additional over-fetching and missing pagination

## Work In Progress
None

## Completed Work
- **2026-08-01**: PR created — "fix: consolidate N+5 project stats queries and batch-deactivate users"
  - `DatabaseHelper.getProjectStats()`: 5 queries → 1 (−80% round-trips)
  - `UserService.deactivateUsers()`: N×2 DB calls → 1 bulk UPDATE
  - Branch: `efficiency/consolidate-n-plus-one-project-stats`

## Tasks Last Run (for round-robin)
- 2026-08-01: Task 1 (commands validated), Task 2 (opportunities identified), Task 3 (implemented), Task 7 (monthly summary)
- Next run should focus: Task 4 (PR maintenance), Task 5 (comment on issues), Task 6 (measurement infra)
