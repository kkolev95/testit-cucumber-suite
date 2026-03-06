package testit.steps;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import testit.context.ScenarioContext;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProfileSteps {

    private final ScenarioContext context;

    public ProfileSteps(ScenarioContext context) {
        this.context = context;
    }

    @When("the user patches their first name to {string}")
    public void theUserPatchesTheirFirstNameTo(String firstName) {
        Response response = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of("first_name", firstName))
        .when()
            .patch("/auth/me/");
        context.setLastResponse(response);
    }

    @When("the user patches their last name to {string}")
    public void theUserPatchesTheirLastNameTo(String lastName) {
        Response response = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of("last_name", lastName))
        .when()
            .patch("/auth/me/");
        context.setLastResponse(response);
    }

    @When("the user patches their name to first {string} and last {string}")
    public void theUserPatchesTheirNameToFirstAndLast(String firstName, String lastName) {
        Response response = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of("first_name", firstName, "last_name", lastName))
        .when()
            .patch("/auth/me/");
        context.setLastResponse(response);
    }

    @When("an unauthenticated user patches the profile first name to {string}")
    public void anUnauthenticatedUserPatchesTheProfileFirstNameTo(String firstName) {
        Response response = given()
            .contentType(JSON)
            .body(Map.of("first_name", firstName))
        .when()
            .patch("/auth/me/");
        context.setLastResponse(response);
    }

    @Then("the first name in the response is {string}")
    public void theFirstNameInTheResponseIs(String expected) {
        String actual = context.getLastResponse().jsonPath().getString("first_name");
        assertEquals(expected, actual,
            "Expected first_name='" + expected + "' but was '" + actual + "'");
    }

    @Then("the last name in the response is {string}")
    public void theLastNameInTheResponseIs(String expected) {
        String actual = context.getLastResponse().jsonPath().getString("last_name");
        assertEquals(expected, actual,
            "Expected last_name='" + expected + "' but was '" + actual + "'");
    }
}
