package in.testpress.course.util;



import androidx.test.core.app.ApplicationProvider;

import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;

import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;


@RunWith(RobolectricTestRunner.class)
@Config(application=TestpressApplication.class)
public class TestpressApplicationTest {

    private TestpressApplication application;

    @Before
    public void setUp() {
        application = (TestpressApplication) ApplicationProvider.getApplicationContext();
    }

    @Test
    public void testBuildDataSourceFactory() {
        Assert.assertTrue(application.buildDataSourceFactory() instanceof CacheDataSourceFactory);
    }

    @Test
    public void testGetDownloadCache() {
        Assert.assertTrue(application.getDownloadCache() instanceof SimpleCache);
    }

    @Test
    public void testGetDownloadManager() {
        Assert.assertNotNull(application.getDownloadManager());
    }

    @Test
    public void testGetDownloadTracker() {
        Assert.assertNotNull(application.getDownloadTracker());
        Assert.assertNotNull(application.getDownloadManager());

    }
}
