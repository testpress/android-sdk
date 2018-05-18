package in.testpress.exam;

import android.content.Context;
import android.support.v4.app.FragmentActivity;

import org.junit.Test;

import in.testpress.core.TestpressSession;
import in.testpress.models.greendao.Content;
import in.testpress.models.greendao.CourseAttempt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

public class TestpressExamTest {

    @Test
    public void testShowExams_withNullValues() throws Exception {
        TestpressSession testpressSession = mock(TestpressSession.class);
        try {
            TestpressExam.show(null, 0, testpressSession);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Activity must not be null.", e.getMessage());
        }
        FragmentActivity activity = mock(FragmentActivity.class);
        try {
            TestpressExam.show(activity, 0, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("TestpressSession must not be null.", e.getMessage());
        }
        try {
            TestpressExam.show(null, testpressSession);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Context must not be null.", e.getMessage());
        }
        Context context = mock(Context.class);
        try {
            TestpressExam.show(context, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("TestpressSession must not be null.", e.getMessage());
        }
    }

    @Test
    public void testShowCategories_withNullValues() throws Exception {
        TestpressSession testpressSession = mock(TestpressSession.class);
        try {
            TestpressExam.showCategories(null, 0, testpressSession);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Activity must not be null.", e.getMessage());
        }
        FragmentActivity activity = mock(FragmentActivity.class);
        try {
            TestpressExam.showCategories(activity, 0, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("TestpressSession must not be null.", e.getMessage());
        }
        try {
            TestpressExam.showCategories(null, false, testpressSession);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Context must not be null.", e.getMessage());
        }
        Context context = mock(Context.class);
        try {
            TestpressExam.showCategories(context, false, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("TestpressSession must not be null.", e.getMessage());
        }
    }

    @Test
    public void testStartExam_withNullValues() throws Exception {
        TestpressSession testpressSession = mock(TestpressSession.class);
        try {
            TestpressExam.startExam(null, "DummySlug", testpressSession);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Activity must not be null.", e.getMessage());
        }
        FragmentActivity activity = mock(FragmentActivity.class);
        try {
            TestpressExam.startExam(activity, "DummySlug", null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("TestpressSession must not be null.", e.getMessage());
        }
        try {
            TestpressExam.startExam(activity, null, testpressSession);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("PARAM_EXAM_SLUG must not be null or empty.", e.getMessage());
        }
        try {
            TestpressExam.startExam(activity, "", testpressSession);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("PARAM_EXAM_SLUG must not be null or empty.", e.getMessage());
        }
    }

    @Test
    public void testShowExamAttemptedState_withNullValues() throws Exception {
        TestpressSession testpressSession = mock(TestpressSession.class);
        try {
            TestpressExam.showExamAttemptedState(null, "DummySlug", testpressSession);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Activity must not be null.", e.getMessage());
        }
        FragmentActivity activity = mock(FragmentActivity.class);
        try {
            TestpressExam.showExamAttemptedState(activity, "DummySlug", null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("TestpressSession must not be null.", e.getMessage());
        }
        try {
            TestpressExam.showExamAttemptedState(activity, null, testpressSession);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("EXAM_SLUG must not be null or empty.", e.getMessage());
        }
        try {
            TestpressExam.showExamAttemptedState(activity, "", testpressSession);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("EXAM_SLUG must not be null or empty.", e.getMessage());
        }
    }

    @Test
    public void testShowAnalytics_withNullValues() throws Exception {
        TestpressSession testpressSession = mock(TestpressSession.class);
        FragmentActivity activity = mock(FragmentActivity.class);
        try {
            TestpressExam.showAnalytics(null, "DummyUrl", testpressSession);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Activity must not be null.", e.getMessage());
        }
        try {
            TestpressExam.showAnalytics(activity, null, testpressSession);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("analyticsUrl must not be null or empty.", e.getMessage());
        }
        try {
            TestpressExam.showAnalytics(activity, "", testpressSession);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("analyticsUrl must not be null or empty.", e.getMessage());
        }
        try {
            TestpressExam.showAnalytics(activity, "DummyUrl", null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("TestpressSession must not be null.", e.getMessage());
        }
    }

    @Test
    public void testStartCourseExam_withNullValues() throws Exception {
        TestpressSession testpressSession = mock(TestpressSession.class);
        Content courseContent = mock(Content.class);
        FragmentActivity activity = mock(FragmentActivity.class);
        try {
            TestpressExam.startCourseExam(null, courseContent, false, testpressSession);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Activity must not be null.", e.getMessage());
        }
        try {
            TestpressExam.startCourseExam(activity, null, false, testpressSession);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("PARAM_COURSE_CONTENT must not be null.", e.getMessage());
        }
        try {
            TestpressExam.startCourseExam(activity, courseContent, false, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("TestpressSession must not be null.", e.getMessage());
        }
    }

    @Test
    public void testResumeCourseAttempt_withNullValues() throws Exception {
        TestpressSession testpressSession = mock(TestpressSession.class);
        Content courseContent = mock(Content.class);
        CourseAttempt courseAttempt = mock(CourseAttempt.class);
        FragmentActivity activity = mock(FragmentActivity.class);
        try {
            TestpressExam.resumeCourseAttempt(null, courseContent, courseAttempt, false,
                    testpressSession);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Activity must not be null.", e.getMessage());
        }
        try {
            TestpressExam.resumeCourseAttempt(activity, null, courseAttempt, false, testpressSession);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("PARAM_COURSE_CONTENT must not be null.", e.getMessage());
        }
        try {
            TestpressExam.resumeCourseAttempt(activity, courseContent, null, false, testpressSession);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("PARAM_COURSE_ATTEMPT must not be null.", e.getMessage());
        }
        try {
            TestpressExam.resumeCourseAttempt(activity, courseContent, courseAttempt, false, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("TestpressSession must not be null.", e.getMessage());
        }
    }

    @Test
    public void testEndCourseAttempt_withNullValues() throws Exception {
        TestpressSession testpressSession = mock(TestpressSession.class);
        Content courseContent = mock(Content.class);
        CourseAttempt courseAttempt = mock(CourseAttempt.class);
        FragmentActivity activity = mock(FragmentActivity.class);
        try {
            TestpressExam.endCourseAttempt(null, courseContent, courseAttempt, testpressSession);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Activity must not be null.", e.getMessage());
        }
        try {
            TestpressExam.endCourseAttempt(activity, null, courseAttempt, testpressSession);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("PARAM_COURSE_CONTENT must not be null.", e.getMessage());
        }
        try {
            TestpressExam.endCourseAttempt(activity, courseContent, null, testpressSession);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("PARAM_COURSE_ATTEMPT must not be null.", e.getMessage());
        }
        try {
            TestpressExam.endCourseAttempt(activity, courseContent, courseAttempt, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("TestpressSession must not be null.", e.getMessage());
        }
    }

    @Test
    public void testShowBookmarks_withNullValues() throws Exception {
        TestpressSession testpressSession = mock(TestpressSession.class);
        try {
            TestpressExam.showBookmarks(null, testpressSession);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Context must not be null.", e.getMessage());
        }
        Context context = mock(Context.class);
        try {
            TestpressExam.showBookmarks(context, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("TestpressSession must not be null.", e.getMessage());
        }
    }

}
