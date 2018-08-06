package in.testpress.course.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

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

    private ExoPlayerUtil exoPlayerUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testpress_exo_player_view);

        PlayerView playerView = findViewById(R.id.exo_player_view);
        int matchParent = ConstraintLayout.LayoutParams.MATCH_PARENT;
        playerView.setLayoutParams(new ConstraintLayout.LayoutParams(matchParent, matchParent));
        ImageView fullScreenIconView = findViewById(R.id.exo_fullscreen_icon);
        findViewById(R.id.exo_fullscreen_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK, getDataToSetResult());
                exoPlayerUtil.releasePlayer();
                finish();
            }
        });
        fullScreenIconView.setImageResource(R.drawable.testpress_fullscreen_exit);
        String url = getIntent().getStringExtra(VIDEO_URL);
        long startPosition = getIntent().getLongExtra(START_POSITION, 0);
        View exoPlayerLayout = findViewById(R.id.exo_player_layout);
        exoPlayerUtil = new ExoPlayerUtil(this, exoPlayerLayout, url, startPosition, true);
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
    public void onBackPressed() {
        setResult(RESULT_CANCELED, getDataToSetResult());
        exoPlayerUtil.releasePlayer();
        super.onBackPressed();
    }

    Intent getDataToSetResult() {
        Intent intent = new Intent();
        intent.putExtra(START_POSITION, Math.max(0, exoPlayerUtil.getStartPosition()));
        intent.putExtra(PLAY_WHEN_READY, exoPlayerUtil.isPlayWhenReady());
        return intent;
    }

}
