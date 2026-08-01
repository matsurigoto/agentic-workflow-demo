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
- `DateUtils.getQuarter()` has a documented off-by-one bug (issue #7) - returns 0-3, should return 1-4
- `addBusinessDays()` has a bug: checks `DAY_OF_WEEK != 6 && != 7` but in Java Calendar, Friday=6 and Saturday=7, so this incorrectly skips Friday and Saturday rather than Saturday and Sunday
- `StringUtils.padRight()` has a known StringIndexOutOfBoundsException bug (issue #8) - throws when str.length() > width
- Thread-safety bug in `DateUtils` using static `SimpleDateFormat` fields (issue #5)
- `startOfDay()` doesn't reset milliseconds (minor bug)
- `isWithinRange()` uses exclusive bounds but should be inclusive

## Maintainer Priorities

- No specific priorities communicated yet. Several open issues indicate the codebase has known bugs.

## Testing Backlog (prioritized)

1. **DONE**: Regression tests for DateUtils bugs and StringUtils.padRight - PR created 2026-08-01
2. **MEDIUM**: Fix `TaskServiceTest` - add proper @BeforeEach cleanup, add assertions to no-assertion tests
3. **MEDIUM**: Add `@Tag("known-bug")` to failing regression tests so CI can filter them
4. **LOW**: Thread-safety tests for DateUtils (demonstrates issue #5)

## Work In Progress

None.

## Completed Work

- 2026-08-01: PR created with 20 regression tests for DateUtils (getQuarter, startOfDay, isWithinRange, addBusinessDays, isOverdue) and StringUtils (padRight, toSnakeCase, isEmpty edge cases)

## Task Round-Robin History

- 2026-08-01 Run 1: Task 1 (Commands), Task 2 (Opportunities), Task 7 (Monthly Summary)
- 2026-08-01 Run 2: Task 3 (Implement Tests), Task 7 (Monthly Summary)

## Backlog Cursor

- Issues reviewed: all open issues scanned
- Next testing opportunity: Fix TaskServiceTest (item 2 in backlog)
