package testit.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import testit.context.ScenarioContext;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class StateTransitionSteps {

    private final ScenarioContext context;

    public StateTransitionSteps(ScenarioContext context) {
        this.context = context;
    }

    // -------------------------------------------------------------------------
    // Delete without clearing the stored slug so subsequent steps can use it
    // to verify that the resource is now inaccessible.
    // (The @After hook uses DELETE /auth/me/ to clean up — no individual
    // test deletion is needed.)
    // -------------------------------------------------------------------------

    @Given("the user has deleted the created test")
    public void theUserHasDeletedTheCreatedTest() {
        given()
            .header("Authorization", "Bearer " + context.getAccessToken())
        .when()
            .delete("/tests/" + context.getTestSlug() + "/")
        .then()
            .statusCode(204);
        // Intentionally do NOT clear testSlug — subsequent steps need it to verify 404
    }

    // -------------------------------------------------------------------------
    // Access checks on now-deleted resources
    // -------------------------------------------------------------------------

    @When("the user fetches the now-deleted test")
    public void theUserFetchesTheNowDeletedTest() {
        Response response = given()
            .header("Authorization", "Bearer " + context.getAccessToken())
        .when()
            .get("/tests/" + context.getTestSlug() + "/");
        context.setLastResponse(response);
    }

    @When("the user tries to start an attempt on the deleted test")
    public void theUserTriesToStartAnAttemptOnTheDeletedTest() {
        Response response = given()
            .contentType(JSON)
            .body(Map.of("anonymous_name", "Ghost Taker"))
        .when()
            .post("/tests/" + context.getTestSlug() + "/attempts/");
        context.setLastResponse(response);
    }

    @When("the user requests results of the deleted test")
    public void theUserRequestsResultsOfTheDeletedTest() {
        Response response = given()
            .header("Authorization", "Bearer " + context.getAccessToken())
        .when()
            .get("/tests/" + context.getTestSlug() + "/results/");
        context.setLastResponse(response);
    }

    // -------------------------------------------------------------------------
    // Test list verification after deletion
    // -------------------------------------------------------------------------

    @Then("the deleted test is not in the list")
    public void theDeletedTestIsNotInTheList() {
        List<Map<String, Object>> tests = context.getLastResponse().jsonPath().getList("$");
        String slug = context.getTestSlug();
        assertNotNull(tests, "Test list must not be null — API should return an empty array after deletion");
        assertNotNull(slug, "Test slug must not be null — test setup failed");
        boolean found = tests.stream().anyMatch(t -> slug.equals(t.get("slug")));
        assertFalse(found, "Deleted test with slug '" + slug + "' should not appear in the test list");
    }
}
