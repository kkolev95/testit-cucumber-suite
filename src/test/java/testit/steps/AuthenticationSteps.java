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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class AuthenticationSteps {

    private final ScenarioContext context;

    public AuthenticationSteps(ScenarioContext context) {
        this.context = context;
    }

    // -------------------------------------------------------------------------
    // Setup helpers (shared across feature files)
    // -------------------------------------------------------------------------

    private void registerNewUser() {
        String email = "cuke_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8) + "@example.com";
        String password = "Password123!";
        context.setEmail(email);
        context.setPassword(password);

        Response response = given()
            .contentType(JSON)
            .body(Map.of(
                "email", email,
                "password", password,
                "password_confirm", password,
                "first_name", "Cucumber",
                "last_name", "Tester"
            ))
        .when()
            .post("/auth/register/");

        context.setLastResponse(response);
    }

    private void loginCurrentUser() {
        Response response = given()
            .contentType(JSON)
            .body(Map.of(
                "email", context.getEmail(),
                "password", context.getPassword()
            ))
        .when()
            .post("/auth/login/");

        context.setLastResponse(response);
        if (response.statusCode() == 200) {
            context.setAccessToken(response.jsonPath().getString("access"));
        }
    }

    // -------------------------------------------------------------------------
    // Step definitions
    // -------------------------------------------------------------------------

    @When("a new user registers with valid credentials")
    public void aNewUserRegistersWithValidCredentials() {
        registerNewUser();
    }

    @When("a user tries to register with email {string} password {string} and confirm {string}")
    public void aUserTriesToRegisterWithEmailPasswordAndConfirm(String email, String password, String confirm) {
        Response response = given()
            .contentType(JSON)
            .body(Map.of(
                "email",            email,
                "password",         password,
                "password_confirm", confirm,
                "first_name",       "Test",
                "last_name",        "User"
            ))
        .when()
            .post("/auth/register/");
        context.setLastResponse(response);
    }

    @Given("a registered user exists")
    public void aRegisteredUserExists() {
        registerNewUser();
        context.getLastResponse().then().statusCode(201);
    }

    @Given("a registered and authenticated user")
    public void aRegisteredAndAuthenticatedUser() {
        registerNewUser();
        context.getLastResponse().then().statusCode(201);
        loginCurrentUser();
        context.getLastResponse().then().statusCode(200);
    }

    @Given("a registered and authenticated author")
    public void aRegisteredAndAuthenticatedAuthor() {
        aRegisteredAndAuthenticatedUser();
    }

    @When("the user logs in with correct credentials")
    public void theUserLogsInWithCorrectCredentials() {
        loginCurrentUser();
    }

    @When("the user logs in with the wrong password")
    public void theUserLogsInWithTheWrongPassword() {
        Response response = given()
            .contentType(JSON)
            .body(Map.of(
                "email", context.getEmail(),
                "password", "WrongPassword999!"
            ))
        .when()
            .post("/auth/login/");
        context.setLastResponse(response);
    }

    @When("the profile endpoint is requested without authentication")
    public void theProfileEndpointIsRequestedWithoutAuthentication() {
        Response response = given()
        .when()
            .get("/auth/me/");
        context.setLastResponse(response);
    }

    @When("the user requests their profile")
    public void theUserRequestsTheirProfile() {
        Response response = given()
            .header("Authorization", "Bearer " + context.getAccessToken())
        .when()
            .get("/auth/me/");
        context.setLastResponse(response);
    }

    @When("a user tries to register with the same email again")
    public void aUserTriesToRegisterWithTheSameEmailAgain() {
        Response response = given()
            .contentType(JSON)
            .body(Map.of(
                "email",            context.getEmail(),
                "password",         "Password123!",
                "password_confirm", "Password123!",
                "first_name",       "Duplicate",
                "last_name",        "User"
            ))
        .when()
            .post("/auth/register/");
        context.setLastResponse(response);
    }

    @Then("the response status should be {int}")
    public void theResponseStatusShouldBe(int expectedStatus) {
        context.getLastResponse().then().statusCode(expectedStatus);
    }

    @Then("the response contains an access token")
    public void theResponseContainsAnAccessToken() {
        context.getLastResponse().then().body("access", notNullValue());
        context.setAccessToken(context.getLastResponse().jsonPath().getString("access"));
    }

    @Then("the profile email matches the registered email")
    public void theProfileEmailMatchesTheRegisteredEmail() {
        context.getLastResponse().then()
            .body("email", equalTo(context.getEmail()));
    }
}
