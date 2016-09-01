package in.testpress.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class TestpressSessionTest {

    @Test
    public void testConstructor_withNullBaseUrl() throws Exception {
        try {
            new TestpressSession(null, "dummyToken");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("BaseUrl must not be null or Empty.", e.getMessage());
        }
    }

    @Test
    public void testConstructor_withEmptyBaseUrl() throws Exception {
        try {
            new TestpressSession("", "dummyToken");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("BaseUrl must not be null or Empty.", e.getMessage());
        }
    }

    @Test
    public void testConstructor_withNullToken() throws Exception {
        try {
            new TestpressSession("dummyBaseUrl", null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("AuthToken must not be null or Empty.", e.getMessage());
        }
    }

    @Test
    public void testConstructor_withEmptyToken() throws Exception {
        try {
            new TestpressSession("dummyBaseUrl", "");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("AuthToken must not be null or Empty.", e.getMessage());
        }
    }

    @Test
    public void testSetToken_withNullBaseUrl() throws Exception {
        try {
            TestpressSession session = new TestpressSession("dummyBaseUrl", "dummyToken");
            session.setBaseUrl(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("BaseUrl must not be null or Empty.", e.getMessage());
        }
    }

    @Test
    public void testSetToken_withNullToken() throws Exception {
        try {
            TestpressSession session = new TestpressSession("dummyBaseUrl", "dummyToken");
            session.setToken(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("AuthToken must not be null or Empty.", e.getMessage());
        }
    }

    @Test
    public void testSerialize_withNullSession() throws Exception {
        try {
            TestpressSession.serialize(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("TestpressSession must not be null.", e.getMessage());
        }
    }

    @Test
    public void testDeserialize_withNullSession() throws Exception {
        assertNull(TestpressSession.deserialize(null));
    }

    @Test
    public void testDeserialize_withEmptySession() throws Exception {
        assertNull(TestpressSession.deserialize(""));
    }
}
