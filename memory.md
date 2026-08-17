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
14. **[DONE] GET /api/users no pagination** - PR #122 open
15. **[DONE] GET /api/projects no pagination** - PR #122 open
16. **[DONE] NotificationService.sendSlackNotification()** - OutputStream and HttpURLConnection resource leak - PR #131 open

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
- 2026-08-11 run 12: PR #114 (perf-assist/batch-import-saveall) - `perf: replace N individual saves in importTasks() with saveAll() batch insert`
- 2026-08-12 run 13: Task 2 scan - found 2 new pagination gaps (users, projects); Task 4 - checked PRs, no CI failures
- 2026-08-13 run 14: PR #122 created - `perf: add pagination to GET /api/users and GET /api/projects`
- 2026-08-14 run 15: Task 2 deep scan - no new major opportunities; Task 5 - no issues; Task 7 - updated monthly summary
- 2026-08-15 run 16: Task 4 - checked all 6 open PRs, still open, no CI failures; Task 7 - updated monthly summary
- 2026-08-16 run 17: PR #131 created - `perf: close OutputStream and disconnect HttpURLConnection in sendSlackNotification()`; Task 7 - updated monthly summary
- 2026-08-17 run 18: Task 4 - checked all 7 open PRs, still open, no CI failures; Task 7 - updated monthly summary

## Backlog Cursor
All identified opportunities addressed. Remaining: #13 (due_date migration, awaiting maintainer).

## Last Run Tasks
- 2026-08-17 run 18: Task 4 (PR check), Task 7 (monthly summary)

## Previously Checked Off Items (by maintainer)
None yet.
