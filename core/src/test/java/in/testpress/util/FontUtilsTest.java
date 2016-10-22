package in.testpress.util;

import android.content.Context;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

public class FontUtilsTest {

    @Test
    public void testGetTypeface_withNullContext() throws Exception {
        try {
            FontUtils.getTypeface(null, "dummyPath");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Context must not be null.", e.getMessage());
        }
    }

    @Test
    public void testGetTypeface_withNullFontAssetPath() throws Exception {
        try {
            Context context = mock(Context.class);
            FontUtils.getTypeface(context, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Font asset path must not be null or Empty.", e.getMessage());
        }
    }

    @Test
    public void testGetTypeface_withEmptyFontAssetPath() throws Exception {
        try {
            Context context = mock(Context.class);
            FontUtils.getTypeface(context, "");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Font asset path must not be null or Empty.", e.getMessage());
        }
    }

    @Test
    public void testReplaceFont_withNullContext() throws Exception {
        try {
            FontUtils.replaceFont(null, "DEFAULT", "dummyPath");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Context must not be null.", e.getMessage());
        }
    }

    @Test
    public void testReplaceFont_withNullFontAssetPath() throws Exception {
        try {
            Context context = mock(Context.class);
            FontUtils.replaceFont(context, "DEFAULT", null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Font asset path must not be null or Empty.", e.getMessage());
        }
    }

    @Test
    public void testReplaceFont_withEmptyFontAssetPath() throws Exception {
        try {
            Context context = mock(Context.class);
            FontUtils.replaceFont(context, "DEFAULT", "");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Font asset path must not be null or Empty.", e.getMessage());
        }
    }
}
