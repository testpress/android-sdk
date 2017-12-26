package in.testpress.core;

import android.content.Context;

import org.junit.Test;

import in.testpress.models.InstituteSettings;

import static org.mockito.Mockito.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TestpressSdkTest {

    @Test
    public void testGetTestpressSession_withNullContext() throws Exception {
        try {
            TestpressSdk.getTestpressSession(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Context must not be null.", e.getMessage());
        }
    }

    @Test
    public void testSetTestpressSession_withNullContext() throws Exception {
        try {
            TestpressSession testpressSession = mock(TestpressSession.class);
            TestpressSdk.setTestpressSession(null, testpressSession);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Context must not be null.", e.getMessage());
        }
    }

    @Test
    public void testSetTestpressSession_withNullSession() throws Exception {
        try {
            Context context = mock(Context.class);
            TestpressSdk.setTestpressSession(context, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("TestpressSession must not be null.", e.getMessage());
        }
    }

    @Test
    public void testClearActiveSession_withNullContext() throws Exception {
        try {
            TestpressSdk.clearActiveSession(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Context must not be null.", e.getMessage());
        }
    }

    @Test
    public void testHasActiveSession_withNullContext() throws Exception {
        try {
            TestpressSdk.hasActiveSession(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Context must not be null.", e.getMessage());
        }
    }

    @Test
    public void testInitializingSdk_withNullValues() throws Exception {
        TestpressSdk.Provider provider = TestpressSdk.Provider.FACEBOOK;
        InstituteSettings instituteSettings = mock(InstituteSettings.class);
        try {
            TestpressSdk.initialize(null, instituteSettings, "", "", provider);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Context must not be null.", e.getMessage());
        }
        Context context = mock(Context.class);
        try {
            TestpressSdk.initialize(context, null, "", "", provider);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("InstituteSettings must not be null.", e.getMessage());
        }
        try {
            TestpressSdk.initialize(context, instituteSettings, null, "", provider);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("UserId & AccessToken & Provider must not be null.", e.getMessage());
        }
        try {
            TestpressSdk.initialize(context, instituteSettings, "", null, provider);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("UserId & AccessToken & Provider must not be null.", e.getMessage());
        }
        try {
            TestpressSdk.initialize(context, instituteSettings, "", "", null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("UserId & AccessToken & Provider must not be null.", e.getMessage());
        }
    }

    @Test
    public void testGetTypeface_withNullContext() throws Exception {
        try {
            TestpressSdk.getTypeface(null, "dummyPath");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Context must not be null.", e.getMessage());
        }
    }

    @Test
    public void testGetTypeface_withNullPath() throws Exception {
        try {
            Context context = mock(Context.class);
            TestpressSdk.getTypeface(context, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("FontPath must not be null.", e.getMessage());
        }
    }

    @Test
    public void testSetCourseDB_withNullValues() throws Exception {
        try {
            TestpressSdk.setTestpressCourseDBSession(null, "DummyToken");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Context must not be null.", e.getMessage());
        }
        try {
            Context context = mock(Context.class);
            TestpressSdk.setTestpressCourseDBSession(context, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("SessionToken must not be null or Empty.", e.getMessage());
        }
        try {
            Context context = mock(Context.class);
            TestpressSdk.setTestpressCourseDBSession(context, "");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("SessionToken must not be null or Empty.", e.getMessage());
        }
    }

    @Test
    public void testIsNewCourseDBSession_withNullValues() throws Exception {
        try {
            TestpressSdk.isNewCourseDBSession(null, "DummyToken");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Context must not be null.", e.getMessage());
        }
        try {
            Context context = mock(Context.class);
            TestpressSdk.isNewCourseDBSession(context, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("SessionToken must not be null or Empty.", e.getMessage());
        }
        try {
            Context context = mock(Context.class);
            TestpressSdk.isNewCourseDBSession(context, "");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("SessionToken must not be null or Empty.", e.getMessage());
        }
    }

    @Test
    public void testSetExamDB_withNullValues() throws Exception {
        try {
            TestpressSdk.setTestpressExamDBSession(null, "DummyToken");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Context must not be null.", e.getMessage());
        }
        try {
            Context context = mock(Context.class);
            TestpressSdk.setTestpressExamDBSession(context, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("SessionToken must not be null or Empty.", e.getMessage());
        }
        try {
            Context context = mock(Context.class);
            TestpressSdk.setTestpressExamDBSession(context, "");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("SessionToken must not be null or Empty.", e.getMessage());
        }
    }

    @Test
    public void testIsNewExamDBSession_withNullValues() throws Exception {
        try {
            TestpressSdk.isNewExamDBSession(null, "DummyToken");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Context must not be null.", e.getMessage());
        }
        try {
            Context context = mock(Context.class);
            TestpressSdk.isNewExamDBSession(context, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("SessionToken must not be null or Empty.", e.getMessage());
        }
        try {
            Context context = mock(Context.class);
            TestpressSdk.isNewExamDBSession(context, "");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("SessionToken must not be null or Empty.", e.getMessage());
        }
    }

}
