package testit.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.restassured.response.Response;
import testit.context.ScenarioContext;

import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.junit.jupiter.api.Assertions.*;

public class CascadingDeleteSteps {

    private final ScenarioContext context;

    public CascadingDeleteSteps(ScenarioContext context) {
        this.context = context;
    }

    // -------------------------------------------------------------------------
    // Setup: company + company test
    // -------------------------------------------------------------------------

    @Given("the author has set up a company with a company test")
    public void theAuthorHasSetUpACompanyWithACompanyTest() {
        String uid = UUID.randomUUID().toString().replace("-", "").substring(0, 6);

        Response companyResp = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of("name", "CascadeCo_" + uid))
        .when()
            .post("/companies/");
        companyResp.then().statusCode(201);
        context.setCompanyId(companyResp.jsonPath().getInt("id"));

        Response testResp = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of("title", "CascadeTest_" + uid))
        .when()
            .post("/tests/company/" + context.getCompanyId() + "/");
        testResp.then().statusCode(201);
        context.setTestSlug(testResp.jsonPath().getString("slug"));
    }

    // -------------------------------------------------------------------------
    // Setup: personal test + an unrelated company (no cross-association)
    // -------------------------------------------------------------------------

    @Given("the author has set up a personal test and an unrelated company")
    public void theAuthorHasSetUpAPersonalTestAndAnUnrelatedCompany() {
        String uid = UUID.randomUUID().toString().replace("-", "").substring(0, 6);

        Response testResp = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of(
                "title",             "PersonalTest_" + uid,
                "visibility",        "link_only",
                "max_attempts",      3,
                "show_answers_after", false
            ))
        .when()
            .post("/tests/");
        testResp.then().statusCode(201);
        context.setTestSlug(testResp.jsonPath().getString("slug"));

        Response companyResp = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of("name", "SurvivorCo_" + uid))
        .when()
            .post("/companies/");
        companyResp.then().statusCode(201);
        context.setCompanyId(companyResp.jsonPath().getInt("id"));
    }

    // -------------------------------------------------------------------------
    // Setup: personal test assigned to a folder inside a company
    // -------------------------------------------------------------------------

    @Given("the author has set up a personal test assigned to a folder")
    public void theAuthorHasSetUpAPersonalTestAssignedToAFolder() {
        String uid = UUID.randomUUID().toString().replace("-", "").substring(0, 6);

        Response companyResp = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of("name", "FolderCo_" + uid))
        .when()
            .post("/companies/");
        companyResp.then().statusCode(201);
        context.setCompanyId(companyResp.jsonPath().getInt("id"));

        Response folderResp = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of("name", "TestFolder_" + uid))
        .when()
            .post("/companies/" + context.getCompanyId() + "/folders/");
        folderResp.then().statusCode(201);
        context.setFolderId(folderResp.jsonPath().getInt("id"));

        Response testResp = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of(
                "title",             "FolderTest_" + uid,
                "visibility",        "link_only",
                "max_attempts",      3,
                "show_answers_after", false
            ))
        .when()
            .post("/tests/");
        testResp.then().statusCode(201);
        context.setTestSlug(testResp.jsonPath().getString("slug"));

        // Assign the personal test to the folder
        Response assignResp = given()
            .contentType(JSON)
            .header("Authorization", "Bearer " + context.getAccessToken())
            .body(Map.of("folder", context.getFolderId()))
        .when()
            .patch("/tests/" + context.getTestSlug() + "/");
        assignResp.then().statusCode(200);
        Integer assignedFolder = assignResp.jsonPath().get("folder");
        assertEquals(context.getFolderId(), assignedFolder,
            "Test must be assigned to folder before testing deletion");
    }

    // -------------------------------------------------------------------------
    // Assertions
    // -------------------------------------------------------------------------

    @Then("the company test is not accessible via the company endpoint")
    public void theCompanyTestIsNotAccessibleViaTheCompanyEndpoint() {
        given()
            .header("Authorization", "Bearer " + context.getAccessToken())
        .when()
            .get("/tests/company/" + context.getCompanyId() + "/" + context.getTestSlug() + "/")
        .then()
            .statusCode(404);
    }

    @Then("the company test is not accessible via the personal test endpoint")
    public void theCompanyTestIsNotAccessibleViaThePersonalTestEndpoint() {
        given()
            .header("Authorization", "Bearer " + context.getAccessToken())
        .when()
            .get("/tests/" + context.getTestSlug() + "/")
        .then()
            .statusCode(404);
    }

    @Then("the personal test is still accessible")
    public void thePersonalTestIsStillAccessible() {
        given()
            .header("Authorization", "Bearer " + context.getAccessToken())
        .when()
            .get("/tests/" + context.getTestSlug() + "/")
        .then()
            .statusCode(200);
    }

    @Then("the test's folder field is null")
    public void theTestsFolderFieldIsNull() {
        Object folderField = given()
            .header("Authorization", "Bearer " + context.getAccessToken())
        .when()
            .get("/tests/" + context.getTestSlug() + "/")
        .then()
            .statusCode(200)
            .extract().jsonPath().get("folder");
        assertNull(folderField,
            "After folder deletion, the test's folder field must be null but was: " + folderField);
    }
}
