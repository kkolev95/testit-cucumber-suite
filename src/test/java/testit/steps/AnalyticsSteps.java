package testit.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import testit.context.ScenarioContext;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.junit.jupiter.api.Assertions.*;

public class AnalyticsSteps {

    private final ScenarioContext context;

    public AnalyticsSteps(ScenarioContext context) {
        this.context = context;
    }

    @When("the author requests analytics for the test")
    public void theAuthorRequestsAnalyticsForTheTest() {
        Response response = given()
            .header("Authorization", "Bearer " + context.getAccessToken())
        .when()
            .get("/analytics/tests/" + context.getTestSlug() + "/");
        context.setLastResponse(response);
    }

    @Then("the total attempts should be {int}")
    public void theTotalAttemptsShouldBe(int expected) {
        context.getLastResponse().then().statusCode(200);
        int actual = context.getLastResponse().jsonPath().getInt("total_attempts");
        assertEquals(expected, actual,
            "Expected total_attempts=" + expected + " but was " + actual);
    }

    @Then("the analytics include question stats")
    public void theAnalyticsIncludeQuestionStats() {
        List<?> questionStats = context.getLastResponse().jsonPath().getList("question_stats");
        assertNotNull(questionStats, "question_stats should not be null");
        assertFalse(questionStats.isEmpty(),
            "question_stats should not be empty after a submission");
    }

    @When("an unauthenticated user requests analytics for the test")
    public void anUnauthenticatedUserRequestsAnalyticsForTheTest() {
        Response response = given()
        .when()
            .get("/analytics/tests/" + context.getTestSlug() + "/");
        context.setLastResponse(response);
    }

    @When("a different authenticated user requests analytics for the test")
    public void aDifferentAuthenticatedUserRequestsAnalyticsForTheTest() {
        String email = "other_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8) + "@test.com";
        String password = "Password123!";

        given()
            .contentType(JSON)
            .body(Map.of(
                "email",            email,
                "password",         password,
                "password_confirm", password,
                "first_name",       "Other",
                "last_name",        "User"
            ))
        .when()
            .post("/auth/register/")
        .then()
            .statusCode(201);

        Response loginResp = given()
            .contentType(JSON)
            .body(Map.of("email", email, "password", password))
        .when()
            .post("/auth/login/");
        loginResp.then().statusCode(200);
        String otherToken = loginResp.jsonPath().getString("access");

        Response response = given()
            .header("Authorization", "Bearer " + otherToken)
        .when()
            .get("/analytics/tests/" + context.getTestSlug() + "/");
        context.setLastResponse(response);
    }

    @Then("access to the analytics is denied")
    public void accessToTheAnalyticsIsDenied() {
        int status = context.getLastResponse().statusCode();
        assertEquals(404, status,
            "Expected 404 Not Found for non-author analytics access (API hides analytics for non-owners) but got " + status);
    }

    // -------------------------------------------------------------------------
    // Average score
    // -------------------------------------------------------------------------

    @Given("one anonymous user submits correctly and another submits with no answers")
    public void oneSubmitsCorrectlyAndAnotherSubmitsNothing() {
        String slug = context.getTestSlug();
        String qId  = String.valueOf(context.getQuestionId());
        int correctId = context.getCorrectAnswerId();

        // Client 1: submits the correct answer
        Response start1 = given()
            .contentType(JSON)
            .body(Map.of("anonymous_name", "AvgTester1"))
        .when()
            .post("/tests/" + slug + "/attempts/");
        start1.then().statusCode(201);
        int attemptId1 = start1.jsonPath().getInt("id");
        Map<String, String> cookies1 = start1.cookies();

        given()
            .contentType(JSON).cookies(cookies1)
            .body(Map.of("draft_answers", Map.of(qId, List.of(correctId))))
        .when()
            .put("/tests/" + slug + "/attempts/" + attemptId1 + "/");
        given()
            .contentType(JSON).cookies(cookies1)
            .body(Map.of("draft_answers", Map.of(qId, List.of(correctId))))
        .when()
            .post("/tests/" + slug + "/attempts/" + attemptId1 + "/submit/")
        .then().statusCode(200);

        // Client 2: submits with no answers → 0%
        Response start2 = given()
            .contentType(JSON)
            .body(Map.of("anonymous_name", "AvgTester2"))
        .when()
            .post("/tests/" + slug + "/attempts/");
        start2.then().statusCode(201);
        int attemptId2 = start2.jsonPath().getInt("id");
        Map<String, String> cookies2 = start2.cookies();

        given()
            .contentType(JSON).cookies(cookies2)
            .body(Map.of("draft_answers", Map.of()))
        .when()
            .post("/tests/" + slug + "/attempts/" + attemptId2 + "/submit/")
        .then().statusCode(200);
    }

    @Then("the average score is approximately 50 percent")
    public void theAverageScoreIsApproximately50Percent() {
        context.getLastResponse().then().statusCode(200);
        Double avgScore = context.getLastResponse().jsonPath().getDouble("average_score");
        assertNotNull(avgScore, "average_score should not be null");
        assertEquals(50.0, avgScore, 0.001,
            "Expected average_score=50.0 (1 correct + 1 zero-score) but was " + avgScore);
    }

    // -------------------------------------------------------------------------
    // Completion rate
    // -------------------------------------------------------------------------

