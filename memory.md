# Perf Improver Memory - matsurigoto/agentic-workflow-demo

## Discovered Commands
- **Build**: `mvn package -q` (NOTE: local maven repo not accessible in CI sandbox - infrastructure issue)
- **Test**: `mvn test -q` (same issue)
- **No benchmark suite exists**
- **No lint tool configured**

## Performance Opportunities Backlog
1. **[DONE - PR created #50] getTaskStatistics() full-table-scan** - loads all tasks twice (findAll x2), 13 stream passes. Fixed with aggregate JPQL queries.
2. **[DONE - PR created #aw_stats2_pr] DatabaseHelper N+1 queries in getProjectStats()** - 5 separate COUNT queries → 1 CASE/SUM query. Also fixed SQL injection + resource leak.
3. **[DONE - PR created #aw_stats2_pr] autoAssignTask() O(users) N+1** - one query per user → 1 LEFT JOIN aggregate native query findLeastBusyActiveUser().
4. **getAllTasks() no pagination** - returns unbounded list; needs Pageable support. (NEXT TARGET)
5. **DatabaseHelper resource leaks (getTasksByUser, executeQuery)** - still leak ResultSet/Statement.
6. **getOverdueTasks() date parsing in Java** - could be pushed to DB with schema normalization.

## Work In Progress
None.

## Completed Work
- 2026-08-01 run 1: PR created - `perf: replace full-table-scan statistics with aggregate queries` → issue #50, branch `perf-assist/task-statistics-queries-1dc45b6da8d17a7d`
- 2026-08-01 run 2: PR created - `perf: consolidate project stats query and fix auto-assign N+1` → branch `perf-assist/project-stats-and-auto-assign`

## Backlog Cursor
Next area: getAllTasks() pagination (TaskService line 275, TaskController), DatabaseHelper.getTasksByUser resource leaks

## Last Run Tasks
- 2026-08-01 run 2: Task 3 (implement - project stats + auto-assign), Task 7 (monthly summary)

## Previously Checked Off Items (by maintainer)
None yet.
