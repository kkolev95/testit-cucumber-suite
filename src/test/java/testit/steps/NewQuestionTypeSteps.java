package testit.steps;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import testit.context.ScenarioContext;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
}
