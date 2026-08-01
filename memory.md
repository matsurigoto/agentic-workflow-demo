# Test Improver Memory - matsurigoto/agentic-workflow-demo

## Build/Test/Coverage Commands

- **Build**: `mvn compile`
- **Test**: `mvn test`
- **Coverage**: Not configured (issue #27 requests JaCoCo setup)
- **Lint/Format**: None configured

**Status**: `mvn test` FAILS in CI sandbox due to Maven local repo permissions (`/home/runner/.m2/repository` not writable). This is an infrastructure constraint, not a code issue.

## Testing Framework

- JUnit 5 (JUnit Jupiter)
- Spring Boot Test (`@SpringBootTest`)
- Java 11, Spring Boot 2.7.18

## Testing Notes

- Tests have significant quality issues (issue #21): disabled tests, no-assertion tests, fragile data-dependent tests
- `DataInitializer` runs during tests, polluting test state (no @BeforeEach cleanup)
- `DateUtils.getQuarter()` has a documented off-by-one bug (issue #7) - returns 0-3, should return 1-4; `testGetQuarter` test already captures this and currently FAILS
- `addBusinessDays()` has a bug: checks `DAY_OF_WEEK != 6 && != 7` but in Java Calendar, Saturday=7 and Sunday=1, so this incorrectly skips Friday (6) and Saturday (7) rather than Saturday (7) and Sunday (1)
- `StringUtils.padRight()` has a known StringIndexOutOfBoundsException bug (issue #8) - no test coverage
- Thread-safety bug in `DateUtils` using static `SimpleDateFormat` fields (issue #5)
- `startOfDay()` doesn't reset milliseconds (minor bug)
- `isWithinRange()` uses exclusive bounds but should be inclusive

## Maintainer Priorities

- No specific priorities communicated yet. Several open issues indicate the codebase has known bugs.

## Testing Backlog (prioritized)

1. **HIGH**: Add regression tests for known bugs in DateUtils (getQuarter, addBusinessDays, startOfDay milliseconds, isWithinRange boundary) - document bugs, not hide them
2. **HIGH**: Add tests for `StringUtils.padRight()` to document/expose the StringIndexOutOfBoundsException bug (issue #8)
3. **MEDIUM**: Fix `TaskServiceTest` - add proper @BeforeEach cleanup, add assertions to no-assertion tests
4. **MEDIUM**: Add tests for DateUtils.isOverdue, isWithinRange edge cases
5. **LOW**: Thread-safety tests for DateUtils (demonstrates issue #5)

## Work In Progress

None currently.

## Completed Work

None.

## Task Round-Robin History

- 2026-08-01: Run 1 - Task 1 (Commands), Task 2 (Opportunities), Task 7 (Monthly Summary)

## Backlog Cursor

- Issues reviewed: all open issues scanned
- Next testing opportunity: DateUtils regression tests (item 1 in backlog)
