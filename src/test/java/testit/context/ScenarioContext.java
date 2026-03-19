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
    private List<String> createdTestSlugs = new ArrayList<>();

    // Multi-select: multiple correct/wrong answer IDs
    private List<Integer> correctAnswerIds = new ArrayList<>();
    private List<Integer> wrongAnswerIds = new ArrayList<>();

    // Invite flow — second user and invite token
    private String inviteeEmail;
    private String inviteePassword;
    private String inviteeToken;
    private String inviteToken;

    // Company / folder management
    private int companyId;
    private String companyName;
    private int folderId;
    private Map<String, Integer> folderIdByName = new HashMap<>();

    // Data integrity and cross-user detail
    private double lastScore;
    private int questionId2;
    private int correctAnswerId2;
    private int inviteeUserId;
    private int resultId;

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
        createdTestSlugs = new ArrayList<>();
        correctAnswerIds = new ArrayList<>();
        wrongAnswerIds = new ArrayList<>();
        inviteeEmail = null;
        inviteePassword = null;
        inviteeToken = null;
        inviteToken = null;
        companyId = 0;
        companyName = null;
        folderId = 0;
        folderIdByName = new HashMap<>();
        lastScore = 0.0;
        questionId2 = 0;
        correctAnswerId2 = 0;
        inviteeUserId = 0;
        resultId = 0;
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

    public List<String> getCreatedTestSlugs() { return createdTestSlugs; }
    public void addCreatedTestSlug(String slug) { this.createdTestSlugs.add(slug); }

    public List<Integer> getCorrectAnswerIds() { return correctAnswerIds; }
    public void setCorrectAnswerIds(List<Integer> correctAnswerIds) { this.correctAnswerIds = correctAnswerIds; }

    public List<Integer> getWrongAnswerIds() { return wrongAnswerIds; }
    public void setWrongAnswerIds(List<Integer> wrongAnswerIds) { this.wrongAnswerIds = wrongAnswerIds; }

    public String getInviteeEmail() { return inviteeEmail; }
    public void setInviteeEmail(String inviteeEmail) { this.inviteeEmail = inviteeEmail; }

    public String getInviteePassword() { return inviteePassword; }
    public void setInviteePassword(String inviteePassword) { this.inviteePassword = inviteePassword; }

    public String getInviteeToken() { return inviteeToken; }
    public void setInviteeToken(String inviteeToken) { this.inviteeToken = inviteeToken; }

    public String getInviteToken() { return inviteToken; }
    public void setInviteToken(String inviteToken) { this.inviteToken = inviteToken; }

    public int getCompanyId() { return companyId; }
    public void setCompanyId(int companyId) { this.companyId = companyId; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public int getFolderId() { return folderId; }
    public void setFolderId(int folderId) { this.folderId = folderId; }

    public Map<String, Integer> getFolderIdByName() { return folderIdByName; }
    public void putFolderId(String name, int id) {
        this.folderIdByName.put(name, id);
        this.folderId = id;
    }

    public double getLastScore() { return lastScore; }
    public void setLastScore(double lastScore) { this.lastScore = lastScore; }

    public int getQuestionId2() { return questionId2; }
    public void setQuestionId2(int questionId2) { this.questionId2 = questionId2; }

    public int getCorrectAnswerId2() { return correctAnswerId2; }
    public void setCorrectAnswerId2(int correctAnswerId2) { this.correctAnswerId2 = correctAnswerId2; }

    public int getInviteeUserId() { return inviteeUserId; }
    public void setInviteeUserId(int inviteeUserId) { this.inviteeUserId = inviteeUserId; }

    public int getResultId() { return resultId; }
    public void setResultId(int resultId) { this.resultId = resultId; }
}
