package in.testpress.util;

import android.content.Context;
import android.graphics.Typeface;
import android.view.ViewGroup;

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

    @Test
    public void testReplaceFont_withNullStaticTypefaceFieldName() throws Exception {
        try {
            Typeface typeface = mock(Typeface.class);
            FontUtils.replaceFont(null, typeface);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Static Typeface FieldName must not be null or Empty.", e.getMessage());
        }
    }

    @Test
    public void testReplaceFont_withEmptyStaticTypefaceFieldName() throws Exception {
        try {
            Typeface typeface = mock(Typeface.class);
            FontUtils.replaceFont("", typeface);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Static Typeface FieldName must not be null or Empty.", e.getMessage());
        }
    }

    @Test
    public void testReplaceFont_withNullTypeface() throws Exception {
        try {
            FontUtils.replaceFont("DEFAULT", null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Typeface must not be null.", e.getMessage());
        }
    }

    @Test
    public void testApplyTestpressFont_withNullContext() throws Exception {
        ViewGroup viewGroup = mock(ViewGroup.class);
        try {
            FontUtils.applyTestpressFont(null, viewGroup);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Context must not be null.", e.getMessage());
        }
        try {
            FontUtils.applyTestpressFont(null, viewGroup, true);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Context must not be null.", e.getMessage());
        }
    }

    @Test
    public void testApplyTestpressFont_withNullViewGroup() throws Exception {
        Context context = mock(Context.class);
        try {
            FontUtils.applyTestpressFont(context, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("ViewGroup must not be null.", e.getMessage());
        }
        try {
            FontUtils.applyTestpressFont(context, null, true);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("ViewGroup must not be null.", e.getMessage());
        }
    }

}
