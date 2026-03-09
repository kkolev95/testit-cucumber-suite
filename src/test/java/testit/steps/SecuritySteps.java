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
import static org.junit.jupiter.api.Assertions.*;

public class SecuritySteps {

    private final ScenarioContext context;

    public SecuritySteps(ScenarioContext context) {
        this.context = context;
    }

    // -------------------------------------------------------------------------
    // Setup: register and authenticate a second user (stored as inviteeToken
    // so the @After hook cleans up the account)
    // -------------------------------------------------------------------------

    @Given("another user is registered and authenticated")
    public void anotherUserIsRegisteredAndAuthenticated() {
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
        // Store in inviteeToken so the @After hook deletes this account too
        context.setInviteeToken(loginResp.jsonPath().getString("access"));
    }

    // -------------------------------------------------------------------------
    // Cross-user access
    // -------------------------------------------------------------------------

    @When("the other user requests the test")
    public void theOtherUserRequestsTheTest() {
        Response response = given()
            .header("Authorization", "Bearer " + context.getInviteeToken())
        .when()
            .get("/tests/" + context.getTestSlug() + "/");
        context.setLastResponse(response);
    }

    @When("the other user tries to update the test")
    public void theOtherUserTriesToUpdateTheTest() {
        Response response = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getInviteeToken())
            .body(Map.of("title", "Hacked Title"))
        .when()
            .patch("/tests/" + context.getTestSlug() + "/");
        context.setLastResponse(response);
    }

    @When("the other user tries to delete the test")
    public void theOtherUserTriesToDeleteTheTest() {
        Response response = given()
            .header("Authorization", "Bearer " + context.getInviteeToken())
        .when()
            .delete("/tests/" + context.getTestSlug() + "/");
        context.setLastResponse(response);
    }

    @When("the other user requests the test results")
    public void theOtherUserRequestsTheTestResults() {
        Response response = given()
            .header("Authorization", "Bearer " + context.getInviteeToken())
        .when()
            .get("/tests/" + context.getTestSlug() + "/results/");
        context.setLastResponse(response);
    }

    // -------------------------------------------------------------------------
    // Token validation
    // -------------------------------------------------------------------------

    @When("a request is made with an invalid token")
    public void aRequestIsMadeWithAnInvalidToken() {
        Response response = given()
            .header("Authorization", "Bearer invalid_token_xyz123abc")
        .when()
            .get("/auth/me/");
        context.setLastResponse(response);
    }

    @When("a request is made with a malformed token")
    public void aRequestIsMadeWithAMalformedToken() {
        Response response = given()
            .header("Authorization", "Bearer notavalidjwt")
        .when()
            .get("/auth/me/");
        context.setLastResponse(response);
    }

    // -------------------------------------------------------------------------
    // Payload safety
    // -------------------------------------------------------------------------

    @When("the user creates a test with an oversized title")
    public void theUserCreatesATestWithAnOversizedTitle() {
        String oversizedTitle = "A".repeat(10_000);
        Response response = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of(
                "title",             oversizedTitle,
                "visibility",        "link_only",
                "max_attempts",      3,
                "show_answers_after", false
            ))
        .when()
            .post("/tests/");
        context.setLastResponse(response);
    }

    // -------------------------------------------------------------------------
    // Shared assertions (accessible from all feature files)
    // -------------------------------------------------------------------------

    @Then("access is denied")
    public void accessIsDenied() {
        int status = context.getLastResponse().statusCode();
        assertTrue(status == 401 || status == 403 || status == 404,
            "Expected 401, 403, or 404 but got " + status);
    }

    @Then("the response status should not be {int}")
    public void theResponseStatusShouldNotBe(int unexpectedStatus) {
        int actual = context.getLastResponse().statusCode();
        assertNotEquals(unexpectedStatus, actual,
            "Expected status to not be " + unexpectedStatus + " but it was " + actual);
    }
}
