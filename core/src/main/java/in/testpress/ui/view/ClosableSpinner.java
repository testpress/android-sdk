package in.testpress.ui.view;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.AppCompatSpinner;
import android.util.AttributeSet;

public class ClosableSpinner extends AppCompatSpinner {

    public ClosableSpinner(Context context) {
        super(context);
    }

    public ClosableSpinner(Context context, int mode) {
        super(context, mode);
    }

    public ClosableSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ClosableSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ClosableSpinner(Context context, AttributeSet attrs, int defStyleAttr, int mode) {
        super(context, attrs, defStyleAttr, mode);
    }

    public ClosableSpinner(Context context, AttributeSet attrs, int defStyleAttr, int mode, Resources.Theme popupTheme) {
        super(context, attrs, defStyleAttr, mode, popupTheme);
    }

    public void dismissPopUp() {
        onDetachedFromWindow();
    }
}
