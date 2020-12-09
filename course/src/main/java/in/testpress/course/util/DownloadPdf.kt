package `in`.testpress.course.util

import android.os.AsyncTask

class DownloadPdf: AsyncTask<String,Unit,Unit> () {

    override fun doInBackground(vararg params: String?) {
        val fileUrl = params[0]
    }

}