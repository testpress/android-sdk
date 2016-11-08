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
    public void testShowExams_withNullValues() throws Exception {
        TestpressSession testpressSession = mock(TestpressSession.class);
        try {
            TestpressCourse.show(null, 0, testpressSession);
            fail();
        } catch (AssertionError e) {
            assertEquals("Activity must not be null.", e.getMessage());
        }
        FragmentActivity activity = mock(FragmentActivity.class);
        try {
            TestpressCourse.show(activity, 0, null);
            fail();
        } catch (AssertionError e) {
            assertEquals("TestpressSession must not be null.", e.getMessage());
        }
        try {
            TestpressCourse.show(null, testpressSession);
            fail();
        } catch (AssertionError e) {
            assertEquals("Context must not be null.", e.getMessage());
        }
        Context context = mock(Context.class);
        try {
            TestpressCourse.show(context, null);
            fail();
        } catch (AssertionError e) {
            assertEquals("TestpressSession must not be null.", e.getMessage());
        }
    }
}
