package in.testpress.core;

import android.graphics.Typeface;

import com.google.gson.Gson;

import junit.framework.AssertionFailedError;

import org.junit.Test;

import static in.testpress.core.TestpressFont.DEFAULT_FONT_SIZE;
import static in.testpress.core.TestpressFont.DEFAULT_LINE_MULTIPLIER;
import static in.testpress.core.TestpressFont.TestpressTypeface.DEFAULT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

public class TestpressFontTest {

    @Test
    public void testConstructor_withEmptyFontAssetPath() throws Exception {
        try {
            new TestpressFont("");
            new TestpressFont("", DEFAULT_FONT_SIZE, DEFAULT_LINE_MULTIPLIER);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Font asset path must not be null or Empty.", e.getMessage());
        }
    }

    @Test
    public void testConstructor_withNullFontSize() throws Exception {
        assertFontSizeEqualsDefault(new TestpressFont(null, DEFAULT_LINE_MULTIPLIER));
        assertFontSizeEqualsDefault(new TestpressFont(DEFAULT, null, DEFAULT_LINE_MULTIPLIER));
        assertFontSizeEqualsDefault(new TestpressFont("dummyPath", null, DEFAULT_LINE_MULTIPLIER));
    }

    @Test
    public void testConstructor_withFontSizeZero() throws Exception {
        assertFontSizeEqualsDefault(new TestpressFont(0.0, DEFAULT_LINE_MULTIPLIER));
        assertFontSizeEqualsDefault(new TestpressFont(DEFAULT, 0.0, DEFAULT_LINE_MULTIPLIER));
        assertFontSizeEqualsDefault(new TestpressFont("dummyPath", 0.0, DEFAULT_LINE_MULTIPLIER));
    }

    @Test
    public void testConstructor_withNullLineMultiplier() throws Exception {
        assertLineMultiplierEqualsDefault(new TestpressFont(DEFAULT_FONT_SIZE, null));
        assertLineMultiplierEqualsDefault(new TestpressFont(DEFAULT, DEFAULT_FONT_SIZE, null));
        assertLineMultiplierEqualsDefault(new TestpressFont("dummyPath", DEFAULT_FONT_SIZE, null));
    }

    @Test
    public void testConstructor_withLineMultiplierZero() throws Exception {
        assertLineMultiplierEqualsDefault(new TestpressFont(DEFAULT_FONT_SIZE, 0.0));
        assertLineMultiplierEqualsDefault(new TestpressFont(DEFAULT, DEFAULT_FONT_SIZE, 0.0));
        assertLineMultiplierEqualsDefault(new TestpressFont("dummyPath", DEFAULT_FONT_SIZE, 0.0));

    }

    @Test
    public void testSetFontSize_withNullFontSize() throws Exception {
        TestpressFont testpressFont = new TestpressFont(DEFAULT);
        testpressFont.setSize(null);
        assertFontSizeEqualsDefault(testpressFont);
    }

    @Test
    public void testSetFontSize_withFontSizeZero() throws Exception {
        TestpressFont testpressFont = new TestpressFont(DEFAULT);
        testpressFont.setSize(0.0);
        assertFontSizeEqualsDefault(testpressFont);
    }

    @Test
    public void testSetLineMultiplier_withNullLineMultiplier() throws Exception {
        TestpressFont testpressFont = new TestpressFont(DEFAULT);
        testpressFont.setLineMultiplier(null);
        assertLineMultiplierEqualsDefault(testpressFont);
    }

    @Test
    public void testSetLineMultiplier_withLineMultiplierZero() throws Exception {
        TestpressFont testpressFont = new TestpressFont(DEFAULT);
        testpressFont.setLineMultiplier(0.0);
        assertLineMultiplierEqualsDefault(testpressFont);
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
                new TestpressFont(DEFAULT, DEFAULT_FONT_SIZE, DEFAULT_LINE_MULTIPLIER);
        Gson gson = new Gson();
        assertEquals(gson.toJson(defaultTestpressFont), TestpressFont.serialize(null));
    }

    @Test
    public void testDeserialize_withNullSerializedTestpressFont() throws Exception {
        assertTestpressFontEqualDefault(TestpressFont.deserialize(null));
    }

    @Test
    public void testDeserialize_withEmptySerializedTestpressFont() throws Exception {
        assertTestpressFontEqualDefault(TestpressFont.deserialize(""));
    }

    private void assertBuiltInTypefaceEqualsDefault(TestpressFont testpressFont) {
        assertEquals(Typeface.DEFAULT, testpressFont.getBuiltInTypeface());
    }

    private void assertFontSizeEqualsDefault(TestpressFont testpressFont) {
        assertEquals(DEFAULT_FONT_SIZE, testpressFont.getSize());
    }

    private void assertLineMultiplierEqualsDefault(TestpressFont testpressFont) {
        assertEquals(DEFAULT_LINE_MULTIPLIER, testpressFont.getLineMultiplier());
    }

    private void assertTestpressFontEqualDefault(TestpressFont testpressFont) {
        if (!testpressFont.getSize().equals(DEFAULT_FONT_SIZE) &&
                !testpressFont.getLineMultiplier().equals(DEFAULT_LINE_MULTIPLIER)) {
            throw new AssertionFailedError("TestpressFont not equals to default");
        }
        assertBuiltInTypefaceEqualsDefault(testpressFont);
    }
}
