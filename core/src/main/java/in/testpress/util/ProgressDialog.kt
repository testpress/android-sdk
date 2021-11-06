package `in`.testpress.util

import `in`.testpress.R
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog

class ProgressDialog {
    fun getAlertDialog(
        context: Context,
        layout: Int,
        setCancellationOnTouchOutside: Boolean
    ): AlertDialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val customLayout: View = inflater.inflate(layout, null)
        builder.setView(customLayout)
        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(setCancellationOnTouchOutside)
        return dialog
    }

    fun showProgressDialog(context: Context, message: String): AlertDialog {
        val dialog = getAlertDialog(context, R.layout.custom_progress_dialog, setCancellationOnTouchOutside = false)
        dialog.show()
        dialog.findViewById<TextView>(R.id.text_progress_bar)?.text = message
        return dialog
    }
}