# Efficiency Improver Memory — matsurigoto/agentic-workflow-demo

## Last Updated
2026-08-13

## Discovered Commands
- **Build**: `mvn -Dmaven.repo.local=/tmp/gh-aw/agent/m2 package -DskipTests`
- **Test**: `mvn -Dmaven.repo.local=/tmp/gh-aw/agent/m2 test`
- **Note**: Default m2 repo at `~/.m2` is not writable; always use `-Dmaven.repo.local=/tmp/gh-aw/agent/m2`
- **Pre-existing test failures** (on main): `DateUtilsTest.testGetQuarter` (date boundary only — TaskServiceTest divide-by-zero now fixed)

## Efficiency Notes
- DatabaseHelper uses raw JDBC with shared static Connection (not thread-safe)
- Spring JPA repositories also exist; prefer them over DatabaseHelper for new code
- TaskService is ~620 lines (dead code removal PR #101 not yet merged)
- `due_date` stored as free-form multi-format string — hard to push date comparisons to DB
- ConfigManager: singleton with race condition; InputStream leak fixed (branch exists, not merged)
- mysql-connector-java: removed from pom.xml (PR created 2026-08-06, not merged)
- NotificationService: resource leaks fixed (PR #95, open)
- Dead code: PR #101 open
- StringUtils: replaced with Commons Lang3 (PR #106, open)
- exportTasksAsCsv: static DateTimeFormatter (PR #111, open)
- deactivateUsers(): bulk UPDATE (PR #115, open)
- getOverdueTasks(): O(n²) sort → List.sort() (PR #119, open)
- getTaskStatistics(): 13 passes → 1 pass + 2nd findAll() eliminated + divide-by-zero fixed (PR branch efficiency/single-pass-task-statistics, pending)

## Optimisation Backlog

| Priority | Focus Area | Opportunity | Estimated Impact |
|---|---|---|---|
| HIGH | Data | `getAllTasks()` has no pagination — unbounded `findAll()` (perf-improver #69 also on this) | HIGH at scale |
| MEDIUM | Network/IO | `notifyUsers()` sends sequentially — stub impl, low gain until real channels | LOW currently |
| LOW | Code | `System.out/err.println` used for audit/logging throughout | LOW |

## Backlog Cursor
- Next scan: Task 4 (PR maintenance check), Task 6 (JMH if #96 signed off)

## Work In Progress
None

## Completed Work
- **2026-08-01**: Issue #47 created — consolidate N+5 project stats queries + batch-deactivate users
- **2026-08-02**: PR created — push active+role filter to DB in getActiveUsersByRole
- **2026-08-03**: PR created — PreparedStatement + column projection in DatabaseHelper
- **2026-08-04**: PR created — push task statistics counts to DB, fix O(n²) overdue sort
- **2026-08-05**: PR created — close InputStream in ConfigManager.loadConfig() via try-with-resources; commented on #29
- **2026-08-06**: PR created — remove unused mysql-connector-java dependency from pom.xml
- **2026-08-07**: PR #95 created — close OutputStream+HttpURLConnection in NotificationService; issue #96 for JMH benchmarks
- **2026-08-08**: PR #101 created — remove dead code (3 methods + 6 fields); commented on #18 and #23
- **2026-08-09**: PR #106 created — replace custom StringUtils (229 lines) with Apache Commons Lang3
- **2026-08-10**: PR #111 created — replace per-iteration SimpleDateFormat with static DateTimeFormatter in exportTasksAsCsv; commented on #110
- **2026-08-11**: PR #115 created — replace N×2 DB calls in deactivateUsers() with single @Modifying bulk UPDATE
- **2026-08-12**: PR #119 created — replace O(n²) bubble sort with List.sort()+Comparator in getOverdueTasks(); eliminate double findAll() in getTaskStatistics()
- **2026-08-13**: PR created (branch: efficiency/single-pass-task-statistics) — consolidate 13 iteration passes in getTaskStatistics() into one O(n) loop; inline overdue count to eliminate 2nd findAll(); fix divide-by-zero

## Tasks Last Run (for round-robin)
- 2026-08-12: Task 3, Task 4, Task 7
- 2026-08-13: Task 3, Task 5, Task 7
- Next run should focus: Task 4 (PR check), Task 6 (JMH)

## Issues Commented On (Task 5)
- #20 (DatabaseHelper → JPA): 2026-08-01
- #26 (thread-unsafe singleton): 2026-08-01
- #29 (unused mysql-connector-java): 2026-08-05
- #18 (StringUtils duplication): 2026-08-08
- #23 (dead code): 2026-08-08
- #110 (due_date typed column): 2026-08-10

## Previously Checked Off by Maintainer
(none yet)
