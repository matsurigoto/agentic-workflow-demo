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
- `addBusinessDays()` has a bug: checks `DAY_OF_WEEK != 6 && != 7` but 6=FRIDAY, 7=SATURDAY - so it skips Friday and Saturday but counts Sunday
- `StringUtils.padRight()` has a known StringIndexOutOfBoundsException bug (issue #8)
- Thread-safety bug in `DateUtils` using static `SimpleDateFormat` fields (issue #5)
- `startOfDay()` doesn't reset milliseconds (minor bug)
- `isWithinRange()` uses exclusive bounds but should be inclusive
- GitHub Actions cannot create PRs in this repo; branches are pushed and issues created with PR links

## Maintainer Priorities

- No specific priorities communicated yet. Several open issues indicate the codebase has known bugs.

## Testing Backlog (prioritized)

1. **DONE**: Fix `TaskServiceTest` - add @BeforeEach cleanup, proper assertions - branch pushed 2026-08-02
2. **DONE**: Regression tests for DateUtils (isWithinRange, addBusinessDays, isOverdue) - branch pushed 2026-08-03
3. **DONE**: Regression tests for StringUtils.padRight StringIndexOutOfBoundsException - branch pushed 2026-08-03
4. **NEXT**: Controller integration tests - `@WebMvcTest` for TaskController (0% coverage)
5. **LOW**: Thread-safety tests for DateUtils (demonstrates issue #5)
6. **LOW**: Integration tests for TaskService

## Work In Progress

None.

## Completed Work

- 2026-08-02: Pushed branch fixing TaskServiceTest (@BeforeEach cleanup, proper assertions) - issue #66
- 2026-08-03: Pushed branch adding regression tests for DateUtils+StringUtils - issue #71
- 2026-08-04: Commented on #21 (progress update) and #27 (JaCoCo guidance)

## Task Round-Robin History

- 2026-08-01 Run 1: Task 1 (Commands), Task 2 (Opportunities), Task 7 (Monthly Summary)
- 2026-08-01 Run 2: Task 3 (Implement Tests - regression tests branch), Task 7 (Monthly Summary)
- 2026-08-02 Run 3: Task 4 (PR check - none open), Task 3 (Fix TaskServiceTest), Task 7 (Monthly Summary)
- 2026-08-03 Run 4: Task 3 (Regression tests DateUtils+StringUtils), Task 7 (Monthly Summary)
- 2026-08-04 Run 5: Task 4 (PR check - no open test-improver PRs), Task 5 (Commented on #21, #27), Task 7 (Monthly Summary)

## Backlog Cursor

- Issues reviewed: #21, #27 (commented 2026-08-04)
- Next run should focus on: Task 3 (controller integration tests), Task 6 (test infrastructure - JaCoCo)

## Previously Checked Off Items

None yet.
