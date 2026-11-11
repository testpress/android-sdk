package in.testpress.course.util;

import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.drm.MediaDrmCallbackException;
import com.google.android.exoplayer2.upstream.HttpDataSource;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class DrmErrorIdentifier {

    public static String getFailureReason(PlaybackException exception) {
        Throwable currentThrowable = exception;
        int traversalDepth = 0;

        while (currentThrowable != null && traversalDepth++ < 20) {
            
            if (currentThrowable instanceof MediaDrmCallbackException) {
                return getWidevineFailureReason((MediaDrmCallbackException) currentThrowable);
            }

            String licenseApiReason = getLicenseApiFailureReason(currentThrowable);
            if (licenseApiReason != null) {
                return licenseApiReason;
            }

            currentThrowable = currentThrowable.getCause();
        }
        return null;
    }

    private static String getWidevineFailureReason(MediaDrmCallbackException drmException) {
        Throwable nestedDrmThrowable = drmException.getCause();
        int searchDepth = 0;

        while (nestedDrmThrowable != null && searchDepth++ < 15) {
            
            if (nestedDrmThrowable instanceof HttpDataSource.InvalidResponseCodeException) {
                int responseCode = ((HttpDataSource.InvalidResponseCodeException) nestedDrmThrowable).responseCode;
                return "KEY_HTTP_" + responseCode;
            }

            if (nestedDrmThrowable instanceof UnknownHostException) return "KEY_NO_INTERNET";
            if (nestedDrmThrowable instanceof SocketTimeoutException) return "KEY_TIMEOUT";
            if (nestedDrmThrowable instanceof ConnectException) return "KEY_CONNECT_FAIL";
            
            nestedDrmThrowable = nestedDrmThrowable.getCause();
        }

        Throwable rootCause = drmException.getCause();
        if (rootCause == null) {
            return "KEY_UNKNOWN_DRM_CALLBACK_ERROR";
        }

        if (rootCause instanceof HttpDataSource.HttpDataSourceException) {
            return "KEY_HTTP_DATA_SOURCE_ERROR";
        } else if (rootCause instanceof android.media.MediaDrm.MediaDrmStateException) {
            return "KEY_MEDIA_DRM_STATE_ERROR";
        } else if (rootCause instanceof android.media.NotProvisionedException) {
            return "KEY_NOT_PROVISIONED";
        } else if (rootCause instanceof android.media.DeniedByServerException) {
            return "KEY_DENIED_BY_SERVER";
        }

        return "KEY_UNIDENTIFIED_DRM_ERROR";
    }

    private static String getLicenseApiFailureReason(Throwable throwable) {
        String errorMessage = throwable.getMessage();
        if (errorMessage != null && errorMessage.contains("DRM license URL")) {
            
            if (errorMessage.contains("missing in response")) {
                return "LICENSE_URL_EMPTY";
            }
            
            int httpCode = extractHttpCode(errorMessage);
            if (httpCode != -1) {
                return "LICENSE_URL_HTTP_" + httpCode;
            }
        }
        return null;
    }

    private static int extractHttpCode(String message) {
        int httpPatternStartIdx = message.indexOf("HTTP ");
        if (httpPatternStartIdx == -1) return -1;

        int numberStartIdx = httpPatternStartIdx + 5;
        int numberEndIdx = numberStartIdx;

        while (numberEndIdx < message.length() && Character.isDigit(message.charAt(numberEndIdx))) {
            numberEndIdx++;
        }

        if (numberEndIdx == numberStartIdx) return -1;

        try {
            return Integer.parseInt(message.substring(numberStartIdx, numberEndIdx));
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }
}
