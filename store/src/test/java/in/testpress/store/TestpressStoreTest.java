package in.testpress.store;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.FragmentActivity;

import org.junit.Test;

import in.testpress.core.TestpressSession;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

public class TestpressStoreTest {

    @Test
    public void testShowStore_withNullValues() throws Exception {
        TestpressSession testpressSession = mock(TestpressSession.class);
        try {
            TestpressStore.show(null, 0, testpressSession);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Activity must not be null.", e.getMessage());
        }
        FragmentActivity activity = mock(FragmentActivity.class);
        try {
            TestpressStore.show(activity, 0, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("TestpressSession must not be null.", e.getMessage());
        }
        try {
            TestpressStore.show(null, testpressSession);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Activity must not be null.", e.getMessage());
        }
        try {
            TestpressStore.show(activity, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("TestpressSession must not be null.", e.getMessage());
        }
    }
}
