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

public class TestTakingSteps {

    private final ScenarioContext context;

    public TestTakingSteps(ScenarioContext context) {
        this.context = context;
    }

    @Given("the author has created a test with a multiple choice question")
    public void theAuthorHasCreatedATestWithAMultipleChoiceQuestion() {
        // Create the test
        Response testResp = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of(
                "title", "CukeTaking_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8),
                "visibility", "link_only",
                "max_attempts", 5,
                "show_answers_after", false
            ))
        .when()
            .post("/tests/");

        testResp.then().statusCode(201);
        context.setTestSlug(testResp.jsonPath().getString("slug"));

        // Add a multiple choice question with one correct answer
        Response qResp = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of(
                "question_text", "What is the capital of France?",
                "question_type", "multiple_choice",
                "answers", List.of(
                    Map.of("answer_text", "Paris",  "is_correct", true,  "order", 1),
                    Map.of("answer_text", "London", "is_correct", false, "order", 2),
                    Map.of("answer_text", "Berlin", "is_correct", false, "order", 3)
                )
            ))
        .when()
            .post("/tests/" + context.getTestSlug() + "/questions/");

        qResp.then().statusCode(201);
        context.setQuestionId(qResp.jsonPath().getInt("id"));

        // Store correct and wrong answer IDs (is_correct may be absent — treat null as false)
        List<Map<String, Object>> answers = qResp.jsonPath().getList("answers");
        for (Map<String, Object> answer : answers) {
            Object isCorrectObj = answer.get("is_correct");
            boolean isCorrect = isCorrectObj != null && (boolean) isCorrectObj;
            int id = (int) answer.get("id");
            if (isCorrect) {
                context.setCorrectAnswerId(id);
            } else if (context.getWrongAnswerId() == 0) {
                context.setWrongAnswerId(id);
            }
        }
    }

    @When("an anonymous user accesses the test")
    public void anAnonymousUserAccessesTheTest() {
        Response response = given()
        .when()
            .get("/tests/" + context.getTestSlug() + "/take/");
        context.setLastResponse(response);
    }

    @Then("the questions are returned without revealing correct answers")
    @SuppressWarnings("unchecked")
    public void theQuestionsAreReturnedWithoutRevealingCorrectAnswers() {
        List<Map<String, Object>> questions = context.getLastResponse().jsonPath().getList("questions");
        assertNotNull(questions, "Questions list should not be null");
        assertFalse(questions.isEmpty(), "Questions list should not be empty");

        for (Map<String, Object> question : questions) {
            List<Map<String, Object>> answers = (List<Map<String, Object>>) question.get("answers");
            if (answers != null) {
                for (Map<String, Object> answer : answers) {
                    Object isCorrectObj = answer.get("is_correct");
                    boolean isCorrect = isCorrectObj != null && (boolean) isCorrectObj;
                    assertFalse(isCorrect,
                        "is_correct must be false on the take endpoint to prevent cheating");
                }
            }
        }
    }

    @When("an anonymous user starts an attempt")
    public void anAnonymousUserStartsAnAttempt() {
        Response response = given()
            .contentType(JSON)
            .body(Map.of("anonymous_name", "Schema Tester"))
        .when()
            .post("/tests/" + context.getTestSlug() + "/attempts/");

        response.then().statusCode(201);
        context.setAttemptId(response.jsonPath().getInt("id"));
        context.setAnonCookies(response.cookies());
        context.setLastResponse(response);
    }

    @Given("an anonymous user has started the test")
    public void anAnonymousUserHasStartedTheTest() {
        Response response = given()
            .contentType(JSON)
            .body(Map.of("anonymous_name", "Cucumber Taker"))
        .when()
            .post("/tests/" + context.getTestSlug() + "/attempts/");

        response.then().statusCode(201);
        context.setAttemptId(response.jsonPath().getInt("id"));
        // Capture session cookies so anonymous ownership is preserved across requests
        context.setAnonCookies(response.cookies());
    }

    @When("the user submits all {string} answers")
    public void theUserSubmitsAllAnswers(String answerType) {
        int answerId = "correct".equals(answerType)
            ? context.getCorrectAnswerId()
            : context.getWrongAnswerId();
        submitAnswers(answerId);
    }

    private void submitAnswers(int answerId) {
        String qId = String.valueOf(context.getQuestionId());
        String slug = context.getTestSlug();
        int attemptId = context.getAttemptId();

        Map<String, Object> draft = Map.of("draft_answers", Map.of(qId, List.of(answerId)));

        // Save draft (with session cookies to prove ownership of the attempt)
        given()
            .contentType(JSON)
            .cookies(context.getAnonCookies())
            .body(draft)
        .when()
            .put("/tests/" + slug + "/attempts/" + attemptId + "/");

        // Submit
        Response response = given()
            .contentType(JSON)
            .cookies(context.getAnonCookies())
            .body(draft)
        .when()
            .post("/tests/" + slug + "/attempts/" + attemptId + "/submit/");

        response.then().statusCode(200);
        context.setLastResponse(response);
    }

    @Then("the score should be {int}")
    public void theScoreShouldBe(int expectedScore) {
        List<Map<String, Object>> results = given()
            .header("Authorization", "Bearer " + context.getAccessToken())
        .when()
            .get("/tests/" + context.getTestSlug() + "/results/")
        .then()
            .statusCode(200)
            .extract().jsonPath().getList("$");

        assertFalse(results.isEmpty(), "Results list should not be empty");
        double actualScore = ((Number) results.get(0).get("score")).doubleValue();
        assertEquals(expectedScore, (int) actualScore,
            "Expected score " + expectedScore + "% but got " + actualScore + "%");
    }

    @Given("an anonymous user has completed the test")
    public void anAnonymousUserHasCompletedTheTest() {
        anAnonymousUserHasStartedTheTest();
        theUserSubmitsAllAnswers("correct");
    }

    @When("the author views the test results")
    public void theAuthorViewsTheTestResults() {
        Response response = given()
            .header("Authorization", "Bearer " + context.getAccessToken())
        .when()
            .get("/tests/" + context.getTestSlug() + "/results/");
        context.setLastResponse(response);
    }

    @Then("the results list is not empty")
    public void theResultsListIsNotEmpty() {
        List<?> results = context.getLastResponse().jsonPath().getList("$");
        assertNotNull(results, "Results should not be null");
        assertFalse(results.isEmpty(), "Results list should not be empty");
    }

    @Given("an anonymous user has submitted the test")
    public void anAnonymousUserHasSubmittedTheTest() {
        anAnonymousUserHasCompletedTheTest();
    }

    @Given("the author has created a test with an exact answer question {string}")
    public void theAuthorHasCreatedATestWithAnExactAnswerQuestion(String correctAnswer) {
        Response testResp = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of(
                "title", "CukeExact_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8),
                "visibility", "link_only",
                "max_attempts", 5,
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
                "question_text", "What is the capital of France?",
                "question_type", "exact_answer",
                "correct_answer", correctAnswer
            ))
        .when()
            .post("/tests/" + context.getTestSlug() + "/questions/");
        qResp.then().statusCode(201);
        context.setQuestionId(qResp.jsonPath().getInt("id"));
    }

    @Given("the author has created a test with a multi select question")
    public void theAuthorHasCreatedATestWithAMultiSelectQuestion() {
        Response testResp = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of(
                "title", "CukeMulti_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8),
                "visibility", "link_only",
                "max_attempts", 5,
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
                "question_text", "Which are EU capitals?",
                "question_type", "multi_select",
                "answers", List.of(
                    Map.of("answer_text", "Paris",  "is_correct", true,  "order", 1),
                    Map.of("answer_text", "Berlin", "is_correct", true,  "order", 2),
                    Map.of("answer_text", "London", "is_correct", false, "order", 3),
                    Map.of("answer_text", "Sydney", "is_correct", false, "order", 4)
                )
            ))
        .when()
            .post("/tests/" + context.getTestSlug() + "/questions/");
        qResp.then().statusCode(201);
        context.setQuestionId(qResp.jsonPath().getInt("id"));

        List<Map<String, Object>> answers = qResp.jsonPath().getList("answers");
        List<Integer> correctIds = new java.util.ArrayList<>();
        List<Integer> wrongIds = new java.util.ArrayList<>();
        for (Map<String, Object> answer : answers) {
            Object isCorrectObj = answer.get("is_correct");
            boolean isCorrect = isCorrectObj != null && (boolean) isCorrectObj;
            int id = (int) answer.get("id");
            if (isCorrect) correctIds.add(id);
            else wrongIds.add(id);
        }
        context.setCorrectAnswerIds(correctIds);
        context.setWrongAnswerIds(wrongIds);
    }

    @When("the user submits the text answer {string}")
    public void theUserSubmitsTheTextAnswer(String textAnswer) {
        String qId = String.valueOf(context.getQuestionId());
        String slug = context.getTestSlug();
        int attemptId = context.getAttemptId();

        Map<String, Object> draft = Map.of("draft_answers", Map.of(qId, textAnswer));

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

    @When("the user submits {string} multi select answers")
    public void theUserSubmitsMultiSelectAnswers(String selection) {
        List<Integer> answerIds = "all correct".equals(selection)
            ? context.getCorrectAnswerIds()
            : context.getWrongAnswerIds();

        String qId = String.valueOf(context.getQuestionId());
        String slug = context.getTestSlug();
        int attemptId = context.getAttemptId();

        Map<String, Object> draft = Map.of("draft_answers", Map.of(qId, answerIds));

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

    @Given("the author has created a password-protected test with password {string}")
    public void theAuthorHasCreatedAPasswordProtectedTestWithPassword(String password) {
        Response testResp = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of(
                "title",             "CukePW_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8),
                "visibility",        "password_protected",
                "password",          password,
                "max_attempts",      5,
                "show_answers_after", false
            ))
        .when()
            .post("/tests/");
        testResp.then().statusCode(201);
        context.setTestSlug(testResp.jsonPath().getString("slug"));
    }

    @Then("the response indicates a password is required")
    public void theResponseIndicatesAPasswordIsRequired() {
        Boolean requiresPassword = context.getLastResponse().jsonPath().getBoolean("requires_password");
        assertNotNull(requiresPassword, "Response should contain requires_password field");
        assertTrue(requiresPassword, "requires_password should be true");
    }

    @When("an anonymous user verifies the password {string}")
    public void anAnonymousUserVerifiesThePassword(String password) {
        Response response = given()
            .contentType(JSON)
            .body(Map.of("password", password))
        .when()
            .post("/tests/" + context.getTestSlug() + "/verify-password/");
        context.setLastResponse(response);
    }

    @When("the user tries to submit the same attempt again")
    public void theUserTriesToSubmitTheSameAttemptAgain() {
        Response response = given()
            .contentType(JSON)
            .cookies(context.getAnonCookies())
            .body(Map.of("draft_answers", Map.of()))
        .when()
            .post("/tests/" + context.getTestSlug() + "/attempts/" + context.getAttemptId() + "/submit/");
        context.setLastResponse(response);
    }
}
