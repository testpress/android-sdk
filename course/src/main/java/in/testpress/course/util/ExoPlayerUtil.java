package in.testpress.course.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaCodec;
import android.os.Build;
import android.os.Handler;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.mediarouter.media.MediaControlIntent;
import androidx.mediarouter.media.MediaRouteSelector;
import androidx.mediarouter.media.MediaRouter;
import androidx.mediarouter.media.MediaRouter.RouteInfo;
import com.airbnb.lottie.LottieAnimationView;
import com.github.vkay94.dtpv.DoubleTapPlayerView;
import com.github.vkay94.dtpv.youtube.YouTubeOverlay;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.drm.DefaultDrmSessionManager;
import com.google.android.exoplayer2.drm.DrmSession;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.DrmSessionManagerProvider;
import com.google.android.exoplayer2.drm.MediaDrmCallbackException;
import com.google.android.exoplayer2.offline.DownloadRequest;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.source.MediaSourceFactory;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.ExoTrackSelection;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.util.Log;
import com.google.android.exoplayer2.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.testpress.core.TestpressCallback;
import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSdk;
import in.testpress.core.TestpressSession;
import in.testpress.core.TestpressUserDetails;
import in.testpress.course.R;
import in.testpress.course.api.TestpressCourseApiClient;
import in.testpress.course.fragments.LiveStreamCallbackListener;
import in.testpress.course.helpers.CustomHttpDrmMediaCallback;
import in.testpress.course.helpers.DownloadTask;
import in.testpress.course.helpers.VideoDownload;
import in.testpress.course.repository.VideoWatchDataRepository;
import in.testpress.database.OfflineVideoDao;
import in.testpress.database.TestpressDatabase;
import in.testpress.models.ProfileDetails;
import in.testpress.models.greendao.Content;
import in.testpress.models.greendao.VideoAttempt;
import in.testpress.ui.ExploreSpinnerAdapter;
import in.testpress.util.CommonUtils;
import in.testpress.util.InternetConnectivityChecker;
import io.sentry.Scope;
import io.sentry.ScopeCallback;
import io.sentry.Sentry;
import kotlin.Pair;

import static android.view.WindowManager.LayoutParams.FLAG_SECURE;
import static androidx.mediarouter.media.MediaRouter.RouteInfo.CONNECTION_STATE_CONNECTED;
import static com.google.android.exoplayer2.ExoPlaybackException.TYPE_SOURCE;
import static in.testpress.course.api.TestpressCourseApiClient.LAST_POSITION;
import static in.testpress.course.api.TestpressCourseApiClient.TIME_RANGES;

import org.greenrobot.greendao.annotation.NotNull;

public class ExoPlayerUtil implements VideoTimeRangeListener, DrmSessionManagerProvider {
    private static final int SEEK_TIME_IN_MILLISECOND = 15000; //15s

    private FrameLayout exoPlayerMainFrame;
    private View exoPlayerLayout;
    private DoubleTapPlayerView playerView;
    private LottieAnimationView progressBar;
    private TextView errorMessageTextView;
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    public ExoPlayer player;
    private ImageView fullscreenIcon;
    private Dialog fullscreenDialog;
    private TrackSelectionDialog trackSelectionDialog;
    private YouTubeOverlay youtubeOverlay;
    private LinearLayout noticeScreen;
    private TextView noticeMessage;
    List<String[]> watchedTimeRanges = new ArrayList<>();


    private Activity activity;
    private long videoAttemptId = -1;
    private Content content;
    private String url;
    private boolean isopenFullscreenDialogCalled;
    private boolean iscloseFullscreenDialogCalled;
    private float startPosition;
    private boolean playWhenReady = true;
    boolean isPreparing = false;
    private float speedRate = 1;
    private Spinner speedRateSpinner;
    private ExploreSpinnerAdapter speedSpinnerAdapter;
    private BroadcastReceiver usbConnectionStateReceiver;
    private MediaRouter mediaRouter;
    private MediaRouteSelector mediaRouteSelector;
    private MediaRouter.Callback mediaRouterCallback;
    private boolean fullscreen = false;
    private int drmLicenseRetries = 0;

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    public AudioManager audioManager;
    AudioManager.OnAudioFocusChangeListener audioFocusChangeListener;
    private DefaultTrackSelector trackSelector;
    private DialogInterface.OnClickListener dialogOnClickListener;
    private VideoWatchDataRepository videoWatchDataRepository;
    private ScaleGestureDetector scaleGestureDetector;
    private PinchToZoomGesture pinchToZoomGesture;
    private LiveStreamCallbackListener liveStreamCallbackListener;
    boolean isDynamic = false;
    private TestpressSession session;
    private boolean firstSeekCalled = false;
    private final Handler seekHandler = new Handler();
    private long lastApiCallTime = System.currentTimeMillis() / 1000;
    long throttleTimeRemaining = 0;
    private ProfileDetails profileDetails = null;

