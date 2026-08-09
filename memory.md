# Efficiency Improver Memory — matsurigoto/agentic-workflow-demo

## Last Updated
2026-08-09

## Discovered Commands
- **Build**: `mvn -Dmaven.repo.local=/tmp/gh-aw/agent/m2 package -DskipTests`
- **Test**: `mvn -Dmaven.repo.local=/tmp/gh-aw/agent/m2 test`
- **Note**: Default m2 repo at `~/.m2` is not writable; always use `-Dmaven.repo.local=/tmp/gh-aw/agent/m2`
- **Pre-existing test failures** (on main): `DateUtilsTest.testGetQuarter` (date boundary), `TaskServiceTest.testGetTaskStatistics` (ArithmeticException divide-by-zero)

## Efficiency Notes
- DatabaseHelper uses raw JDBC with shared static Connection (not thread-safe)
- Spring JPA repositories also exist; prefer them over DatabaseHelper for new code
- TaskService is a 700+ line god class (now ~620 lines after dead code removal)
- `due_date` stored as free-form multi-format string — hard to push date comparisons to DB
- ConfigManager: singleton with race condition; InputStream leak now fixed (branch exists)
- mysql-connector-java: removed from pom.xml (PR created 2026-08-06)
- NotificationService: resource leaks (OutputStream + HttpURLConnection) now fixed (branch 2026-08-07)
- Dead code removed: getOldDashboardStats, migrateLegacyIds, generateWeeklyReport, workflow_state, legacy_id, color, icon (PR 2026-08-08)
- StringUtils.java: removed and replaced with Apache Commons Lang3 (PR 2026-08-09); isBlank→Commons, sanitize inlined as private helper

## Optimisation Backlog

| Priority | Focus Area | Opportunity | Estimated Impact |
|---|---|---|---|
| HIGH | Data | `getOverdueTasks()` still does `findAll()` — hard to push to DB without schema change (perf-improver #77 also on this) | HIGH if table grows |
| HIGH | Data | `getAllTasks()` has no pagination — unbounded `findAll()` (perf-improver #69 also on this) | HIGH at scale |
| MEDIUM | Network/IO | `notifyUsers()` sends sequentially, no batching | MEDIUM |
| LOW | Code | `System.out/err.println` used for audit/logging throughout | LOW |

## Backlog Cursor
- Next scan: Task 5 (comment on efficiency issues), Task 6 (JMH benchmark follow-up)

## Work In Progress
None

## Completed Work
- **2026-08-01 run 1**: Issue #47 created — consolidate N+5 project stats queries + batch-deactivate users
- **2026-08-02 run 1**: PR created — push active+role filter to DB in getActiveUsersByRole
- **2026-08-03 run 1**: PR created — PreparedStatement + column projection in DatabaseHelper
- **2026-08-04 run 1**: PR created — push task statistics counts to DB, fix O(n²) overdue sort
- **2026-08-05 run 1**: PR created — close InputStream in ConfigManager.loadConfig() via try-with-resources; commented on #29
- **2026-08-06 run 1**: PR created — remove unused mysql-connector-java dependency from pom.xml
- **2026-08-07 run 1**: PR created — close OutputStream+HttpURLConnection in NotificationService; issue created proposing JMH benchmarks
- **2026-08-08 run 1**: PR created — remove dead code (3 methods + 6 fields); commented on #18 and #23
- **2026-08-09 run 1**: PR created — replace custom StringUtils (229 lines) with Apache Commons Lang3; deleted StringUtils.java + StringUtilsTest.java

## Tasks Last Run (for round-robin)
- 2026-08-08 run 1: Task 3 (dead code removal), Task 4 (PR check), Task 5 (#18, #23), Task 7
- 2026-08-09 run 1: Task 3 (StringUtils removal), Task 7
- Next run should focus: Task 4 (PR maintenance), Task 5 (efficiency issue comments), Task 6 (JMH benchmark infrastructure)

## Issues Commented On (Task 5)
- #20 (DatabaseHelper → JPA): Efficiency Improver comment added 2026-08-01
- #26 (thread-unsafe singleton): Efficiency Improver comment added 2026-08-01
- #29 (unused mysql-connector-java): Efficiency Improver comment added 2026-08-05
- #18 (StringUtils duplication): Efficiency Improver comment added 2026-08-08
- #23 (dead code): Efficiency Improver comment added 2026-08-08

## Previously Checked Off by Maintainer
(none yet)
