package testit.steps;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import testit.context.ScenarioContext;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.junit.jupiter.api.Assertions.*;

public class PatchSemanticsSteps {

    private final ScenarioContext context;

    public PatchSemanticsSteps(ScenarioContext context) {
        this.context = context;
    }

    // -------------------------------------------------------------------------
    // Partial PATCH actions
    // -------------------------------------------------------------------------

    @When("the user patches only the title to {string}")
    public void theUserPatchesOnlyTheTitleTo(String newTitle) {
        Response response = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of("title", newTitle))
        .when()
            .patch("/tests/" + context.getTestSlug() + "/");
        context.setLastResponse(response);
    }

    @When("the user patches only the visibility to {string}")
    public void theUserPatchesOnlyTheVisibilityTo(String visibility) {
        Response response = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of("visibility", visibility))
        .when()
            .patch("/tests/" + context.getTestSlug() + "/");
        context.setLastResponse(response);
    }

    @When("the user sends an empty PATCH to the test")
    public void theUserSendsAnEmptyPatchToTheTest() {
        Response response = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of())
        .when()
            .patch("/tests/" + context.getTestSlug() + "/");
        context.setLastResponse(response);
    }

    // -------------------------------------------------------------------------
    // Field isolation assertions
    // -------------------------------------------------------------------------

    @Then("the test visibility is still {string}")
    public void theTestVisibilityIsStill(String expectedVisibility) {
        String visibility = context.getLastResponse().jsonPath().getString("visibility");
        assertEquals(expectedVisibility, visibility,
            "Expected visibility to remain '" + expectedVisibility + "' after PATCH, but got '" + visibility + "'");
    }

    @Then("the test title still contains {string}")
    public void theTestTitleStillContains(String substring) {
        String title = context.getLastResponse().jsonPath().getString("title");
        assertNotNull(title, "title field must not be null after PATCH");
        assertTrue(title.contains(substring),
            "Expected title to contain '" + substring + "' after PATCH, but was '" + title + "'");
    }

    // -------------------------------------------------------------------------
    // New gap-coverage PATCH steps
    // -------------------------------------------------------------------------

    @When("an unauthenticated user patches the test title to {string}")
    public void anUnauthenticatedUserPatchesTheTestTitleTo(String newTitle) {
        Response response = given()
            .contentType(JSON)
            .body(Map.of("title", newTitle))
        .when()
            .patch("/tests/" + context.getTestSlug() + "/");
        context.setLastResponse(response);
    }

    @When("the other user patches the test title to {string}")
    public void theOtherUserPatchesTheTestTitleTo(String newTitle) {
        Response response = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getInviteeToken())
            .body(Map.of("title", newTitle))
        .when()
            .patch("/tests/" + context.getTestSlug() + "/");
        context.setLastResponse(response);
    }

    @When("the user patches the question text to {string}")
    public void theUserPatchesTheQuestionTextTo(String newText) {
        Response response = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of("question_text", newText))
        .when()
            .patch("/tests/" + context.getTestSlug() + "/questions/" + context.getQuestionId() + "/");
        context.setLastResponse(response);
    }

    @Then("the question answer count is {int}")
    public void theQuestionAnswerCountIs(int expectedCount) {
        List<?> answers = context.getLastResponse().jsonPath().getList("answers");
        assertNotNull(answers, "answers must be present in the PATCH response");
        assertEquals(expectedCount, answers.size(),
            "Expected " + expectedCount + " answers but got " + answers.size());
    }

    @When("the other user patches the question text")
    public void theOtherUserPatchesTheQuestionText() {
        Response response = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getInviteeToken())
            .body(Map.of("question_text", "Hacked question text"))
        .when()
            .patch("/tests/" + context.getTestSlug() + "/questions/" + context.getQuestionId() + "/");
        context.setLastResponse(response);
    }

    @When("the user patches the company name to {string}")
    public void theUserPatchesTheCompanyNameTo(String newName) {
        Response response = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of("name", newName))
        .when()
            .patch("/companies/" + context.getCompanyId() + "/");
        context.setLastResponse(response);
        if (response.statusCode() == 200) {
            context.setCompanyName(newName);
        }
    }

    @When("the other user tries to patch the company name")
    public void theOtherUserTriesToPatchTheCompanyName() {
        Response response = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getInviteeToken())
            .body(Map.of("name", "Hacked Corp"))
        .when()
            .patch("/companies/" + context.getCompanyId() + "/");
        context.setLastResponse(response);
    }

    @When("the user patches the folder name to {string}")
    public void theUserPatchesTheFolderNameTo(String newName) {
        Response response = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of("name", newName))
        .when()
            .patch("/companies/" + context.getCompanyId() + "/folders/" + context.getFolderId() + "/");
        context.setLastResponse(response);
    }

    @When("the user patches {string} to be a child of {string}")
    public void theUserPatchesToBeAChildOf(String childName, String parentName) {
        int childId  = context.getFolderIdByName().get(childName);
        int parentId = context.getFolderIdByName().get(parentName);
        Response response = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of("parent", parentId))
        .when()
            .patch("/companies/" + context.getCompanyId() + "/folders/" + childId + "/");
        context.setLastResponse(response);
    }

    @Then("the folder parent field is the id of {string}")
    public void theFolderParentFieldIsTheIdOf(String parentName) {
        int expectedParentId = context.getFolderIdByName().get(parentName);
        Integer actualParentId = context.getLastResponse().jsonPath().getInt("parent");
        assertEquals(expectedParentId, actualParentId,
            "Expected parent=" + expectedParentId + " but got " + actualParentId);
    }
}
