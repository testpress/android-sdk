package `in`.testpress.util.extension

import `in`.testpress.util.ViewUtils
import android.content.Intent
import android.net.Uri
import androidx.fragment.app.Fragment

fun Fragment.openUrlInBrowser(url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    if (intent.resolveActivity(this.requireContext().packageManager) != null) {
        this.requireActivity().startActivity(intent)
    } else {
        ViewUtils.toast(this.requireContext(),"No suitable app was found to open this URL. Please install any browser app")
    }
}