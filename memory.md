# Perf Improver Memory - matsurigoto/agentic-workflow-demo

## Discovered Commands
- **Build**: `mvn package -q` (NOTE: local maven repo not accessible in CI sandbox - infrastructure issue)
- **Test**: `mvn test -q` (same issue)
- **No benchmark suite exists**
- **No lint tool configured**

## Performance Opportunities Backlog
1. **[DONE] getTaskStatistics() full-table-scan** - Branch: perf-assist/task-statistics-queries-*
2. **[DONE] DatabaseHelper N+1 queries in getProjectStats()** - Branch: perf-assist/project-stats-and-auto-assign
3. **[DONE] autoAssignTask() O(users) N+1** - Branch: perf-assist/project-stats-and-auto-assign
4. **[DONE] getAllTasks() no pagination** - Branch: perf-assist/add-task-list-pagination
5. **[DONE] DatabaseHelper resource leaks** - Branch: perf-assist/fix-databasehelper-resource-leaks
6. **getOverdueTasks() date parsing in Java** - could be pushed to DB with schema normalization. (NEXT TARGET)

## Work In Progress
None.

## Completed Work
- 2026-08-01 run 1: PR - `perf: replace full-table-scan statistics with aggregate queries` → branch perf-assist/task-statistics-queries-*
- 2026-08-01 run 2: PR - `perf: consolidate project stats query and fix auto-assign N+1` → branch perf-assist/project-stats-and-auto-assign
- 2026-08-02 run 3: PR - `perf: add pagination to GET /api/tasks` → branch perf-assist/add-task-list-pagination
- 2026-08-03 run 4: PR - `perf: fix resource leaks in DatabaseHelper using try-with-resources` → branch perf-assist/fix-databasehelper-resource-leaks

## Backlog Cursor
Next area: getOverdueTasks() date parsing in Java → push to DB

## Last Run Tasks
- 2026-08-03 run 4: Task 3 (implement - DatabaseHelper resource leaks), Task 7 (monthly summary)

## Previously Checked Off Items (by maintainer)
None yet.
