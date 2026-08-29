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
16. **[DONE] NotificationService.sendSlackNotification()** - resource leak - PR #131 open

## Work In Progress
None.

## Completed Work (recent)
- 2026-08-16 run 17: PR #131 created - fix resource leaks in sendSlackNotification()
- 2026-08-13 run 14: PR #122 created - pagination for /api/users and /api/projects
- 2026-08-11 run 12: PR #114 created - saveAll() batch insert in importTasks()
- 2026-08-10 run 11: Issue #110 created for due_date String → typed DATE migration
- 2026-08-09 run 10: PR #105 - replace double findAll() with COUNT queries
- 2026-08-08 run 9: PR #100 - UserService DB-filter + bulk update
- 2026-08-07 run 8: PR #94 - ConfigManager thread-safe singleton + InputStream fix
- 2026-08-06 run 7: PR #90 - findActiveTasks() exclude cancelled tasks
- Runs 1-6 (Aug 1-5): PRs for statistics, N+1, pagination, resource leaks, DateUtils, overdue sort

## Backlog Cursor
All identified opportunities addressed. Remaining: #13 (due_date migration, awaiting maintainer).

## Last Run Tasks
- 2026-08-29 run 30: Task 4 (PR check - all 7 PRs still open), Task 7 (monthly summary)

## Previously Checked Off Items (by maintainer)
None yet.
