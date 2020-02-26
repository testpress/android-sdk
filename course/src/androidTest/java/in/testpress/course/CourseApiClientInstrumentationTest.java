package in.testpress.course;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import in.testpress.course.api.TestpressCourseApiClient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class CourseApiClientInstrumentationTest {

    @Test
    public void testConstructor_withNullSession() throws Exception {
        try {
            new TestpressCourseApiClient(InstrumentationRegistry.getTargetContext());
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("TestpressSession must not be null.", e.getMessage());
        }
    }
}
