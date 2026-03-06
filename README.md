# TestIT Cucumber Suite

BDD acceptance test suite for the TestIT API, written in Java using Cucumber-JVM and RestAssured.
Mirrors and extends the C# xUnit suite at `~/examtest1`.

---

## Prerequisites

The tools are pre-installed in `~/tools/` — no PATH changes needed.

| Tool | Path |
|------|------|
| Java 21 | `~/tools/jdk-21.0.2` |
| Maven 3.9.6 | `~/tools/apache-maven-3.9.6` |

The API must be running and reachable at `http://localhost:8000` before any tests are run.

---

## Running the suite

### Recommended — wrapper script

```bash
cd ~/testit-cucumber-suite
./run.sh                  # plain run
./run.sh login-fix        # run with a label (used in the report filename)
```

The script:
- Runs Maven and streams output to the terminal
- Copies the HTML report to `reports/report-<timestamp>-<label>.html`
- Updates `reports/latest-report.html` to always point to the most recent run
- Exits with Maven's exit code (0 = all passed, non-zero = failures)

### Raw Maven command

```bash
JAVA_HOME=~/tools/jdk-21.0.2 ~/tools/apache-maven-3.9.6/bin/mvn test
```

---

## Reports

After each run, two files are always current:

```
reports/latest-report.html   ← open this in a browser
reports/latest-log.log       ← full terminal output
```

Timestamped copies are kept alongside them. The `reports/` directory is gitignored.

---

## Project structure

```
src/test/
├── java/testit/
│   ├── context/
│   │   └── ScenarioContext.java      # shared state injected into all step classes
│   ├── hooks/
│   │   └── Hooks.java                # Before/After hooks (context reset, base URL)
│   ├── runner/
│   │   └── CucumberTestRunner.java   # JUnit 5 suite entry point
│   └── steps/
│       ├── AuthenticationSteps.java
│       ├── TestManagementSteps.java
│       ├── TestVisibilitySteps.java
│       ├── QuestionManagementSteps.java
│       ├── TestTakingSteps.java
│       ├── FolderSteps.java
│       ├── AnalyticsSteps.java
│       ├── CompanySteps.java
│       └── ...
└── resources/
    └── features/
        ├── authentication.feature
        ├── test_management.feature
        ├── test_visibility.feature
        ├── question_management.feature
        ├── test_taking.feature
        ├── scoring.feature
        ├── folders.feature
        ├── analytics.feature
        ├── password_protected.feature
        └── company_management.feature
```

---

## Coverage (54 scenarios)

| Feature file | Scenarios | What is tested |
|---|---|---|
| `authentication` | 12 | Register, login, wrong password, profile, duplicate email, invalid data outline |
| `test_management` | 4 | Create, list, update title, delete |
| `test_visibility` | 4 | public / link_only / password_protected access, update visibility |
| `question_management` | 7 | Add MC / exact / multi-select questions, update, delete, reorder, 404 |
| `test_taking` | 6 | Anonymous access, score outline, view results, double-submit blocked |
| `scoring` | 5 | Exact answer case-insensitive match, multi-select correct/wrong |
| `folders` | 5 | Create, assign/unassign, reassign between folders, non-existent 400 |
| `analytics` | 4 | Zero stats, post-submission stats, unauthenticated 401, non-author denied |
| `password_protected` | 3 | Blocked without password, correct password, wrong password |
| `company_management` | 7 | Full CRUD, member listing, company test creation, unauthenticated 401 |

---

## How it works

- **Gherkin** (`.feature` files) describes behaviour in plain English
- **Step definitions** (`.java` files in `steps/`) match each Gherkin line via annotations like `@Given`, `@When`, `@Then`
- **ScenarioContext** is a shared state object injected into every step class via PicoContainer — it carries tokens, slugs, IDs, and cookies between steps within a scenario
- **Hooks.java** resets the context before each scenario and sets the RestAssured base URI

### Key implementation detail — anonymous session cookies

The API uses session cookies to prove ownership of an anonymous attempt. RestAssured does not carry cookies automatically between requests, so after starting an attempt the cookies are captured manually:

```java
context.setAnonCookies(response.cookies());
// then on every subsequent draft/submit call:
given().cookies(context.getAnonCookies())...
```

---

## Adding new scenarios

1. Add a `.feature` file (or a new `Scenario` block in an existing one) under `src/test/resources/features/`
2. Run the suite — Cucumber prints `Undefined` for any unmatched step
3. Implement the missing steps in the appropriate `*Steps.java` class
4. If the step needs shared state, add the field to `ScenarioContext.java`
5. Run again to confirm green

---

## Key dependencies (pom.xml)

| Library | Version | Purpose |
|---|---|---|
| cucumber-java | 7.18.0 | Step definition annotations |
| cucumber-junit-platform-engine | 7.18.0 | JUnit 5 integration |
| cucumber-picocontainer | 7.18.0 | Dependency injection into step classes |
| rest-assured | 5.4.0 | HTTP client DSL for API calls |
| junit-jupiter | 5.10.2 | Assertions (`assertEquals`, `assertTrue`, etc.) |
