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
8. **[DONE] findActiveTasks() wrong filter** - Branch: perf-assist/fix-active-tasks-query
9. **[DONE] ConfigManager thread-unsafe singleton + InputStream leak** - Branch: perf-assist/fix-configmanager-thread-safety
10. **[DONE] UserService getActiveUsersByRole() in-memory filter + deactivateUsers() N+1** - Branch: perf-assist/fix-userservice-inefficiencies
11. **due_date stored as String** - typed date column would allow full DB-side date filtering (schema migration needed, needs issue first)
12. **NotificationService leaks** - already addressed by PR #95 (efficiency-improver)

## Work In Progress
None.

## Completed Work
- 2026-08-01 run 1: PR - `perf: replace full-table-scan statistics with aggregate queries`
- 2026-08-01 run 2: PR - `perf: consolidate project stats query and fix auto-assign N+1`
- 2026-08-02 run 3: PR - `perf: add pagination to GET /api/tasks`
- 2026-08-03 run 4: PR - `perf: fix resource leaks in DatabaseHelper using try-with-resources`
- 2026-08-04 run 5: PR - `perf: push overdue-task filtering to DB, remove O(n²) sort`
- 2026-08-05 run 6: PR - `perf: replace SimpleDateFormat with thread-safe DateTimeFormatter in DateUtils`
- 2026-08-06 run 7: PR - `perf: fix findActiveTasks/findActiveTasksByAssignee to exclude cancelled tasks`
- 2026-08-07 run 8: PR #94 - `perf: fix thread-unsafe singleton and InputStream leak in ConfigManager`
- 2026-08-08 run 9: PR - `perf: push role+active filter to DB and replace N+1 deactivation with bulk update in UserService`

## Backlog Cursor
Next area: due_date stored as String (item 11, needs issue first)

## Last Run Tasks
- 2026-08-08 run 9: Task 3 (implement - UserService in-memory filter + N+1 deactivation), Task 7 (monthly summary)

## Previously Checked Off Items (by maintainer)
None yet.
