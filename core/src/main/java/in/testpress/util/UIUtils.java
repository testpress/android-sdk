package in.testpress.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.ProgressBar;

import in.testpress.R;

public class UIUtils {

    public static void setIndeterminateDrawable(Context context, Object view, int width) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            float pixelWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, width, metrics);
            if (view instanceof ProgressDialog) {
                ((ProgressDialog) view).setIndeterminateDrawable(new CircularProgressDrawable(
                        ContextCompat.getColor(context, R.color.testpress_color_primary), pixelWidth));
            } else if (view instanceof ProgressBar) {
                ((ProgressBar) view).setIndeterminateDrawable(new CircularProgressDrawable(
                        ContextCompat.getColor(context, R.color.testpress_color_primary), pixelWidth));
            }
        }
    }

}
