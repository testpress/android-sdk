package in.testpress.course.ui;

import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testpress_exo_player_view);

        PlayerView playerView = findViewById(R.id.exo_player_view);
        int matchParent = ConstraintLayout.LayoutParams.MATCH_PARENT;
        playerView.setLayoutParams(new ConstraintLayout.LayoutParams(matchParent, matchParent));
        findViewById(R.id.exo_fullscreen_button).setVisibility(View.GONE);
        String url = getIntent().getStringExtra(VIDEO_URL);
        long startPosition = getIntent().getLongExtra(START_POSITION, 0);
        float speedRate = getIntent().getFloatExtra(SPEED_RATE, 1);
        FrameLayout exoPlayerMainFrame = findViewById(R.id.exo_player_main_frame);
        exoPlayerUtil =
                new ExoPlayerUtil(this, exoPlayerMainFrame, url, startPosition, true, speedRate);
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

}
