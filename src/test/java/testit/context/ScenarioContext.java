package testit.context;

import io.restassured.response.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Shared state container injected into all step definition classes via PicoContainer.
 * Holds everything that needs to flow between Given / When / Then steps.
 */
public class ScenarioContext {

    private String email;
    private String password;
    private String accessToken;
    private Response lastResponse;
    private String testSlug;
    private int questionId;
    private int correctAnswerId;
    private int wrongAnswerId;
    private int attemptId;
    private Map<String, String> anonCookies = new HashMap<>();
    private List<Integer> questionIds = new ArrayList<>();

    // Multi-select: multiple correct/wrong answer IDs
    private List<Integer> correctAnswerIds = new ArrayList<>();
    private List<Integer> wrongAnswerIds = new ArrayList<>();

    // Folder management
    private int companyId;
    private int folderId;
    private Map<String, Integer> folderIdByName = new HashMap<>();

    public void reset() {
        email = null;
        password = null;
        accessToken = null;
        lastResponse = null;
        testSlug = null;
        questionId = 0;
        correctAnswerId = 0;
        wrongAnswerId = 0;
        attemptId = 0;
        anonCookies = new HashMap<>();
        questionIds = new ArrayList<>();
        correctAnswerIds = new ArrayList<>();
        wrongAnswerIds = new ArrayList<>();
        companyId = 0;
        folderId = 0;
        folderIdByName = new HashMap<>();
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public Response getLastResponse() { return lastResponse; }
    public void setLastResponse(Response lastResponse) { this.lastResponse = lastResponse; }

    public String getTestSlug() { return testSlug; }
    public void setTestSlug(String testSlug) { this.testSlug = testSlug; }

    public int getQuestionId() { return questionId; }
    public void setQuestionId(int questionId) { this.questionId = questionId; }

    public int getCorrectAnswerId() { return correctAnswerId; }
    public void setCorrectAnswerId(int correctAnswerId) { this.correctAnswerId = correctAnswerId; }

    public int getWrongAnswerId() { return wrongAnswerId; }
    public void setWrongAnswerId(int wrongAnswerId) { this.wrongAnswerId = wrongAnswerId; }

    public int getAttemptId() { return attemptId; }
    public void setAttemptId(int attemptId) { this.attemptId = attemptId; }

    public Map<String, String> getAnonCookies() { return anonCookies; }
    public void setAnonCookies(Map<String, String> anonCookies) { this.anonCookies = anonCookies; }

    public List<Integer> getQuestionIds() { return questionIds; }
    public void setQuestionIds(List<Integer> questionIds) { this.questionIds = questionIds; }

    public List<Integer> getCorrectAnswerIds() { return correctAnswerIds; }
    public void setCorrectAnswerIds(List<Integer> correctAnswerIds) { this.correctAnswerIds = correctAnswerIds; }

    public List<Integer> getWrongAnswerIds() { return wrongAnswerIds; }
    public void setWrongAnswerIds(List<Integer> wrongAnswerIds) { this.wrongAnswerIds = wrongAnswerIds; }

    public int getCompanyId() { return companyId; }
    public void setCompanyId(int companyId) { this.companyId = companyId; }

    public int getFolderId() { return folderId; }
    public void setFolderId(int folderId) { this.folderId = folderId; }

    public Map<String, Integer> getFolderIdByName() { return folderIdByName; }
    public void putFolderId(String name, int id) {
        this.folderIdByName.put(name, id);
        this.folderId = id;
    }
}
