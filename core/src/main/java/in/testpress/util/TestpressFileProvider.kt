package `in`.testpress.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

class TestpressFileProvider : FileProvider() {

    companion object {
        fun getUriForFile(context: Context, authority: String, file: File): Uri {
            return FileProvider.getUriForFile(context, authority, file)
        }
    }
}