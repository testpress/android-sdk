package in.testpress.core;

public class TestpressException extends Throwable {

    public static final int BAD_REQUEST = 400;
    public static final int NETWORK_ERROR = 404;

    private int statusCode;

    public TestpressException(Throwable cause) {
        super(cause);
    }

    public TestpressException(String detailMessage, Throwable cause) {
        super(detailMessage, cause);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

}
