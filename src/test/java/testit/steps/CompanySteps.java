package testit.steps;

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

    @Given("the user has created a company test titled {string}")
    public void theUserHasCreatedACompanyTestTitled(String title) {
        theUserCreatesACompanyTestTitled(title);
        context.getLastResponse().then().statusCode(201);
    }

    @When("the user fetches the company test by slug")
    public void theUserFetchesTheCompanyTestBySlug() {
        Response response = given()
            .header("Authorization", "Bearer " + context.getAccessToken())
        .when()
            .get("/tests/company/" + context.getCompanyId() + "/" + context.getTestSlug() + "/");
        context.setLastResponse(response);
    }

    @When("the user updates the company test title to {string}")
    public void theUserUpdatesTheCompanyTestTitleTo(String newTitle) {
        Response response = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of("title", newTitle))
        .when()
            .patch("/tests/company/" + context.getCompanyId() + "/" + context.getTestSlug() + "/");
        context.setLastResponse(response);
    }

    @When("the user deletes the company test")
    public void theUserDeletesTheCompanyTest() {
        Response response = given()
            .header("Authorization", "Bearer " + context.getAccessToken())
        .when()
            .delete("/tests/company/" + context.getCompanyId() + "/" + context.getTestSlug() + "/");
        context.setLastResponse(response);
    }

    @When("a non-member requests the company test")
    public void aNonMemberRequestsTheCompanyTest() {
        Response response = given()
        .when()
            .get("/tests/company/" + context.getCompanyId() + "/" + context.getTestSlug() + "/");
        context.setLastResponse(response);
    }

    // -------------------------------------------------------------------------
    // Member management
    // -------------------------------------------------------------------------

    /**
     * Registers a second user, has the admin send an invite, and has the second
     * user accept it. Stores the second user's token in inviteeToken (so the
     * @After hook cleans up their account) and their user ID in inviteeUserId.
     */
    @Given("the admin has invited a second user who joined as {string}")
    public void theAdminHasInvitedASecondUserWhoJoinedAs(String role) {
        // Register second user
        String email = "member_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8) + "@example.com";
        String password = "Password123!";

        given()
            .contentType(JSON)
            .body(Map.of(
                "email",            email,
                "password",         password,
                "password_confirm", password,
                "first_name",       "Second",
                "last_name",        "Member"
            ))
        .when()
            .post("/auth/register/")
        .then()
            .statusCode(201);

        context.setInviteeEmail(email);
        context.setInviteePassword(password);

        // Admin sends invite
        Response inviteResp = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of("email", email, "role", role))
        .when()
            .post("/companies/" + context.getCompanyId() + "/invites/");
        inviteResp.then().statusCode(201);
        context.setInviteToken(inviteResp.jsonPath().getString("token"));

        // Second user logs in
        Response loginResp = given()
            .contentType(JSON)
            .body(Map.of("email", email, "password", password))
        .when()
            .post("/auth/login/");
        loginResp.then().statusCode(200);
        context.setInviteeToken(loginResp.jsonPath().getString("access"));

        // Second user accepts invite
        given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getInviteeToken())
            .body(Map.of())
        .when()
            .post("/invites/" + context.getInviteToken() + "/accept/")
        .then()
            .statusCode(200);

        // Store second user's ID for member management calls
        int inviteeUserId = given()
            .header("Authorization", "Bearer " + context.getInviteeToken())
        .when()
            .get("/auth/me/")
        .then()
            .statusCode(200)
            .extract().jsonPath().getInt("id");
        context.setInviteeUserId(inviteeUserId);
    }

    @When("the admin changes the second member's role to {string}")
    public void theAdminChangesTheSecondMembersRoleTo(String newRole) {
        Response response = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of("role", newRole))
        .when()
            .put("/companies/" + context.getCompanyId() + "/members/" + context.getInviteeUserId() + "/");
        context.setLastResponse(response);
    }

    @When("a non-admin member tries to update a member's role")
    public void aNonAdminMemberTriesToUpdateAMembersRole() {
        Response response = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getInviteeToken())
            .body(Map.of("role", "admin"))
        .when()
            .put("/companies/" + context.getCompanyId() + "/members/" + context.getInviteeUserId() + "/");
        context.setLastResponse(response);
    }

    @When("the admin removes the second member from the company")
    public void theAdminRemovesTheSecondMemberFromTheCompany() {
        Response response = given()
            .header("Authorization", "Bearer " + context.getAccessToken())
        .when()
            .delete("/companies/" + context.getCompanyId() + "/members/" + context.getInviteeUserId() + "/");
        context.setLastResponse(response);
    }

    @When("the admin tries to remove themselves from the company")
    public void theAdminTriesToRemoveThemselvesFromTheCompany() {
        int adminUserId = given()
            .header("Authorization", "Bearer " + context.getAccessToken())
        .when()
            .get("/auth/me/")
        .then()
            .statusCode(200)
            .extract().jsonPath().getInt("id");

        Response response = given()
            .header("Authorization", "Bearer " + context.getAccessToken())
        .when()
            .delete("/companies/" + context.getCompanyId() + "/members/" + adminUserId + "/");
        context.setLastResponse(response);
    }
}
