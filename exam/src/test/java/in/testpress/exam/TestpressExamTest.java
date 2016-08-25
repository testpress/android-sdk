package in.testpress.exam;

import android.content.Context;
import android.support.v4.app.FragmentActivity;

import org.junit.Test;

import in.testpress.core.TestpressSession;

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
}