    @Given("one anonymous user abandons the test and another submits")
    public void oneAbandonsAndAnotherSubmits() {
        String slug = context.getTestSlug();

        // Client 1: starts but never submits (abandoned)
        given()
            .contentType(JSON)
            .body(Map.of("anonymous_name", "Abandoner"))
        .when()
            .post("/tests/" + slug + "/attempts/")
        .then().statusCode(201);

        // Client 2: starts and submits
        Response start2 = given()
            .contentType(JSON)
            .body(Map.of("anonymous_name", "Completer"))
        .when()
            .post("/tests/" + slug + "/attempts/");
        start2.then().statusCode(201);
        int attemptId2 = start2.jsonPath().getInt("id");
        Map<String, String> cookies2 = start2.cookies();

        given()
            .contentType(JSON).cookies(cookies2)
            .body(Map.of("draft_answers", Map.of()))
        .when()
            .post("/tests/" + slug + "/attempts/" + attemptId2 + "/submit/")
        .then().statusCode(200);
    }

    @Then("the completion rate is approximately 50 percent")
    public void theCompletionRateIsApproximately50Percent() {
        context.getLastResponse().then().statusCode(200);
        Double completionRate = context.getLastResponse().jsonPath().getDouble("completion_rate");
        assertNotNull(completionRate, "completion_rate should not be null");
        assertEquals(50.0, completionRate, 0.001,
            "Expected completion_rate=50.0 (1 submitted out of 2 started) but was " + completionRate);
    }

    // -------------------------------------------------------------------------
    // Answer distribution
    // -------------------------------------------------------------------------

    @Given("one anonymous user selects the correct answer and another selects the wrong answer")
    public void oneSelectsCorrectAndAnotherSelectsWrong() {
        String slug = context.getTestSlug();
        String qId  = String.valueOf(context.getQuestionId());
        int correctId = context.getCorrectAnswerId();
        int wrongId   = context.getWrongAnswerId();

        // Client 1: correct answer
        Response start1 = given()
            .contentType(JSON)
            .body(Map.of("anonymous_name", "DistTester1"))
        .when()
            .post("/tests/" + slug + "/attempts/");
        start1.then().statusCode(201);
        int id1 = start1.jsonPath().getInt("id");
        Map<String, String> c1 = start1.cookies();

        given()
            .contentType(JSON).cookies(c1)
            .body(Map.of("draft_answers", Map.of(qId, List.of(correctId))))
        .when()
            .put("/tests/" + slug + "/attempts/" + id1 + "/");
        given()
            .contentType(JSON).cookies(c1)
            .body(Map.of("draft_answers", Map.of(qId, List.of(correctId))))
        .when()
            .post("/tests/" + slug + "/attempts/" + id1 + "/submit/")
        .then().statusCode(200);

        // Client 2: wrong answer
        Response start2 = given()
            .contentType(JSON)
            .body(Map.of("anonymous_name", "DistTester2"))
        .when()
            .post("/tests/" + slug + "/attempts/");
        start2.then().statusCode(201);
        int id2 = start2.jsonPath().getInt("id");
        Map<String, String> c2 = start2.cookies();

        given()
            .contentType(JSON).cookies(c2)
            .body(Map.of("draft_answers", Map.of(qId, List.of(wrongId))))
        .when()
            .put("/tests/" + slug + "/attempts/" + id2 + "/");
        given()
            .contentType(JSON).cookies(c2)
            .body(Map.of("draft_answers", Map.of(qId, List.of(wrongId))))
        .when()
            .post("/tests/" + slug + "/attempts/" + id2 + "/submit/")
        .then().statusCode(200);
    }

    @Then("the question stats show 2 answered and 1 correct")
    public void theQuestionStatsShow2Answered1Correct() {
        context.getLastResponse().then().statusCode(200);
        List<Map<String, Object>> qStats = context.getLastResponse().jsonPath().getList("question_stats");
        assertFalse(qStats.isEmpty(), "question_stats should not be empty");
        Map<String, Object> stat = qStats.get(0);
        int totalAnswered = ((Number) stat.get("total_answered")).intValue();
        int correctCount  = ((Number) stat.get("correct_count")).intValue();
        assertEquals(2, totalAnswered,
            "Expected total_answered=2 but was " + totalAnswered);
        assertEquals(1, correctCount,
            "Expected correct_count=1 but was " + correctCount);
    }

    // -------------------------------------------------------------------------
    // Difficulty
    // -------------------------------------------------------------------------

    @Then("the question difficulty is within range 0 to 1")
    public void theQuestionDifficultyIsWithinRange() {
        context.getLastResponse().then().statusCode(200);
        List<Map<String, Object>> qStats = context.getLastResponse().jsonPath().getList("question_stats");
        assertFalse(qStats.isEmpty(), "question_stats should not be empty");
        double difficulty = ((Number) qStats.get(0).get("difficulty")).doubleValue();
        assertEquals(0.0, difficulty, 0.001,
            "Expected difficulty=0.0 after 1/1 correct submission but was " + difficulty);
    }

    // -------------------------------------------------------------------------
    // Pass rate
    // -------------------------------------------------------------------------

    @Then("the pass rate is within valid range")
    public void thePassRateIsWithinValidRange() {
        context.getLastResponse().then().statusCode(200);
        Double passRate = context.getLastResponse().jsonPath().getDouble("pass_rate");
        assertNotNull(passRate, "pass_rate should not be null after a submission");
        assertEquals(100.0, passRate, 0.001,
            "Expected pass_rate=100.0 after a 100% correct submission but was " + passRate);
    }
}
