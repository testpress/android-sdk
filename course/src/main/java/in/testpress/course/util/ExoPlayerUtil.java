package in.testpress.course.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.mediarouter.media.MediaControlIntent;
import androidx.mediarouter.media.MediaRouteSelector;
import androidx.mediarouter.media.MediaRouter;
import androidx.mediarouter.media.MediaRouter.RouteInfo;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultControlDispatcher;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.PlaybackPreparer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.ui.TrackSelectionView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
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
import in.testpress.models.ProfileDetails;
import in.testpress.models.greendao.Content;
import in.testpress.models.greendao.VideoAttempt;
import in.testpress.ui.ExploreSpinnerAdapter;
import in.testpress.util.CommonUtils;
import in.testpress.util.UserAgentProvider;

import static android.content.Context.AUDIO_SERVICE;
import static androidx.mediarouter.media.MediaRouter.RouteInfo.CONNECTION_STATE_CONNECTED;
import static android.view.WindowManager.LayoutParams.FLAG_SECURE;
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


    private Activity activity;
    private long videoAttemptId;
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

    public ExoPlayerUtil(Activity activity, FrameLayout exoPlayerMainFrame, String url,
                         float startPosition) {

        this.activity = activity;
        this.exoPlayerMainFrame = exoPlayerMainFrame;
        this.url = url;
        this.startPosition = startPosition;
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
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();

        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);
        trackSelector =
                new DefaultTrackSelector(videoTrackSelectionFactory);
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
        player.seekTo(milliSeconds);
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

                    Pair<AlertDialog, TrackSelectionView> dialogPair =
                            TrackSelectionView.getDialog(activity, "Quality", trackSelector, rendererIndex);
                    Window window = dialogPair.first.getWindow();

                    if (window != null) {
                        window.setBackgroundDrawable(new ColorDrawable(Color.DKGRAY));
                        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    }

                    dialogPair.second.setShowDisableOption(false);
                    dialogPair.second.setAllowAdaptiveSelections(allowAdaptiveSelections);
                    dialogPair.first.show();
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
            player = ExoPlayerFactory.newSimpleInstance(new DefaultRenderersFactory(activity),
                    trackSelector, new DefaultLoadControl());

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
        MediaSource mediaSource = buildMediaSource(Uri.parse(url));
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
        audioManager.abandonAudioFocus(audioFocusChangeListener);
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

    private MediaSource buildMediaSource(Uri uri) {
        String userAgent = UserAgentProvider.get(activity);
        DataSource.Factory dataSourceFactory =
                new DefaultHttpDataSourceFactory(userAgent);
        int type = Util.inferContentType(uri);
        switch (type) {
            case C.TYPE_HLS:
                return new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
            case C.TYPE_OTHER:
                return new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
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
        emailIdTextView = exoPlayerMainFrame.findViewById(R.id.email_id);
        emailIdTextView.setText(overlayText);
        emailIdLayout = exoPlayerMainFrame.findViewById(R.id.email_id_layout);
        overlayPositionHandler = new Handler();
        startOverlayMarquee();
    }

    private void startOverlayMarquee() {
        if (emailIdLayout == null) return;
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

}
