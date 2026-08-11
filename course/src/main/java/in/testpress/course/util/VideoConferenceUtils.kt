package `in`.testpress.course.util

import com.auth0.android.jwt.JWT
import io.sentry.Sentry

fun isTokenValid(token: String?, leewayInSeconds: Long = 10): Boolean {
    if (token.isNullOrBlank()) {
        return false
    }
    return try {
        !JWT(token).isExpired(leewayInSeconds)
    } catch (e: Exception) {
        Sentry.captureException(e)
        false
    }
}
