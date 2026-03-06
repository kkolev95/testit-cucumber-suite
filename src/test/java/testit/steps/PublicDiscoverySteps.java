package testit.steps;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import testit.context.ScenarioContext;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

public class PublicDiscoverySteps {

    private final ScenarioContext context;

    public PublicDiscoverySteps(ScenarioContext context) {
        this.context = context;
    }

    @When("an anonymous user requests the public test list")
    public void anAnonymousUserRequestsThePublicTestList() {
        Response response = given()
        .when()
            .get("/tests/public/");
        context.setLastResponse(response);
    }

    @Then("the test appears in the public list")
    public void theTestAppearsInThePublicList() {
        context.getLastResponse().then().statusCode(200);
        List<Map<String, Object>> tests = context.getLastResponse().jsonPath().getList("$");
        assertNotNull(tests, "Public test list should not be null");
        boolean found = tests.stream()
            .anyMatch(t -> context.getTestSlug().equals(t.get("slug")));
        assertTrue(found,
            "Test with slug '" + context.getTestSlug() + "' should appear in the public list");
    }

    @Then("the test does not appear in the public list")
    public void theTestDoesNotAppearInThePublicList() {
        context.getLastResponse().then().statusCode(200);
        List<Map<String, Object>> tests = context.getLastResponse().jsonPath().getList("$");
        assertNotNull(tests, "Public test list should not be null");
        boolean found = tests.stream()
            .anyMatch(t -> context.getTestSlug().equals(t.get("slug")));
        assertFalse(found,
            "Test with slug '" + context.getTestSlug() + "' should NOT appear in the public list");
    }
}
