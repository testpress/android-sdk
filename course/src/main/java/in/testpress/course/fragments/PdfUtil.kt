package `in`.testpress.course.fragments

import android.annotation.SuppressLint
import android.os.AsyncTask
import androidx.annotation.VisibleForTesting
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class PdfUtil(private val inputStreamListener: InputStreamListener) {

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    var inputStream: InputStream? = null

    @SuppressLint("StaticFieldLeak")
    fun get(url: String?) {
        object : AsyncTask<String, Unit, InputStream>() {

            override fun doInBackground(vararg params: String): InputStream? {
                try {
                    val uri = URL(params[0])
                    val urlConnection: HttpURLConnection = uri.openConnection() as HttpURLConnection
                    if (urlConnection.responseCode == 200) {
                        inputStream = BufferedInputStream(urlConnection.inputStream)
                    }
                } catch (e: IOException) {
                    return null
                }
                return inputStream
            }

            override fun onPostExecute(result: InputStream?) {
                super.onPostExecute(result)
                inputStreamListener.getResponse(inputStream)
            }
        }.execute(url)
    }
}

interface InputStreamListener {
    fun getResponse(response: InputStream?)
}
