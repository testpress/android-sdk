package in.testpress.exam;

import org.junit.Test;

import in.testpress.exam.network.TestpressExamApiClient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TestpressExamApiClientTest {

    @Test
    public void testInitializeApiClient_withoutInitializeExamModule() throws Exception {
        try {
            new TestpressExamApiClient();
            fail();
        } catch (IllegalStateException e) {
            assertEquals("Exam module is not initialized.", e.getMessage());
        }
    }
}
