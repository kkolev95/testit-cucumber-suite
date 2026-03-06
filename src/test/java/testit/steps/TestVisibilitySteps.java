package testit.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import testit.context.ScenarioContext;

import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;

public class TestVisibilitySteps {

    private final ScenarioContext context;

    public TestVisibilitySteps(ScenarioContext context) {
        this.context = context;
    }

    @Given("the author has created a test with visibility {string}")
    public void theAuthorHasCreatedATestWithVisibility(String visibility) {
        Map<String, Object> body = visibility.equals("password_protected")
            ? Map.of(
                "title",             "CukeVis_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8),
                "visibility",        visibility,
                "password",          "secret123",
                "max_attempts",      5,
                "show_answers_after", false
              )
            : Map.of(
                "title",             "CukeVis_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8),
                "visibility",        visibility,
                "max_attempts",      5,
                "show_answers_after", false
              );

        Response response = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(body)
        .when()
            .post("/tests/");

        response.then().statusCode(201);
        context.setTestSlug(response.jsonPath().getString("slug"));
    }

    @When("the author changes the test visibility to {string}")
    public void theAuthorChangesTheTestVisibilityTo(String visibility) {
        Response response = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of("visibility", visibility))
        .when()
            .patch("/tests/" + context.getTestSlug() + "/");
        context.setLastResponse(response);
    }

    @Then("an anonymous user can access the test")
    public void anAnonymousUserCanAccessTheTest() {
        given()
        .when()
            .get("/tests/" + context.getTestSlug() + "/take/")
        .then()
            .statusCode(200);
    }
}
