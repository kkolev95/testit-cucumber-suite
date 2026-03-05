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
        RestAssured.baseURI = "https://exampractices.com/api";
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        context.reset();
    }

    @After
    public void tearDown() {
        // Delete the test account (cascade-deletes all tests, questions, attempts)
        if (context.getAccessToken() != null) {
            given()
                .header("Authorization", "Bearer " + context.getAccessToken())
            .when()
                .delete("/auth/me/");
        }
    }
}
