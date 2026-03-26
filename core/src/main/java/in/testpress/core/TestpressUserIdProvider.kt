package `in`.testpress.core

import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.core.TestpressUserDetails
import `in`.testpress.models.ProfileDetails
import android.content.Context
import kotlinx.coroutines.suspendCancellableCoroutine

object TestpressUserIdProvider {
    suspend fun getUserId(context: Context): String {
        TestpressUserDetails.getInstance().profileDetails?.id?.toString()?.let { return it }

        return suspendCancellableCoroutine { continuation ->
            val call = TestpressUserDetails.getInstance().load(context, object : TestpressCallback<ProfileDetails>() {
                override fun onSuccess(userDetails: ProfileDetails) {
                    if (continuation.isActive) {
                        continuation.resume(userDetails.id?.toString() ?: "") {}
                    }
                }

                override fun onException(exception: TestpressException) {
                    if (continuation.isActive) {
                        continuation.resume("") {}
                    }
                }
            })

            continuation.invokeOnCancellation {
                call?.cancel()
            }
        }
    }
}
