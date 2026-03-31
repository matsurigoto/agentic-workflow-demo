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

## Known Bugs (from code analysis + failing tests)

1. **`DateUtils.getQuarter()`** — returns 0-based (0–3) instead of 1-based (1–4). Fix: `(month/3)+1`. Bug issue filed.
2. **`TaskService.getTaskStatistics()`** — `ArithmeticException: / by zero` when no completed tasks. Bug issue filed.
3. **`DateUtils.addBusinessDays()`** — skips Friday (DAY_OF_WEEK=6) instead of Friday=6 (correct but…) wait: actually it skips 6 (FRIDAY) and 7 (SATURDAY), not skipping SUNDAY (1). Bug: treats Sunday as a business day.
4. **`DateUtils.startOfDay()`** — doesn't reset milliseconds.
5. **`DateUtils.isWithinRange()`** — exclusive range; probably should be inclusive.
6. **`StringUtils.padRight()`** — `StringIndexOutOfBoundsException` when `str.length() > width`.

## Testing Backlog (prioritized)

1. [HIGH] Fix `DateUtils.getQuarter()` bug (bug issue filed: aw_bug1)
2. [HIGH] Fix `TaskService.getTaskStatistics()` div/zero (bug issue filed: aw_bug2)
3. [MEDIUM] Tests for `StringUtils.padRight()` to document the SIOOBE bug
4. [MEDIUM] Tests for `DateUtils.addBusinessDays()` weekend-crossing case (Sun treated as weekday)
5. [MEDIUM] `TaskServiceTest` refactor — add `@BeforeEach` cleanup, remove assertion-free tests
6. [LOW] Add Jacoco coverage plugin to pom.xml (propose via issue first)

## Completed Work

| Date | Work | PR/Issue |
|------|------|---------|
| 2026-03-31 | Added 8 new DateUtils tests (isOverdue, isWithinRange, addBusinessDays, startOfDay) | PR: test-assist/dateutils-missing-coverage |
| 2026-03-31 | Filed bug: DateUtils.getQuarter off-by-one | aw_bug1 |
| 2026-03-31 | Filed bug: TaskService.getTaskStatistics div/zero | aw_bug2 |

## Last Run Tasks

- 2026-03-31: Task 1 (Discover commands), Task 3 (Implement tests), Task 7 (Monthly summary)
- Next run: Task 2 (more opportunities), Task 4 (PR maintenance), Task 5 (issue comments)
