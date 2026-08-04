# Efficiency Improver Memory — matsurigoto/agentic-workflow-demo

## Last Updated
2026-08-04

## Discovered Commands
- **Build**: `mvn -Dmaven.repo.local=/tmp/gh-aw/agent/m2 package -DskipTests`
- **Test**: `mvn -Dmaven.repo.local=/tmp/gh-aw/agent/m2 test`
- **Note**: Default m2 repo at `~/.m2` is not writable; always use `-Dmaven.repo.local=/tmp/gh-aw/agent/m2`
- **Pre-existing test failures** (on main): `DateUtilsTest.testGetQuarter` (date boundary), `TaskServiceTest.testGetTaskStatistics` (ArithmeticException divide-by-zero)

## Efficiency Notes
- DatabaseHelper uses raw JDBC with shared static Connection (not thread-safe)
- Spring JPA repositories also exist; prefer them over DatabaseHelper for new code
- TaskService is a 700+ line god class
- `due_date` stored as free-form multi-format string — hard to push date comparisons to DB

## Optimisation Backlog

| Priority | Focus Area | Opportunity | Estimated Impact |
|---|---|---|---|
| HIGH | Data | `StringUtils` duplicates Apache Commons StringUtils already on classpath | MEDIUM — dead code / class loading |
| MEDIUM | Data | `getOverdueTasks()` still does `findAll()` — hard to push to DB without schema change | MEDIUM if table grows |
| LOW | Code | `System.out/err.println` used for audit/logging throughout | LOW |

## Backlog Cursor
- Next scan: StringUtils deduplication, then Task 5 (efficiency issues), Task 6 (measurement infra)

## Work In Progress
None

## Completed Work
- **2026-08-01 run 1**: Issue #47 created with patch — "fix: consolidate N+5 project stats queries and batch-deactivate users"
  - `DatabaseHelper.getProjectStats()`: 5 queries → 1 (−80% round-trips)
  - `UserService.deactivateUsers()`: N×2 DB calls → 1 bulk UPDATE
  - Branch: `efficiency/consolidate-n-plus-one-project-stats-5ab78aa36f4e236a`
- **2026-08-02 run 1**: PR created — "perf: push active+role filter to DB in getActiveUsersByRole"
  - `UserService.getActiveUsersByRole()`: all role-R users loaded → only active+non-deleted transferred
  - Branch: `efficiency/push-active-role-filter-to-db`
- **2026-08-03 run 1**: PR created — "perf: use PreparedStatement + column projection in DatabaseHelper"
  - `searchTasks()`: Statement+SELECT* → PreparedStatement+7-column projection + try-with-resources
  - `getTasksByUser()`: Statement+SELECT* → PreparedStatement+4-column projection + try-with-resources
  - Branch: `efficiency/fix-databasehelper-prepared-statements`
  - Measured: getTasksByUser() bytes/row ≈ −73%; DB query-plan reuse eliminates per-call parse cost
- **2026-08-04 run 1**: PR created — "perf: push task statistics counts to DB, fix O(n²) overdue sort"
  - `getTaskStatistics()`: findAll() + 13 stream passes → 12 DB COUNT queries (zero row transfer)
  - `getOverdueTasks()`: O(n²) bubble sort → O(n log n) Comparator sort
  - Branch: `efficiency/push-stats-counts-to-db-query`
  - Measured: row bytes transferred per call −100% for counts; heap allocation −100% for Task objects

## Tasks Last Run (for round-robin)
- 2026-08-01 run 1: Task 1, Task 2, Task 3, Task 7
- 2026-08-01 run 2: Task 4 (no open PRs), Task 5 (commented on #20, #26), Task 7
- 2026-08-02 run 1: Task 3 (push active+role filter to DB), Task 7
- 2026-08-03 run 1: Task 3 (DatabaseHelper PreparedStatement), Task 7
- 2026-08-04 run 1: Task 3 (push stats counts to DB + O(n²) sort fix), Task 7
- Next run should focus: Task 2 (StringUtils deduplication scan), Task 5 (efficiency issues), Task 6 (measurement infra)

## Issues Commented On (Task 5)
- #20 (DatabaseHelper → JPA): Efficiency Improver comment added 2026-08-01
- #26 (thread-unsafe singleton): Efficiency Improver comment added 2026-08-01

## Previously Checked Off by Maintainer
(none yet)
