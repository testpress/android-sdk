package `in`.testpress.course.util

import `in`.testpress.course.R
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.widget.TextView
import com.google.android.material.progressindicator.CircularProgressIndicator

class ProgressDialog(context: Context, message: String? = null, indeterminate: Boolean = true) : Dialog(context) {

    private val progressBar: CircularProgressIndicator
    private val progressText: TextView

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.progress_dialog, null)
        setContentView(view)
        setCancelable(false)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        progressBar = view.findViewById(R.id.progressBar)
        progressText = view.findViewById(R.id.progress_text)
        progressBar.isIndeterminate  = indeterminate

        message?.let {
            progressText.text = it
        }
    }

    fun updateProgress(progress: Int) {
        progressBar.progress = progress
        progressBar.max = 100
        progressText.text = "Syncing Exam..."
    }

    companion object {
        fun create(context: Context, message: String? = null, indeterminate: Boolean = true): ProgressDialog {
            return ProgressDialog(context, message, indeterminate)
        }
    }
}
