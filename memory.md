# Perf Improver Memory - matsurigoto/agentic-workflow-demo

## Discovered Commands
- **Build**: `mvn package -q` (NOTE: local maven repo not accessible in CI sandbox - infrastructure issue)
- **Test**: `mvn test -q` (same issue)
- **No benchmark suite exists**
- **No lint tool configured**

## Performance Opportunities Backlog
1. **[DONE - PR created] getTaskStatistics() full-table-scan** - loads all tasks twice (findAll x2), 13 stream passes. Fixed with aggregate JPQL queries.
2. **getOverdueTasks() full-table-scan** - now partially fixed (pre-filters in DB, still parses dates in Java due to mixed format). Could be fully pushed to DB with date normalization work.
3. **DatabaseHelper N+1 queries in getProjectStats()** - 5 separate COUNT queries; could be one query with CASE/SUM. Also has SQL injection.
4. **getAllTasks() no pagination** - returns unbounded list; needs Pageable support.
5. **autoAssignTask() loads all users + all tasks** - O(users * tasks) inefficiency.
6. **DatabaseHelper SQL injection + resource leaks** - searchTasks, getTasksByUser, executeQuery all vulnerable and leak ResultSet/Statement.

## Work In Progress
None.

## Completed Work
- 2026-08-01: PR created - `perf: replace full-table-scan statistics with aggregate queries` on branch `perf-assist/task-statistics-queries`

## Backlog Cursor
Last examined: TaskService lines 438-486 (getTaskStatistics), 316-343 (getOverdueTasks)
Next area: DatabaseHelper getProjectStats N+1, getAllTasks pagination

## Last Run Tasks
- 2026-08-01: Task 1 (discover commands), Task 2 (identify opportunities), Task 3 (implement), Task 7 (monthly summary)

## Previously Checked Off Items (by maintainer)
None yet.
