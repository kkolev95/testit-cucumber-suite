package testit.hooks;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.restassured.RestAssured;
import testit.context.ScenarioContext;

import static io.restassured.RestAssured.given;

public class Hooks {

    private final ScenarioContext context;

    public Hooks(ScenarioContext context) {
        this.context = context;
    }

    @Before
    public void setUp() {
        // Allow base URL to be overridden via env var (for CI) or system property
        String baseUrl = System.getenv("API_BASE_URL");
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = System.getProperty("api.base.url", "https://exampractices.com/api");
        }
        RestAssured.baseURI = baseUrl;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        context.reset();
    }

    @After
    public void tearDown() {
        // Delete the invitee account first (if an invite scenario created one)
        if (context.getInviteeToken() != null) {
            given()
                .header("Authorization", "Bearer " + context.getInviteeToken())
            .when()
                .delete("/auth/me/");
        }

        // Delete the main test account (cascade-deletes all tests, questions,
        // attempts, companies, folders, and analytics owned by this user)
        if (context.getAccessToken() != null) {
            given()
                .header("Authorization", "Bearer " + context.getAccessToken())
            .when()
                .delete("/auth/me/");
        }
    }
}
