package in.testpress.util;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.Toast;

import in.testpress.WebViewConstants;

public class FullScreenChromeClient extends WebChromeClient {

    private CustomViewCallback mFullScreenViewCallback;
    private View mCustomView;
    private int mOriginalOrientation;
    private int mOriginalSystemUiVisibility;
    private Activity activity;
    private ValueCallback<Uri[]> filePathCallback;

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

    @Override
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
        if (this.filePathCallback != null) {
            this.filePathCallback.onReceiveValue(null);
            this.filePathCallback = null;
        }

        this.filePathCallback = filePathCallback;
        try {
            Intent intent = fileChooserParams.createIntent();
            activity.startActivityForResult(intent, WebViewConstants.REQUEST_SELECT_FILE);
        } catch (ActivityNotFoundException e) {
            this.filePathCallback = null;
            Toast.makeText(activity.getApplicationContext(), "Cannot open file chooser", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    public void SelectFile(int resultCode, Intent data){
        if (this.filePathCallback == null) {
            return;
        }
        this.filePathCallback.onReceiveValue(FileChooserParams.parseResult(resultCode, data));
        this.filePathCallback = null;
    }
}
