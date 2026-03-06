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

public class SchemaValidationSteps {

    private final ScenarioContext context;

    public SchemaValidationSteps(ScenarioContext context) {
        this.context = context;
    }

    // -------------------------------------------------------------------------
    // Login schema
    // -------------------------------------------------------------------------

    @Then("the response contains a refresh token")
    public void theResponseContainsARefreshToken() {
        String refresh = context.getLastResponse().jsonPath().getString("refresh");
        assertNotNull(refresh, "Login response should contain a 'refresh' field");
        assertFalse(refresh.isBlank(), "Refresh token should not be blank");
    }

    @Then("the response does not contain a password field")
    public void theResponseDoesNotContainAPasswordField() {
        Object password = context.getLastResponse().jsonPath().get("password");
        assertNull(password, "Response must not expose a 'password' field");
    }

    // -------------------------------------------------------------------------
    // Profile schema
    // -------------------------------------------------------------------------

    @Then("the profile response has required fields")
    public void theProfileResponseHasRequiredFields() {
        Response r = context.getLastResponse();
        r.then().statusCode(200);

        assertNotNull(r.jsonPath().get("id"),
            "Profile response must contain 'id'");
        assertNotNull(r.jsonPath().getString("email"),
            "Profile response must contain 'email'");
        assertNotNull(r.jsonPath().getString("first_name"),
            "Profile response must contain 'first_name'");
        assertNotNull(r.jsonPath().getString("last_name"),
            "Profile response must contain 'last_name'");
    }

    @Then("the profile response does not contain a password field")
    public void theProfileResponseDoesNotContainAPasswordField() {
        Object password = context.getLastResponse().jsonPath().get("password");
        assertNull(password, "Profile response must not expose a 'password' field");
    }

    // -------------------------------------------------------------------------
    // Test schema
    // -------------------------------------------------------------------------

    @Then("the test response has required fields")
    public void theTestResponseHasRequiredFields() {
        Response r = context.getLastResponse();
        r.then().statusCode(201);

        assertTrue(r.jsonPath().getInt("id") > 0,
            "Test response must contain a positive 'id'");
        assertNotNull(r.jsonPath().getString("title"),
            "Test response must contain 'title'");
        assertNotNull(r.jsonPath().getString("slug"),
            "Test response must contain 'slug'");
        assertNotNull(r.jsonPath().getString("visibility"),
            "Test response must contain 'visibility'");
    }

    // -------------------------------------------------------------------------
    // Attempt schema
    // -------------------------------------------------------------------------

    @Then("the attempt response has required fields")
    public void theAttemptResponseHasRequiredFields() {
        Response r = context.getLastResponse();
        r.then().statusCode(201);

        assertTrue(r.jsonPath().getInt("id") > 0,
            "Attempt response must contain a positive 'id'");
        assertNotNull(r.jsonPath().getString("started_at"),
            "Attempt response must contain 'started_at'");
    }

    // -------------------------------------------------------------------------
    // Result schema
    // -------------------------------------------------------------------------

    @Then("the result response items have required fields")
    public void theResultResponseItemsHaveRequiredFields() {
        Response r = context.getLastResponse();
        r.then().statusCode(200);

        List<Map<String, Object>> results = r.jsonPath().getList("$");
        assertNotNull(results, "Results list must not be null");
        assertFalse(results.isEmpty(), "Results list must not be empty");

        Map<String, Object> first = results.get(0);
        assertNotNull(first.get("id"),
            "Result item must contain 'id'");
        assertNotNull(first.get("score"),
            "Result item must contain 'score'");
        assertNotNull(first.get("anonymous_name"),
            "Result item must contain 'anonymous_name'");
        assertNotNull(first.get("submitted_at"),
            "Result item must contain 'submitted_at'");
    }

    // -------------------------------------------------------------------------
    // Analytics schema
    // -------------------------------------------------------------------------

    @Then("the analytics response has required fields")
    public void theAnalyticsResponseHasRequiredFields() {
        Response r = context.getLastResponse();
        r.then().statusCode(200);

        assertNotNull(r.jsonPath().get("total_attempts"),
            "Analytics response must contain 'total_attempts'");
        assertNotNull(r.jsonPath().get("question_stats"),
            "Analytics response must contain 'question_stats'");
    }
}
