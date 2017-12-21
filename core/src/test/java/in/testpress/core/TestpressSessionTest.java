package in.testpress.core;

import org.junit.Test;

import in.testpress.models.InstituteSettings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

public class TestpressSessionTest {

    @Test
    public void testConstructor_withNullInstituteSettings() throws Exception {
        try {
            new TestpressSession(null, "dummyToken");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("InstituteSettings must not be null.", e.getMessage());
        }
    }

    @Test
    public void testConstructor_withNullToken() throws Exception {
        InstituteSettings instituteSettings = mock(InstituteSettings.class);
        try {
            new TestpressSession(instituteSettings, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("AuthToken must not be null or Empty.", e.getMessage());
        }
    }

    @Test
    public void testConstructor_withEmptyToken() throws Exception {
        InstituteSettings instituteSettings = mock(InstituteSettings.class);
        try {
            new TestpressSession(instituteSettings, "");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("AuthToken must not be null or Empty.", e.getMessage());
        }
    }

    @Test
    public void testBaseUrl_withNullBaseUrl() throws Exception {
        InstituteSettings instituteSettings = mock(InstituteSettings.class);
        try {
            TestpressSession session = new TestpressSession(instituteSettings, "dummyToken");
            session.setInstituteSettings(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("InstituteSettings must not be null.", e.getMessage());
        }
    }

    @Test
    public void testSetToken_withNullToken() throws Exception {
        InstituteSettings instituteSettings = mock(InstituteSettings.class);
        try {
            TestpressSession session = new TestpressSession(instituteSettings, "dummyToken");
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
