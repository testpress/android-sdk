package `in`.testpress.util.extension

import `in`.testpress.util.ViewUtils
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.fragment.app.Fragment

fun Fragment.openUrlInBrowser(url: String?) {
    if (url == null) return
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    try {
        this.requireActivity().startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        ViewUtils.toast(this.requireContext(), "No suitable app found to open this link")
    }
}