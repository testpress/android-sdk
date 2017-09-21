package in.testpress.samples.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import in.testpress.samples.R;

public class ViewUtils {

    @SuppressLint("InflateParams")
    public static void showInputDialogBox(Activity activity, String title,
                                          final OnInputCompletedListener inputCompletedListener) {

        final View dialog = activity.getLayoutInflater().inflate(R.layout.edit_text_dialog_box, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity,
                R.style.TestpressAppCompatAlertDialogStyle);

        builder.setTitle(title);
        builder.setView(dialog);
        final EditText editText = (EditText) dialog.findViewById(R.id.edit_text);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String inputText = editText.getText().toString();
                if (inputText.trim().isEmpty()) {
                    return;
                }
                inputCompletedListener.onInputComplete(inputText);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }

    public interface OnInputCompletedListener {
        void onInputComplete(String inputText);
    }

    public static void toast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

}
