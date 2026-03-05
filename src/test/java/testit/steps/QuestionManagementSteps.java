package testit.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import testit.context.ScenarioContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.junit.jupiter.api.Assertions.*;

public class QuestionManagementSteps {

    private final ScenarioContext context;

    public QuestionManagementSteps(ScenarioContext context) {
        this.context = context;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Response addMultipleChoiceQuestion(String questionText) {
        return given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of(
                "question_text", questionText,
                "question_type", "multiple_choice",
                "answers", List.of(
                    Map.of("answer_text", "Paris",  "is_correct", true,  "order", 1),
                    Map.of("answer_text", "London", "is_correct", false, "order", 2),
                    Map.of("answer_text", "Berlin", "is_correct", false, "order", 3)
                )
            ))
        .when()
            .post("/tests/" + context.getTestSlug() + "/questions/");
    }

    // -------------------------------------------------------------------------
    // Step definitions
    // -------------------------------------------------------------------------

    @When("the author adds a multiple choice question")
    public void theAuthorAddsAMultipleChoiceQuestion() {
        Response response = addMultipleChoiceQuestion("What is the capital of France?");
        context.setLastResponse(response);
        if (response.statusCode() == 201) {
            context.setQuestionId(response.jsonPath().getInt("id"));
        }
    }

    @When("the author adds an exact answer question with correct answer {string}")
    public void theAuthorAddsAnExactAnswerQuestionWithCorrectAnswer(String correctAnswer) {
        Response response = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of(
                "question_text", "Name the capital of France.",
                "question_type", "exact_answer",
                "correct_answer", correctAnswer
            ))
        .when()
            .post("/tests/" + context.getTestSlug() + "/questions/");

        context.setLastResponse(response);
        if (response.statusCode() == 201) {
            context.setQuestionId(response.jsonPath().getInt("id"));
        }
    }

    @When("the author adds a multi select question")
    public void theAuthorAddsAMultiSelectQuestion() {
        Response response = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of(
                "question_text", "Which are EU capitals?",
                "question_type", "multi_select",
                "answers", List.of(
                    Map.of("answer_text", "Paris",   "is_correct", true,  "order", 1),
                    Map.of("answer_text", "Berlin",  "is_correct", true,  "order", 2),
                    Map.of("answer_text", "London",  "is_correct", false, "order", 3),
                    Map.of("answer_text", "Canberra","is_correct", false, "order", 4)
                )
            ))
        .when()
            .post("/tests/" + context.getTestSlug() + "/questions/");

        context.setLastResponse(response);
        if (response.statusCode() == 201) {
            context.setQuestionId(response.jsonPath().getInt("id"));
        }
    }

    @Then("the question id is present in the response")
    public void theQuestionIdIsPresentInTheResponse() {
        Integer id = context.getLastResponse().jsonPath().getInt("id");
        assertNotNull(id, "Question id should be present in the response");
        assertTrue(id > 0, "Question id should be a positive integer");
    }

    @Given("the author has added a multiple choice question")
    public void theAuthorHasAddedAMultipleChoiceQuestion() {
        theAuthorAddsAMultipleChoiceQuestion();
        context.getLastResponse().then().statusCode(201);
    }

    @When("the author updates the question text to {string}")
    public void theAuthorUpdatesTheQuestionTextTo(String newText) {
        Response response = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of(
                "question_text", newText,
                "question_type", "multiple_choice",
                "answers", List.of(
                    Map.of("answer_text", "Paris",  "is_correct", true,  "order", 1),
                    Map.of("answer_text", "London", "is_correct", false, "order", 2)
                )
            ))
        .when()
            .put("/tests/" + context.getTestSlug() + "/questions/" + context.getQuestionId() + "/");

        context.setLastResponse(response);
    }

    @Then("the question text is {string}")
    public void theQuestionTextIs(String expectedText) {
        context.getLastResponse().then().statusCode(200);
        String actual = context.getLastResponse().jsonPath().getString("question_text");
        assertEquals(expectedText, actual, "Question text should match after update");
    }

    @When("the author deletes the question")
    public void theAuthorDeletesTheQuestion() {
        Response response = given()
            .header("Authorization", "Bearer " + context.getAccessToken())
        .when()
            .delete("/tests/" + context.getTestSlug() + "/questions/" + context.getQuestionId() + "/");

        context.setLastResponse(response);
    }

    @Then("the question no longer exists in the test")
    @SuppressWarnings("unchecked")
    public void theQuestionNoLongerExistsInTheTest() {
        int deletedId = context.getQuestionId();

        List<Map<String, Object>> questions = given()
            .header("Authorization", "Bearer " + context.getAccessToken())
        .when()
            .get("/tests/" + context.getTestSlug() + "/")
        .then()
            .statusCode(200)
            .extract().jsonPath().getList("questions");

        if (questions != null) {
            boolean stillPresent = questions.stream()
                .anyMatch(q -> ((Number) q.get("id")).intValue() == deletedId);
            assertFalse(stillPresent, "Deleted question id=" + deletedId + " should not appear in the test");
        }
    }

    @Given("the author has added 3 questions to the test")
    public void theAuthorHasAdded3QuestionsToTheTest() {
        List<Integer> ids = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            Response response = addMultipleChoiceQuestion("Question number " + i);
            response.then().statusCode(201);
            ids.add(response.jsonPath().getInt("id"));
        }
        context.setQuestionIds(ids);
    }

    @When("the author reorders the questions in reverse")
    public void theAuthorReordersTheQuestionsInReverse() {
        List<Integer> ids = context.getQuestionIds();
        List<Map<String, Object>> reverseOrder = new ArrayList<>();
        for (int i = 0; i < ids.size(); i++) {
            reverseOrder.add(Map.of("id", ids.get(ids.size() - 1 - i), "order", i));
        }

        Response response = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of("order", reverseOrder))
        .when()
            .post("/tests/" + context.getTestSlug() + "/questions/reorder/");

        context.setLastResponse(response);
        response.then().statusCode(200);
    }

    @Then("the questions appear in reverse order")
    @SuppressWarnings("unchecked")
    public void theQuestionsAppearInReverseOrder() {
        List<Map<String, Object>> questions = given()
            .header("Authorization", "Bearer " + context.getAccessToken())
        .when()
            .get("/tests/" + context.getTestSlug() + "/")
        .then()
            .statusCode(200)
            .extract().jsonPath().getList("questions");

        assertNotNull(questions, "Questions list should not be null");

        List<Integer> actualIds = questions.stream()
            .sorted(Comparator.comparingInt(q -> ((Number) q.get("order")).intValue()))
            .map(q -> ((Number) q.get("id")).intValue())
            .collect(Collectors.toList());

        List<Integer> expectedIds = new ArrayList<>(context.getQuestionIds());
        Collections.reverse(expectedIds);

        assertEquals(expectedIds, actualIds, "Questions should appear in reverse order after reorder");
    }

    @When("the author updates a non-existent question")
    public void theAuthorUpdatesANonExistentQuestion() {
        Response response = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of(
                "question_text", "Ghost question",
                "question_type", "multiple_choice",
                "answers", List.of(
                    Map.of("answer_text", "A", "is_correct", true, "order", 1)
                )
            ))
        .when()
            .put("/tests/" + context.getTestSlug() + "/questions/99999/");

        context.setLastResponse(response);
    }
}
