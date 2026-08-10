# Efficiency Improver Memory — matsurigoto/agentic-workflow-demo

## Last Updated
2026-08-10

## Discovered Commands
- **Build**: `mvn -Dmaven.repo.local=/tmp/gh-aw/agent/m2 package -DskipTests`
- **Test**: `mvn -Dmaven.repo.local=/tmp/gh-aw/agent/m2 test`
- **Note**: Default m2 repo at `~/.m2` is not writable; always use `-Dmaven.repo.local=/tmp/gh-aw/agent/m2`
- **Pre-existing test failures** (on main): `DateUtilsTest.testGetQuarter` (date boundary), `TaskServiceTest.testGetTaskStatistics` (ArithmeticException divide-by-zero)

## Efficiency Notes
- DatabaseHelper uses raw JDBC with shared static Connection (not thread-safe)
- Spring JPA repositories also exist; prefer them over DatabaseHelper for new code
- TaskService is a 700+ line god class (now ~620 lines after dead code removal PR #101, not merged yet)
- `due_date` stored as free-form multi-format string — hard to push date comparisons to DB (perf-improver #110 proposes schema migration)
- ConfigManager: singleton with race condition; InputStream leak now fixed (branch exists)
- mysql-connector-java: removed from pom.xml (PR created 2026-08-06)
- NotificationService: resource leaks (OutputStream + HttpURLConnection) now fixed (branch 2026-08-07, PR #95)
- Dead code removed: getOldDashboardStats, migrateLegacyIds, generateWeeklyReport, workflow_state, legacy_id, color, icon (PR #101, open)
- StringUtils.java: removed and replaced with Apache Commons Lang3 (PR #106, open)
- exportTasksAsCsv: had new SimpleDateFormat inside loop; now static DateTimeFormatter (PR created 2026-08-10)

## Optimisation Backlog

| Priority | Focus Area | Opportunity | Estimated Impact |
|---|---|---|---|
| HIGH | Data | `getOverdueTasks()` still does `findAll()` — deferring to perf-improver #110 | HIGH if table grows |
| HIGH | Data | `getAllTasks()` has no pagination — unbounded `findAll()` (perf-improver #69 also on this) | HIGH at scale |
| MEDIUM | Network/IO | `notifyUsers()` sends sequentially — stub impl so no real I/O gain currently | LOW until real channels |
| LOW | Code | `System.out/err.println` used for audit/logging throughout | LOW |

## Backlog Cursor
- Next scan: Task 6 (JMH benchmark — wait for maintainer sign-off on #96), Task 3 (look for new opportunities)

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
- **2026-08-10**: PR created — replace per-iteration SimpleDateFormat with static DateTimeFormatter in exportTasksAsCsv; commented on #110

## Tasks Last Run (for round-robin)
- 2026-08-09: Task 3 (StringUtils removal), Task 7
- 2026-08-10: Task 3 (SDF→DateTimeFormatter), Task 4 (PR check), Task 5 (#110 comment), Task 7
- Next run should focus: Task 6 (JMH benchmark infrastructure, if #96 has maintainer sign-off), Task 5 (new issues)

## Issues Commented On (Task 5)
- #20 (DatabaseHelper → JPA): 2026-08-01
- #26 (thread-unsafe singleton): 2026-08-01
- #29 (unused mysql-connector-java): 2026-08-05
- #18 (StringUtils duplication): 2026-08-08
- #23 (dead code): 2026-08-08
- #110 (due_date typed column): 2026-08-10

## Previously Checked Off by Maintainer
(none yet)
