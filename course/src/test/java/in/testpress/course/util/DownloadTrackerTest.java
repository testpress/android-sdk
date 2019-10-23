package in.testpress.course.util;

import android.net.Uri;

import androidx.fragment.app.Fragment;
import androidx.test.core.app.ApplicationProvider;

import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.offline.Download;
import com.google.android.exoplayer2.offline.DownloadCursor;
import com.google.android.exoplayer2.offline.DownloadIndex;
import com.google.android.exoplayer2.offline.DownloadManager;
import com.google.android.exoplayer2.offline.DownloadProgress;
import com.google.android.exoplayer2.offline.DownloadRequest;
import com.google.android.exoplayer2.offline.DownloadService;
import com.google.android.exoplayer2.offline.StreamKey;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

@RunWith(RobolectricTestRunner.class)
@Config(application=TestpressApplication.class)
public class DownloadTrackerTest {

    private TestpressApplication application;
    private DownloadTracker downloadTracker;
    private DownloadManager downloadManager;
    public static final String DATA = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static Random RANDOM = new Random();

    @Before
    public void setUp() {
        application = (TestpressApplication) ApplicationProvider.getApplicationContext();
        downloadTracker = application.getDownloadTracker();
        downloadManager = application.getDownloadManager();

    }

    public static String getRandomString(int len) {
        StringBuilder sb = new StringBuilder(len);

        for (int i = 0; i < len; i++) {
            sb.append(DATA.charAt(RANDOM.nextInt(DATA.length())));
        }

        return sb.toString();
    }

    public HashMap<Uri, Download> createDownload(int n) {
        HashMap<Uri, Download> hashMap = new HashMap<>();
        ArrayList<StreamKey> streamKeys= new ArrayList<>();
        for (int i=0; i<n; i++) {
            String url = String.format("https://google.com/%s/", getRandomString(8));
            DownloadRequest downloadRequest = new DownloadRequest("a", "b", Uri.parse(url), streamKeys, null, null);
            DownloadProgress downloadProgress =  new DownloadProgress();
            downloadProgress.bytesDownloaded = 4000;
            downloadProgress.percentDownloaded = 25;
            Download download = new Download(downloadRequest, 1, 12l, 12l, 12l, 1, 0, downloadProgress);
            hashMap.put(Uri.parse(url), download);
        }
        return hashMap;
    }

    public void setDownloads(HashMap downloads) {
        try {
            Field f1 = downloadTracker.getClass().getDeclaredField("downloads");
            f1.setAccessible(true);
            f1.set(downloadTracker, downloads);
        } catch (Exception e){}
    }

    public Fragment getFragment() {
        return new Fragment();
    }

    public RenderersFactory getRenderers() {
        return new DefaultRenderersFactory(application);
    }


    @Test
    public void testGetDownloads() {
        setDownloads(createDownload(5));

        Assert.assertEquals(downloadTracker.getDownloads().size(), 5);
    }

    @Test
    public void testIsDownloaded() {
        HashMap<Uri, Download> downloads = createDownload(5);
        setDownloads(downloads);

        Assert.assertTrue(downloadTracker.isDownloaded(downloads.keySet().iterator().next()));
        Assert.assertFalse(downloadTracker.isDownloaded(Uri.parse("http://yahoo.com")));
    }

    @Test
    public void testGetPercentDownloaded() {
        HashMap<Uri, Download> downloads = createDownload(5);
        Download download = downloads.get(downloads.keySet().iterator().next());

        Assert.assertEquals(download.getPercentDownloaded(), 25.0, 0.1);
    }

    @Test
    public void testIsRemoved() {
        HashMap<Uri, Download> downloads = createDownload(5);
        setDownloads(downloads);

        Assert.assertTrue(downloadTracker.isRemoved(Uri.parse("https://yahoo.com")));
        Assert.assertFalse(downloadTracker.isRemoved(downloads.keySet().iterator().next()));
    }
}
