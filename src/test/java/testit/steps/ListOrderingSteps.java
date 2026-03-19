package testit.steps;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
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

public class ListOrderingSteps {

    private final ScenarioContext context;

    public ListOrderingSteps(ScenarioContext context) {
        this.context = context;
    }

    // -------------------------------------------------------------------------
    // Arrange
    // -------------------------------------------------------------------------

    @Given("the author has created {int} tests in sequence")
    public void theAuthorHasCreatedTestsInSequence(int count) {
        for (int i = 1; i <= count; i++) {
            Response resp = given()
                .contentType(JSON)
                .header("Authorization", "Bearer " + context.getAccessToken())
                .body(Map.of(
                    "title",      "SeqTest_" + i + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 6),
                    "visibility", "link_only"
                ))
            .when()
                .post("/tests/");
            resp.then().statusCode(201);
            String slug = resp.jsonPath().getString("slug");
            context.addCreatedTestSlug(slug);
            context.setTestSlug(slug); // keep testSlug pointing to the most recently created
        }
    }

    @Given("{int} anonymous users have submitted the test in sequence")
    public void anonymousUsersHaveSubmittedTheTestInSequence(int count) {
        for (int i = 1; i <= count; i++) {
            Response startResp = given()
                .contentType(JSON)
                .body(Map.of("anonymous_name", "Submitter " + i))
            .when()
                .post("/tests/" + context.getTestSlug() + "/attempts/");
            startResp.then().statusCode(201);
            int attemptId = startResp.jsonPath().getInt("id");
            Map<String, String> cookies = startResp.cookies();

            given()
                .contentType(JSON)
                .cookies(cookies)
                .body(Map.of())
            .when()
                .post("/tests/" + context.getTestSlug() + "/attempts/" + attemptId + "/submit/")
            .then()
                .statusCode(200);
        }
    }

    // -------------------------------------------------------------------------
    // Act
    // -------------------------------------------------------------------------

    @When("the author lists their tests")
    public void theAuthorListsTheirTests() {
        Response resp = given()
            .header("Authorization", "Bearer " + context.getAccessToken())
        .when()
            .get("/tests/");
        resp.then().statusCode(200);
        context.setLastResponse(resp);
    }

    @When("the author lists their tests with a limit of {int}")
    public void theAuthorListsTheirTestsWithALimitOf(int limit) {
        Response resp = given()
            .header("Authorization", "Bearer " + context.getAccessToken())
            .queryParam("limit", limit)
        .when()
            .get("/tests/");
        resp.then().statusCode(200);
        context.setLastResponse(resp);
    }

    // -------------------------------------------------------------------------
    // Assert
    // -------------------------------------------------------------------------

    @Then("the first test in the list is the most recently created")
    public void theFirstTestInTheListIsTheMostRecentlyCreated() {
        List<Map<String, Object>> tests = context.getLastResponse().jsonPath().getList("$");
        assertFalse(tests.isEmpty(), "Test list must not be empty");

        String firstSlug = (String) tests.get(0).get("slug");
        String lastCreatedSlug = context.getTestSlug(); // setTestSlug is updated on each creation

        assertEquals(lastCreatedSlug, firstSlug,
            "The most recently created test must appear first in the list (newest-first ordering). " +
            "Expected slug '" + lastCreatedSlug + "' at index 0 but got '" + firstSlug + "'");
    }

    @Then("the list contains at least {int} tests")
    public void theListContainsAtLeastTests(int minimum) {
        List<?> tests = context.getLastResponse().jsonPath().getList("$");
        assertTrue(tests.size() >= minimum,
            "Expected at least " + minimum + " tests in the list but got " + tests.size());
    }

    @Then("all {int} created tests appear in the list")
    public void allCreatedTestsAppearInTheList(int count) {
        List<Map<String, Object>> tests = context.getLastResponse().jsonPath().getList("$");
        List<String> returnedSlugs = tests.stream()
            .map(t -> (String) t.get("slug"))
            .toList();

        List<String> createdSlugs = context.getCreatedTestSlugs();
        assertEquals(count, createdSlugs.size(),
            "Expected " + count + " slugs tracked in context but found " + createdSlugs.size());

        for (String slug : createdSlugs) {
            assertTrue(returnedSlugs.contains(slug),
                "Created test '" + slug + "' is missing from the list response");
        }
    }

    @Then("the results are ordered newest submitted first")
    public void theResultsAreOrderedNewestSubmittedFirst() {
        List<Map<String, Object>> results = context.getLastResponse().jsonPath().getList("$");
        assertTrue(results.size() >= 2, "Need at least 2 results to verify ordering");

        // The API orders by submitted_at descending — verify timestamps are non-increasing
        for (int i = 0; i < results.size() - 1; i++) {
            String current = (String) results.get(i).get("submitted_at");
            String next    = (String) results.get(i + 1).get("submitted_at");
            if (current != null && next != null) {
                assertTrue(current.compareTo(next) >= 0,
                    "Results must be ordered newest submitted first. " +
                    "Item " + i + " submitted_at='" + current + "' is older than item " + (i + 1) + " submitted_at='" + next + "'");
            }
        }
    }
}
