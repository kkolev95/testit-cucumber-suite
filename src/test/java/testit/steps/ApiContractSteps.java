package testit.steps;

import io.cucumber.java.en.Then;
import testit.context.ScenarioContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ApiContractSteps {

    private final ScenarioContext context;

    public ApiContractSteps(ScenarioContext context) {
        this.context = context;
    }

    // -------------------------------------------------------------------------
    // Empty collection
    // -------------------------------------------------------------------------

    @Then("the response is an empty JSON array")
    public void theResponseIsAnEmptyJsonArray() {
        context.getLastResponse().then().statusCode(200);
        List<?> list = context.getLastResponse().jsonPath().getList("$");
        assertNotNull(list, "Response body must be a JSON array, not null");
        assertTrue(list.isEmpty(),
            "Expected an empty array but found " + list.size() + " item(s)");
    }

    // -------------------------------------------------------------------------
    // List count consistency
    // -------------------------------------------------------------------------

    @Then("the list contains exactly {int} items")
    public void theListContainsExactlyItems(int expected) {
        context.getLastResponse().then().statusCode(200);
        List<?> list = context.getLastResponse().jsonPath().getList("$");
        assertNotNull(list, "Response body must be a JSON array");
        assertEquals(expected, list.size(),
            "Expected exactly " + expected + " item(s) in the list but found " + list.size());
    }

    // -------------------------------------------------------------------------
    // Content-Type header
    // -------------------------------------------------------------------------

    @Then("the response Content-Type is application\\/json")
    public void theResponseContentTypeIsApplicationJson() {
        String contentType = context.getLastResponse().getContentType();
        assertNotNull(contentType, "Content-Type header must be present");
        assertTrue(contentType.contains("application/json"),
            "Expected Content-Type application/json but got: " + contentType);
    }
}
