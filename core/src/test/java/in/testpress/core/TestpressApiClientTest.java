package in.testpress.core;

import android.content.Context;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;

import in.testpress.network.TestpressApiClient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

@RunWith(PowerMockRunner.class)
public class TestpressApiClientTest {

    @Mock
    TestpressSession session;

    @Mock
    Context context;

    @Test
    public void testConstructor_withNullBaseUrl() throws Exception {
        try {
            new TestpressApiClient(null, context);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("BaseUrl must not be null or Empty.", e.getMessage());
        }
    }

    @Test
    public void testConstructor_withNullContext() throws Exception {
        try {
            new TestpressApiClient("dummyBaseUrl", null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Context must not be null.", e.getMessage());
        }
        try {
            new TestpressApiClient(null, session);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Context must not be null.", e.getMessage());
        }
    }

    @Test
    public void testConstructor_withNullSession() throws Exception {
        try {
            new TestpressApiClient(context, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("TestpressSession must not be null.", e.getMessage());
        }
    }

}
