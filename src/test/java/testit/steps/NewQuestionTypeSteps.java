package testit.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import testit.context.ScenarioContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.junit.jupiter.api.Assertions.*;

public class NewQuestionTypeSteps {

    private final ScenarioContext context;

    public NewQuestionTypeSteps(ScenarioContext context) {
        this.context = context;
    }

    // -------------------------------------------------------------------------
    // multi_select — creation validation
    // -------------------------------------------------------------------------

    @When("the author creates a multi_select question with no correct answers")
    public void theAuthorCreatesAMultiSelectQuestionWithNoCorrectAnswers() {
        Response response = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of(
                "question_text", "All wrong question",
                "question_type", "multi_select",
                "answers", List.of(
                    Map.of("answer_text", "Wrong A", "is_correct", false, "order", 1),
                    Map.of("answer_text", "Wrong B", "is_correct", false, "order", 2)
                )
            ))
        .when()
            .post("/tests/" + context.getTestSlug() + "/questions/");
        context.setLastResponse(response);
    }

    // -------------------------------------------------------------------------
    // exact_answer — creation validation
    // -------------------------------------------------------------------------

    @When("the author creates an exact_answer question with correct_answer {string}")
    public void theAuthorCreatesAnExactAnswerQuestionWithCorrectAnswer(String correctAnswer) {
        Response response = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of(
                "question_text", "What is the capital of France?",
                "question_type", "exact_answer",
                "correct_answer", correctAnswer
            ))
        .when()
            .post("/tests/" + context.getTestSlug() + "/questions/");
        context.setLastResponse(response);
    }

    @When("the author creates an exact_answer question with correct_answer of 31 chars")
    public void theAuthorCreatesAnExactAnswerQuestionWithCorrectAnswerOf31Chars() {
        String tooLong = "A".repeat(31);
        Response response = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of(
                "question_text", "Describe in one word",
                "question_type", "exact_answer",
                "correct_answer", tooLong
            ))
        .when()
            .post("/tests/" + context.getTestSlug() + "/questions/");
        context.setLastResponse(response);
    }

    // -------------------------------------------------------------------------
    // exact_answer — take endpoint security
    // -------------------------------------------------------------------------

    @Then("the take endpoint does not reveal the correct answer")
    public void theTakeEndpointDoesNotRevealTheCorrectAnswer() {
        List<Map<String, Object>> questions = context.getLastResponse().jsonPath().getList("questions");
        for (Map<String, Object> question : questions) {
            String qType = (String) question.get("question_type");
            if ("exact_answer".equals(qType)) {
                Object correctAnswer = question.get("correct_answer");
                assertTrue(
                    correctAnswer == null || "".equals(correctAnswer),
                    "correct_answer must be null or empty in the take endpoint to prevent cheating"
                );
            }
        }
    }

    // -------------------------------------------------------------------------
    // multi_select — update correct answers
    // -------------------------------------------------------------------------

    @When("the author updates the multi_select question to make only the first wrong answer correct")
    public void theAuthorUpdatesTheMultiSelectQuestionToMakeOnlyTheFirstWrongAnswerCorrect() {
        // PUT replaces the question entirely — supply new answer text, no IDs
        Response response = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of(
                "question_text", "Which are EU capitals? (updated)",
                "question_type", "multi_select",
                "answers", List.of(
                    Map.of("answer_text", "London", "is_correct", true,  "order", 1),
                    Map.of("answer_text", "Paris",  "is_correct", false, "order", 2),
                    Map.of("answer_text", "Berlin", "is_correct", false, "order", 3)
                )
            ))
        .when()
            .put("/tests/" + context.getTestSlug() + "/questions/" + context.getQuestionId() + "/");
        context.setLastResponse(response);
    }

    @Then("the updated question has exactly {int} correct answer")
    public void theUpdatedQuestionHasExactlyCorrectAnswer(int expectedCount) {
        context.getLastResponse().then().statusCode(200);
        List<Map<String, Object>> answers = context.getLastResponse().jsonPath().getList("answers");
        assertNotNull(answers, "answers must be in the response");
        long correctCount = answers.stream()
            .filter(a -> Boolean.TRUE.equals(a.get("is_correct")))
            .count();
        assertEquals(expectedCount, (int) correctCount,
            "Expected " + expectedCount + " correct answer(s) but got " + correctCount);
    }

    // -------------------------------------------------------------------------
    // exact_answer — update correct answer
    // -------------------------------------------------------------------------

    @When("the author updates the exact answer question to accept {string}")
    public void theAuthorUpdatesTheExactAnswerQuestionToAccept(String newAnswer) {
        Response response = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of(
                "question_text",  "What is the capital of France?",
                "question_type",  "exact_answer",
                "correct_answer", newAnswer
            ))
        .when()
            .put("/tests/" + context.getTestSlug() + "/questions/" + context.getQuestionId() + "/");
        context.setLastResponse(response);
        response.then().statusCode(200);
    }

    // -------------------------------------------------------------------------
    // Mixed test setups (MC + MS, MC + EA)
    // -------------------------------------------------------------------------

    @Given("the author has created a test with one MC and one multi_select question")
    public void theAuthorHasCreatedATestWithOneMCAndOneMultiSelectQuestion() {
        Response testResp = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of(
                "title",              "MixedMCMS_" + UUID.randomUUID().toString().replace("-", "").substring(0, 6),
                "visibility",         "link_only",
                "max_attempts",       5,
                "show_answers_after", false
            ))
        .when()
            .post("/tests/");
        testResp.then().statusCode(201);
        context.setTestSlug(testResp.jsonPath().getString("slug"));

        // MC question: "What is 2+2?" — correct: "4"
        Response mcResp = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of(
                "question_text", "What is 2+2?",
                "question_type", "multiple_choice",
                "answers", List.of(
                    Map.of("answer_text", "4", "is_correct", true,  "order", 1),
                    Map.of("answer_text", "5", "is_correct", false, "order", 2),
                    Map.of("answer_text", "6", "is_correct", false, "order", 3)
                )
            ))
        .when()
            .post("/tests/" + context.getTestSlug() + "/questions/");
        mcResp.then().statusCode(201);
        context.setQuestionId(mcResp.jsonPath().getInt("id"));
        List<Map<String, Object>> mcAnswers = mcResp.jsonPath().getList("answers");
        for (Map<String, Object> answer : mcAnswers) {
            Object isCorrectObj = answer.get("is_correct");
            boolean isCorrect = isCorrectObj != null && (boolean) isCorrectObj;
            int id = (int) answer.get("id");
            if (isCorrect) context.setCorrectAnswerId(id);
            else if (context.getWrongAnswerId() == 0) context.setWrongAnswerId(id);
        }

        // MS question: "Which are primary colours?" — correct: Red, Blue
        Response msResp = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of(
                "question_text", "Which are primary colours?",
                "question_type", "multi_select",
                "answers", List.of(
                    Map.of("answer_text", "Red",    "is_correct", true,  "order", 1),
                    Map.of("answer_text", "Blue",   "is_correct", true,  "order", 2),
                    Map.of("answer_text", "Green",  "is_correct", false, "order", 3),
                    Map.of("answer_text", "Yellow", "is_correct", false, "order", 4)
                )
            ))
        .when()
            .post("/tests/" + context.getTestSlug() + "/questions/");
        msResp.then().statusCode(201);
        context.setQuestionId2(msResp.jsonPath().getInt("id"));
        List<Map<String, Object>> msAnswers = msResp.jsonPath().getList("answers");
        List<Integer> correctIds = new ArrayList<>();
        List<Integer> wrongIds   = new ArrayList<>();
        for (Map<String, Object> answer : msAnswers) {
            Object isCorrectObj = answer.get("is_correct");
            boolean isCorrect = isCorrectObj != null && (boolean) isCorrectObj;
            int id = (int) answer.get("id");
            if (isCorrect) correctIds.add(id);
            else wrongIds.add(id);
        }
        context.setCorrectAnswerIds(correctIds);
        context.setWrongAnswerIds(wrongIds);
    }

    @Given("the author has created a test with one MC and one exact_answer question")
    public void theAuthorHasCreatedATestWithOneMCAndOneExactAnswerQuestion() {
        Response testResp = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of(
                "title",              "MixedMCEA_" + UUID.randomUUID().toString().replace("-", "").substring(0, 6),
                "visibility",         "link_only",
                "max_attempts",       5,
                "show_answers_after", false
            ))
        .when()
            .post("/tests/");
        testResp.then().statusCode(201);
        context.setTestSlug(testResp.jsonPath().getString("slug"));

        // MC question
        Response mcResp = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of(
                "question_text", "What is 2+2?",
                "question_type", "multiple_choice",
                "answers", List.of(
                    Map.of("answer_text", "4", "is_correct", true,  "order", 1),
                    Map.of("answer_text", "5", "is_correct", false, "order", 2)
                )
            ))
        .when()
            .post("/tests/" + context.getTestSlug() + "/questions/");
        mcResp.then().statusCode(201);
        context.setQuestionId(mcResp.jsonPath().getInt("id"));
        List<Map<String, Object>> mcAnswers = mcResp.jsonPath().getList("answers");
        for (Map<String, Object> answer : mcAnswers) {
            Object isCorrectObj = answer.get("is_correct");
            boolean isCorrect = isCorrectObj != null && (boolean) isCorrectObj;
            int id = (int) answer.get("id");
            if (isCorrect) context.setCorrectAnswerId(id);
        }

        // EA question: correct answer "Paris"
        Response eaResp = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of(
                "question_text",  "What is the capital of France?",
                "question_type",  "exact_answer",
                "correct_answer", "Paris"
            ))
        .when()
            .post("/tests/" + context.getTestSlug() + "/questions/");
        eaResp.then().statusCode(201);
        context.setQuestionId2(eaResp.jsonPath().getInt("id"));
    }

    @When("the user submits all correct answers for both questions in the mixed MC+MS test")
    public void theUserSubmitsAllCorrectAnswersForBothQuestionsInTheMixedMCMSTest() {
        String mcQId = String.valueOf(context.getQuestionId());
        String msQId = String.valueOf(context.getQuestionId2());

        Map<String, Object> draftAnswers = new HashMap<>();
        draftAnswers.put(mcQId, List.of(context.getCorrectAnswerId()));
        draftAnswers.put(msQId, context.getCorrectAnswerIds());
        Map<String, Object> draft = Map.of("draft_answers", draftAnswers);

        given()
            .contentType(JSON)
            .cookies(context.getAnonCookies())
            .body(draft)
        .when()
            .put("/tests/" + context.getTestSlug() + "/attempts/" + context.getAttemptId() + "/");

        Response response = given()
            .contentType(JSON)
            .cookies(context.getAnonCookies())
            .body(draft)
        .when()
            .post("/tests/" + context.getTestSlug() + "/attempts/" + context.getAttemptId() + "/submit/");
        response.then().statusCode(200);
        context.setLastResponse(response);
    }

    @When("the user submits all correct answers for both questions in the mixed MC+EA test")
    public void theUserSubmitsAllCorrectAnswersForBothQuestionsInTheMixedMCEATest() {
        String mcQId = String.valueOf(context.getQuestionId());
        String eaQId = String.valueOf(context.getQuestionId2());

        Map<String, Object> draftAnswers = new HashMap<>();
        draftAnswers.put(mcQId, List.of(context.getCorrectAnswerId()));
        draftAnswers.put(eaQId, "Paris");
        Map<String, Object> draft = Map.of("draft_answers", draftAnswers);

        given()
            .contentType(JSON)
            .cookies(context.getAnonCookies())
            .body(draft)
        .when()
            .put("/tests/" + context.getTestSlug() + "/attempts/" + context.getAttemptId() + "/");

        Response response = given()
            .contentType(JSON)
            .cookies(context.getAnonCookies())
            .body(draft)
        .when()
            .post("/tests/" + context.getTestSlug() + "/attempts/" + context.getAttemptId() + "/submit/");
        response.then().statusCode(200);
        context.setLastResponse(response);
    }
}
