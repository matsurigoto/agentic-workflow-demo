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
6. **[DONE] getOverdueTasks() full-table-scan + O(n²) sort** - Branch: perf-assist/optimize-overdue-tasks-query
7. **[DONE] DateUtils thread-safety** - Branch: perf-assist/fix-dateutils-thread-safety
8. **due_date stored as String** - typed date column would allow full DB-side date filtering (schema migration needed, needs issue first)
9. **findActiveTasks() wrong filter** - JPQL excludes only status=2 but not status=3 (cancelled); minor correctness + index hit

## Work In Progress
None.

## Completed Work
- 2026-08-01 run 1: PR - `perf: replace full-table-scan statistics with aggregate queries` → branch perf-assist/task-statistics-queries-*
- 2026-08-01 run 2: PR - `perf: consolidate project stats query and fix auto-assign N+1` → branch perf-assist/project-stats-and-auto-assign
- 2026-08-02 run 3: PR - `perf: add pagination to GET /api/tasks` → branch perf-assist/add-task-list-pagination
- 2026-08-03 run 4: PR - `perf: fix resource leaks in DatabaseHelper using try-with-resources` → branch perf-assist/fix-databasehelper-resource-leaks
- 2026-08-04 run 5: PR - `perf: push overdue-task filtering to DB, remove O(n²) sort` → branch perf-assist/optimize-overdue-tasks-query
- 2026-08-05 run 6: PR - `perf: replace SimpleDateFormat with thread-safe DateTimeFormatter in DateUtils` → branch perf-assist/fix-dateutils-thread-safety

## Backlog Cursor
Next area: findActiveTasks() wrong filter (item 9) or due_date stored as String (item 8, needs issue first)

## Last Run Tasks
- 2026-08-05 run 6: Task 3 (implement - DateUtils thread-safety fix), Task 7 (monthly summary)

## Previously Checked Off Items (by maintainer)
None yet.
