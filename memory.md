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

- `DataInitializer` runs during tests, polluting test state - fixed by @BeforeEach taskRepository.deleteAll()
- `DateUtils.getQuarter()` has a documented off-by-one bug (issue #7) - returns 0-3, should return 1-4
- `addBusinessDays()` has a bug: checks `DAY_OF_WEEK != 6 && != 7` but in Java Calendar, Friday=6 and Saturday=7
- `StringUtils.padRight()` has a known StringIndexOutOfBoundsException bug (issue #8)
- Thread-safety bug in `DateUtils` using static `SimpleDateFormat` fields (issue #5)
- `startOfDay()` doesn't reset milliseconds (minor bug)
- `isWithinRange()` uses exclusive bounds but should be inclusive

## Maintainer Priorities

- No specific priorities communicated yet. Several open issues indicate the codebase has known bugs.

## Testing Backlog (prioritized)

1. **DONE**: Fix `TaskServiceTest` - add @BeforeEach cleanup, proper assertions - PR created 2026-08-02
2. **MEDIUM**: Add regression tests for DateUtils bugs (getQuarter off-by-one, addBusinessDays wrong day constants)
3. **MEDIUM**: Add regression tests for StringUtils.padRight StringIndexOutOfBoundsException
4. **LOW**: Thread-safety tests for DateUtils (demonstrates issue #5)

## Work In Progress

None.

## Completed Work

- 2026-08-02: PR created fixing TaskServiceTest (@BeforeEach cleanup, proper assertions for 6 no-assertion tests, 2 new tests)

## Task Round-Robin History

- 2026-08-01 Run 1: Task 1 (Commands), Task 2 (Opportunities), Task 7 (Monthly Summary)
- 2026-08-01 Run 2: Task 3 (Implement Tests - regression tests PR), Task 7 (Monthly Summary)
- 2026-08-02 Run 3: Task 4 (PR check - none open), Task 3 (Fix TaskServiceTest), Task 7 (Monthly Summary)

## Backlog Cursor

- Issues reviewed: all open issues scanned
- Previous regression tests PR: not found in open PRs (may not have been created or was closed)
- Next testing opportunity: Regression tests for DateUtils/StringUtils bugs (item 2 in backlog)
