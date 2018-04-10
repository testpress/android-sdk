package in.testpress.course;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.FragmentActivity;

import org.junit.Test;

import in.testpress.core.TestpressSession;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

public class TestpressCourseTest {

    @Test
    public void testShowCourses_withNullValues() throws Exception {
        TestpressSession testpressSession = mock(TestpressSession.class);
        try {
            TestpressCourse.show(null, 0, testpressSession);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Activity must not be null.", e.getMessage());
        }
        FragmentActivity activity = mock(FragmentActivity.class);
        try {
            TestpressCourse.show(activity, 0, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("TestpressSession must not be null.", e.getMessage());
        }
        try {
            TestpressCourse.show(null, testpressSession);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Context must not be null.", e.getMessage());
        }
        Context context = mock(Context.class);
        try {
            TestpressCourse.show(context, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("TestpressSession must not be null.", e.getMessage());
        }
    }

    @Test
    public void testGetCoursesListFragment_withNullValues() throws Exception {
        try {
            TestpressCourse.getCoursesListFragment(null, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Context must not be null.", e.getMessage());
        }
        Context context = mock(Context.class);
        try {
            TestpressCourse.getCoursesListFragment(context, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("TestpressSession must not be null.", e.getMessage());
        }
    }

    @Test
    public void testShowChapters_withNullValues() throws Exception {
        TestpressSession testpressSession = mock(TestpressSession.class);
        try {
            TestpressCourse.showChapters(null, "", 0, testpressSession);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Activity must not be null.", e.getMessage());
        }
        Activity activity = mock(Activity.class);
        try {
            TestpressCourse.showChapters(activity, "DummyTitle", null, testpressSession);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("courseId must not be null.", e.getMessage());
        }
        try {
            TestpressCourse.showChapters(activity, "DummyTitle", 0, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("TestpressSession must not be null.", e.getMessage());
        }
    }

    @Test
    public void testShowContentsOfChapter_withNullValues() throws Exception {
        TestpressSession testpressSession = mock(TestpressSession.class);
        try {
            TestpressCourse.showChapterContents(null, "", testpressSession);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Activity must not be null.", e.getMessage());
        }
        Activity activity = mock(Activity.class);
        try {
            TestpressCourse.showChapterContents(activity, null, testpressSession);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("chapterUrl must not be null or empty.", e.getMessage());
        }
        try {
            TestpressCourse.showChapterContents(activity, "", testpressSession);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("chapterUrl must not be null or empty.", e.getMessage());
        }
        try {
            TestpressCourse.showChapterContents(activity, "DummyUrl", null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("TestpressSession must not be null.", e.getMessage());
        }
    }

    @Test
    public void testShowContentDetail_withNullValues() throws Exception {
        TestpressSession testpressSession = mock(TestpressSession.class);
        try {
            TestpressCourse.showContentDetail(null, "", testpressSession);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Activity must not be null.", e.getMessage());
        }
        Activity activity = mock(Activity.class);
        try {
            TestpressCourse.showContentDetail(activity, null, testpressSession);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("contentId must not be null or empty.", e.getMessage());
        }
        try {
            TestpressCourse.showContentDetail(activity, "", testpressSession);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("contentId must not be null or empty.", e.getMessage());
        }
        try {
            TestpressCourse.showContentDetail(activity, "1", null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("TestpressSession must not be null.", e.getMessage());
        }
    }

    @Test
    public void testShowLeaderboard_withNullValues() throws Exception {
        TestpressSession testpressSession = mock(TestpressSession.class);
        try {
            TestpressCourse.showLeaderboard(null, 0, testpressSession);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Activity must not be null.", e.getMessage());
        }
        FragmentActivity activity = mock(FragmentActivity.class);
        try {
            TestpressCourse.showLeaderboard(activity, 0, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("TestpressSession must not be null.", e.getMessage());
        }
        try {
            TestpressCourse.showLeaderboard(null, testpressSession);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Context must not be null.", e.getMessage());
        }
        Context context = mock(Context.class);
        try {
            TestpressCourse.showLeaderboard(context, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("TestpressSession must not be null.", e.getMessage());
        }
    }

    @Test
    public void testGetLeaderboardFragment_withNullValues() throws Exception {
        try {
            TestpressCourse.getLeaderboardFragment(null, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Context must not be null.", e.getMessage());
        }
        Context context = mock(Context.class);
        try {
            TestpressCourse.getLeaderboardFragment(context, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("TestpressSession must not be null.", e.getMessage());
        }
    }

}
