package in.testpress.course;

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
            TestpressCourse.showChapters(null, "", null, testpressSession);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Context must not be null.", e.getMessage());
        }
        Context context = mock(Context.class);
        try {
            TestpressCourse.showChapters(context, null, null, testpressSession);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("courseName must not be null or empty.", e.getMessage());
        }
        try {
            TestpressCourse.showChapters(context, "", null, testpressSession);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("courseName must not be null or empty.", e.getMessage());
        }
        try {
            TestpressCourse.showChapters(context, "DummyTitle", null, testpressSession);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("courseId must not be null.", e.getMessage());
        }
        try {
            TestpressCourse.showChapters(context, "DummyTitle", 0, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("TestpressSession must not be null.", e.getMessage());
        }
    }

    @Test
    public void testShowContents_withNullValues() throws Exception {
        TestpressSession testpressSession = mock(TestpressSession.class);
        try {
            TestpressCourse.showContents(null, "", "", testpressSession);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Context must not be null.", e.getMessage());
        }
        Context context = mock(Context.class);
        try {
            TestpressCourse.showContents(context, null, null, testpressSession);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("title must not be null or empty.", e.getMessage());
        }
        try {
            TestpressCourse.showContents(context, "", null, testpressSession);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("title must not be null or empty.", e.getMessage());
        }
        try {
            TestpressCourse.showContents(context, "DummyTitle", null, testpressSession);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("contentsUrl must not be null or empty.", e.getMessage());
        }
        try {
            TestpressCourse.showContents(context, "DummyTitle", "", testpressSession);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("contentsUrl must not be null or empty.", e.getMessage());
        }
        try {
            TestpressCourse.showContents(context, "DummyTitle", "DummyUrl", null);
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
