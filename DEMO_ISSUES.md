# GitHub Issues for Demo

以下是建議在此 repo 建立的 GitHub Issues，用於展示 Agentic Workflows 的能力。
這些 Issue 模擬一個真實的 Legacy 專案中累積的各種問題。

---

## 🔴 Bugs (Severity: Critical/High)

### Issue 1: SQL Injection vulnerability in task search endpoint
**Labels:** (none - unlabeled for triage demo)
```
The `/api/tasks/search` endpoint is vulnerable to SQL injection. 
The keyword parameter is directly concatenated into SQL query string.

Steps to reproduce:
1. Call GET /api/tasks/search?keyword=' OR 1=1 --
2. All tasks are returned regardless of search term

This is a critical security vulnerability.
```

### Issue 2: Passwords stored in plaintext
**Labels:** (none)
```
User passwords are stored in plaintext in the database. The User entity stores the raw password string and the authenticate() method compares plaintext values.

This violates basic security practices and likely violates compliance requirements.

Found by: Security audit 2023-Q4
```

### Issue 3: Application crashes when viewing empty project dashboard
**Labels:** (none)
```
When a project has 0 tasks, the dashboard endpoint throws ArithmeticException: / by zero

Stack trace:
java.lang.ArithmeticException: / by zero
    at com.taskflow.model.Project.getProgress(Project.java:98)
    at com.taskflow.controller.ProjectController.getProjectDashboard(ProjectController.java:82)

Environment: Production
Frequency: Every time for new projects
```

### Issue 4: Intermittent date parsing errors under high load
**Labels:** (none)
```
In production, we're seeing intermittent NumberFormatException and ArrayIndexOutOfBoundsException from DateUtils.parseDate().

This happens under high load (>100 concurrent requests).

Stack trace:
java.lang.NumberFormatException: For input string: ""
    at java.text.SimpleDateFormat.parse(SimpleDateFormat.java:...)
    at com.taskflow.util.DateUtils.parseDate(DateUtils.java:35)

We suspect it's related to thread safety but haven't confirmed.
Workaround: Restart the server when it happens.
```

### Issue 5: NullPointerException in Task.toString()
**Labels:** (none)
```
Getting NPE when logging tasks that don't have an assignee or due date.

java.lang.NullPointerException
    at com.taskflow.model.Task.toString(Task.java:98)

This crashes several batch jobs that log task details.
```

---

## 🟡 Feature Requests / Improvements

### Issue 6: Need API pagination - OOM in production
**Labels:** (none)
```
GET /api/tasks returns ALL tasks. We now have 50,000+ tasks and this endpoint 
causes OutOfMemoryError in production.

We need pagination support on all list endpoints.
```

### Issue 7: Need proper authentication and authorization
**Labels:** (none)
```
Currently there is no authentication middleware. Any request can access any endpoint.
The login endpoint returns a fake JWT token.

We need:
- Proper JWT authentication
- Role-based access control
- Endpoint-level authorization

Priority: High (compliance audit coming in Q2)
```

### Issue 8: Upgrade Spring Boot from 2.7 to 3.x
**Labels:** (none)
```
Spring Boot 2.7.x is EOL. We need to upgrade to Spring Boot 3.x.

This will also require:
- Migrating from javax.* to jakarta.* namespace
- Updating Java version requirement (probably to 17+)
- Reviewing deprecated API usage

I know this is a big change but we're accumulating security debt.
```

### Issue 9: TaskService is too large, needs refactoring
**Labels:** (none)
```
TaskService.java is a God class with 600+ lines handling:
- CRUD operations
- Notifications
- Statistics
- Import/export
- Assignment logic
- Status workflow

This makes it impossible to:
- Write proper unit tests
- Understand the code
- Make changes without breaking something else

Suggest breaking into: TaskCrudService, TaskWorkflowService, TaskNotificationService, TaskReportingService
```

### Issue 10: Replace custom StringUtils with Apache Commons
**Labels:** (none)
```
We have a custom StringUtils class that duplicates functionality already available in 
Apache Commons Lang (which is already in our dependencies!).

Our version has bugs:
- sanitize() is trivially bypassable
- padRight() throws StringIndexOutOfBoundsException
- toSnakeCase() doesn't handle consecutive uppercase

Should delete our custom version and use the standard library.
```

---

## 🟠 Technical Debt

