package testit.steps;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import testit.context.ScenarioContext;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

public class ResultDetailSteps {

    private final ScenarioContext context;

    public ResultDetailSteps(ScenarioContext context) {
        this.context = context;
    }

    @When("the author fetches the first result's detail")
    public void theAuthorFetchesTheFirstResultsDetail() {
        List<Map<String, Object>> results = given()
            .header("Authorization", "Bearer " + context.getAccessToken())
        .when()
            .get("/tests/" + context.getTestSlug() + "/results/")
        .then()
            .statusCode(200)
            .extract().jsonPath().getList("$");
        assertFalse(results.isEmpty(), "Results list should not be empty");

        int resultId = ((Number) results.get(0).get("id")).intValue();
        context.setResultId(resultId);

        Response response = given()
            .header("Authorization", "Bearer " + context.getAccessToken())
        .when()
            .get("/tests/" + context.getTestSlug() + "/results/" + resultId + "/");
        context.setLastResponse(response);
    }

    @When("the other user requests the result detail")
    public void theOtherUserRequestsTheResultDetail() {
        // Fetch result ID using the author's token (accessToken is still the author's)
        List<Map<String, Object>> results = given()
            .header("Authorization", "Bearer " + context.getAccessToken())
        .when()
            .get("/tests/" + context.getTestSlug() + "/results/")
        .then()
            .statusCode(200)
            .extract().jsonPath().getList("$");
        assertFalse(results.isEmpty(), "Results list should not be empty");

        int resultId = ((Number) results.get(0).get("id")).intValue();
        context.setResultId(resultId);

        // Attempt to access the detail as the other user
        Response response = given()
            .header("Authorization", "Bearer " + context.getInviteeToken())
        .when()
            .get("/tests/" + context.getTestSlug() + "/results/" + resultId + "/");
        context.setLastResponse(response);
    }

    @Then("the result detail has required fields")
    public void theResultDetailHasRequiredFields() {
        Response r = context.getLastResponse();
        r.then().statusCode(200);

        assertNotNull(r.jsonPath().get("id"),
            "Result detail must contain 'id'");
        assertNotNull(r.jsonPath().get("score"),
            "Result detail must contain 'score'");
        assertNotNull(r.jsonPath().getString("anonymous_name"),
            "Result detail must contain 'anonymous_name'");
        assertNotNull(r.jsonPath().getString("submitted_at"),
            "Result detail must contain 'submitted_at'");
    }
}
