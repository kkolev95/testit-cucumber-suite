package testit.steps;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import testit.context.ScenarioContext;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.junit.jupiter.api.Assertions.*;

public class AnalyticsSteps {

    private final ScenarioContext context;

    public AnalyticsSteps(ScenarioContext context) {
        this.context = context;
    }

    @When("the author requests analytics for the test")
    public void theAuthorRequestsAnalyticsForTheTest() {
        Response response = given()
            .header("Authorization", "Bearer " + context.getAccessToken())
        .when()
            .get("/analytics/tests/" + context.getTestSlug() + "/");
        context.setLastResponse(response);
    }

    @Then("the total attempts should be {int}")
    public void theTotalAttemptsShouldBe(int expected) {
        context.getLastResponse().then().statusCode(200);
        int actual = context.getLastResponse().jsonPath().getInt("total_attempts");
        assertEquals(expected, actual,
            "Expected total_attempts=" + expected + " but was " + actual);
    }

    @Then("the analytics include question stats")
    public void theAnalyticsIncludeQuestionStats() {
        List<?> questionStats = context.getLastResponse().jsonPath().getList("question_stats");
        assertNotNull(questionStats, "question_stats should not be null");
        assertFalse(questionStats.isEmpty(),
            "question_stats should not be empty after a submission");
    }

    @When("an unauthenticated user requests analytics for the test")
    public void anUnauthenticatedUserRequestsAnalyticsForTheTest() {
        Response response = given()
        .when()
            .get("/analytics/tests/" + context.getTestSlug() + "/");
        context.setLastResponse(response);
    }

    @When("a different authenticated user requests analytics for the test")
    public void aDifferentAuthenticatedUserRequestsAnalyticsForTheTest() {
        String email = "other_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8) + "@test.com";
        String password = "Password123!";

        given()
            .contentType(JSON)
            .body(Map.of(
                "email",            email,
                "password",         password,
                "password_confirm", password,
                "first_name",       "Other",
                "last_name",        "User"
            ))
        .when()
            .post("/auth/register/")
        .then()
            .statusCode(201);

        Response loginResp = given()
            .contentType(JSON)
            .body(Map.of("email", email, "password", password))
        .when()
            .post("/auth/login/");
        loginResp.then().statusCode(200);
        String otherToken = loginResp.jsonPath().getString("access");

        Response response = given()
            .header("Authorization", "Bearer " + otherToken)
        .when()
            .get("/analytics/tests/" + context.getTestSlug() + "/");
        context.setLastResponse(response);
    }

    @Then("access to the analytics is denied")
    public void accessToTheAnalyticsIsDenied() {
        int status = context.getLastResponse().statusCode();
        assertTrue(status == 403 || status == 404,
            "Expected 403 or 404 but got " + status);
    }
}
