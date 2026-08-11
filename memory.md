# Perf Improver Memory - matsurigoto/agentic-workflow-demo

## Discovered Commands
- **Build**: `mvn -Dmaven.repo.local=/tmp/m2 package -DskipTests`
- **Test**: `mvn -Dmaven.repo.local=/tmp/m2 test`
- ⚠️ Two pre-existing test failures on `main`: `DateUtilsTest.testGetQuarter`, `TaskServiceTest.testGetTaskStatistics`
- No benchmark suite configured; no lint tool configured

## Performance Opportunities Backlog
1. **[DONE] getTaskStatistics() full-table-scan**
2. **[DONE] DatabaseHelper N+1 queries in getProjectStats()**
3. **[DONE] autoAssignTask() O(users) N+1**
4. **[DONE] getAllTasks() no pagination**
5. **[DONE] DatabaseHelper resource leaks**
6. **[DONE] getOverdueTasks() full-table-scan + O(n²) sort**
7. **[DONE] DateUtils thread-safety**
8. **[DONE] findActiveTasks() wrong filter**
9. **[DONE] ConfigManager thread-unsafe singleton + InputStream leak**
10. **[DONE] UserService getActiveUsersByRole() in-memory filter + deactivateUsers() N+1**
11. **[DONE] generateWeeklyReport() double findAll() → 3 COUNT queries**
12. **[DONE] importTasks() N individual saves → saveAll() batch insert**
13. **due_date stored as String** - issue #110 created; awaiting maintainer input before schema migration
14. **NotificationService leaks** - already addressed by PR #95 (efficiency-improver)

## Work In Progress
None.

## Completed Work
- 2026-08-01 run 1: PR - `perf: replace full-table-scan statistics with aggregate queries`
- 2026-08-01 run 2: PR - `perf: consolidate project stats query and fix auto-assign N+1`
- 2026-08-02 run 3: PR - `perf: add pagination to GET /api/tasks`
- 2026-08-03 run 4: PR - `perf: fix resource leaks in DatabaseHelper using try-with-resources`
- 2026-08-04 run 5: PR - `perf: push overdue-task filtering to DB, remove O(n²) sort`
- 2026-08-05 run 6: PR - `perf: replace SimpleDateFormat with thread-safe DateTimeFormatter in DateUtils`
- 2026-08-06 run 7: PR #90 - `perf: fix findActiveTasks/findActiveTasksByAssignee to exclude cancelled tasks`
- 2026-08-07 run 8: PR #94 - `perf: fix thread-unsafe singleton and InputStream leak in ConfigManager`
- 2026-08-08 run 9: PR #100 - `perf: push role+active filter to DB and replace N+1 deactivation with bulk update in UserService`
- 2026-08-09 run 10: PR #105 - `perf: replace double findAll() in generateWeeklyReport() with COUNT queries`
- 2026-08-10 run 11: Issue #110 created for due_date String → typed DATE migration
- 2026-08-11 run 12: PR (perf-assist/batch-import-saveall) - `perf: replace N individual saves in importTasks() with saveAll() batch insert`

## Backlog Cursor
All identified opportunities addressed or have active issues/PRs. Remaining: due_date migration (awaiting maintainer sign-off), monitor for new code.

## Last Run Tasks
- 2026-08-11 run 12: Task 3 (importTasks batch insert PR), Task 7 (monthly summary)

## Previously Checked Off Items (by maintainer)
None yet.
