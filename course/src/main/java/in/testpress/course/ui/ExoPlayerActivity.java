package in.testpress.course.ui;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.View;
import android.widget.FrameLayout;

import com.google.android.exoplayer2.ui.PlayerView;

import in.testpress.course.R;
import in.testpress.course.util.ExoPlayerUtil;

/**
 * A fullscreen activity to play audio or video streams.
 */
public class ExoPlayerActivity extends AppCompatActivity {

    public static final String VIDEO_URL = "videoUrl";
    public static final String START_POSITION = "startPosition";
    public static final String PLAY_WHEN_READY = "playWhenReady";
    public static final String SPEED_RATE = "speedRate";

    private ExoPlayerUtil exoPlayerUtil;

    private OrientationEventListener mOrientationListener;
    public boolean isLandscape;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testpress_exo_player_view);

        PlayerView playerView = findViewById(R.id.exo_player_view);
        int matchParent = ConstraintLayout.LayoutParams.MATCH_PARENT;
        playerView.setLayoutParams(new ConstraintLayout.LayoutParams(matchParent, matchParent));
        findViewById(R.id.exo_fullscreen_button).setVisibility(View.GONE);
        String url = getIntent().getStringExtra(VIDEO_URL);
        float startPosition = getIntent().getFloatExtra(START_POSITION, 0);
        float speedRate = getIntent().getFloatExtra(SPEED_RATE, 1);
        FrameLayout exoPlayerMainFrame = findViewById(R.id.exo_player_main_frame);
        exoPlayerUtil =
                new ExoPlayerUtil(this, exoPlayerMainFrame, url, startPosition, true, speedRate);

        intializeOrientationListener();
    }

    @Override
    public void onStart() {
        super.onStart();
        exoPlayerUtil.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        exoPlayerUtil.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        exoPlayerUtil.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        exoPlayerUtil.onStop();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        exoPlayerUtil=null;
    }

    private void intializeOrientationListener() {
        mOrientationListener = new OrientationEventListener(this,
                SensorManager.SENSOR_DELAY_NORMAL) {

            @Override
            public void onOrientationChanged(int orientation) {

                // Check does user have turned off the auto rotation
                boolean isAutoRotationIsON = (android.provider.Settings.System.getInt(getContentResolver(),
                        Settings.System.ACCELEROMETER_ROTATION, 0) == 1);

                if (exoPlayerUtil != null && isAutoRotationIsON) {
                    boolean misLandscape = isLandscape;

                    if ((orientation > 0 && orientation < 20) || (orientation == 170 && orientation < 190)) {
                        misLandscape = false;
                    } else if ((orientation > 80 && orientation < 110) || (orientation > 220) && orientation < 270) {
                        misLandscape = true;
                    }

                    if (misLandscape != isLandscape) {
                        isLandscape = misLandscape;
                        exoPlayerUtil.onOrientationchange(isLandscape);
                    }
                }
            }
        };

        if (mOrientationListener.canDetectOrientation() == true) {
            mOrientationListener.enable();
        } else {
            mOrientationListener.disable();
        }
    }

}
