package in.testpress.course;

import junit.framework.AssertionFailedError;

import org.junit.Test;

import in.testpress.course.network.TestpressCourseApiClient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TestpressCourseApiClientTest {

    @Test
    public void testConstructor_withNullContext() throws Exception {
        try {
            new TestpressCourseApiClient(null);
            fail();
        } catch (AssertionFailedError e) {
            assertEquals("Context must not be null.", e.getMessage());
        }
    }

}
