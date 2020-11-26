package in.testpress.course.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.mediarouter.media.MediaControlIntent;
import androidx.mediarouter.media.MediaRouteSelector;
import androidx.mediarouter.media.MediaRouter;
import androidx.mediarouter.media.MediaRouter.RouteInfo;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultControlDispatcher;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.PlaybackPreparer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.offline.DownloadHelper;
import com.google.android.exoplayer2.offline.DownloadRequest;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.util.Util;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import in.testpress.core.TestpressCallback;
import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSdk;
import in.testpress.core.TestpressSession;
import in.testpress.core.TestpressUserDetails;
import in.testpress.course.R;
import in.testpress.course.api.TestpressCourseApiClient;
import in.testpress.course.helpers.DownloadTask;
import in.testpress.course.helpers.VideoDownload;
import in.testpress.models.ProfileDetails;
import in.testpress.models.greendao.Content;
import in.testpress.models.greendao.VideoAttempt;
import in.testpress.ui.ExploreSpinnerAdapter;
import in.testpress.util.CommonUtils;

import static android.content.Context.AUDIO_SERVICE;
import static android.view.WindowManager.LayoutParams.FLAG_SECURE;
import static androidx.mediarouter.media.MediaRouter.RouteInfo.CONNECTION_STATE_CONNECTED;
import static com.google.android.exoplayer2.ExoPlaybackException.TYPE_SOURCE;
import static in.testpress.course.api.TestpressCourseApiClient.LAST_POSITION;
import static in.testpress.course.api.TestpressCourseApiClient.TIME_RANGES;

public class ExoPlayerUtil {

    private static final int OVERLAY_POSITION_CHANGE_INTERVAL = 15000; // 15s

    private FrameLayout exoPlayerMainFrame;
    private View exoPlayerLayout;
    private PlayerView playerView;
    private LottieAnimationView progressBar;
    private TextView errorMessageTextView;
    private LinearLayout emailIdLayout;
    private TextView emailIdTextView;
    private SimpleExoPlayer player;
    private ImageView fullscreenIcon;
    private Dialog fullscreenDialog;
    private TrackSelectionDialog trackSelectionDialog;


    private Activity activity;
    private long videoAttemptId = -1;
    private Content content;
    private String url;
    private boolean isopenFullscreenDialogCalled;
    private boolean iscloseFullscreenDialogCalled;
    private float startPosition;
    private boolean playWhenReady = true;
    private float speedRate = 1;
    private Spinner speedRateSpinner;
    private ExploreSpinnerAdapter speedSpinnerAdapter;
    private BroadcastReceiver usbConnectionStateReceiver;
    private MediaRouter mediaRouter;
    private MediaRouteSelector mediaRouteSelector;
    private MediaRouter.Callback mediaRouterCallback;
    private Handler overlayPositionHandler;
    private Runnable overlayPositionChangeTask = new Runnable() {
        @Override
        public void run() {
            displayOverlayText();
            overlayPositionHandler.postDelayed(this, OVERLAY_POSITION_CHANGE_INTERVAL);
        }
    };
    private boolean fullscreen = false;
    private boolean errorOnVideoAttemptUpdate;
    private Handler videoAttemptUpdateHandler;
    private Runnable videoAttemptUpdateTask = new Runnable() {
        @Override
        public void run() {
            if (!isScreenCasted()) {
                updateVideoAttempt();
            }
        }
    };
    AudioManager audioManager;
    AudioManager.OnAudioFocusChangeListener audioFocusChangeListener;
    private DefaultTrackSelector trackSelector;
    private DialogInterface.OnClickListener dialogOnClickListener;

