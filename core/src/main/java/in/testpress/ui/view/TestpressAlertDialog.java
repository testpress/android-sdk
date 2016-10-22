package in.testpress.ui.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.support.v7.app.AlertDialog;
import android.widget.TextView;

import in.testpress.R;
import in.testpress.core.TestpressSdk;

public class TestpressAlertDialog extends AlertDialog.Builder {

    public TestpressAlertDialog(@NonNull Context context) {
        super(context, R.style.TestpressAppCompatAlertDialogStyle);
    }

    public TestpressAlertDialog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
    }

    @Override
    public AlertDialog show() {
        AlertDialog alertDialog = super.show();
        //noinspection ConstantConditions
        TextView messageView = (TextView) alertDialog.getWindow().findViewById(android.R.id.message);
        messageView.setTypeface(TestpressSdk.getTestpressFont(getContext()).getTypeface(getContext()));
        return alertDialog;
    }
}
