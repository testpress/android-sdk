package in.testpress.store.payu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.payu.india.Model.PayuConfig;
import com.payu.india.Payu.PayuConstants;

import in.testpress.core.TestpressSdk;
import in.testpress.store.R;
import in.testpress.ui.BaseToolBarActivity;

public class MakePaymentActivity extends BaseToolBarActivity {

    private ProgressBar progressBar;
    private AlertDialog.Builder builder;
    private WebView mWebView;
    private LinearLayout emptyView;
    private ImageView emptyViewImage;
    private TextView emptyTitleView;
    private TextView emptyDescView;

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testpress_activity_make_payment);
        emptyView = (LinearLayout) findViewById(R.id.empty_container);
        emptyViewImage = (ImageView) findViewById(R.id.image_view);
        emptyTitleView = (TextView) findViewById(R.id.empty_title);
        emptyDescView = (TextView) findViewById(R.id.empty_description);
        emptyTitleView.setTypeface(TestpressSdk.getRubikMediumFont(this));
        emptyDescView.setTypeface(TestpressSdk.getRubikRegularFont(this));
        Button retryButton = (Button) findViewById(R.id.retry_button);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadUrl();
            }
        });
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        builder = new AlertDialog.Builder(this, R.style.TestpressAppCompatAlertDialogStyle)
                .setTitle(R.string.testpress_are_you_sure)
                .setMessage(R.string.testpress_want_to_cancel)
                .setPositiveButton(R.string.testpress_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                })
                .setNegativeButton(R.string.testpress_no, null);

        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.getSettings().setSupportMultipleWindows(true);
        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                emptyView.setVisibility(View.GONE);
                mWebView.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
                mWebView.setVisibility(View.VISIBLE);
                super.onPageFinished(view, url);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request,
                                        WebResourceError error) {

                super.onReceivedError(view, request, error);
                setEmptyText(R.string.testpress_network_error,
                        R.string.testpress_no_internet_try_again,
                        R.drawable.testpress_no_wifi);
            }
        });
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.addJavascriptInterface(new PayU(this), "PayU");
        loadUrl();
    }

    void loadUrl() {
        Bundle bundle = getIntent().getExtras();
        //noinspection ConstantConditions
        PayuConfig payuConfig = bundle.getParcelable(PayuConstants.PAYU_CONFIG);
        //noinspection ConstantConditions
        String url = payuConfig.getEnvironment() == PayuConstants.PRODUCTION_ENV ?
                PayuConstants.PRODUCTION_PAYMENT_URL : PayuConstants.MOBILE_TEST_PAYMENT_URL;

        byte[] encodedData = payuConfig.getData().getBytes();

        mWebView.postUrl(url, encodedData);
    }

    public class PayU {
        Context mContext;
        Intent intent;

        PayU(Context c) {
            mContext = c;
            intent = new Intent();
        }

        @JavascriptInterface
        public void onSuccess(final String result) {
            intent.putExtra(Pay_U_Constants.PAYMENT_RESULT, result);
            setResult(RESULT_OK, intent);
            finish();
        }

        @JavascriptInterface
        public void onFailure(final String result) {
            intent.putExtra(Pay_U_Constants.PAYMENT_RESULT, result);
            setResult(RESULT_CANCELED, intent);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        builder.create().show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setEmptyText(int title, int description, int imageRes) {
        progressBar.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
        emptyTitleView.setText(title);
        emptyViewImage.setImageResource(imageRes);
        emptyDescView.setText(description);
    }
}
