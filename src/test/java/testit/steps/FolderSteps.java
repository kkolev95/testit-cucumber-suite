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

public class FolderSteps {

    private final ScenarioContext context;

    public FolderSteps(ScenarioContext context) {
        this.context = context;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private int resolveFolderId(String folderName) {
        Integer id = context.getFolderIdByName().get(folderName);
        assertNotNull(id, "No folder id found for: " + folderName);
        return id;
    }

    private int getFolderTestCount(String folderName) {
        int id = resolveFolderId(folderName);
        return given()
            .header("Authorization", "Bearer " + context.getAccessToken())
        .when()
            .get("/companies/" + context.getCompanyId() + "/folders/" + id + "/")
        .then()
            .statusCode(200)
            .extract().jsonPath().getInt("test_count");
    }

    // -------------------------------------------------------------------------
    // Step definitions
    // -------------------------------------------------------------------------

    @Given("the user has created a company")
    public void theUserHasCreatedACompany() {
        String name = "CukeCo_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        Response response = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of("name", name))
        .when()
            .post("/companies/");

        response.then().statusCode(201);
        context.setCompanyId(response.jsonPath().getInt("id"));
    }

    @When("the user creates a folder named {string}")
    public void theUserCreatesAFolderNamed(String folderName) {
        Response response = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of("name", folderName))
        .when()
            .post("/companies/" + context.getCompanyId() + "/folders/");

        context.setLastResponse(response);

        if (response.statusCode() == 201) {
            int id = response.jsonPath().getInt("id");
            if (id > 0) {
                context.putFolderId(folderName, id);
            } else {
                // Fallback: find by name in the list
                id = getFolderIdFromListByName(folderName);
                context.putFolderId(folderName, id);
            }
        }
    }

    private int getFolderIdFromListByName(String folderName) {
        List<Map<String, Object>> folders = given()
            .header("Authorization", "Bearer " + context.getAccessToken())
        .when()
            .get("/companies/" + context.getCompanyId() + "/folders/")
        .then()
            .statusCode(200)
            .extract().jsonPath().getList("$");

        return folders.stream()
            .filter(f -> folderName.equals(f.get("name")))
            .mapToInt(f -> ((Number) f.get("id")).intValue())
            .findFirst()
            .orElseThrow(() -> new AssertionError("Folder not found in list: " + folderName));
    }

    @Given("the user has created a folder named {string}")
    public void theUserHasCreatedAFolderNamedGiven(String folderName) {
        theUserCreatesAFolderNamed(folderName);
        context.getLastResponse().then().statusCode(201);
    }

    @Then("the folder appears in the company folder list")
    public void theFolderAppearsInTheCompanyFolderList() {
        List<Map<String, Object>> folders = given()
            .header("Authorization", "Bearer " + context.getAccessToken())
        .when()
            .get("/companies/" + context.getCompanyId() + "/folders/")
        .then()
            .statusCode(200)
            .extract().jsonPath().getList("$");

        assertNotNull(folders, "Folder list should not be null");
        assertFalse(folders.isEmpty(), "Folder list should not be empty after creating a folder");
    }

    @When("the user assigns the test to the folder")
    public void theUserAssignsTheTestToTheFolder() {
        Response response = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of("folder", context.getFolderId()))
        .when()
            .patch("/tests/" + context.getTestSlug() + "/");

