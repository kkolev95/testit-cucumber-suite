package testit.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import testit.context.ScenarioContext;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.junit.jupiter.api.Assertions.*;

public class CompanySteps {

    private final ScenarioContext context;

    public CompanySteps(ScenarioContext context) {
        this.context = context;
    }

    @When("the user creates a company named {string}")
    public void theUserCreatesACompanyNamed(String name) {
        Response response = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of("name", name))
        .when()
            .post("/companies/");
        context.setLastResponse(response);
        if (response.statusCode() == 201) {
            context.setCompanyId(response.jsonPath().getInt("id"));
            context.setCompanyName(name);
        }
    }

    @Given("the user has created a company named {string}")
    public void theUserHasCreatedACompanyNamed(String name) {
        theUserCreatesACompanyNamed(name);
        context.getLastResponse().then().statusCode(201);
    }

    @Then("the company is returned with a positive id")
    public void theCompanyIsReturnedWithAPositiveId() {
        int id = context.getLastResponse().jsonPath().getInt("id");
        assertTrue(id > 0, "Company id should be positive but was " + id);
    }

    @Then("the company appears in the user's company list")
    public void theCompanyAppearsInTheUserSCompanyList() {
        List<Map<String, Object>> companies = given()
            .header("Authorization", "Bearer " + context.getAccessToken())
        .when()
            .get("/companies/")
        .then()
            .statusCode(200)
            .extract().jsonPath().getList("$");
        assertNotNull(companies, "Companies list should not be null");
        assertFalse(companies.isEmpty(),
            "Companies list should not be empty after creating a company");
    }

    @When("the user updates the company name to {string}")
    public void theUserUpdatesTheCompanyNameTo(String newName) {
        Response response = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of("name", newName))
        .when()
            .put("/companies/" + context.getCompanyId() + "/");
        context.setLastResponse(response);
        context.setCompanyName(newName);
    }

    @Then("the company name is {string}")
    public void theCompanyNameIs(String expectedName) {
        String actualName = context.getLastResponse().jsonPath().getString("name");
        assertEquals(expectedName, actualName,
            "Company name should be '" + expectedName + "' but was '" + actualName + "'");
    }

    @When("the user deletes the company")
    public void theUserDeletesTheCompany() {
        Response response = given()
            .header("Authorization", "Bearer " + context.getAccessToken())
        .when()
            .delete("/companies/" + context.getCompanyId() + "/");
        context.setLastResponse(response);
    }

    @Then("the company no longer appears in the list")
    public void theCompanyNoLongerAppearsInTheList() {
        int deletedId = context.getCompanyId();
        List<Map<String, Object>> companies = given()
            .header("Authorization", "Bearer " + context.getAccessToken())
        .when()
            .get("/companies/")
        .then()
            .statusCode(200)
            .extract().jsonPath().getList("$");
        boolean found = companies != null && companies.stream()
            .anyMatch(c -> deletedId == ((Number) c.get("id")).intValue());
        assertFalse(found, "Deleted company (id=" + deletedId + ") should not appear in the list");
    }

    @When("an unauthenticated user creates a company named {string}")
    public void anUnauthenticatedUserCreatesACompanyNamed(String name) {
        Response response = given()
            .contentType(JSON)
            .body(Map.of("name", name))
        .when()
            .post("/companies/");
        context.setLastResponse(response);
    }

    @When("the user lists the company members")
    public void theUserListsTheCompanyMembers() {
        Response response = given()
            .header("Authorization", "Bearer " + context.getAccessToken())
        .when()
            .get("/companies/" + context.getCompanyId() + "/members/");
        context.setLastResponse(response);
    }

    @Then("the member list contains the user with role {string}")
    public void theMemberListContainsTheUserWithRole(String expectedRole) {
        List<Map<String, Object>> members = context.getLastResponse().then()
            .statusCode(200)
            .extract().jsonPath().getList("$");
        assertNotNull(members, "Members list should not be null");
        assertFalse(members.isEmpty(), "Members list should not be empty");
        String role = (String) members.get(0).get("role");
        assertEquals(expectedRole, role,
            "Creator should have role '" + expectedRole + "' but was '" + role + "'");
    }

    @When("the user creates a company test titled {string}")
    public void theUserCreatesACompanyTestTitled(String title) {
        Response response = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of("title", title))
        .when()
            .post("/tests/company/" + context.getCompanyId() + "/");
        context.setLastResponse(response);
        if (response.statusCode() == 201) {
            context.setTestSlug(response.jsonPath().getString("slug"));
        }
    }

    @Then("the test appears in the company test list")
    public void theTestAppearsInTheCompanyTestList() {
        String slug = context.getTestSlug();
        List<Map<String, Object>> tests = given()
            .header("Authorization", "Bearer " + context.getAccessToken())
        .when()
            .get("/tests/company/" + context.getCompanyId() + "/")
        .then()
            .statusCode(200)
            .extract().jsonPath().getList("$");
        assertNotNull(tests, "Company tests list should not be null");
        boolean found = tests.stream().anyMatch(t -> slug.equals(t.get("slug")));
        assertTrue(found,
            "Company test with slug '" + slug + "' should appear in company test list");
    }
}
