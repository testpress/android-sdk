package in.testpress.exam.ui;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import in.testpress.exam.models.Permission;
import in.testpress.models.greendao.Attempt;
import in.testpress.models.greendao.Content;
import in.testpress.models.greendao.CourseAttempt;
import in.testpress.models.greendao.Exam;
import in.testpress.util.CommonTestUtils;

import static in.testpress.exam.ui.TestActivity.PARAM_EXAM_SLUG;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TestActivityTest {

    private static final int NUMBER_OF_RETROFIT_CALLS = 4;

    @Mock private TestActivity activity;
    @Mock private Content content;
    @Mock private CourseAttempt courseAttempt;

    @Test
    public void test_onDataInitialized_checkPermission_ifPermissionAndCourseAttemptIsNull() {
        activity.courseContent = content;

        doCallRealMethod().when(activity).onDataInitialized();
        activity.onDataInitialized();

        verify(activity, times(1)).checkPermission();
    }

    @Test
    public void test_onDataInitialized_checkStartExamScreenState_ifPermissionIsNonNull() {
        activity.courseContent = content;
        activity.permission = mock(Permission.class);

        doCallRealMethod().when(activity).onDataInitialized();
        activity.onDataInitialized();

        verify(content, times(1)).getRawExam();
        verify(activity, times(1)).checkStartExamScreenState();
    }

    @Test
    public void test_onDataInitialized_checkStartExamScreenState_ifCourseAttemptIsNonNull() {
        activity.courseContent = content;
        activity.courseAttempt = courseAttempt;

        doCallRealMethod().when(activity).onDataInitialized();
        activity.onDataInitialized();

        verify(content, times(1)).getRawExam();
        verify(activity, times(1)).checkStartExamScreenState();
    }

    @Test
    public void test_onDataInitialized_courseAttemptUpdated_ifAttemptIsNonNull() {
        activity.courseContent = content;
        activity.courseAttempt = courseAttempt;
        activity.attempt = mock(Attempt.class);

        doCallRealMethod().when(activity).onDataInitialized();
        activity.onDataInitialized();

        verify(courseAttempt, times(1)).setAssessment(activity.attempt);
    }

    @Test
    public void test_onDataInitialized_checkStartExamScreenState_ifContentIsNullAndExamIsNonNull() {
        activity.exam = mock(Exam.class);

        doCallRealMethod().when(activity).onDataInitialized();
        activity.onDataInitialized();

        verify(activity, times(1)).checkStartExamScreenState();
    }

    @Test
    public void test_onDataInitialized_loadExam_ifContentAndExamIsNullAndSlugIsNonNull() {
        TestActivity activity = mock(TestActivity.class, RETURNS_DEEP_STUBS);
        String testUrl = "DummyUrl";
        when(activity.getIntent().getStringExtra(PARAM_EXAM_SLUG)).thenReturn(testUrl);

        doCallRealMethod().when(activity).onDataInitialized();
        activity.onDataInitialized();

        verify(activity, times(1)).loadExam(testUrl);
    }

    @Test
    public void test_onDataInitialized_throwException_ifContentAndExamAndSlugIsNull() {
        TestActivity activity = mock(TestActivity.class, RETURNS_DEEP_STUBS);
        when(activity.getIntent().getStringExtra(PARAM_EXAM_SLUG)).thenReturn(null);

        doCallRealMethod().when(activity).onDataInitialized();

        try {
            activity.onDataInitialized();
            fail("PARAM_EXAM_SLUG must not be null or empty.");
        } catch (IllegalArgumentException e) {
            assertEquals("PARAM_EXAM_SLUG must not be null or empty.", e.getMessage());
        }
    }

    @Test
    public void testTestActivity_getRetrofitCalls_returnCorrectValues() {
        CommonTestUtils.testGetRetrofitCallsReturnCorrectValues(
                new TestActivity(),
                NUMBER_OF_RETROFIT_CALLS
        );
    }
}
