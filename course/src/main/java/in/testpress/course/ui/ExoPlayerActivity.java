package in.testpress.course.ui;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;

import com.google.android.exoplayer2.ui.PlayerView;

import in.testpress.course.R;
import in.testpress.course.util.ExoPlayerUtil;
import in.testpress.course.util.ExoplayerFullscreenHelper;

/**
 * A fullscreen activity to play audio or video streams.
 */
public class ExoPlayerActivity extends AppCompatActivity {

    public static final String VIDEO_URL = "videoUrl";
    public static final String START_POSITION = "startPosition";
    public static final String PLAY_WHEN_READY = "playWhenReady";
    public static final String SPEED_RATE = "speedRate";
    public static final String WIDE_VINE_URL = "WideVineUrl";

    private ExoPlayerUtil exoPlayerUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testpress_exo_player_view);

        PlayerView playerView = findViewById(R.id.exo_player_view);
        int matchParent = ConstraintLayout.LayoutParams.MATCH_PARENT;
        playerView.setLayoutParams(new ConstraintLayout.LayoutParams(matchParent, matchParent));
        findViewById(R.id.exo_fullscreen_button).setVisibility(View.GONE);
        String url = getIntent().getStringExtra(VIDEO_URL);
        String wideVineUrl = getIntent().getStringExtra(WIDE_VINE_URL);
        float startPosition = getIntent().getFloatExtra(START_POSITION, 0);
        float speedRate = getIntent().getFloatExtra(SPEED_RATE, 1);
        FrameLayout exoPlayerMainFrame = findViewById(R.id.exo_player_main_frame);
        exoPlayerUtil =
                new ExoPlayerUtil(this, exoPlayerMainFrame, url, startPosition, true, speedRate, wideVineUrl);

        exoPlayerUtil.openOnlyInFullScreen();
    }

    @Override
    public void onStart() {
        super.onStart();
        exoPlayerUtil.onStart();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
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
    }
}
