package `in`.testpress.course.util

import `in`.testpress.course.R
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.widget.TextView

class ProgressDialog {
    companion object {
        fun progressDialog(context: Context, message: String? = null): Dialog {
            val dialog = Dialog(context)
            val inflate = LayoutInflater.from(context).inflate(R.layout.progress_dialog, null)
            message?.let {
                inflate.findViewById<TextView>(R.id.progress_text).text = message
            }
            dialog.setContentView(inflate)
            dialog.setCancelable(false)
            dialog.window!!.setBackgroundDrawable(
                ColorDrawable(Color.TRANSPARENT)
            )
            return dialog
        }
    }
}