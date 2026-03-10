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
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BoundaryValueSteps {

    private final ScenarioContext context;

    public BoundaryValueSteps(ScenarioContext context) {
        this.context = context;
    }

    // -------------------------------------------------------------------------
    // Test creation with specific max_attempts (no MCQ needed, just validates 201)
    // -------------------------------------------------------------------------

    @When("the user creates a test with max_attempts of {int}")
    public void theUserCreatesATestWithMaxAttemptsOf(int maxAttempts) {
        Response response = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of(
                "title",              "BoundTest_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8),
                "visibility",         "link_only",
                "max_attempts",       maxAttempts,
                "show_answers_after", false
            ))
        .when()
            .post("/tests/");
        context.setLastResponse(response);
        if (response.statusCode() == 201) {
            context.setTestSlug(response.jsonPath().getString("slug"));
        }
    }

    // -------------------------------------------------------------------------
    // Author setup: test with MCQ and specified max_attempts
    // -------------------------------------------------------------------------

    @Given("the author has created a test with max_attempts of {int}")
    public void theAuthorHasCreatedATestWithMaxAttemptsOf(int maxAttempts) {
        Response testResp = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of(
                "title",              "BoundAuth_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8),
                "visibility",         "link_only",
                "max_attempts",       maxAttempts,
                "show_answers_after", false
            ))
        .when()
            .post("/tests/");
        testResp.then().statusCode(201);
        context.setTestSlug(testResp.jsonPath().getString("slug"));

        Response qResp = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of(
                "question_text", "What colour is the sky?",
                "question_type", "multiple_choice",
                "answers", List.of(
                    Map.of("answer_text", "Blue",  "is_correct", true,  "order", 1),
                    Map.of("answer_text", "Green", "is_correct", false, "order", 2)
                )
            ))
        .when()
            .post("/tests/" + context.getTestSlug() + "/questions/");
        qResp.then().statusCode(201);
        context.setQuestionId(qResp.jsonPath().getInt("id"));

        List<Map<String, Object>> answers = qResp.jsonPath().getList("answers");
        for (Map<String, Object> answer : answers) {
            Object isCorrectObj = answer.get("is_correct");
            boolean isCorrect = isCorrectObj != null && (boolean) isCorrectObj;
            int id = (int) answer.get("id");
            if (isCorrect) context.setCorrectAnswerId(id);
            else if (context.getWrongAnswerId() == 0) context.setWrongAnswerId(id);
        }
    }

    // -------------------------------------------------------------------------
    // Second attempt from the same anonymous session
    // -------------------------------------------------------------------------

    @When("the same anonymous user tries to start a new attempt")
    public void theSameAnonymousUserTriesToStartANewAttempt() {
        Response response = given()
            .contentType(JSON)
            .cookies(context.getAnonCookies())
            .body(Map.of("anonymous_name", "Same Taker"))
        .when()
            .post("/tests/" + context.getTestSlug() + "/attempts/");
        context.setLastResponse(response);
    }

    @Then("starting the attempt is rejected")
    public void startingTheAttemptIsRejected() {
        int status = context.getLastResponse().statusCode();
        assertTrue(status == 400 || status == 403 || status == 429,
            "Expected 400, 403, or 429 to indicate attempt limit reached, but got " + status);
    }
}
