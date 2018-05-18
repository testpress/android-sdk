package in.testpress.util;

import android.app.Activity;
import android.view.View;
import android.webkit.WebChromeClient;
import android.widget.FrameLayout;

public class FullScreenChromeClient extends WebChromeClient {

    private CustomViewCallback mFullScreenViewCallback;
    private View mCustomView;
    private int mOriginalOrientation;
    private int mOriginalSystemUiVisibility;
    private Activity activity;

    public FullScreenChromeClient(Activity activity) {
        this.activity = activity;
    }

    public boolean isFullScreen() {
        return mCustomView != null;
    }

    public void onHideCustomView()
    {
        ((FrameLayout) activity.getWindow().getDecorView()).removeView(mCustomView);
        mCustomView = null;
        activity.getWindow().getDecorView().setSystemUiVisibility(mOriginalSystemUiVisibility);
        activity.setRequestedOrientation(this.mOriginalOrientation);
        mFullScreenViewCallback.onCustomViewHidden();
        mFullScreenViewCallback = null;
    }

    public void onShowCustomView(View paramView,
                                 WebChromeClient.CustomViewCallback paramCustomViewCallback) {

        if (mCustomView != null)
        {
            onHideCustomView();
            return;
        }
        mCustomView = paramView;
        mOriginalSystemUiVisibility =
                activity.getWindow().getDecorView().getSystemUiVisibility();

        mOriginalOrientation = activity.getRequestedOrientation();
        mFullScreenViewCallback = paramCustomViewCallback;
        ((FrameLayout) activity.getWindow().getDecorView())
                .addView(mCustomView, new FrameLayout.LayoutParams(-1, -1));

        // https://stackoverflow.com/a/38799514/5134215
        activity.getWindow().getDecorView().setSystemUiVisibility(3846);
    }
}
