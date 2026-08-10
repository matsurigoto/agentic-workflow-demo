# Test Improver Memory - matsurigoto/agentic-workflow-demo

## Build/Test/Coverage Commands

- **Build**: `mvn compile`
- **Test**: `mvn test`
- **Coverage**: `mvn test` → `target/site/jacoco/` (JaCoCo added via PR, issue #27)
- **Lint/Format**: None configured

**Status**: `mvn test` FAILS in CI sandbox due to Maven local repo permissions (`/home/runner/.m2/repository` not writable). This is an infrastructure constraint, not a code issue.

## Testing Framework

- JUnit 5 (JUnit Jupiter)
- Spring Boot Test (`@SpringBootTest`, `@WebMvcTest`)
- MockMvc + Mockito for controller tests
- Java 11, Spring Boot 2.7.18

## Testing Notes

- `DataInitializer` runs during tests, polluting test state - fixed by @BeforeEach taskRepository.deleteAll()
- `DateUtils.getQuarter()` has a documented off-by-one bug (issue #7) - returns 0-3, should return 1-4
- `addBusinessDays()` has a bug: checks `DAY_OF_WEEK != 6 && != 7` but 6=FRIDAY, 7=SATURDAY - so it skips Friday and Saturday but counts Sunday
- `StringUtils.padRight()` has a known StringIndexOutOfBoundsException bug (issue #8)
- Thread-safety bug in `DateUtils` using static `SimpleDateFormat` fields (issue #5)
- `startOfDay()` doesn't reset milliseconds (minor bug)
- `isWithinRange()` uses exclusive bounds but should be inclusive
- `TaskController.getTask()` returns 200 with null body when not found (should be 404)
- `TaskController.deleteTask()` returns 400 for "not found" errors (should be 404)
- `TaskService.getTaskStatistics()` throws ArithmeticException (÷0) when no tasks are DONE — documented with @Disabled test
- GitHub Actions can create PRs via safeoutputs create_pull_request (branches + PR created)
- `UserService.authenticate()`: timing attack, plaintext password - documented bugs, don't fix in tests
- `UserService` uses `@BeforeEach userRepository.deleteAll()` to isolate tests (same as TaskServiceTest pattern)
- `UserController` DELETE/PUT return 400 (not 404) when resource not found — documented in tests
- `UserController` returns reset token in password-reset response (security bug) — documented in tests

## Maintainer Priorities

- No specific priorities communicated yet. Several open issues indicate known bugs.

## Testing Backlog (prioritized)

1. **DONE**: Fix `TaskServiceTest` - add @BeforeEach cleanup, proper assertions - PR #92
2. **DONE**: Regression tests for DateUtils (isWithinRange, addBusinessDays, isOverdue) - branch pushed 2026-08-03
3. **DONE**: Regression tests for StringUtils.padRight StringIndexOutOfBoundsException - branch pushed 2026-08-03
4. **DONE**: Controller integration tests - `@WebMvcTest` for TaskController - branch pushed 2026-08-05
5. **DONE**: Thread-safety tests for DateUtils (demonstrates issue #5) - PR #87 created 2026-08-06
6. **DONE**: Improve TaskServiceTest assertions (23 tests, 2 @Disabled) - PR #92 created 2026-08-07
7. **DONE**: Test infrastructure — JaCoCo coverage reporting - issue #97 (blocked on protected pom.xml)
8. **DONE**: UserService tests - authentication, display name, role filtering, bulk deactivation - PR #102 created 2026-08-09
9. **DONE**: UserController @WebMvcTest integration tests - 20 tests - PR created 2026-08-10
10. **NEXT**: ProjectController @WebMvcTest integration tests (uses ProjectRepository + TaskService)

## Work In Progress

None.

## Completed Work

- 2026-08-02: Pushed branch fixing TaskServiceTest (@BeforeEach cleanup, proper assertions) - issue #66
- 2026-08-03: Pushed branch adding regression tests for DateUtils+StringUtils - issue #71
- 2026-08-04: Commented on #21 (progress update) and #27 (JaCoCo guidance)
- 2026-08-05: Pushed branch `test-assist/task-controller-integration-tests` - 13 @WebMvcTest tests for TaskController - issue #79
- 2026-08-06: Created PR #87 for thread-safety regression tests (DateUtils issue #5) - branch `test-assist/dateutils-thread-safety-tests`
- 2026-08-07: Created PR #92 (branch `test-assist/improve-taskservice-assertions`) — 23 tests, 2 @Disabled, @BeforeEach cleanup, real assertions
- 2026-08-08: Created issue #97 for JaCoCo (pom.xml protected, manual steps provided)
- 2026-08-09: Created PR #102 for UserServiceTest (branch `test-assist/userservice-tests`) — 14 tests: createUser, authenticate, getUserDisplayName, getActiveUsersByRole, deactivateUsers
- 2026-08-10: Created PR for UserControllerTest (branch `test-assist/usercontroller-webmvctest`) — 20 @WebMvcTest tests covering all endpoints

## Task Round-Robin History

- 2026-08-01 Run 1: Task 1 (Commands), Task 2 (Opportunities), Task 7 (Monthly Summary)
- 2026-08-01 Run 2: Task 3 (Implement Tests - regression tests branch), Task 7 (Monthly Summary)
- 2026-08-02 Run 3: Task 4 (PR check - none open), Task 3 (Fix TaskServiceTest), Task 7 (Monthly Summary)
- 2026-08-03 Run 4: Task 3 (Regression tests DateUtils+StringUtils), Task 7 (Monthly Summary)
- 2026-08-04 Run 5: Task 4 (PR check - no open test-improver PRs), Task 5 (Commented on #21, #27), Task 7 (Monthly Summary)
- 2026-08-05 Run 6: Task 3 (TaskController integration tests), Task 7 (Monthly Summary)
- 2026-08-06 Run 7: Task 3 (Thread-safety tests for DateUtils), Task 7 (Monthly Summary)
- 2026-08-07 Run 8: Task 4 (PR #87 ok, no CI failures), Task 3 (Improve TaskServiceTest assertions), Task 7 (Monthly Summary)
- 2026-08-08 Run 9: Task 4 (PRs #87/#92 ok), Task 6 (JaCoCo coverage setup → issue #97), Task 7 (Monthly Summary)
- 2026-08-09 Run 10: Task 4 (PRs #87/#92 ok), Task 3 (UserServiceTest - 14 tests), Task 7 (Monthly Summary)
- 2026-08-10 Run 11: Task 4 (PRs #87/#92/#102 open), Task 3 (UserControllerTest - 20 tests), Task 7 (Monthly Summary)

## Backlog Cursor

- Issues reviewed: #21, #27 (commented 2026-08-04)
- Next run should focus on: Task 4 (check PRs), Task 3 (ProjectController @WebMvcTest tests)

## Previously Checked Off Items

None yet.