    public ExoPlayerUtil(Activity activity, FrameLayout exoPlayerMainFrame, String url,
                         float startPosition) {

        this.activity = activity;
        this.exoPlayerMainFrame = exoPlayerMainFrame;
        this.url = url;
        this.startPosition = startPosition;

        initializeViews();
        exoPlayerLayout = exoPlayerMainFrame.findViewById(R.id.exo_player_layout);
        playerView = exoPlayerMainFrame.findViewById(R.id.exo_player_view);
        fullscreenIcon = exoPlayerMainFrame.findViewById(R.id.exo_fullscreen_icon);
        progressBar = exoPlayerMainFrame.findViewById(R.id.exo_player_progress);
        errorMessageTextView = exoPlayerMainFrame.findViewById(R.id.error_message);
        TestpressSession session = TestpressSdk.getTestpressSession(activity);
        if (session != null && session.getInstituteSettings().isDisplayUserEmailOnVideo()) {
            setUserEmailOverlay();
        }
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
        playerView.setPlaybackPreparer(new PlaybackPreparer() {
            @Override
            public void preparePlayback() {
                initializePlayer();
            }
        });

        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory();
        trackSelector =  new DefaultTrackSelector(activity, videoTrackSelectionFactory);
        initFullscreenDialog();
        initResolutionSelector();

        // set activity as portrait mode at first
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    public ExoPlayerUtil(Activity activity, FrameLayout exoPlayerMainFrame, String url,
                         float startPosition, boolean playWhenReady, float speedRate) {

        this(activity, exoPlayerMainFrame, url, startPosition);
        this.playWhenReady = playWhenReady;
        setSpeedRate(speedRate);
    }

    private void initializeViews() {
        emailIdTextView = exoPlayerMainFrame.findViewById(R.id.email_id);
        emailIdLayout = exoPlayerMainFrame.findViewById(R.id.email_id_layout);
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

    public void openOnlyInFullScreen() {
        openFullscreenDialog();
        fullscreenDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                fullscreenDialog.dismiss();
                activity.finish();
            }
        });
    }

    public void seekTo(Long milliSeconds) {
        player.seekTo(milliSeconds);
    }

    public void fastForward(Long milliSeconds) {
        player.seekTo(player.getCurrentPosition() + milliSeconds);
    }

    public void backward(Long milliSeconds) {
        player.seekTo(player.getCurrentPosition() - milliSeconds);
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

    private void initAudioFocusChangeListener() {
        audioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
            public void onAudioFocusChange(int focusChange) {
                switch (focusChange) {
                    case (AudioManager.AUDIOFOCUS_LOSS_TRANSIENT):
                        player.setPlayWhenReady(false);
                        break;
                    case (AudioManager.AUDIOFOCUS_LOSS) :
                        player.setPlayWhenReady(false);
                        break;
                    default:
                        break;
                }
            }
        };
    }

    public void initializePlayer() {
        errorMessageTextView.setVisibility(View.GONE);
        if (player == null) {
            progressBar.setVisibility(View.VISIBLE);
            player = new SimpleExoPlayer.Builder(activity, new DefaultRenderersFactory(activity))
                    .setTrackSelector(trackSelector).build();

            player.addListener(new PlayerEventListener());
            playerView.setPlayer(player);
            player.setPlayWhenReady(playWhenReady);
            audioManager = (AudioManager) activity.getSystemService(AUDIO_SERVICE);
            initAudioFocusChangeListener();
            audioManager.requestAudioFocus(audioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            // Convert seconds to ms
            player.seekTo((long) (startPosition * 1000));
            player.setPlaybackParameters(new PlaybackParameters(speedRate));
        }

        MediaSource mediaSource = getMediaSource();
        player.prepare(mediaSource, false, false);
        if (overlayPositionHandler != null) {
            overlayPositionHandler
                    .postDelayed(overlayPositionChangeTask, OVERLAY_POSITION_CHANGE_INTERVAL);
        }
        if (usbConnectionStateReceiver != null) {
            IntentFilter filter = new IntentFilter("android.hardware.usb.action.USB_STATE");
            activity.registerReceiver(usbConnectionStateReceiver, filter);
            mediaRouter.addCallback(mediaRouteSelector, mediaRouterCallback,
                    MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN);
        }

        addPlayPauseOnClickListener();
    }

    private DialogInterface.OnClickListener trackSelectionListener() {
        if (dialogOnClickListener == null) {
            dialogOnClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
                    int rendererIndex = getRendererIndex(C.TRACK_TYPE_VIDEO, mappedTrackInfo);
                    DefaultTrackSelector.ParametersBuilder parametersBuilder = trackSelector.buildUponParameters();
                    if (!trackSelectionDialog.getOverrides().isEmpty()) {
                        parametersBuilder.clearSelectionOverrides(rendererIndex)
                                .setSelectionOverride(rendererIndex, mappedTrackInfo.getTrackGroups(rendererIndex), trackSelectionDialog.getOverrides().get(0));
                        trackSelector.setParameters(parametersBuilder.build());
                    }
                }
            };
        }

        return dialogOnClickListener;
    }

    private MediaSource getMediaSource() {
        DownloadTask downloadTask = new DownloadTask(url, activity);
        DownloadRequest downloadRequest = VideoDownload.getDownloadRequest(url, activity);
        if (downloadTask.isDownloaded()) {
            return DownloadHelper.createMediaSource(downloadRequest, buildDataSourceFactory());
        } else {
            return buildNetworkMediaSource(Uri.parse(url));
        }
    }


    private DataSource.Factory buildDataSourceFactory() {
        return new ExoPlayerDataSourceFactory(activity).build();
    }


    private void addPlayPauseOnClickListener() {
        playerView.setControlDispatcher(new DefaultControlDispatcher() {
            @Override
            public boolean dispatchSetPlayWhenReady(Player player, boolean playWhenReady) {
                if (playWhenReady) {
                    audioManager.requestAudioFocus(audioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
                }
                updateVideoAttempt();
                return super.dispatchSetPlayWhenReady(player, playWhenReady);
            }
        });
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
            if (overlayPositionHandler != null) {
                overlayPositionHandler.removeCallbacks(overlayPositionChangeTask);
            }
        }
        if (usbConnectionStateReceiver != null) {
            activity.unregisterReceiver(usbConnectionStateReceiver);
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

    private MediaSource buildNetworkMediaSource(Uri uri) {
        DataSource.Factory dataSourceFactory = buildDataSourceFactory();

        int type = Util.inferContentType(uri);
        switch (type) {
            case C.TYPE_HLS:
                return new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
            case C.TYPE_OTHER:
                return new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
            default:
                throw new IllegalStateException("Unsupported type: " + type);
        }
    }

    public float getCurrentPosition() {
        // Convert milliseconds to seconds
        return ((float) Math.max(0, player.getContentPosition())) / 1000;
    }

    public void setStartPosition(float startPosition) {
        this.startPosition = startPosition;
    }

    public boolean isPlayWhenReady() {
        return player.getPlayWhenReady();
    }

    public void setPlayWhenReady(boolean playWhenReady) {
        this.playWhenReady = playWhenReady;
    }

    public float getSpeedRate() {
        return speedRate;
    }

    public void setSpeedRate(float speedRate) {
        this.speedRate = speedRate;

        int itemPosition = speedSpinnerAdapter.getItemPositionFromTag(
                String.valueOf(speedRate).replace(".0", ""));

        speedRateSpinner.setSelection(itemPosition);
    }

    public void setVideoAttemptParameters(long videoAttemptId, Content content) {
        this.videoAttemptId = videoAttemptId;
        this.content = content;
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
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    public void onStop() {
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
    }

    private void setUserEmailOverlay() {
        ProfileDetails profileDetails = TestpressUserDetails.getInstance().getProfileDetails();
        if (profileDetails != null) {
            setUserEmailOverlay(profileDetails);
        } else {
            TestpressUserDetails.getInstance().load(activity, new TestpressCallback<ProfileDetails>() {
                @Override
                public void onSuccess(ProfileDetails userDetails) {
                    setUserEmailOverlay(userDetails);
                }

                @Override
                public void onException(TestpressException exception) {
                }
            });
        }
    }

    private void setUserEmailOverlay(ProfileDetails profileDetails) {
        String overlayText;
        if (profileDetails.getEmail() != null && !profileDetails.getEmail().isEmpty()) {
            overlayText = profileDetails.getEmail();
        } else {
            overlayText = profileDetails.getUsername();
        }
        emailIdTextView.setText(overlayText);
        overlayPositionHandler = new Handler();
        startOverlayMarquee();
    }

    private void startOverlayMarquee() {
        Animation marquee = AnimationUtils.loadAnimation(activity, R.anim.testpress_marquee);
        emailIdLayout.startAnimation(marquee);
    }

    private void displayOverlayText() {
        int height = Math.max(emailIdLayout.getMeasuredHeight(), 1);
        Random random = new Random();
        float randomY = random.nextInt(height) + emailIdLayout.getY();
        emailIdTextView.setY(randomY);
        startOverlayMarquee();
    }

    private void openFullscreenDialog() {

        if (!isopenFullscreenDialogCalled) {
            iscloseFullscreenDialogCalled = false;
            isopenFullscreenDialogCalled = true;
            exoPlayerMainFrame.removeView(exoPlayerLayout);
            fullscreenDialog.addContentView(exoPlayerLayout, new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            fullscreenDialog.getWindow().addFlags(FLAG_SECURE);
            setFullscreenIcon(R.drawable.testpress_fullscreen_exit);
            fullscreen = true;
            fullscreenDialog.show();
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        }
    }

    private void closeFullscreenDialog() {

        if (!iscloseFullscreenDialogCalled) {
            activity.getWindow().setFlags(FLAG_SECURE, FLAG_SECURE);
            isopenFullscreenDialogCalled = false;
            iscloseFullscreenDialogCalled = true;
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
            ((ViewGroup) exoPlayerLayout.getParent()).removeView(exoPlayerLayout);
            exoPlayerMainFrame.addView(exoPlayerLayout);
            fullscreen = false;
            fullscreenDialog.dismiss();
            setFullscreenIcon(R.drawable.testpress_fullscreen);
        }
    }

    private void setFullscreenIcon(@DrawableRes int imageResId) {
        fullscreenIcon.setImageDrawable(ContextCompat.getDrawable(activity, imageResId));
        startOverlayMarquee();
    }

    Map<String, Object> getVideoAttemptParameters() {
        Map<String, Object> parameters = new HashMap<>();
        float currentPosition = getCurrentPosition();
        parameters.put(LAST_POSITION, currentPosition);
        String[][] timeRanges = new String[][] {{
                String.valueOf(startPosition),
                String.valueOf(currentPosition)
        }};
        parameters.put(TIME_RANGES, timeRanges);
        return parameters;
    }

    public void updateVideoAttempt() {
        if (videoAttemptId == -1) {
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
                        errorOnVideoAttemptUpdate = false;
                    }

                    @Override
                    public void onException(TestpressException exception) {
                        errorOnVideoAttemptUpdate = true;
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

    private void hideError(@StringRes int message) {
        if (errorMessageTextView.getText().equals(activity.getString(message))) {
            errorMessageTextView.setVisibility(View.GONE);
            player.setPlayWhenReady(true);
            player.getPlaybackState();
        }
    }

    private void handleError(boolean networkError) {
        if (networkError) {
            displayError(R.string.testpress_no_internet_try_again);
        }
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

    private class PlayerEventListener extends Player.DefaultEventListener {

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            if (usbConnectionStateReceiver != null && !isScreenCasted() &&
                    CommonUtils.isUsbConnected(activity)) {
                
                displayError(R.string.testpress_usb_connected);
            } else {
                hideError(R.string.testpress_usb_connected);
                if (errorOnVideoAttemptUpdate) {
                    errorMessageTextView.setVisibility(View.GONE);
                    updateVideoAttempt();
                } else if (playbackState == Player.STATE_READY) {
                    errorMessageTextView.setVisibility(View.GONE);
                }
            }
            if (playbackState == Player.STATE_BUFFERING) {
                progressBar.setVisibility(View.VISIBLE);
            } else {
                progressBar.setVisibility(View.GONE);
            }
            if (playbackState == Player.STATE_IDLE || playbackState == Player.STATE_ENDED ||
                    !playWhenReady) {

                playerView.setKeepScreenOn(false);
            } else {
                playerView.setKeepScreenOn(true);
            }
        }

        @Override
        public void onPlayerError(ExoPlaybackException exception) {
            handleError(exception.type == TYPE_SOURCE);
        }

        @Override
        public void onPositionDiscontinuity(int reason) {
            super.onPositionDiscontinuity(reason);
            startPosition = getCurrentPosition();
        }

        @Override
        public void onSeekProcessed() {
            super.onSeekProcessed();
            updateVideoAttempt();
        }
    }

    public static int getRendererIndex(int trackType, MappingTrackSelector.MappedTrackInfo mappedTrackInfo) {
        for (int i=0; i < mappedTrackInfo.getRendererCount(); i++) {
            if (mappedTrackInfo.getRendererType(i) == trackType) {
                return i;
            }
        }

        return -1;
    }

}