    public ExoPlayerUtil(Activity activity, FrameLayout exoPlayerMainFrame, String url,
                         float startPosition, LiveStreamCallbackListener liveStreamCallbackListener) {
        this(activity, exoPlayerMainFrame, url, startPosition);
        this.liveStreamCallbackListener = liveStreamCallbackListener;
    }

    public ExoPlayerUtil(Activity activity, FrameLayout exoPlayerMainFrame, String url,
                         float startPosition) {

        this.activity = activity;
        this.exoPlayerMainFrame = exoPlayerMainFrame;
        this.url = url;
        this.startPosition = startPosition;
        session = TestpressSdk.getTestpressSession(activity);

        initializeViews();
        exoPlayerLayout = exoPlayerMainFrame.findViewById(R.id.exo_player_layout);
        playerView = exoPlayerMainFrame.findViewById(R.id.exo_player_view);
        fullscreenIcon = exoPlayerMainFrame.findViewById(R.id.exo_fullscreen_icon);
        progressBar = exoPlayerMainFrame.findViewById(R.id.exo_player_progress);
        errorMessageTextView = exoPlayerMainFrame.findViewById(R.id.error_message);
        speedRateSpinner = exoPlayerMainFrame.findViewById(R.id.exo_speed_rate_spinner);
        String[] speedValues = activity.getResources().getStringArray(R.array.exo_speed_values);
        speedSpinnerAdapter =
                new ExploreSpinnerAdapter(activity.getLayoutInflater(), activity.getResources(), false);

        speedSpinnerAdapter.setLayoutId(R.layout.testpress_exo_player_current_speed);
        for (String speedValue : speedValues) {
            speedSpinnerAdapter.addItem(speedValue, speedValue + "x", true, 0);
        }
        speedRateSpinner.setAdapter(speedSpinnerAdapter);
        speedRateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ExoPlayerUtil.this.speedRate = Float.parseFloat(speedSpinnerAdapter.getTag(position));
                if (player != null) {
                    player.setPlaybackParameters(new PlaybackParameters(ExoPlayerUtil.this.speedRate));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        if (session != null && session.getInstituteSettings().isScreenshotDisabled()) {
            initScreenRecordTrackers();
        }
        setSpeedRate(1);

        ExoTrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory();
        trackSelector =  new DefaultTrackSelector(activity, videoTrackSelectionFactory);
        initFullscreenDialog();
        initResolutionSelector();
        initializePinchToZoom();

        if(isLandscapeModeEnabled()){
            openFullscreenDialog();
        }
        preventScreenshot();
        hideLiveStreamNotStartedScreen();
        initWatermarkOverlay();
    }

    private void preventScreenshot() {
        TestpressSession session = TestpressSdk.getTestpressSession(activity);
        if (session != null && session.getInstituteSettings().isScreenshotDisabled()) {
            activity.getWindow().setFlags(FLAG_SECURE, FLAG_SECURE);
        }
    }

    private boolean isLandscapeModeEnabled() {
        return activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    public ExoPlayerUtil(Activity activity, FrameLayout exoPlayerMainFrame, String url,
                         float startPosition, boolean playWhenReady, float speedRate) {

        this(activity, exoPlayerMainFrame, url, startPosition);
        this.playWhenReady = playWhenReady;
        setSpeedRate(speedRate);
    }

    private void initializeViews() {
        youtubeOverlay = activity.findViewById(R.id.youtube_overlay);
        noticeScreen = exoPlayerMainFrame.findViewById(R.id.notice_screen);
        noticeMessage = exoPlayerMainFrame.findViewById(R.id.notice_message);
    }

    private void initWatermarkOverlay() {
        if (session == null || session.getInstituteSettings().getVideoWatermarkType() == null) {
            return;
        }

        String watermarkType = session.getInstituteSettings().getVideoWatermarkType();
        isDynamic = watermarkType.equals("Dynamic");
        if (!watermarkType.equals("Dynamic") && !watermarkType.equals("Static")) {
            return;
        }

        profileDetails = TestpressUserDetails.getInstance().getProfileDetails();

        if (profileDetails == null) {
            fetchProfileDetails();
        } else {
            addWatermarkOverlay(profileDetails);
        }
    }

    private void fetchProfileDetails() {
        TestpressUserDetails.getInstance().load(activity, new TestpressCallback<ProfileDetails>() {
            @Override
            public void onSuccess(ProfileDetails userDetails) {
                profileDetails = userDetails;
                addWatermarkOverlay(userDetails);
            }

            @Override
            public void onException(TestpressException exception) {
                Log.e("Watermark", "Failed to load user details", exception);
            }
        });
    }

    private void addWatermarkOverlay(ProfileDetails profileDetails) {
        String overlayText = (profileDetails.getUsername() != null && !profileDetails.getUsername().isEmpty())
                ? profileDetails.getUsername()
                : profileDetails.getEmail();

        FrameLayout parentLayout = getOrCreateParentLayout();
        WatermarkOverlay watermark = createWatermark(overlayText);

        parentLayout.addView(watermark);
    }

    private FrameLayout getOrCreateParentLayout() {
        FrameLayout parent;

        if (exoPlayerLayout.getParent() instanceof FrameLayout) {
            Log.d("TAG", "Using existing parent layout");
            parent = (FrameLayout) exoPlayerLayout.getParent();
        } else {
            Log.d("TAG", "Creating new parent layout");
            parent = new FrameLayout(activity);
            ViewGroup.LayoutParams layoutParams = exoPlayerLayout.getLayoutParams();
            parent.setLayoutParams(layoutParams);

            exoPlayerLayout.setLayoutParams(new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            ));

            parent.addView(exoPlayerLayout);
        }

        return parent;
    }

    private WatermarkOverlay createWatermark(String text) {
        WatermarkOverlay watermark = new WatermarkOverlay(activity);
        watermark.setWatermark(text);

        if (isDynamic) {
            watermark.setDynamicWatermark();
        } else {
            String position = getVideoWatermarkPosition();
            watermark.setStaticWatermark(position);
        }
        return watermark;
    }

    private String getVideoWatermarkPosition() {
        return session.getInstituteSettings().getVideoWatermarkPosition() != null
                ? session.getInstituteSettings().getVideoWatermarkPosition()
                : "top-right";
    }

    private void initFullscreenDialog() {
        fullscreenDialog = new Dialog(activity, android.R.style.Theme_Black_NoTitleBar_Fullscreen) {
            public void onBackPressed() {
                if (fullscreen) {
                    closeFullscreenDialog();
                }
                super.onBackPressed();
            }
        };
        FrameLayout fullScreenButton = playerView.findViewById(R.id.exo_fullscreen_button);
        fullScreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!fullscreen) {
                    openFullscreenDialog();
                } else {
                    closeFullscreenDialog();
                }
            }
        });
    }

    public void seekTo(Long milliSeconds) {
        if (player != null){
            player.seekTo(milliSeconds);
        }
    }

    private void initResolutionSelector() {
        FrameLayout resolutionButton = playerView.findViewById(R.id.exo_resolution_button);
        resolutionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();

                if (mappedTrackInfo != null) {
                    int rendererIndex = C.TRACK_TYPE_DEFAULT;
                    int rendererType = mappedTrackInfo.getRendererType(rendererIndex);
                    boolean allowAdaptiveSelections =
                            rendererType == C.TRACK_TYPE_DEFAULT
                                    || (rendererType == C.TRACK_TYPE_AUDIO
                                    && mappedTrackInfo.getTypeSupport(C.TRACK_TYPE_VIDEO)
                                    == MappingTrackSelector.MappedTrackInfo.RENDERER_SUPPORT_NO_TRACKS);

                    trackSelectionDialog = new TrackSelectionDialog(trackSelector);
                    trackSelectionDialog.setOnClickListener(trackSelectionListener());
                    trackSelectionDialog.show(((AppCompatActivity)activity).getSupportFragmentManager(), null);
                }
            }
        });
    }

    public void initializePlayer() {
        if (url == null || url.isEmpty()) {
            progressBar.setVisibility(View.GONE);
            liveStreamCallbackListener.onUrlNullOrEmpty();
            showLiveStreamNotStartedScreen();
            return;
        }
        if (player == null) {
            progressBar.setVisibility(View.VISIBLE);
            buildPlayer();
            initializeDoubleClickOverlay();
        }
        preparePlayer();
        player.seekTo(getStartPositionInMilliSeconds());
        registerListeners();
    }

    private void showLiveStreamNotStartedScreen() {
        noticeScreen.setVisibility(View.VISIBLE);
        noticeMessage.setText(R.string.waiting_for_live_stream_desc);
    }

    private void hideLiveStreamNotStartedScreen() {
        noticeScreen.setVisibility(View.GONE);
    }

    private void buildPlayer() {
        MediaSourceFactory mediaSourceFactory = new DefaultMediaSourceFactory(new ExoPlayerDataSourceFactory(activity).build());
        DownloadTask downloadTask = new DownloadTask(url, activity);
        MediaItem mediaItem = getMediaItem(downloadTask.isDownloaded());
        if (!downloadTask.isDownloaded()) {
            mediaSourceFactory.setDrmSessionManagerProvider(this);
        }

        DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(activity);
        renderersFactory.setEnableDecoderFallback(true);

        player = new ExoPlayer.Builder(activity, renderersFactory)
                .setMediaSourceFactory(mediaSourceFactory)
                .setSeekForwardIncrementMs(SEEK_TIME_IN_MILLISECOND)
                .setSeekBackIncrementMs(SEEK_TIME_IN_MILLISECOND)
                .setTrackSelector(trackSelector).build();

        player.addListener(new PlayerEventListener());
        player.addAnalyticsListener(new ExoplayerAnalyticsListener(this));
        player.setAudioAttributes(AudioAttributes.DEFAULT,true);
        playerView.setPlayer(player);
        player.setPlayWhenReady(playWhenReady);
        player.setPlaybackParameters(new PlaybackParameters(speedRate));
        player.setMediaItem(mediaItem);
        youtubeOverlay.player(player);
        playerView.controller(youtubeOverlay);
    }

    public MediaItem getMediaItem(boolean isDownloaded) {
        MediaItem mediaItem = new MediaItem.Builder()
                .setUri(url)
                .setDrmUuid(C.WIDEVINE_UUID)
                .setDrmMultiSession(true).build();

        DownloadRequest downloadRequest = VideoDownload.getDownloadRequest(url, activity);
        if (isDownloaded && downloadRequest != null) {
            MediaItem.Builder builder = mediaItem.buildUpon();
            builder
                    .setMediaId(downloadRequest.id)
                    .setUri(downloadRequest.uri)
                    .setCustomCacheKey(downloadRequest.customCacheKey)
                    .setMimeType(downloadRequest.mimeType)
                    .setStreamKeys(downloadRequest.streamKeys)
                    .setDrmKeySetId(downloadRequest.keySetId);
            mediaItem = builder.build();
        }
        return mediaItem;
    }

    private long getStartPositionInMilliSeconds() {
        return (long)(startPosition * 1000);
    }

    private void initializeDoubleClickOverlay() {
        youtubeOverlay.playerView(playerView)
                .performListener(new YouTubeOverlay.PerformListener() {
                    @Override
                    public void onAnimationStart() {
                        youtubeOverlay.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd() {
                        youtubeOverlay.setVisibility(View.GONE);
                    }
                });
    }

    private void initializePinchToZoom() {
        pinchToZoomGesture = new PinchToZoomGesture(activity,exoPlayerMainFrame);
        scaleGestureDetector = new ScaleGestureDetector(activity, pinchToZoomGesture);
    }

    private void preparePlayer() {
        isPreparing = true;
        player.prepare();
    }

    private void registerListeners() {
        registerUsbConnectionStateReceiver();
    }

    private void registerUsbConnectionStateReceiver() {
        if (usbConnectionStateReceiver != null) {
            IntentFilter filter = new IntentFilter("android.hardware.usb.action.USB_STATE");
            activity.registerReceiver(usbConnectionStateReceiver, filter);
            mediaRouter.addCallback(mediaRouteSelector, mediaRouterCallback,
                    MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN);
        }
    }

    private DialogInterface.OnClickListener trackSelectionListener() {
        if (dialogOnClickListener == null) {
            dialogOnClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
                    int rendererIndex = getRendererIndex(C.TRACK_TYPE_VIDEO, mappedTrackInfo);
                    DefaultTrackSelector.ParametersBuilder parametersBuilder = trackSelector.buildUponParameters();
                    if (trackSelectionDialog.getOverrides().isEmpty()) {
                        parametersBuilder.clearSelectionOverrides(rendererIndex);
                        trackSelector.setParameters(parametersBuilder.build());
                    } else {
                        parametersBuilder.clearSelectionOverrides(rendererIndex)
                                .setSelectionOverride(rendererIndex, mappedTrackInfo.getTrackGroups(rendererIndex), trackSelectionDialog.getOverrides().get(0));
                        trackSelector.setParameters(parametersBuilder.build());
                    }
                }
            };
        }

        return dialogOnClickListener;
    }

    public void releasePlayer() {
        if (audioManager != null) {
            audioManager.abandonAudioFocus(audioFocusChangeListener);
        }
        if (player != null) {
            startPosition = getCurrentPosition();
            playWhenReady = player.getPlayWhenReady();
            player.release();
            player = null;
        }
        if (usbConnectionStateReceiver != null) {
            try {
                activity.unregisterReceiver(usbConnectionStateReceiver);
            } catch (IllegalArgumentException exception) {
                Log.i("ExoplayerUtil", "unregisterReceiver: "+ exception.getMessage());
            }
            mediaRouter.removeCallback(mediaRouterCallback);
        }
    }

    private void initScreenRecordTrackers() {
        usbConnectionStateReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                boolean connected = intent.getExtras() != null &&
                        intent.getExtras().getBoolean("connected");

                if (connected) {
                    displayError(R.string.testpress_usb_connected);
                } else {
                    hideError(R.string.testpress_usb_connected);
                }
            }
        };
        mediaRouter = MediaRouter.getInstance(activity);
        mediaRouterCallback = new MediaRouter.Callback(){};
        mediaRouteSelector = new MediaRouteSelector.Builder()
                .addControlCategory(MediaControlIntent.CATEGORY_LIVE_VIDEO)
                .build();
    }

    public float getCurrentPosition() {
        if (player == null) return 0;
        // Convert milliseconds to seconds
        return ((float) Math.max(0, player.getContentPosition())) / 1000;
    }

    public void setStartPosition(float startPosition) {
        this.startPosition = startPosition;
    }

    public void setSpeedRate(float speedRate) {
        this.speedRate = speedRate;

        int itemPosition = speedSpinnerAdapter.getItemPositionFromTag(
                String.valueOf(speedRate).replace(".0", ""));

        speedRateSpinner.setSelection(itemPosition);
    }

    public void setContent(Content content) {
        this.content = content;
    }

    public void setVideoAttemptParameters(long videoAttemptId, Content content) {
        this.videoAttemptId = videoAttemptId;
        this.content = content;
        OfflineVideoDao offlineVideoDao = TestpressDatabase.Companion.invoke(activity).offlineVideoDao();
        videoWatchDataRepository = new VideoWatchDataRepository(activity, offlineVideoDao);
    }

    public void onStart() {
        if (Util.SDK_INT > 23) {
            initializePlayer();
        }
    }

    public void onResume() {
        if ((Util.SDK_INT <= 23 || player == null)) {
            initializePlayer();
        }
    }

    public void onPause() {
        onTimeRangeChange((long)startPosition, (long)getCurrentPosition());
        if (Util.SDK_INT <= 23) {
            updateVideoAttempt();
            releasePlayer();
        }
    }

    public void onStop() {
        onTimeRangeChange((long)startPosition, (long)getCurrentPosition());
        if (Util.SDK_INT > 23) {
            updateVideoAttempt();
            releasePlayer();
        }
    }

    private void openFullscreenDialog() {

        if (!isopenFullscreenDialogCalled) {
            iscloseFullscreenDialogCalled = false;
            isopenFullscreenDialogCalled = true;
            addPlayerLayoutToDialog();
            changeOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            setFullscreenIcon(R.drawable.testpress_fullscreen_exit);
            hideSystemBars();
            addPinchToZoom();
            initWatermarkOverlay();
        }
    }

    private void addPinchToZoom() {
        pinchToZoomGesture.resetPinchToZoomGesture(PinchToZoomGesture.ZoomMode.ORIGINAL);
        playerView.setOnTouchListener(new VideoTouchDragHandler(playerView,pinchToZoomGesture,scaleGestureDetector));
        activity.findViewById(R.id.blank_layout).setVisibility(View.VISIBLE);
    }

    private void addPlayerLayoutToDialog() {
        exoPlayerMainFrame.removeView(exoPlayerLayout);
        fullscreenDialog.addContentView(exoPlayerLayout, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        fullscreenDialog.getWindow().addFlags(FLAG_SECURE);
        fullscreenDialog.show();
    }

    private void changeOrientation(int orientation) {
        activity.setRequestedOrientation(orientation);
        fullscreen = orientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
    }

    private void hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(fullscreenDialog.getWindow(), false);
        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(fullscreenDialog.getWindow(), playerView);
        controller.hide(WindowInsetsCompat.Type.systemBars());
        controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
    }

    private void closeFullscreenDialog() {

        if (!iscloseFullscreenDialogCalled) {
            isopenFullscreenDialogCalled = false;
            iscloseFullscreenDialogCalled = true;
            changeOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
            removePlayerViewFromDialog();
            setFullscreenIcon(R.drawable.testpress_fullscreen);
            removePinchToZoom();
            initWatermarkOverlay();
        }
    }

    private void removePinchToZoom() {
        pinchToZoomGesture.resetPinchToZoomGesture(PinchToZoomGesture.ZoomMode.ORIGINAL);
        playerView.setOnTouchListener(null);
        activity.findViewById(R.id.blank_layout).setVisibility(View.GONE);
    }

    private void removePlayerViewFromDialog() {
        ((ViewGroup) exoPlayerLayout.getParent()).removeView(exoPlayerLayout);
        exoPlayerMainFrame.addView(exoPlayerLayout);
        fullscreenDialog.dismiss();
    }

    private void setFullscreenIcon(@DrawableRes int imageResId) {
        fullscreenIcon.setImageDrawable(ContextCompat.getDrawable(activity, imageResId));
    }

    Map<String, Object> getVideoAttemptParameters() {
        Map<String, Object> parameters = new HashMap<>();
        float currentPosition = getCurrentPosition();
        parameters.put(LAST_POSITION, currentPosition);
        parameters.put(TIME_RANGES, watchedTimeRanges);
        return parameters;
    }

    public void updateVideoAttempt() {
        android.util.Log.d("TAG", "updateVideoAttempt: "+throttleTimeRemaining);
        long currentTime = System.currentTimeMillis() / 1000;  // Current time in seconds
        if (currentTime - lastApiCallTime < throttleTimeRemaining) {
            // If throttling is still in effect, don't make the API call
            return;
        }
        if (content.getContentType().equals("Live Stream") && content.getVideo() == null){
            return;
        }

        if (videoAttemptId == -1 && videoWatchDataRepository != null) {
            videoWatchDataRepository.save(content, getVideoAttemptParameters());
            return;
        }

        Map<String, Object> parameters = getVideoAttemptParameters();
        new TestpressCourseApiClient(activity).updateVideoAttempt(videoAttemptId, parameters)
                .enqueue(new TestpressCallback<VideoAttempt>() {
                    @Override
                    public void onSuccess(VideoAttempt videoAttempt) {
                        if (videoAttempt != null) {
                            updateVideoWatchedPercentage(videoAttempt);
                        }
                        lastApiCallTime = System.currentTimeMillis() / 1000;
                        throttleTimeRemaining = 0;
                    }

                    @Override
                    public void onException(TestpressException exception) {
                        if (videoWatchDataRepository != null) {
                            videoWatchDataRepository.save(content, getVideoAttemptParameters());
                        }
                        if (exception.isTooManyRequest()){
                            throttleTimeRemaining = exception.getThrottleTime();
                        } else {
                            throttleTimeRemaining = 60;
                        }
                        lastApiCallTime = System.currentTimeMillis() / 1000;
                    }
                });
    }

    void updateVideoWatchedPercentage(VideoAttempt videoAttempt) {
        long totalDuration = (long)Double.parseDouble(videoAttempt.getRawVideoContent().getDuration());;
        if (totalDuration == 0) {
            return;
        }
        long watchedDuration = (long)
                (Float.parseFloat(videoAttempt.getWatchedDuration()) * 1000);

        int watchedPercentage =
                (int) (((watchedDuration * 100) / totalDuration) / 1000);

        content.setVideoWatchedPercentage(watchedPercentage);
        content.update();
    }

    private void displayError(@StringRes int message) {
        player.setPlayWhenReady(false);
        player.getPlaybackState();
        errorMessageTextView.setText(message);
        errorMessageTextView.setVisibility(View.VISIBLE);
    }

    private void displayError(String message,int errorCode) {
        player.setPlayWhenReady(false);
        player.getPlaybackState();
        if (errorCode == 4001 || errorCode == 4003){
            setHtmlText(errorMessageTextView, message);
        } else {
            errorMessageTextView.setText(message);
        }
        errorMessageTextView.setVisibility(View.VISIBLE);
    }

    public void setHtmlText(TextView textView, String message) {
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            textView.setText(Html.fromHtml(message, Html.FROM_HTML_MODE_LEGACY));
        } else {
            textView.setText(Html.fromHtml(message));
        }
    }

    private void hideError(@StringRes int message) {
        if (errorMessageTextView.getText().equals(activity.getString(message))) {
            errorMessageTextView.setVisibility(View.GONE);
            player.setPlayWhenReady(true);
            player.getPlaybackState();
        }
    }

    private void handleError(PlaybackException exception,String playbackId) {
        String errorMessage = "";
        if (1000 <= exception.errorCode && exception.errorCode < 2000) { // Miscellaneous errors
            errorMessage = activity.getString(R.string.exoplayer_miscellaneous_error, exception.getErrorCodeName(), exception.errorCode, playbackId);
        } else if (2001 == exception.errorCode) { // No network error
            errorMessage = activity.getString(R.string.testpress_no_internet_try_again);
        } else if (2000 <= exception.errorCode && exception.errorCode <= 3000) { // Input/Output errors
            errorMessage = activity.getString(R.string.exoplayer_input_or_output_error, exception.getErrorCodeName(), exception.errorCode, playbackId);
        } else if (3000 <= exception.errorCode && exception.errorCode <= 4000) { // Content parsing errors
            errorMessage = activity.getString(R.string.exoplayer_content_parsing_error, exception.getErrorCodeName(), exception.errorCode, playbackId);
        } else if (4001 == exception.errorCode) {
            errorMessage ="<html><body><p>An error occurred while playing the video. Try restarting your device or playing another video. More help <a href='https://tpstreams.com/help/troubleshooting-steps-for-error-code-4001'>click here</a>.<br> Player code: "+exception.errorCode+". Player Id: "+playbackId+"</p></body></html>";
        } else if (4003 == exception.errorCode) {
            errorMessage ="<html><body><p>An error occurred while playing the video. Try restarting your device or selecting a different resolution. More help <a href='https://tpstreams.com/help/troubleshooting-steps-for-error-code-4001'>click here</a>.<br> Player code: "+exception.errorCode+". Player Id: "+playbackId+"</p></body></html>";
        } else if (4000 <= exception.errorCode && exception.errorCode <= 5000) { // Decoding errors
            errorMessage = activity.getString(R.string.exoplayer_decoding_error, exception.getErrorCodeName(), exception.errorCode, playbackId);
        } else if (5000 <= exception.errorCode && exception.errorCode <= 6000) { // AudioTrack errors
            errorMessage = activity.getString(R.string.exoplayer_audio_track_error, exception.getErrorCodeName(), exception.errorCode, playbackId);
        } else if (6000 <= exception.errorCode && exception.errorCode <= 7000) { // DRM errors
            errorMessage = activity.getString(R.string.exoplayer_drm_error, exception.getErrorCodeName(), exception.errorCode, playbackId);
        }
        displayError(errorMessage, exception.errorCode);
        logPlaybackException(errorMessage, playbackId, exception);
    }

    private boolean isScreenCasted() {
        if (mediaRouter != null) {
            for (RouteInfo info : mediaRouter.getRoutes()) {
                if (info.getConnectionState() == CONNECTION_STATE_CONNECTED) {
                    displayError(R.string.testpress_disconnect_live_video);
                    return true;
                }
            }
        }
        hideError(R.string.testpress_disconnect_live_video);
        return false;
    }

    public void onOrientationChange(boolean fullscreen) {

        if (fullscreen) {
            openFullscreenDialog();
        } else {
            closeFullscreenDialog();
        }
    }

    @Override
    public void onTimeRangeChange(long startTime, long endTime) {
        watchedTimeRanges.add(new String[]{String.valueOf(startTime), String.valueOf(endTime)});
    }

    @Override
    public DrmSessionManager get(MediaItem mediaItem) {
        return new DefaultDrmSessionManager.Builder().build(new CustomHttpDrmMediaCallback(activity, content.getId()));
    }

    private class PlayerEventListener implements Player.Listener, DRMLicenseFetchCallback {

        @Override
        public void onPlayWhenReadyChanged(boolean playWhenReady, int reason) {
            // Following code will execute when the user pauses the video
            if (!playWhenReady && reason == Player.PLAY_WHEN_READY_CHANGE_REASON_USER_REQUEST) {
                updateVideoAttempt();
            }
            Player.Listener.super.onPlayWhenReadyChanged(playWhenReady, reason);
        }

        @Override
        public void onPlaybackStateChanged(int playbackState) {
            if(isPreparing && playbackState == Player.STATE_READY){
                playLowBitrateTrack();
                isPreparing = false;
            }

            if (usbConnectionStateReceiver != null && !isScreenCasted() &&
                    CommonUtils.isUsbConnected(activity)) {

                displayError(R.string.testpress_usb_connected);
            } else {
                hideError(R.string.testpress_usb_connected);
                if (playbackState == Player.STATE_READY) {
                    errorMessageTextView.setVisibility(View.GONE);
                }
            }
            if (playbackState == Player.STATE_BUFFERING) {
                progressBar.setVisibility(View.VISIBLE);
            } else {
                progressBar.setVisibility(View.GONE);
            }

            if (playbackState == Player.STATE_ENDED) {
                onTimeRangeChange((long)startPosition, (long)getCurrentPosition());
                updateVideoAttempt();
            }

            playerView.setKeepScreenOn(playbackState != Player.STATE_IDLE && playbackState != Player.STATE_ENDED);
        }

        @Override
        public void onPlayerError(PlaybackException exception) {
            String playBackId = VideoUtils.INSTANCE.generatePlayerIdString();
            Throwable cause = exception.getCause();

            if(isLivestreamNotStartedError(exception)){
                showLiveStreamNotStartedScreen();
                liveStreamCallbackListener.onUrlReturnError(url);
                return;
            }

            if (isDRMException(cause)) {
                DownloadTask downloadTask = new DownloadTask(url, activity);
                drmLicenseRetries += 1;
                if (drmLicenseRetries < 2 && downloadTask.isDownloaded()) {
                    if (!InternetConnectivityChecker.isConnected(activity)) {
                        displayError(R.string.no_internet_to_sync_license);
                        return;
                    }
                    OfflineDRMLicenseHelper.renewLicense(url, content.getId(), activity, this);
                    displayError(R.string.syncing_video);
                } else {
                    String licenseRequestFailedMessage = activity.getString(R.string.license_request_failed, exception.errorCode, playBackId);
                    displayError(licenseRequestFailedMessage, exception.errorCode);
                    logPlaybackException(licenseRequestFailedMessage, playBackId, exception);
                }
            } else {
                handleError(exception,playBackId);
            }
        }

        @Override
        public void onPositionDiscontinuity(int reason) {
            startPosition = getCurrentPosition();
        }

        @Override
        public void onSeekProcessed() {
            // This method is triggered automatically during ExoPlayer initialization when the start position is set.
            // Since the initial API call is unnecessary and redundant at this stage,
            // we skip processing during the first invocation by checking if it is the first seek
            // and proceed only with subsequent calls to update the user watching status.
            if (!firstSeekCalled){
                firstSeekCalled = true;
                return;
            }
            // Cancel any pending seek-related operations
            seekHandler.removeCallbacksAndMessages(null);

            // Schedule a new operation with a debounce time
            seekHandler.postDelayed(() -> {
                updateVideoAttempt(); // API call to save the video status
            }, 1000); // 1000ms debounce time
        }

        @Override
        public void onLicenseFetchSuccess(@NotNull byte[] keySetId) {
            activity.runOnUiThread(() -> {
                float currentPosition = getCurrentPosition();
                MediaItem mediaItem = getMediaItem(true);
                player.setMediaItem(mediaItem);
                errorMessageTextView.setVisibility(View.GONE);
                preparePlayer();
                player.setPlayWhenReady(true);
                player.seekTo((long) (currentPosition * 1000));
            });
        }

        @Override
        public void onLicenseFetchFailure() {
            displayError(R.string.license_error);
        }

        private boolean isLivestreamNotStartedError(PlaybackException exception){
            return liveStreamCallbackListener != null && exception.errorCode == 2004;
        }
    }

    private void playLowBitrateTrack() {
        MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
        if (mappedTrackInfo != null) {
            int rendererIndex = getRendererIndex(C.TRACK_TYPE_VIDEO, mappedTrackInfo);
            TrackGroupArray trackGroups = mappedTrackInfo.getTrackGroups(rendererIndex);
            Pair<Integer, Integer> pair = VideoUtils.getLowBitrateTrackIndex(trackGroups);
            DefaultTrackSelector.SelectionOverride override = new DefaultTrackSelector.SelectionOverride(pair.getSecond(), pair.getFirst());
            DefaultTrackSelector.ParametersBuilder parametersBuilder = trackSelector.buildUponParameters();
            parametersBuilder.clearSelectionOverrides(rendererIndex)
                    .setSelectionOverride(rendererIndex, mappedTrackInfo.getTrackGroups(rendererIndex), override);
            trackSelector.setParameters(parametersBuilder.build());
        }
    }
    
    private boolean isDRMException(Throwable cause) {
        return cause instanceof DrmSession.DrmSessionException || cause instanceof MediaCodec.CryptoException || cause instanceof MediaDrmCallbackException;
    }

    public static int getRendererIndex(int trackType, MappingTrackSelector.MappedTrackInfo mappedTrackInfo) {
        for (int i=0; i < mappedTrackInfo.getRendererCount(); i++) {
            if (mappedTrackInfo.getRendererType(i) == trackType) {
                return i;
            }
        }

        return -1;
    }

    private void logPlaybackException(String errorMessage, String playbackId, PlaybackException exception) {
        String username = (profileDetails != null) ? profileDetails.getUsernameOrEmail() : "null";
        String packageName = (activity != null) ? activity.getPackageName() : "Package name not available";
        long contentId = (content != null) ? content.getId() : -1;
        SentryLoggerKt.logPlaybackException(
                username,
                packageName,
                contentId,
                errorMessage,
                playbackId,
                exception
        );
    }
}
