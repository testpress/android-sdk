package in.testpress.core;

/**
 * References
 * https://gist.github.com/rahulgautam/25c72ffcac70dacb87bd#file-errorhandlingexecutorcalladapterfactory-java
 * https://github.com/square/retrofit/tree/master/samples/src/main/java/com/example/retrofit
 */

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Response;

public class TestpressException extends RuntimeException {
    public static TestpressException httpError(Response response) {
        String message = response.code() + " " + response.message();
        return new TestpressException(message, response, Kind.HTTP, null);
    }

    public static TestpressException httpError(int errorCode, String errorMessage) {
        String message = errorCode + " " + errorMessage;
        TestpressException exception = new TestpressException(message, null, Kind.HTTP, null);
        exception.setStatusCode(errorCode);
        return exception;
    }

    public static TestpressException networkError(IOException exception) {
        return new TestpressException(exception.getMessage(), null, Kind.NETWORK, exception);
    }

    public static TestpressException unexpectedError(Throwable exception) {
        return new TestpressException(exception.getMessage(), null, Kind.UNEXPECTED, exception);
    }

    public static TestpressException unexpectedWebViewError(Throwable exception) {
        return new TestpressException(exception.getMessage(), null, Kind.WEBVIEW_UNEXPECTED, exception);
    }

    /** Identifies the event kind which triggered a {@link TestpressException}. */
    public enum Kind {
        /** An {@link IOException} occurred while communicating to the server. */
        NETWORK,
        /** A non-200 HTTP status code was received from the server. */
        HTTP,
        /**
         * An internal error occurred while attempting to execute a request. It is best practice to
         * re-throw this exception so your application crashes.
         */
        UNEXPECTED,
        /** An unexpected error occurred within the WebView. */
        WEBVIEW_UNEXPECTED
    }

    private final Response response;
    private final Kind kind;
    private int statusCode;
    private String message;

    TestpressException(String message, Response response, Kind kind, Throwable exception) {
        super(message, exception);
        this.response = response;
        this.message = message;
        this.kind = kind;
        if (response != null) {
            statusCode = response.code();
        }
    }

    /** Response object containing status code, headers, body, etc. */
    public Response getResponse() {
        return response;
    }

    /** The event kind which triggered this error. */
    public Kind getKind() {
        return kind;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * HTTP response body converted to specified {@code type}. {@code null} if there is no
     * response.
     */
    public <T> T getErrorBodyAs(Response response, Class<T> type) {
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
        try {
            String json = response.errorBody().string();
            return gson.fromJson(json, type);
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
        return null;
    }

    public long getThrottleTime() {
        TestpressErrorDetail errorDetail = getErrorBodyAs(this.getResponse(), TestpressErrorDetail.class);

        if (errorDetail == null || errorDetail.getDetail() == null) {
            // If the response or its detail is null, return 0 as the default throttle time.
            return 0;
        }

        String detail = errorDetail.getDetail();
        String throttleTimeStr = extractThrottleTime(detail);

        if (throttleTimeStr != null) {
            try {
                return (long) Double.parseDouble(throttleTimeStr);
            } catch (NumberFormatException e) {
                // If parsing fails, continue to return the default throttle duration.
            }
        }

        // If the response detail is not null but parsing fails, return the default throttle duration of 60 seconds.
        return 60;
    }

    private String extractThrottleTime(String detail) {
        // Extracts the throttle time string using regex. The pattern matches "in X seconds"
        // where X can be an integer or a decimal number.
        Pattern pattern = Pattern.compile(".*in (\\d+\\.\\d+|\\d+) seconds.*");
        Matcher matcher = pattern.matcher(detail);
        return matcher.matches() ? matcher.group(1) : null;
    }

    public boolean isNetworkError() {
        return kind == Kind.NETWORK;
    }

    public boolean isWebViewUnexpectedError() {
        return kind == Kind.WEBVIEW_UNEXPECTED;
    }

    public boolean isPageNotFound() {
        return statusCode == 404;
    }

    public boolean isForbidden() {
        return statusCode == 403;
    }

    public boolean isUnauthenticated() {
        return statusCode == 403;
    }

    public boolean isTooManyRequest() {
        return statusCode == 429;
    }
    public boolean isClientError() {
        return statusCode >= 400 && statusCode < 500 && statusCode != 403;
    }

    public boolean isServerError() {
        return statusCode >= 500 && statusCode < 600;
    }
}
