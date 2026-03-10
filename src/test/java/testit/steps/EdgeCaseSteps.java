package testit.steps;

import io.cucumber.java.en.When;
import io.restassured.response.Response;
import testit.context.ScenarioContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;

public class EdgeCaseSteps {

    private final ScenarioContext context;

    public EdgeCaseSteps(ScenarioContext context) {
        this.context = context;
    }

    @When("the user attempts to create a test where {string} has invalid value {string}")
    public void theUserAttemptsToCreateATestWhereHasInvalidValue(String field, String value) {
        Map<String, Object> body = new HashMap<>(Map.of(
            "title",             "EdgeCase_" + UUID.randomUUID().toString().replace("-", "").substring(0, 6),
            "visibility",        "link_only",
            "max_attempts",      3,
            "show_answers_after", false
        ));

        Object fieldValue;
        if (field.equals("max_attempts")) {
            fieldValue = Integer.parseInt(value);
        } else if (value.equals("(whitespace)")) {
            fieldValue = "   ";
        } else {
            fieldValue = value;
        }
        body.put(field, fieldValue);

        Response response = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(body)
        .when()
            .post("/tests/");
        context.setLastResponse(response);
    }

    @When("the user creates a question with empty text")
    public void theUserCreatesAQuestionWithEmptyText() {
        Response response = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of(
                "question_text", "",
                "question_type", "multiple_choice",
                "answers", List.of(
                    Map.of("answer_text", "Yes", "is_correct", true,  "order", 1),
                    Map.of("answer_text", "No",  "is_correct", false, "order", 2)
                )
            ))
        .when()
            .post("/tests/" + context.getTestSlug() + "/questions/");
        context.setLastResponse(response);
    }

    @When("the user creates a multiple choice question with no answers")
    public void theUserCreatesAMultipleChoiceQuestionWithNoAnswers() {
        Response response = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of(
                "question_text", "No answers question",
                "question_type", "multiple_choice",
                "answers",       List.of()
            ))
        .when()
            .post("/tests/" + context.getTestSlug() + "/questions/");
        context.setLastResponse(response);
    }

    @When("the user creates a multiple choice question with no correct answer")
    public void theUserCreatesAMultipleChoiceQuestionWithNoCorrectAnswer() {
        Response response = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of(
                "question_text", "No correct answer question",
                "question_type", "multiple_choice",
                "answers", List.of(
                    Map.of("answer_text", "Option A", "is_correct", false, "order", 1),
                    Map.of("answer_text", "Option B", "is_correct", false, "order", 2)
                )
            ))
        .when()
            .post("/tests/" + context.getTestSlug() + "/questions/");
        context.setLastResponse(response);
    }

    @When("the user creates a question with an invalid type")
    public void theUserCreatesAQuestionWithAnInvalidType() {
        Response response = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of(
                "question_text", "What type am I?",
                "question_type", "super_invalid_type_xyz"
            ))
        .when()
            .post("/tests/" + context.getTestSlug() + "/questions/");
        context.setLastResponse(response);
    }

    @When("the user requests a test with slug {string}")
    public void theUserRequestsATestWithSlug(String slug) {
        Response response = (context.getAccessToken() != null
            ? given().header("Authorization", "Bearer " + context.getAccessToken())
            : given())
        .when()
            .get("/tests/" + slug + "/");
        context.setLastResponse(response);
    }

    /**
     * Registers a new user with the given email format (a unique suffix is inserted
     * before the @ to avoid conflicts on repeated runs while preserving the format).
     * If registration succeeds, logs in and stores the token in inviteeToken so the
     * @After hook can clean up the account.
     */
    @When("a new user registers with email {string}")
    public void aNewUserRegistersWithEmail(String emailTemplate) {
        String unique = UUID.randomUUID().toString().replace("-", "").substring(0, 6);
        int atIdx = emailTemplate.indexOf('@');
        String email = (atIdx > 0)
            ? emailTemplate.substring(0, atIdx) + "_" + unique + emailTemplate.substring(atIdx)
            : emailTemplate + "_" + unique;

        String password = "Password123!";
        Response response = given()
            .contentType(JSON)
            .body(Map.of(
                "email",            email,
                "password",         password,
                "password_confirm", password,
                "first_name",       "Test",
                "last_name",        "User"
            ))
        .when()
            .post("/auth/register/");
        context.setLastResponse(response);

        // If registration succeeded, log in and store token so @After hook cleans up the account
        if (response.statusCode() == 201) {
            Response loginResp = given()
                .contentType(JSON)
                .body(Map.of("email", email, "password", password))
            .when()
                .post("/auth/login/");
            if (loginResp.statusCode() == 200) {
                context.setInviteeToken(loginResp.jsonPath().getString("access"));
            }
        }
    }

    /**
     * Submits Q2's correct answer as the response to Q1 — the answer ID does not
     * belong to Q1's option set. The API must handle this gracefully (not 500).
     */
    @When("the user submits an answer from the wrong question")
    public void theUserSubmitsAnAnswerFromTheWrongQuestion() {
        String qId = String.valueOf(context.getQuestionId());       // Q1
        int crossAnswerId = context.getCorrectAnswerId2();           // Q2's correct answer

        Map<String, Object> draft = Map.of(
            "draft_answers", Map.of(qId, List.of(crossAnswerId))
        );

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

        context.setLastResponse(response);
    }

    @When("the user submits without answering any questions")
    public void theUserSubmitsWithoutAnsweringAnyQuestions() {
        Response response = given()
            .contentType(JSON)
            .cookies(context.getAnonCookies())
            .body(Map.of("draft_answers", Map.of()))
        .when()
            .post("/tests/" + context.getTestSlug() + "/attempts/" + context.getAttemptId() + "/submit/");
        response.then().statusCode(200);
        context.setLastResponse(response);
    }
}
