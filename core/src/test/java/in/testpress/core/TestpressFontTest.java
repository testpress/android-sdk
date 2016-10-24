package in.testpress.core;

import android.graphics.Typeface;

import com.google.gson.Gson;

import org.junit.Test;

import static in.testpress.core.TestpressFont.TestpressTypeface.DEFAULT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

public class TestpressFontTest {

    @Test
    public void testConstructor_withEmptyFontAssetPath() throws Exception {
        try {
            new TestpressFont("");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Font asset path must not be null or Empty.", e.getMessage());
        }
    }

    @Test
    public void testGetBuiltInTypeface_withNullBuiltInTypefaceIdentifier() throws Exception {
        TestpressFont testpressFont = mock(TestpressFont.class);
        assertBuiltInTypefaceEqualsDefault(testpressFont);
    }

    @Test
    public void testSetFontAssetPath_withNullFontAssetPath() throws Exception {
        try {
            TestpressFont testpressFont = new TestpressFont(DEFAULT);
            testpressFont.setFontAssetPath(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Font asset path must not be null or Empty.", e.getMessage());
        }
    }

    @Test
    public void testSetFontAssetPath_withEmptyFontAssetPath() throws Exception {
        try {
            TestpressFont testpressFont = new TestpressFont(DEFAULT);
            testpressFont.setFontAssetPath("");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Font asset path must not be null or Empty.", e.getMessage());
        }
    }

    @Test
    public void testSetBuiltInTypefaceIdentifier_withNullTestpressTypeface() throws Exception {
        TestpressFont testpressFont = mock(TestpressFont.class);
        testpressFont.setBuiltInTypefaceIdentifier(null);
        assertBuiltInTypefaceEqualsDefault(testpressFont);
    }

    @Test
    public void testGetTypeface_withNullContext() throws Exception {
        try {
            TestpressFont testpressFont = new TestpressFont("dummyPath");
            testpressFont.getTypeface(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Context must not be null.", e.getMessage());
        }
    }

    @Test
    public void testSerialize_withNullTestpressFont() throws Exception {
        TestpressFont defaultTestpressFont =
                new TestpressFont(DEFAULT);
        Gson gson = new Gson();
        assertEquals(gson.toJson(defaultTestpressFont), TestpressFont.serialize(null));
    }

    @Test
    public void testDeserialize_withNullSerializedTestpressFont() throws Exception {
        assertBuiltInTypefaceEqualsDefault(TestpressFont.deserialize(null));
    }

    @Test
    public void testDeserialize_withEmptySerializedTestpressFont() throws Exception {
        assertBuiltInTypefaceEqualsDefault(TestpressFont.deserialize(""));
    }

    private void assertBuiltInTypefaceEqualsDefault(TestpressFont testpressFont) {
        assertEquals(Typeface.DEFAULT, testpressFont.getBuiltInTypeface());
    }

}
