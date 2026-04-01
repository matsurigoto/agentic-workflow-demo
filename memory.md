# Test Improver Memory — matsurigoto/agentic-workflow-demo

## Build / Test / Coverage Commands

Validated on 2026-03-31:
- **Build**: `mvn -Dmaven.repo.local=/tmp/gh-aw/agent/.m2/repository clean compile -B`
- **Test**: `mvn -Dmaven.repo.local=/tmp/gh-aw/agent/.m2/repository test -B`
- **Single test class**: `mvn -Dmaven.repo.local=/tmp/gh-aw/agent/.m2/repository test -Dtest=ClassName -B`
- **Package**: `mvn -Dmaven.repo.local=/tmp/gh-aw/agent/.m2/repository package -DskipTests -B`
- No coverage plugin configured (no Jacoco); coverage must be added manually if needed
- Maven local repo must be set to `/tmp/gh-aw/agent/.m2/repository` (default `/home/runner/.m2` is not writable)

## Framework / Stack

- Java 11 source, tested with JDK 17 in CI
- Spring Boot 2.7.18 with JPA (H2 in tests)
- JUnit 5 (JUnit Jupiter) for tests; also JUnit 4 on classpath
- No Mockito — `@SpringBootTest` integration tests only (test context loads full Spring context)
- Test files: `src/test/java/com/taskflow/`

## Testing Notes

- `DataInitializer` runs on application start and seeds sample data; added `@BeforeEach` cleanup in TaskServiceTest (taskRepository.deleteAll()) to prevent interference
- `DateUtils` uses `static SimpleDateFormat` fields — thread-safety bug, not fixed ("won't fix")
- Pre-existing CI failures: `testGetQuarter` (off-by-one in impl) and `testGetTaskStatistics` (div/zero)
- No Mockito/mock usage anywhere — all tests are Spring integration tests
- PRs are created via safeoutputs tool (no direct git push to remote in this env)

## Known Bugs (from code analysis + failing tests)

1. **`DateUtils.getQuarter()`** — returns 0-based (0–3) instead of 1-based (1–4). Bug issues #156 closed — check if fixed.
2. **`TaskService.getTaskStatistics()`** — `ArithmeticException: / by zero` when no completed tasks. Bug issue #157 closed — check if fixed.
3. **`DateUtils.addBusinessDays()`** — skips DAY_OF_WEEK 6 (Friday) and 7 (Saturday) but NOT 1 (Sunday). Bug noted.
4. **`DateUtils.startOfDay()`** — doesn't reset milliseconds. Bug noted.
5. **`DateUtils.isWithinRange()`** — exclusive range (after/before); probably should be inclusive. Documented in tests.
6. **`StringUtils.padRight()`** — `NegativeArraySizeException` when `str.length() > width`. No tests yet.

## Testing Backlog (prioritized)

1. [HIGH] Tests for `DateUtils.isOverdue`, `isWithinRange`, `addBusinessDays` — still missing (March patches in #155, #159 not merged)
2. [HIGH] Investigate if `DateUtils.getQuarter()` bug fixed (issue #156 closed)
3. [HIGH] Investigate if `TaskService.getTaskStatistics()` div/zero fixed (issue #157 closed)
4. [MEDIUM] Tests for `StringUtils.padRight()` to document the NegativeArraySizeException bug
5. [MEDIUM] Tests for `DateUtils.addBusinessDays()` weekend-crossing case (Sun treated as weekday)
6. [LOW] Propose JaCoCo coverage plugin (open issue #146)

## Completed Work

| Date | Work | PR/Issue |
|------|------|---------|
| 2026-04-01 | TaskServiceTest: @BeforeEach cleanup + assertions for testGetTasksByStatus/Assignee + fix testGetTask | PR via safeoutputs (branch test-assist/taskservice-test-cleanup-assertions) |
| 2026-04-01 | Created April 2026 Monthly Activity Summary | issue aw_apr001 |
| 2026-03-31 (run 2) | Retry: 8 new DateUtils tests (isOverdue, isWithinRange, addBusinessDays) | patch in run 23824526408 |
| 2026-03-31 (run 2) | Commented on #157 with testing guidance for TaskService div/zero fix | comment on #157 |
| 2026-03-31 (run 1) | Filed bug: DateUtils.getQuarter off-by-one | issue #156 (now closed) |
| 2026-03-31 (run 1) | Filed bug: TaskService.getTaskStatistics div/zero | issue #157 (now closed) |
| 2026-03-31 (run 1) | 8 new DateUtils tests — git push failed, delivered as issue #155 patch | issue #155 (closed) |

## Last Run Tasks

- 2026-04-01: Task 2 (opportunities), Task 4 (PR check), Task 6 (test infra — TaskServiceTest PR), Task 7 (Monthly summary)
- Next run: Task 1 (validate commands still work), Task 3 (DateUtils tests), Task 5 (comment on issues)

## Monthly Summary Issues

- March 2026: #158 (CLOSED)
- April 2026: aw_apr001 (created this run)

## Maintainer Notes

- Issue #140 (unreliable test suite) is still open — primary focus area
- Issue #146 (JaCoCo) is open — needs discussion before pom.xml changes
- Previous Test Improver issues #155, #156, #157, #158, #159 all CLOSED
