package in.testpress.course.ui.fragments.content_detail_fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonObject;

import in.testpress.core.TestpressCallback;
import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSDKDatabase;
import in.testpress.core.TestpressSdk;
import in.testpress.core.TestpressSession;
import in.testpress.core.TestpressUserDetails;
import in.testpress.course.R;
import in.testpress.course.util.ExoPlayerUtil;
import in.testpress.course.util.ExoplayerFullscreenHelper;
import in.testpress.models.ProfileDetails;
import in.testpress.models.greendao.Content;
import in.testpress.models.greendao.CourseAttempt;
import in.testpress.models.greendao.Video;
import in.testpress.models.greendao.VideoAttempt;
import in.testpress.models.greendao.VideoDao;
import in.testpress.util.FullScreenChromeClient;
import in.testpress.util.WebViewUtils;

import static in.testpress.course.network.TestpressCourseApiClient.EMBED_CODE;
import static in.testpress.course.network.TestpressCourseApiClient.EMBED_DOMAIN_RESTRICTED_VIDEO_PATH;

public class VideoContentFragment extends BaseContentDetailFragment {

    private VideoDao videoDao;
    private FrameLayout exoPlayerMainFrame;
    private TextView titleView;
    private LinearLayout titleLayout;
    private FullScreenChromeClient fullScreenChromeClient;
    private WebViewUtils webViewUtils;
    private WebView webView;
    public ExoplayerFullscreenHelper exoplayerFullscreenHelper;
    private VideoAttempt videoAttempt;
    private ExoPlayerUtil exoPlayerUtil;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        videoDao = TestpressSDKDatabase.getVideoDao(getActivity());
        fullScreenChromeClient = new FullScreenChromeClient(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.video_content_detail, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        exoPlayerMainFrame = view.findViewById(R.id.exo_player_main_frame);
        titleView = (TextView) view.findViewById(R.id.title);
        titleLayout = (LinearLayout) view.findViewById(R.id.title_layout);
        webView = (WebView) view.findViewById(R.id.web_view);


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
    void loadContent() {
        titleView.setText(content.getTitle());
        titleLayout.setVisibility(View.VISIBLE);

        Video video = content.getRawVideo();
        if (video == null) {
            updateContent();
            return;
        }

        if (video.getIsDomainRestricted()) {
            loadDomainRestrictedVideo(video);
        } else if (content.isEmbeddableVideo()) {
            loadEmbeddableVideo(video);
        } else {
            loadNativeVideo(video);
        }
    }

    private void loadDomainRestrictedVideo(Video video) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(EMBED_CODE, video.getEmbedCode());
        String url = courseApiClient.getBaseUrl()+ EMBED_DOMAIN_RESTRICTED_VIDEO_PATH;
        webViewUtils.initWebViewAndPostUrl(url, jsonObject.toString(), getActivity());
        webView.setWebChromeClient(fullScreenChromeClient);
    }

    private void loadEmbeddableVideo(Video video) {
        String html = "<div style='margin-top: 15px; padding-left: 20px; padding-right: 20px;'" +
                "class='videoWrapper'>" + video.getEmbedCode() + "</div>";

        webViewUtils.initWebView(html, getActivity());
        webView.setWebChromeClient(fullScreenChromeClient);
    }

    private void loadNativeVideo(Video video) {
        TestpressSession session = TestpressSdk.getTestpressSession(getActivity());
        if (session != null && session.getInstituteSettings().isDisplayUserEmailOnVideo()) {
            checkProfileDetailExist(video.getHlsUrl());
        } else {
            initExoPlayer(video.getHlsUrl());
        }
    }


    private void checkProfileDetailExist(final String videoUrl) {
        ProfileDetails profileDetails = TestpressUserDetails.getInstance().getProfileDetails();
        if (profileDetails != null) {
            initExoPlayer(videoUrl);
        } else {
            showLoadingProgress();
            TestpressUserDetails.getInstance()
                    .load(getActivity(), new TestpressCallback<ProfileDetails>() {
                        @Override
                        public void onSuccess(ProfileDetails userDetails) {
                            swipeRefresh.setRefreshing(false);
                            initExoPlayer(videoUrl);
                        }

                        @Override
                        public void onException(TestpressException exception) {
                            handleError(exception, false);
                        }
                    });
        }
    }

    private void initExoPlayer(String videoUrl) {
        if (exoplayerFullscreenHelper == null) {
            exoplayerFullscreenHelper = new ExoplayerFullscreenHelper(getActivity());
            exoplayerFullscreenHelper.initializeOrientationListener();
        }

        if (videoAttempt == null) {
            createContentAttempt();
        } else {
            float startPosition;
            try {
                startPosition = Float.parseFloat(videoAttempt.getLastPosition());
            } catch (NumberFormatException e) {
                startPosition = 0;
            }
            exoPlayerUtil = new ExoPlayerUtil(getActivity(), exoPlayerMainFrame, videoUrl, startPosition);
            exoPlayerUtil.setVideoAttemptParameters(videoAttempt.getId(), content);
            exoPlayerMainFrame.setVisibility(View.VISIBLE);
            exoPlayerUtil.initializePlayer();
            exoplayerFullscreenHelper.setExoplayerUtil(exoPlayerUtil);

        }
    }

    @Override
    void onUpdateContent(Content fetchedContent) {
        Video video = fetchedContent.getRawVideo();
        if (video != null) {
            videoDao.insertOrReplace(video);
            fetchedContent.setVideoId(video.getId());
        }
        contentDao.insertOrReplace(fetchedContent);
        content = fetchedContent;
    }

    @Override
    void onCreateContentAttempt(CourseAttempt courseAttempt) {
        swipeRefresh.setRefreshing(false);
        videoAttempt = courseAttempt.getRawVideoAttempt();
        initExoPlayer(content.getRawVideo().getHlsUrl());
    }

    @Override
    void hideContents() {
        webView.setVisibility(View.GONE);
    }
}
