package `in`.testpress.course.util

import `in`.testpress.core.TestpressSdk
import `in`.testpress.core.TestpressSession
import `in`.testpress.models.InstituteSettings
import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelection
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.Robolectric.setupActivity
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.O_MR1])
@RunWith(RobolectricTestRunner::class)
class InitializePlayerTest {

    @Mock
    lateinit var exoPlayerUtil: ExoPlayerUtil

    private var activity = setupActivity(EmptyActivity::class.java)

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        val instituteSettings = InstituteSettings("http://localhost:9200")
        TestpressSdk.setTestpressSession(
                ApplicationProvider.getApplicationContext(),
                TestpressSession(instituteSettings, "USER_TOKEN")
        )
    }

    @Test
    fun testPlayerShouldBeInitialized() {
        exoPlayerUtil.initializePlayer(activity)
        Assert.assertNotNull(exoPlayerUtil.player)
    }

    @Test
    fun testAudioManagerShouldBeInitialized() {
        exoPlayerUtil.initializePlayer(activity)
        Assert.assertNotNull(exoPlayerUtil.audioManager)
    }

    private fun ExoPlayerUtil.initializePlayer(activity: Activity) {
        if (exoPlayerUtil.player == null) {
            exoPlayerUtil.player = buildPlayer(activity)
        }
        initializeAudioManager()
    }

    private fun buildPlayer(activity: Activity): SimpleExoPlayer {
        val videoTrackSelectionFactory: TrackSelection.Factory = AdaptiveTrackSelection.Factory()
        val trackSelector = DefaultTrackSelector(activity, videoTrackSelectionFactory)
        return SimpleExoPlayer.Builder(activity, DefaultRenderersFactory(activity))
                .setTrackSelector(trackSelector).build()
    }

    private fun initializeAudioManager() {
        exoPlayerUtil.audioManager = activity.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }
}
