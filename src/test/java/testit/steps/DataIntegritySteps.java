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

public class DataIntegritySteps {

    private final ScenarioContext context;

    public DataIntegritySteps(ScenarioContext context) {
        this.context = context;
    }

    // -------------------------------------------------------------------------
    // Setup: create a test with N multiple choice questions
    // -------------------------------------------------------------------------

    @Given("the author has created a test with {int} multiple choice questions")
    public void theAuthorHasCreatedATestWithMultipleChoiceQuestions(int count) {
        // Create the test
        Response testResp = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of(
                "title",             "CukeMultiQ_" + UUID.randomUUID().toString().replace("-", "").substring(0, 6),
                "visibility",        "link_only",
                "max_attempts",      10,
                "show_answers_after", false
            ))
        .when()
            .post("/tests/");
        testResp.then().statusCode(201);
        context.setTestSlug(testResp.jsonPath().getString("slug"));

        // Add first question
        Response q1Resp = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of(
                "question_text", "Question 1: What is 1 + 1?",
                "question_type", "multiple_choice",
                "answers", List.of(
                    Map.of("answer_text", "2",    "is_correct", true,  "order", 1),
                    Map.of("answer_text", "3",    "is_correct", false, "order", 2),
                    Map.of("answer_text", "4",    "is_correct", false, "order", 3)
                )
            ))
        .when()
            .post("/tests/" + context.getTestSlug() + "/questions/");
        q1Resp.then().statusCode(201);
        context.setQuestionId(q1Resp.jsonPath().getInt("id"));

        List<Map<String, Object>> q1Answers = q1Resp.jsonPath().getList("answers");
        for (Map<String, Object> answer : q1Answers) {
            Object isCorrectObj = answer.get("is_correct");
            boolean isCorrect = isCorrectObj != null && (boolean) isCorrectObj;
            int id = (int) answer.get("id");
            if (isCorrect) {
                context.setCorrectAnswerId(id);
            } else if (context.getWrongAnswerId() == 0) {
                context.setWrongAnswerId(id);
            }
        }

        // Add additional questions if count > 1
        if (count > 1) {
            Response q2Resp = given()
                .contentType(JSON)
                .header("Authorization", "Bearer " + context.getAccessToken())
                .body(Map.of(
                    "question_text", "Question 2: What is 2 + 2?",
                    "question_type", "multiple_choice",
                    "answers", List.of(
                        Map.of("answer_text", "4",    "is_correct", true,  "order", 1),
                        Map.of("answer_text", "3",    "is_correct", false, "order", 2),
                        Map.of("answer_text", "5",    "is_correct", false, "order", 3)
                    )
                ))
            .when()
                .post("/tests/" + context.getTestSlug() + "/questions/");
            q2Resp.then().statusCode(201);
            context.setQuestionId2(q2Resp.jsonPath().getInt("id"));

            List<Map<String, Object>> q2Answers = q2Resp.jsonPath().getList("answers");
            for (Map<String, Object> answer : q2Answers) {
                Object isCorrectObj = answer.get("is_correct");
                boolean isCorrect = isCorrectObj != null && (boolean) isCorrectObj;
                int id = (int) answer.get("id");
                if (isCorrect) {
                    context.setCorrectAnswerId2(id);
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Submit only the first question correctly (for 50% score test)
    // -------------------------------------------------------------------------

    @When("the user answers only the first question correctly and submits")
    public void theUserAnswersOnlyTheFirstQuestionCorrectlyAndSubmits() {
        String qId = String.valueOf(context.getQuestionId());
        String slug = context.getTestSlug();
        int attemptId = context.getAttemptId();

        // Only answer Q1 correctly; Q2 is left unanswered
        Map<String, Object> draft = Map.of(
            "draft_answers", Map.of(qId, List.of(context.getCorrectAnswerId()))
        );

        given()
            .contentType(JSON)
            .cookies(context.getAnonCookies())
            .body(draft)
        .when()
            .put("/tests/" + slug + "/attempts/" + attemptId + "/");

        Response response = given()
            .contentType(JSON)
            .cookies(context.getAnonCookies())
            .body(draft)
        .when()
            .post("/tests/" + slug + "/attempts/" + attemptId + "/submit/");

        response.then().statusCode(200);
        context.setLastResponse(response);
    }

    // -------------------------------------------------------------------------
    // Score consistency
    // -------------------------------------------------------------------------

    @Then("the score is consistent across {int} fetches")
    public void theScoreIsConsistentAcrossNFetches(int times) {
        Double firstScore = null;
        for (int i = 0; i < times; i++) {
            List<Map<String, Object>> results = given()
                .header("Authorization", "Bearer " + context.getAccessToken())
            .when()
                .get("/tests/" + context.getTestSlug() + "/results/")
            .then()
                .statusCode(200)
                .extract().jsonPath().getList("$");
            assertFalse(results.isEmpty(), "Results should not be empty on fetch " + (i + 1));
            double score = ((Number) results.get(0).get("score")).doubleValue();
            if (firstScore == null) {
                firstScore = score;
            } else {
                assertEquals(firstScore, score, 0.001,
                    "Score changed between fetches: was " + firstScore + " then " + score);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Question update immutability
    // (The "When the author updates the question text to {string}" step is
    //  defined in QuestionManagementSteps and reused here.)
    // -------------------------------------------------------------------------

    @Then("the previously submitted score is unchanged")
    public void thePreviouslySubmittedScoreIsUnchanged() {
        List<Map<String, Object>> results = given()
            .header("Authorization", "Bearer " + context.getAccessToken())
        .when()
            .get("/tests/" + context.getTestSlug() + "/results/")
        .then()
            .statusCode(200)
            .extract().jsonPath().getList("$");
        assertFalse(results.isEmpty(), "Results should not be empty");
        double score = ((Number) results.get(0).get("score")).doubleValue();
        assertEquals(100.0, score, 0.01,
            "Score should still be 100 after updating question text, but was " + score);
    }

    // -------------------------------------------------------------------------
    // Multiple submissions counted
    // -------------------------------------------------------------------------

    @Given("{int} anonymous users have submitted the test")
    public void anonymousUsersHaveSubmittedTheTest(int count) {
        String slug = context.getTestSlug();
        String qId = String.valueOf(context.getQuestionId());

        for (int i = 0; i < count; i++) {
            // Start a new attempt
            Response attemptResp = given()
                .contentType(JSON)
                .body(Map.of("anonymous_name", "Taker_" + (i + 1)))
            .when()
                .post("/tests/" + slug + "/attempts/");
            attemptResp.then().statusCode(201);
            int attemptId = attemptResp.jsonPath().getInt("id");
            Map<String, String> cookies = attemptResp.cookies();

            Map<String, Object> draft = Map.of(
                "draft_answers", Map.of(qId, List.of(context.getCorrectAnswerId()))
            );

            // Save draft
            given()
                .contentType(JSON)
                .cookies(cookies)
                .body(draft)
            .when()
                .put("/tests/" + slug + "/attempts/" + attemptId + "/");

            // Submit
            given()
                .contentType(JSON)
                .cookies(cookies)
                .body(draft)
            .when()
                .post("/tests/" + slug + "/attempts/" + attemptId + "/submit/")
            .then()
                .statusCode(200);
        }
    }

    @Then("the results list has {int} entries")
    public void theResultsListHasEntries(int expectedCount) {
        List<?> results = context.getLastResponse().jsonPath().getList("$");
        assertNotNull(results, "Results list should not be null");
        assertEquals(expectedCount, results.size(),
            "Expected " + expectedCount + " results but found " + results.size());
    }

    // -------------------------------------------------------------------------
    // Submitted attempt immutability
    // -------------------------------------------------------------------------

    @When("the user tries to update the submitted draft")
    public void theUserTriesToUpdateTheSubmittedDraft() {
        String qId = String.valueOf(context.getQuestionId());
        Map<String, Object> draft = Map.of(
            "draft_answers", Map.of(qId, List.of(context.getCorrectAnswerId()))
        );
        Response response = given()
            .contentType(JSON)
            .cookies(context.getAnonCookies())
            .body(draft)
        .when()
            .put("/tests/" + context.getTestSlug() + "/attempts/" + context.getAttemptId() + "/");
        context.setLastResponse(response);
    }
}
