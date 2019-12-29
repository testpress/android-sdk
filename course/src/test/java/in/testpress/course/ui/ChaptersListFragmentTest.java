package in.testpress.course.ui;


import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import in.testpress.core.TestpressSDKDatabase;
import in.testpress.core.TestpressSdk;
import in.testpress.core.TestpressSession;
import in.testpress.course.R;
import in.testpress.models.InstituteSettings;
import in.testpress.models.greendao.Chapter;
import in.testpress.models.greendao.ChapterDao;
import in.testpress.ui.UserActivityFragment;
import in.testpress.ui.UserDevicesActivity;
import in.testpress.util.SingleTypeAdapter;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import static in.testpress.course.TestpressCourse.COURSE_ID;
import static org.mockito.Mockito.spy;

@RunWith(RobolectricTestRunner.class)
public class ChaptersListFragmentTest {

    private static final String USER_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6NDYsInVzZXJfaWQiOjQ2LCJlbWFpbCI6IiIsImV4cCI6MTUxOTAzNjUzM30.FUuyJfYNSAw_VcypZsN8_ZHvZra6gHU3njcXmr-TGVU";
    private ChapterDetailActivity activity;
    private InstituteSettings instituteSettings;
    private Chapter chapter;
    private ChaptersListFragment fragment;
    private MockWebServer mockWebServer;


    public void createChapter() {
        ChapterDao chapterDao = TestpressSDKDatabase.getChapterDao(ApplicationProvider.getApplicationContext());
        Chapter chapter = new Chapter();
        chapter.setId((long) 1);
        chapter.setSlug("chapter");
        chapter.setName("Chapter");
        chapter.setContentsCount(3);
        chapter.setCourseId(1);
        chapter.setActive(true);
        chapterDao.insertInTx(chapter);
        this.chapter = chapter;
    }

    @Before
    public void setUp() throws IOException {
        createChapter();
        mockWebServer = new MockWebServer();
        instituteSettings =
                new InstituteSettings("http://localhost:9200");
        instituteSettings.setLockoutLimit(1);
        TestpressSdk.setTestpressSession(ApplicationProvider.getApplicationContext(),
                new TestpressSession(instituteSettings, USER_TOKEN));
        Intent intent = new Intent();
        intent.putExtra(COURSE_ID, "1");
        activity = Robolectric.buildActivity(ChapterDetailActivity.class, intent)
                .create()
                .resume()
                .get();
        activity = spy(activity);
        fragment = new ChaptersListFragment();
        fragment.setArguments(activity.getIntent().getExtras());
        activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment)
                .commitAllowingStateLoss();
        mockWebServer.start(9200);
    }


    @Test
    public void testFragment() {
        Assert.assertNotNull(fragment);
    }

    @Test
    public void testAdapter() {
        List<Chapter> chapters = new ArrayList<>();
        chapters.add(chapter);
        SingleTypeAdapter adapter = fragment.createAdapter(chapters);

        Assert.assertEquals(chapters.size(), adapter.getCount());
        Assert.assertEquals(adapter.getItem(0), chapter);
    }

    @Test
    public void testPager() throws InterruptedException {
        MockResponse successResponse = new MockResponse().setResponseCode(200);
        mockWebServer.enqueue(successResponse);
        mockWebServer.takeRequest();
        fragment.clearItemsAndRefresh();
        RecordedRequest request = mockWebServer.takeRequest();

        Assert.assertEquals("/api/v2.2.1/courses/1/chapters/?page=1&parent=null", request.getPath());
    }

    @After
    public void tearDown() throws IOException {
        mockWebServer.shutdown();
        resetSingleton(TestpressSDKDatabase.class, "database");
        resetSingleton(TestpressSDKDatabase.class, "daoSession");
    }


    public static void resetSingleton(Class clazz, String fieldName) {
        Field instance;
        try {
            instance = clazz.getDeclaredField(fieldName);
            instance.setAccessible(true);
            instance.set(null, null);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

}
