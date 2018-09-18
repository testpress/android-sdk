package in.testpress.course.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
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

import com.airbnb.lottie.LottieAnimationView;
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
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.Random;

import in.testpress.core.TestpressCallback;
import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSdk;
import in.testpress.core.TestpressSession;
import in.testpress.core.TestpressUserDetails;
import in.testpress.course.R;
import in.testpress.models.ProfileDetails;
import in.testpress.ui.ExploreSpinnerAdapter;
import in.testpress.util.UserAgentProvider;

import static com.google.android.exoplayer2.ExoPlaybackException.TYPE_SOURCE;

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
    private String url;
    private long startPosition;
    private boolean playWhenReady = true;
    private float speedRate = 1;
    private Spinner speedRateSpinner;
    private ExploreSpinnerAdapter speedSpinnerAdapter;
    private BroadcastReceiver usbConnectionStateReceiver;
    private boolean usbConnected;
    private Handler overlayPositionHandler;
    private Runnable overlayPositionChangeTask = new Runnable() {
        @Override
        public void run() {
            displayOverlayText();
            overlayPositionHandler.postDelayed(this, OVERLAY_POSITION_CHANGE_INTERVAL);
        }
    };
    private boolean fullscreen = false;

    public ExoPlayerUtil(Activity activity, FrameLayout exoPlayerMainFrame, String url) {
        this.activity = activity;
        this.exoPlayerMainFrame = exoPlayerMainFrame;
        this.url = url;
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
            usbConnectionStateReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    boolean connected = intent.getExtras() != null &&
                            intent.getExtras().getBoolean("connected");

                    onUSBConnectedStateChanged(connected);
                }
            };
        }
        playerView.setPlaybackPreparer(new PlaybackPreparer() {
            @Override
            public void preparePlayback() {
                initializePlayer();
            }
        });
        initFullscreenDialog();
    }

    public ExoPlayerUtil(Activity activity, FrameLayout exoPlayerMainFrame, String url,
                         long startPosition, boolean playWhenReady, float speedRate) {

        this(activity, exoPlayerMainFrame, url);
        this.startPosition = startPosition;
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

    public void initializePlayer() {
        errorMessageTextView.setVisibility(View.GONE);
        if (player == null) {
            progressBar.setVisibility(View.VISIBLE);
            player = ExoPlayerFactory.newSimpleInstance(new DefaultRenderersFactory(activity),
                    new DefaultTrackSelector(), new DefaultLoadControl());

            player.addListener(new PlayerEventListener());
            playerView.setPlayer(player);
            player.setPlayWhenReady(playWhenReady);
            player.seekTo(startPosition);
            player.setPlaybackParameters(new PlaybackParameters(speedRate));
        }
        MediaSource mediaSource = buildMediaSource(Uri.parse(url));
        player.prepare(mediaSource, false, false);
        if (overlayPositionHandler != null) {
            overlayPositionHandler
                    .postDelayed(overlayPositionChangeTask, OVERLAY_POSITION_CHANGE_INTERVAL);
        }
        if (usbConnectionStateReceiver != null) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.hardware.usb.action.USB_STATE");
            activity.registerReceiver(usbConnectionStateReceiver, filter);
        }
    }

    public void releasePlayer() {
        if (player != null) {
            startPosition = Math.max(0, player.getContentPosition());
            playWhenReady = player.getPlayWhenReady();
            player.release();
            player = null;
            if (overlayPositionHandler != null) {
                overlayPositionHandler.removeCallbacks(overlayPositionChangeTask);
            }
        }
        if (usbConnectionStateReceiver != null) {
            activity.unregisterReceiver(usbConnectionStateReceiver);
        }
    }

    private void onUSBConnectedStateChanged(boolean connected) {
        usbConnected = connected;
        if (connected) {
            player.setPlayWhenReady(false);
            player.getPlaybackState();
            errorMessageTextView.setText(R.string.testpress_usb_connected);
            errorMessageTextView.setVisibility(View.VISIBLE);
        } else {
            if (errorMessageTextView.getText()
                    .equals(activity.getString(R.string.testpress_usb_connected))) {

                errorMessageTextView.setVisibility(View.GONE);
                player.setPlayWhenReady(true);
                player.getPlaybackState();
            }
        }
    }

    private MediaSource buildMediaSource(Uri uri) {
        String userAgent = UserAgentProvider.get(activity);
        return new ExtractorMediaSource.Factory(new DefaultHttpDataSourceFactory(userAgent))
                .createMediaSource(uri);
    }

    public long getStartPosition() {
        return player.getContentPosition();
    }

    public void setStartPosition(long startPosition) {
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
        exoPlayerMainFrame.removeView(exoPlayerLayout);
        fullscreenDialog.addContentView(exoPlayerLayout, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        setFullscreenIcon(R.drawable.testpress_fullscreen_exit);
        fullscreen = true;
        fullscreenDialog.show();
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    private void closeFullscreenDialog() {
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ((ViewGroup) exoPlayerLayout.getParent()).removeView(exoPlayerLayout);
        exoPlayerMainFrame.addView(exoPlayerLayout);
        fullscreen = false;
        fullscreenDialog.dismiss();
        setFullscreenIcon(R.drawable.testpress_fullscreen);
    }

    private void setFullscreenIcon(@DrawableRes int imageResId) {
        fullscreenIcon.setImageDrawable(ContextCompat.getDrawable(activity, imageResId));
        startOverlayMarquee();
    }

    private class PlayerEventListener extends Player.DefaultEventListener {

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            if (usbConnected) {
                onUSBConnectedStateChanged(true);
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
            if (exception.type == TYPE_SOURCE) {
                errorMessageTextView.setText(R.string.testpress_no_internet_try_again);
            } else {
                errorMessageTextView.setText(R.string.testpress_some_thing_went_wrong_try_again);
            }
            errorMessageTextView.setVisibility(View.VISIBLE);
        }
    }
}
