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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestManagementSteps {

    private final ScenarioContext context;

    public TestManagementSteps(ScenarioContext context) {
        this.context = context;
    }

    @When("the user creates a test titled {string}")
    public void theUserCreatesATestTitled(String title) {
        // Append a short random suffix so titles are unique across runs
        String uniqueTitle = title + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 6);

        Response response = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of(
                "title", uniqueTitle,
                "visibility", "link_only",
                "max_attempts", 3,
                "show_answers_after", false
            ))
        .when()
            .post("/tests/");

        context.setLastResponse(response);
        if (response.statusCode() == 201) {
            context.setTestSlug(response.jsonPath().getString("slug"));
        }
    }

    @Then("the test slug is present in the response")
    public void theTestSlugIsPresentInTheResponse() {
        context.getLastResponse().then().body("slug", notNullValue());
    }

    @Given("the user has created a test titled {string}")
    public void theUserHasCreatedATestTitled(String title) {
        theUserCreatesATestTitled(title);
        context.getLastResponse().then().statusCode(201);
    }

    @When("the user lists their tests")
    public void theUserListsTheirTests() {
        Response response = given()
            .header("Authorization", "Bearer " + context.getAccessToken())
        .when()
            .get("/tests/");
        context.setLastResponse(response);
    }

    @Then("the list contains {string}")
    public void theListContains(String expectedTitlePrefix) {
        // The response is a direct JSON array
        List<String> titles = context.getLastResponse().jsonPath().getList("title");
        assertTrue(
            titles != null && titles.stream().anyMatch(t -> t != null && t.contains(expectedTitlePrefix)),
            "Expected test list to contain a title with: " + expectedTitlePrefix
        );
    }

    @When("the user updates the test title to {string}")
    public void theUserUpdatesTheTestTitleTo(String newTitle) {
        Response response = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of("title", newTitle))
        .when()
            .patch("/tests/" + context.getTestSlug() + "/");
        context.setLastResponse(response);
    }

    @Then("the test title is {string}")
    public void theTestTitleIs(String expectedTitle) {
        context.getLastResponse().then().body("title", equalTo(expectedTitle));
    }

    @When("the user deletes the test")
    public void theUserDeletesTheTest() {
        Response response = given()
            .header("Authorization", "Bearer " + context.getAccessToken())
        .when()
            .delete("/tests/" + context.getTestSlug() + "/");
        context.setLastResponse(response);
        // Clear slug so tearDown doesn't try to delete it again
        context.setTestSlug(null);
    }
}
