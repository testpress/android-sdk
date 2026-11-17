package in.testpress.core;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowLooper;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import in.testpress.models.InstituteSettings;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static org.mockito.Mockito.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(RobolectricTestRunner.class)
public class TestpressSdkTest {

    private static final String USER_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6NDYsInVzZXJfaWQiOjQ2LCJlbWFpbCI6IiIsImV4cCI6MTUxOTAzNjUzM30.FUuyJfYNSAw_VcypZsN8_ZHvZra6gHU3njcXmr-TGVU";
    private MockWebServer mockWebServer;

    @Before
    public void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @After
    public void tearDown() throws IOException {
        if (mockWebServer != null) {
            mockWebServer.shutdown();
        }
    }

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
    public void testClearActiveSession_withCallback_nullContext() throws Exception {
        try {
            TestpressSdk.clearActiveSession(null, new TestpressCallback<Void>() {
                @Override public void onSuccess(Void result) {}
                @Override public void onException(TestpressException exception) {}
            });
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Context must not be null.", e.getMessage());
        }
    }

    @Test
    public void testClearActiveSession_withNoSession_invokesCallbackImmediately() {
        Context context = ApplicationProvider.getApplicationContext();
        TestpressSdk.clearActiveSession(context);
        assertFalse(TestpressSdk.hasActiveSession(context));

        final AtomicBoolean successCalled = new AtomicBoolean(false);
        TestpressSdk.clearActiveSession(context, new TestpressCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                successCalled.set(true);
            }

            @Override
            public void onException(TestpressException exception) {
                fail("onException should not be called when session is null");
            }
        });

        assertTrue(successCalled.get());
        assertFalse(TestpressSdk.hasActiveSession(context));
    }

    @Test
    public void testClearActiveSession_withNullCallback() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        InstituteSettings instituteSettings = new InstituteSettings(mockWebServer.url("/").toString());
        TestpressSdk.setTestpressSession(context, new TestpressSession(instituteSettings, USER_TOKEN));
        assertTrue(TestpressSdk.hasActiveSession(context));

        mockWebServer.enqueue(new MockResponse().setResponseCode(200));

        TestpressSdk.clearActiveSession(context, null);
        mockWebServer.takeRequest();
        ShadowLooper.idleMainLooper();

        assertFalse(TestpressSdk.hasActiveSession(context));
    }

    @Test
    public void testClearActiveSession_withCallback_success() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        InstituteSettings instituteSettings = new InstituteSettings(mockWebServer.url("/").toString());
        TestpressSdk.setTestpressSession(context, new TestpressSession(instituteSettings, USER_TOKEN));
        assertTrue(TestpressSdk.hasActiveSession(context));

        mockWebServer.enqueue(new MockResponse().setResponseCode(200));

        final AtomicBoolean successCalled = new AtomicBoolean(false);
        TestpressSdk.clearActiveSession(context, new TestpressCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                successCalled.set(true);
            }

            @Override
            public void onException(TestpressException exception) {
                fail("onException should not be called on success");
            }
        });

        mockWebServer.takeRequest();
        ShadowLooper.idleMainLooper();

        assertTrue(successCalled.get());
        assertFalse(TestpressSdk.hasActiveSession(context));
    }

    @Test
    public void testClearActiveSession_withCallback_failureStillClearsLocalData() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        InstituteSettings instituteSettings = new InstituteSettings(mockWebServer.url("/").toString());
        TestpressSdk.setTestpressSession(context, new TestpressSession(instituteSettings, USER_TOKEN));
        assertTrue(TestpressSdk.hasActiveSession(context));

        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        final AtomicBoolean exceptionCalled = new AtomicBoolean(false);
        TestpressSdk.clearActiveSession(context, new TestpressCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                fail("onSuccess should not be called on HTTP 500");
            }

            @Override
            public void onException(TestpressException exception) {
                exceptionCalled.set(true);
            }
        });

        mockWebServer.takeRequest();
        ShadowLooper.idleMainLooper();

        assertTrue(exceptionCalled.get());
        assertFalse(TestpressSdk.hasActiveSession(context));
    }

    @Test
    public void testClearActiveSession_legacySynchronousClearing() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        InstituteSettings instituteSettings = new InstituteSettings(mockWebServer.url("/").toString());
        TestpressSdk.setTestpressSession(context, new TestpressSession(instituteSettings, USER_TOKEN));
        assertTrue(TestpressSdk.hasActiveSession(context));

        mockWebServer.enqueue(new MockResponse().setResponseCode(200));

        // Legacy clearActiveSession should clear local session synchronously before returning
        TestpressSdk.clearActiveSession(context);

        assertFalse(TestpressSdk.hasActiveSession(context));
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
