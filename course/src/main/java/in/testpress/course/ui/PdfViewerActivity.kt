package `in`.testpress.course.ui

import `in`.testpress.course.R
import android.annotation.SuppressLint
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.layout_pdf_viewer.*
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class PdfViewerActivity : AppCompatActivity(), InputStreamListener {

    var inputStreamListener: InputStreamListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_pdf_viewer)
        inputStreamListener = this
        val file = intent.getStringExtra("pdfUrl") ?: ""
        progressbar.visibility = View.VISIBLE
        ShowPdfFromUri().execute(file)
    }

    override fun getResponse(response: InputStream?) {
        response?.let { loadPDF(it) }?: showErrorView()
    }

    private fun loadPDF(inputStream: InputStream) {
        emptyConatiner.visibility = View.GONE
        pdfView.visibility = View.VISIBLE
        pdfView.fromStream(inputStream).load()
        progressbar.visibility = View.GONE
    }

    private fun showErrorView() {
        pdfView.visibility = View.GONE
        emptyConatiner.visibility = View.VISIBLE
        emptyTitle.setText(R.string.failed_loading_pdf)
        progressbar.visibility = View.GONE
    }

    @SuppressLint("StaticFieldLeak")
    inner class ShowPdfFromUri : AsyncTask<String, Unit, InputStream>() {

        var inputStream: InputStream? = null

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
            inputStreamListener?.getResponse(inputStream)
        }
    }
}

interface InputStreamListener {
    fun getResponse(response: InputStream?)
}
