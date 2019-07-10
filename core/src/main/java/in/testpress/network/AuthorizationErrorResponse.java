package in.testpress.network;

import java.util.ArrayList;
import java.util.List;

public class AuthorizationErrorResponse {

    private List<String> userId = new ArrayList<String>();
    private List<String> accessToken = new ArrayList<String>();
    private List<String> provider = new ArrayList<String>();
    private List<String> nonFieldErrors = new ArrayList<String>();
    private List<String> username = new ArrayList<String>();

    public List<String> getUserId() {
        return userId;
    }

    public void setUserId(List<String> userId) {
        this.userId = userId;
    }

    public List<String> getProvider() {
        return provider;
    }

    public void setProvider(List<String> provider) {
        this.provider = provider;
    }

    public List<String> getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(List<String> accessToken) {
        this.accessToken = accessToken;
    }

    public List<String> getNonFieldErrors() {
        return nonFieldErrors;
    }

    public void setNonFieldErrors(List<String> nonFieldErrors) {
        this.nonFieldErrors = nonFieldErrors;
    }

    public List<String> getUsername() {
        return username;
    }

    public void setUsername(List<String> username) {
        this.username = username;
    }
}