### Issue 11: Remove hardcoded credentials from source code
**Labels:** (none)
```
Found hardcoded credentials in multiple files:
- application.properties: commented-out production database password
- DatabaseHelper.java: DB credentials as static fields
- NotificationService.java: SMTP password and Slack webhook URL
- DataInitializer.java: user passwords

These should be moved to environment variables or a secrets manager.
```

### Issue 12: DatabaseHelper uses raw JDBC with SQL injection vulnerabilities
**Labels:** (none)
```
DatabaseHelper.java contains raw JDBC code that:
1. Concatenates user input directly into SQL strings (SQL injection)
2. Never closes ResultSet, Statement, or Connection objects (resource leaks)
3. Provides an executeQuery() method that runs arbitrary SQL

This class should be replaced with JPA/Spring Data repositories.
```

### Issue 13: Test suite is unreliable
**Labels:** (none)
```
Our test suite has multiple problems:
- 5 tests are @Disabled because they were "flaky"
- Several tests have no assertions (inflate coverage numbers)
- Tests depend on DataInitializer data (fragile)
- No @BeforeEach cleanup, tests interfere with each other
- 0% coverage on controllers
- 0% coverage on security-critical code paths

Current effective test coverage is probably <10%.
```

### Issue 14: Inconsistent naming conventions throughout codebase
**Labels:** (none)
```
The codebase mixes:
- camelCase: `reporterId`, `projectCode`, `createdDate`
- snake_case: `assignee_id`, `due_date`, `created_at`, `phone_number`
- Fields: `full_name` vs `firstName`/`lastName`
- Getters: `getOwner_id()` vs `getCode()`

This makes the code confusing and the API responses inconsistent.
Need to standardize on one convention.
```

### Issue 15: Dead code and deprecated methods should be removed
**Labels:** (none)
```
Found several pieces of dead code:
- TaskService.getOldDashboardStats() - deprecated, referenced by decommissioned admin panel
- TaskService.migrateLegacyIds() - was for JIRA migration in 2019
- TaskService.generateWeeklyReport() - replaced by dashboard
- Task.workflow_state field - from old workflow engine
- Task.legacy_id field - from JIRA migration
- Project.color/icon fields - from old dashboard

This dead code adds confusion and maintenance burden.
```

---

## 🔵 Questions / Discussion

### Issue 16: What's our strategy for the mobile app?
**Labels:** (none)
```
The MOBILE project has been "on hold" since 2021. 
Should we:
a) Resume development?
b) Archive the project?
c) Start fresh with a new tech stack?

cc: @kevin @jennifer
```

### Issue 17: Should we move to microservices?
**Labels:** (none)
```
With all the coupling issues in our monolith, should we consider breaking 
this into microservices?

Concerns:
- TaskService is too coupled to everything
- Scaling issues with the monolith
- Team velocity is decreasing

Counter-arguments:
- We're a small team (3 people)
- Added operational complexity
- Migration effort

Looking for discussion before we decide.
```

### Issue 18: Log4j 1.x still in use - security risk?
**Labels:** (none)
```
We're still using log4j 1.x (version 1.2.17) which has been EOL since 2015.
While this is NOT the same as the Log4Shell vulnerability (that was log4j 2.x),
it still has known CVEs and receives no patches.

Should we prioritize migrating to Logback/SLF4J?
This was supposed to be done in 2022 (see task in backlog).
```

---

## 📝 Notes for Demo

### Triage Workflow Demo
- 上面的 Issues 都沒有 labels，可以展示 Triage workflow 如何自動分類
- 有些是 bugs、有些是 features、有些是 questions
- 有些是 security issues 需要標記為 critical
- 有些可能是 duplicates (Issue 1 和 Issue 12 都提到 SQL injection)

### Fault Investigation Demo
- Issue 3 (division by zero) 有明確的 stack trace 可以調查
- Issue 4 (thread-safety) 是經典的 intermittent bug
- Issue 5 (NPE) 是最常見的 Java bug

### Continuous Refactoring Demo
- Issue 9 (God class) 是最明顯的重構目標
- Issue 10 (StringUtils) 是簡單的「用標準庫取代自製工具」
- Issue 14 (naming conventions) 需要全面統一
- Issue 15 (dead code) 是安全的清理工作

### Metrics & Analytics Demo
- 可以分析 test coverage (很低)
- 可以分析 code complexity (TaskService 很高)
- 可以分析 dependency vulnerabilities (Log4j 1.x, old Jackson)
- 可以分析 code duplication (多處重複邏輯)
- 可以追蹤 bug vs feature ratio
- 可以追蹤 overdue tasks 數量
