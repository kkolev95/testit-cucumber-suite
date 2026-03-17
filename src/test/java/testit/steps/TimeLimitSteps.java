package testit.steps;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.restassured.response.Response;
import testit.context.ScenarioContext;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.junit.jupiter.api.Assertions.*;

public class TimeLimitSteps {

    private final ScenarioContext context;

    public TimeLimitSteps(ScenarioContext context) {
        this.context = context;
    }

    // -------------------------------------------------------------------------
    // Arrange
    // -------------------------------------------------------------------------

    @Given("the author has created a timed test with {int} minutes")
    public void theAuthorHasCreatedATimedTestWithMinutes(int minutes) {
        Response resp = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of(
                "title",              "TimedTest_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8),
                "visibility",         "public",
                "time_limit_minutes", minutes
            ))
        .when()
            .post("/tests/");
        resp.then().statusCode(201);
        context.setTestSlug(resp.jsonPath().getString("slug"));
    }

    @Given("the author has created a timed test with {int} minute and a question")
    public void theAuthorHasCreatedATimedTestWithMinuteAndAQuestion(int minutes) {
        // Create the timed test
        Response testResp = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of(
                "title",              "TimedQ_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8),
                "visibility",         "public",
                "time_limit_minutes", minutes
            ))
        .when()
            .post("/tests/");
        testResp.then().statusCode(201);
        context.setTestSlug(testResp.jsonPath().getString("slug"));

        // Add a multiple choice question so the attempt has content to submit
        Response qResp = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of(
                "question_text", "What is the capital of France?",
                "question_type", "multiple_choice",
                "answers", List.of(
                    Map.of("answer_text", "Paris",  "is_correct", true,  "order", 1),
                    Map.of("answer_text", "London", "is_correct", false, "order", 2)
                )
            ))
        .when()
            .post("/tests/" + context.getTestSlug() + "/questions/");
        qResp.then().statusCode(201);
        context.setQuestionId(qResp.jsonPath().getInt("id"));

        List<Map<String, Object>> answers = qResp.jsonPath().getList("answers");
        for (Map<String, Object> a : answers) {
            boolean correct = Boolean.TRUE.equals(a.get("is_correct"));
            int id = (int) a.get("id");
            if (correct) context.setCorrectAnswerId(id);
            else if (context.getWrongAnswerId() == 0) context.setWrongAnswerId(id);
        }
    }

    @Given("the author has created a test with no time limit")
    public void theAuthorHasCreatedATestWithNoTimeLimit() {
        Response resp = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of(
                "title",      "UntimedTest_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8),
                "visibility", "public"
            ))
        .when()
            .post("/tests/");
        resp.then().statusCode(201);
        context.setTestSlug(resp.jsonPath().getString("slug"));
    }

    @Given("an anonymous user has started the timed test")
    public void anAnonymousUserHasStartedTheTimedTest() {
        Response resp = given()
            .contentType(JSON)
            .body(Map.of("anonymous_name", "Timed Taker"))
        .when()
            .post("/tests/" + context.getTestSlug() + "/attempts/");
        resp.then().statusCode(201);
        context.setAttemptId(resp.jsonPath().getInt("id"));
        context.setAnonCookies(resp.cookies());
    }

    // -------------------------------------------------------------------------
    // Act
    // -------------------------------------------------------------------------

    @When("the time limit elapses")
    public void theTimeLimitElapses() throws InterruptedException {
        // Wait 65 seconds to ensure a 1-minute time limit has fully elapsed
        Thread.sleep(65_000);
    }

    @When("the user submits the timed attempt without answers")
    public void theUserSubmitsTheTimedAttemptWithoutAnswers() {
        Response resp = given()
            .contentType(JSON)
            .cookies(context.getAnonCookies())
            .body(Map.of())
        .when()
            .post("/tests/" + context.getTestSlug() + "/attempts/" + context.getAttemptId() + "/submit/");
        context.setLastResponse(resp);
    }

    @When("the user saves a draft for the timed attempt")
    public void theUserSavesADraftForTheTimedAttempt() {
        // Save a draft with no answers — just verifying the endpoint accepts the request
        Response resp = given()
            .contentType(JSON)
            .cookies(context.getAnonCookies())
            .body(Map.of("draft_answers", Map.of()))
        .when()
            .put("/tests/" + context.getTestSlug() + "/attempts/" + context.getAttemptId() + "/");
        context.setLastResponse(resp);
    }

    // -------------------------------------------------------------------------
    // Assert
    // -------------------------------------------------------------------------

    @Then("the take response includes time_limit_minutes of {int}")
    public void theTakeResponseIncludesTimeLimitMinutesOf(int expected) {
        Integer actual = context.getLastResponse().jsonPath().get("time_limit_minutes");
        assertNotNull(actual,
            "take response must include 'time_limit_minutes' for a timed test");
        assertEquals(expected, (int) actual,
            "time_limit_minutes must match the value set on the test");
    }

    @Then("the take response has null time_limit_minutes")
    public void theTakeResponseHasNullTimeLimitMinutes() {
        Object value = context.getLastResponse().jsonPath().get("time_limit_minutes");
        assertNull(value,
            "time_limit_minutes must be null in the take response when no time limit is configured");
    }

    @Then("the attempt response includes a started_at timestamp")
    public void theAttemptResponseIncludesAStartedAtTimestamp() {
        String startedAt = context.getLastResponse().jsonPath().getString("started_at");
        assertNotNull(startedAt,
            "attempt response must include 'started_at' so the client can compute the countdown");
        assertTrue(startedAt.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.*"),
            "started_at must be a valid ISO 8601 timestamp, got: " + startedAt);
    }

    @Then("the response body contains a submission success message")
    public void theResponseBodyContainsASubmissionSuccessMessage() {
        String message = context.getLastResponse().jsonPath().getString("message");
        assertNotNull(message,
            "submit response must contain a 'message' field");
        assertFalse(message.isBlank(),
            "submit response 'message' must not be blank");
    }
}
