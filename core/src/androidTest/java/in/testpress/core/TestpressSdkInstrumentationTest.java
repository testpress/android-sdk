package in.testpress.core;

import android.graphics.Typeface;
import android.support.test.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class TestpressSdkInstrumentationTest {

    @Test
    public void testGetTypeface_returnFontInDefaultPath() throws Exception {
        try {
            TestpressSdk.getTypeface(InstrumentationRegistry.getContext(), "dummyPath");
            fail();
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("Could not get typeface"));
        }
    }

    @Test
    public void testGetRubikRegularFont_returnFontInDefaultPath() throws Exception {
        try {
            Typeface typeface = TestpressSdk.getRubikRegularFont(InstrumentationRegistry.getContext());
            assertNotNull("Typeface must not be null", typeface);
        } catch (IllegalStateException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testGetRubikMediumFont_returnFontInDefaultPath() throws Exception {
        try {
            Typeface typeface = TestpressSdk.getRubikMediumFont(InstrumentationRegistry.getContext());
            assertNotNull("Typeface must not be null", typeface);
        } catch (IllegalStateException e) {
            fail(e.getMessage());
        }
    }
}
