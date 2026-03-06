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

public class InviteSteps {

    private final ScenarioContext context;

    public InviteSteps(ScenarioContext context) {
        this.context = context;
    }

    @Given("a second user has registered")
    public void aSecondUserHasRegistered() {
        String email = "invitee_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8) + "@example.com";
        String password = "Password123!";

        given()
            .contentType(JSON)
            .body(Map.of(
                "email",            email,
                "password",         password,
                "password_confirm", password,
                "first_name",       "Invitee",
                "last_name",        "User"
            ))
        .when()
            .post("/auth/register/")
        .then()
            .statusCode(201);

        context.setInviteeEmail(email);
        context.setInviteePassword(password);
    }

    @When("the admin sends an invite to the second user as {string}")
    public void theAdminSendsAnInviteToTheSecondUserAs(String role) {
        Response response = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of("email", context.getInviteeEmail(), "role", role))
        .when()
            .post("/companies/" + context.getCompanyId() + "/invites/");

        context.setLastResponse(response);
        if (response.statusCode() == 201) {
            context.setInviteToken(response.jsonPath().getString("token"));
        }
    }

    @Given("the admin has sent an invite to the second user as {string}")
    public void theAdminHasSentAnInviteToTheSecondUserAs(String role) {
        theAdminSendsAnInviteToTheSecondUserAs(role);
        context.getLastResponse().then().statusCode(201);
    }

    @Then("the invite token is returned")
    public void theInviteTokenIsReturned() {
        String token = context.getLastResponse().jsonPath().getString("token");
        assertNotNull(token, "Invite token should not be null");
        assertFalse(token.isBlank(), "Invite token should not be blank");
    }

    @When("the admin lists the company invites")
    public void theAdminListsTheCompanyInvites() {
        Response response = given()
            .header("Authorization", "Bearer " + context.getAccessToken())
        .when()
            .get("/companies/" + context.getCompanyId() + "/invites/");
        context.setLastResponse(response);
    }

    @Then("the second user's email appears in the invite list")
    public void theSecondUsersEmailAppearsInTheInviteList() {
        context.getLastResponse().then().statusCode(200);
        List<Map<String, Object>> invites = context.getLastResponse().jsonPath().getList("$");
        assertNotNull(invites, "Invite list should not be null");
        boolean found = invites.stream()
            .anyMatch(i -> context.getInviteeEmail().equals(i.get("email")));
        assertTrue(found, "Invite list should contain the invitee email: " + context.getInviteeEmail());
    }

    @Given("the second user has logged in")
    public void theSecondUserHasLoggedIn() {
        Response response = given()
            .contentType(JSON)
            .body(Map.of("email", context.getInviteeEmail(), "password", context.getInviteePassword()))
        .when()
            .post("/auth/login/");
        response.then().statusCode(200);
        context.setInviteeToken(response.jsonPath().getString("access"));
    }

    @When("the second user accepts the invite")
    public void theSecondUserAcceptsTheInvite() {
        Response response = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getInviteeToken())
            .body(Map.of())
        .when()
            .post("/invites/" + context.getInviteToken() + "/accept/");
        context.setLastResponse(response);
        response.then().statusCode(200);
    }

    @Then("the company member count is {int}")
    public void theCompanyMemberCountIs(int expectedCount) {
        List<?> members = given()
            .header("Authorization", "Bearer " + context.getAccessToken())
        .when()
            .get("/companies/" + context.getCompanyId() + "/members/")
        .then()
            .statusCode(200)
            .extract().jsonPath().getList("$");
        assertEquals(expectedCount, members.size(),
            "Expected " + expectedCount + " members but found " + members.size());
    }
}
