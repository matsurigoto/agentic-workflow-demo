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

- `DataInitializer` runs on application start and seeds sample data; `TaskServiceTest` does NOT clean up between tests — tests can interfere
- `DateUtils` uses `static SimpleDateFormat` fields — thread-safety bug, not fixed ("won't fix")
- Pre-existing CI failures: `testGetQuarter` (off-by-one in impl) and `testGetTaskStatistics` (div/zero)
- No Mockito/mock usage anywhere — all tests are Spring integration tests
- git push fails in environment — PRs are delivered as patch artifacts in workflow runs; need manual apply

## Known Bugs (from code analysis + failing tests)

1. **`DateUtils.getQuarter()`** — returns 0-based (0–3) instead of 1-based (1–4). Bug issue filed #156.
2. **`TaskService.getTaskStatistics()`** — `ArithmeticException: / by zero` when no completed tasks. Bug issue filed #157.
3. **`DateUtils.addBusinessDays()`** — skips DAY_OF_WEEK 6 (Friday) and 7 (Saturday) but NOT 1 (Sunday). Bug noted.
4. **`DateUtils.startOfDay()`** — doesn't reset milliseconds. Bug noted.
5. **`DateUtils.isWithinRange()`** — exclusive range (after/before); probably should be inclusive. Documented in tests.
6. **`StringUtils.padRight()`** — `NegativeArraySizeException` when `str.length() > width`. No tests yet.

## Testing Backlog (prioritized)

1. [HIGH] Fix `DateUtils.getQuarter()` bug (bug issue filed #156)
2. [HIGH] Fix `TaskService.getTaskStatistics()` div/zero (bug issue filed #157)
3. [MEDIUM] Tests for `StringUtils.padRight()` to document the NegativeArraySizeException bug
4. [MEDIUM] Tests for `DateUtils.addBusinessDays()` weekend-crossing case (Sun treated as weekday)
5. [MEDIUM] `TaskServiceTest` refactor — add `@BeforeEach` cleanup, remove assertion-free tests
6. [LOW] Add Jacoco coverage plugin to pom.xml (propose via issue first)

## Completed Work

| Date | Work | PR/Issue |
|------|------|---------|
| 2026-03-31 (run 2) | Retry: 8 new DateUtils tests (isOverdue, isWithinRange, addBusinessDays) | patch in run 23824526408 |
| 2026-03-31 (run 2) | Commented on #157 with testing guidance for TaskService div/zero fix | comment on #157 |
| 2026-03-31 (run 1) | Filed bug: DateUtils.getQuarter off-by-one | issue #156 |
| 2026-03-31 (run 1) | Filed bug: TaskService.getTaskStatistics div/zero | issue #157 |
| 2026-03-31 (run 1) | 8 new DateUtils tests — git push failed, delivered as issue #155 patch | issue #155 |

## Last Run Tasks

- 2026-03-31 (run 2): Task 3 (retry DateUtils tests), Task 5 (comment on #157), Task 7 (Monthly summary)
- Next run: Task 2 (more opportunities), Task 4 (check PRs), Task 6 (test infrastructure)

## Maintainer Notes

- Maintainer pinged #157 (TaskService stats div/zero) via `/test-assist` — elevated priority
- Monthly summary issue: #158 (March 2026)