        context.setLastResponse(response);
        response.then().statusCode(200);
    }

    @Given("the user has assigned the test to the folder")
    public void theUserHasAssignedTheTestToTheFolder() {
        theUserAssignsTheTestToTheFolder();
    }

    @Then("the folder test count is {int}")
    public void theFolderTestCountIs(int expectedCount) {
        int actual = given()
            .header("Authorization", "Bearer " + context.getAccessToken())
        .when()
            .get("/companies/" + context.getCompanyId() + "/folders/" + context.getFolderId() + "/")
        .then()
            .statusCode(200)
            .extract().jsonPath().getInt("test_count");

        assertEquals(expectedCount, actual,
            "Expected test_count " + expectedCount + " but was " + actual);
    }

    @Then("the test is linked to the folder")
    public void theTestIsLinkedToTheFolder() {
        Integer folderId = given()
            .header("Authorization", "Bearer " + context.getAccessToken())
        .when()
            .get("/tests/" + context.getTestSlug() + "/")
        .then()
            .statusCode(200)
            .extract().jsonPath().getInt("folder");

        assertEquals(context.getFolderId(), folderId,
            "Test should be linked to folder id " + context.getFolderId());
    }

    @When("the user unassigns the test from the folder")
    public void theUserUnassignsTheTestFromTheFolder() {
        // Setting folder to null removes the assignment
        Response response = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body("{\"folder\": null}")
        .when()
            .patch("/tests/" + context.getTestSlug() + "/");

        context.setLastResponse(response);
        response.then().statusCode(200);
    }

    @Given("the user has created folders {string} and {string}")
    public void theUserHasCreatedFoldersAnd(String nameA, String nameB) {
        theUserCreatesAFolderNamed(nameA);
        context.getLastResponse().then().statusCode(201);
        theUserCreatesAFolderNamed(nameB);
        context.getLastResponse().then().statusCode(201);
    }

    @Given("the user has assigned the test to {string}")
    public void theUserHasAssignedTheTestTo(String folderName) {
        given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of("folder", resolveFolderId(folderName)))
        .when()
            .patch("/tests/" + context.getTestSlug() + "/")
        .then()
            .statusCode(200);
    }

    @When("the user reassigns the test to {string}")
    public void theUserReassignsTheTestTo(String folderName) {
        Response response = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of("folder", resolveFolderId(folderName)))
        .when()
            .patch("/tests/" + context.getTestSlug() + "/");

        context.setLastResponse(response);
        response.then().statusCode(200);
    }

    @Then("{string} has a test count of {int}")
    public void folderHasATestCountOf(String folderName, int expectedCount) {
        int actual = getFolderTestCount(folderName);
        assertEquals(expectedCount, actual,
            "Expected " + folderName + " test_count=" + expectedCount + " but was " + actual);
    }

    @When("the user assigns the test to folder id {int}")
    public void theUserAssignsTheTestToFolderId(int folderId) {
        Response response = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of("folder", folderId))
        .when()
            .patch("/tests/" + context.getTestSlug() + "/");

        context.setLastResponse(response);
    }

    // -------------------------------------------------------------------------
    // Rename / delete folder
    // -------------------------------------------------------------------------

    @When("the user renames the folder to {string}")
    public void theUserRenamesTheFolderTo(String newName) {
        Response response = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of("name", newName))
        .when()
            .put("/companies/" + context.getCompanyId() + "/folders/" + context.getFolderId() + "/");
        context.setLastResponse(response);
    }

    @Then("the folder name is {string}")
    public void theFolderNameIs(String expectedName) {
        String actualName = context.getLastResponse().jsonPath().getString("name");
        assertEquals(expectedName, actualName,
            "Expected folder name '" + expectedName + "' but was '" + actualName + "'");
    }

    @When("the user deletes the folder")
    public void theUserDeletesTheFolder() {
        Response response = given()
            .header("Authorization", "Bearer " + context.getAccessToken())
        .when()
            .delete("/companies/" + context.getCompanyId() + "/folders/" + context.getFolderId() + "/");
        context.setLastResponse(response);
    }

    @Given("the user has deleted the folder")
    public void theUserHasDeletedTheFolder() {
        given()
            .header("Authorization", "Bearer " + context.getAccessToken())
        .when()
            .delete("/companies/" + context.getCompanyId() + "/folders/" + context.getFolderId() + "/")
        .then()
            .statusCode(204);
    }

    // -------------------------------------------------------------------------
    // Nested folders
    // -------------------------------------------------------------------------

    @When("the user creates a child folder named {string} under {string}")
    public void theUserCreatesAChildFolderNamedUnder(String childName, String parentName) {
        int parentId = resolveFolderId(parentName);
        Response response = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of("name", childName, "parent", parentId))
        .when()
            .post("/companies/" + context.getCompanyId() + "/folders/");
        context.setLastResponse(response);
        if (response.statusCode() == 201) {
            int childId = getFolderIdFromListByName(childName);
            context.putFolderId(childName, childId);
        }
    }

    @Then("the child folder is nested under {string}")
    public void theChildFolderIsNestedUnder(String parentName) {
        int expectedParentId = resolveFolderId(parentName);
        List<Map<String, Object>> folders = given()
            .header("Authorization", "Bearer " + context.getAccessToken())
        .when()
            .get("/companies/" + context.getCompanyId() + "/folders/")
        .then()
            .statusCode(200)
            .extract().jsonPath().getList("$");
        boolean found = folders.stream().anyMatch(f -> {
            Object p = f.get("parent");
            return p != null && ((Number) p).intValue() == expectedParentId;
        });
        assertTrue(found, "No folder found nested under '" + parentName + "'");
    }

    // -------------------------------------------------------------------------
    // Validation
    // -------------------------------------------------------------------------

    @When("the user creates a folder with an empty name")
    public void theUserCreatesAFolderWithAnEmptyName() {
        Response response = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of("name", ""))
        .when()
            .post("/companies/" + context.getCompanyId() + "/folders/");
        context.setLastResponse(response);
    }

    // -------------------------------------------------------------------------
    // Access control
    // -------------------------------------------------------------------------

    @When("a different user tries to create a folder in the company")
    public void aDifferentUserTriesToCreateAFolderInTheCompany() {
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
        String otherToken = loginResp.jsonPath().getString("access");
        context.setInviteeToken(otherToken);  // stored so @After cleans up

        Response response = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + otherToken)
            .body(Map.of("name", "Intruder Folder"))
        .when()
            .post("/companies/" + context.getCompanyId() + "/folders/");
        context.setLastResponse(response);
    }

    @Then("access to the folder is denied")
    public void accessToTheFolderIsDenied() {
        int status = context.getLastResponse().statusCode();
        assertEquals(404, status,
            "Expected 404 Not Found for non-member folder access (API hides folders from non-members) but got " + status);
    }

    // -------------------------------------------------------------------------
    // Folder listing
    // -------------------------------------------------------------------------

    @When("the user lists the company folders")
    public void theUserListsTheCompanyFolders() {
        Response response = given()
            .header("Authorization", "Bearer " + context.getAccessToken())
        .when()
            .get("/companies/" + context.getCompanyId() + "/folders/");
        context.setLastResponse(response);
    }

    @Then("the folder list does not contain {string}")
    public void theFolderListDoesNotContain(String folderName) {
        context.getLastResponse().then().statusCode(200);
        List<Map<String, Object>> folders = context.getLastResponse().jsonPath().getList("$");
        boolean found = folders.stream().anyMatch(f -> folderName.equals(f.get("name")));
        assertFalse(found, "Folder list should not contain '" + folderName + "' after deletion");
    }
}
