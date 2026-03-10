package testit.steps;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import testit.context.ScenarioContext;

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
}
