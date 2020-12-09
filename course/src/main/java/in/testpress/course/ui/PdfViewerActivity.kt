package `in`.testpress.course.ui

import `in`.testpress.course.R
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.layout_pdf_viewer.*
import java.io.BufferedInputStream
import java.io.InputStream
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL

class PdfViewerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_pdf_viewer)
        val file = intent.getStringExtra("fileUrl") ?: ""
        progressbar.visibility = View.VISIBLE
        RetrievePDFStream(this).execute(file)
    }
}

class RetrievePDFStream(private val activity: PdfViewerActivity) : AsyncTask<String, Unit, InputStream>() {

    private var inputStream: InputStream? = null

    override fun doInBackground(vararg params: String): InputStream? {
        try {
            val uri = URL(params[0])
            val urlConnection: HttpURLConnection = uri.openConnection() as HttpURLConnection
            if (urlConnection.responseCode == 200) {
                inputStream = BufferedInputStream(urlConnection.inputStream)
            }
        } catch (e: Exception) {
            return null
        }
        return inputStream
    }

    override fun onPostExecute(result: InputStream?) {
        super.onPostExecute(result)
        activity.pdfView.fromStream(inputStream).load()
        activity.progressbar.visibility = View.GONE
    }
}
