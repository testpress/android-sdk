package in.testpress.course.ui.fragments.content_detail_fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;

import in.testpress.core.TestpressSDKDatabase;
import in.testpress.core.TestpressSdk;
import in.testpress.course.R;
import in.testpress.models.greendao.Content;
import in.testpress.models.greendao.CourseAttempt;
import in.testpress.models.greendao.HtmlContentDao;
import in.testpress.util.ViewUtils;
import in.testpress.util.WebViewUtils;

import static in.testpress.course.network.TestpressCourseApiClient.EMBED_DOMAIN_RESTRICTED_VIDEO_PATH;

public class HtmlContentFragment extends BaseContentDetailFragment {

    private HtmlContentDao htmlContentDao;
    private WebView webView;
    private WebViewUtils webViewUtils;
    private TextView titleView;
    private LinearLayout titleLayout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        htmlContentDao = TestpressSDKDatabase.getHtmlContentDao(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.html_content_detail, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        webView = (WebView) view.findViewById(R.id.web_view);
        titleView = (TextView) view.findViewById(R.id.title);
        titleLayout = (LinearLayout) view.findViewById(R.id.title_layout);

        ViewUtils.setTypeface(
                new TextView[] {titleView},
                TestpressSdk.getRubikMediumFont(getActivity())
        );

        webViewUtils = new WebViewUtils(webView) {
            @Override
            protected void onPageStarted() {
                super.onPageStarted();
                swipeRefresh.setRefreshing(true);
                emptyContainer.setVisibility(View.GONE);
                swipeRefresh.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onLoadFinished() {
                super.onLoadFinished();
                swipeRefresh.setRefreshing(false);
                webView.setVisibility(View.VISIBLE);
                createContentAttempt();
            }

            @Override
            public String getHeader() {
                return super.getHeader() + getBookmarkHandlerScript();
            }

            @Override
            protected void onNetworkError() {
                setEmptyText(R.string.testpress_network_error,
                        R.string.testpress_no_internet_try_again,
                        R.drawable.ic_error_outline_black_18dp);

                retryButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateContent();
                    }
                });
            }

            @Override
            protected boolean shouldOverrideUrlLoading(Activity activity, String url) {
                if (url.contains(EMBED_DOMAIN_RESTRICTED_VIDEO_PATH)) {
                    return false;
                }
                return super.shouldOverrideUrlLoading(activity, url);
            }
        };
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        if (content != null) {
            loadContent();
        }
    }

    @Override
    void hideContents() {
        webView.setVisibility(View.GONE);
    }

    @Override
    void onCreateContentAttempt(CourseAttempt courseAttempt) {}

    @Override
    void loadContent() {
        titleView.setText(content.getTitle());
        titleLayout.setVisibility(View.VISIBLE);

        if (content.getRawHtmlContent() == null) {
            updateContent();
            return;
        }

        String html = "<div style='padding-left: 20px; padding-right: 20px;'>" +
                content.getRawHtmlContent().getTextHtml() + "</div>";
        webViewUtils.initWebView(html, getActivity());    }

    @Override
    void onUpdateContent(Content fetchedContent) {
        htmlContentDao.insertOrReplaceInTx(fetchedContent.getRawHtmlContent());
        contentDao.insertOrReplaceInTx(fetchedContent);
        content = fetchedContent;
        loadContent();
    }
}
