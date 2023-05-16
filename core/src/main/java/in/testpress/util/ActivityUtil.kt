package `in`.testpress.util

import android.content.Context
import android.content.Intent
import android.net.Uri

object ActivityUtil {

    fun openUrl(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            ViewUtils.toast(context,"No suitable app was found to open this URL. Please install any browser app")
        }
    }

}