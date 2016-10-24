package in.testpress.core;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.Gson;

import in.testpress.util.FontUtils;

public class TestpressFont {

    public enum TestpressTypeface { DEFAULT, MONOSPACE, SANS_SERIF, SERIF }
    private String fontAssetPath;
    private Integer builtInTypefaceIdentifier;

    /**
     * Init TestpressFont with given Typeface.
     *
     * @param testpressTypeface TestpressTypeface, which is an enum of inbuilt Typefaces.
     */
    public TestpressFont(@NonNull TestpressTypeface testpressTypeface) {
        setBuiltInTypefaceIdentifier(testpressTypeface);
    }

    /**
     * Init TestpressFont with given font asset path.
     *
     * @param fontAssetPath Custom font path which stored in your assets folder.
     *                      <br> eg. "verdana.ttf" or "font/verdana.ttf"
     */
    public TestpressFont(@NonNull String fontAssetPath) {
        setFontAssetPath(fontAssetPath);
    }

    @Nullable
    public String getFontAssetPath() {
        return fontAssetPath;
    }

    void setFontAssetPath(String fontAssetPath) {
        if (fontAssetPath == null || fontAssetPath.isEmpty()) {
            throw new IllegalArgumentException("Font asset path must not be null or Empty.");
        }
        this.fontAssetPath = fontAssetPath;
    }

    Typeface getBuiltInTypeface() {
        if (builtInTypefaceIdentifier != null) {
            switch (TestpressTypeface.values()[builtInTypefaceIdentifier]){
                case SERIF:
                    return Typeface.SERIF;
                case SANS_SERIF:
                    return Typeface.SANS_SERIF;
                case MONOSPACE:
                    return Typeface.MONOSPACE;
            }
        }
        return Typeface.DEFAULT;
    }

    void setBuiltInTypefaceIdentifier(TestpressTypeface testpressTypeface) {
        if (testpressTypeface == null) {
            testpressTypeface = TestpressTypeface.DEFAULT;
        }
        this.builtInTypefaceIdentifier = testpressTypeface.ordinal();
    }

    /**
     * Get the {@link Typeface} created from the font path if exists, or from TestpressTypeface
     * if exists or DEFAULT
     *
     * @param context Context
     * @return Typeface
     */
    public Typeface getTypeface(Context context) {
        if (getFontAssetPath() == null) {
            return getBuiltInTypeface();
        }
        return FontUtils.getTypeface(context, getFontAssetPath());
    }

    public static String serialize(TestpressFont testpressFont) {
        if (testpressFont == null) {
            testpressFont = new TestpressFont(TestpressTypeface.DEFAULT);
        }
        Gson gson = new Gson();
        return gson.toJson(testpressFont);
    }

    public static TestpressFont deserialize(String serializedTestpressFont) {
        if (serializedTestpressFont != null && !serializedTestpressFont.isEmpty()) {
            try {
                Gson gson = new Gson();
                return gson.fromJson(serializedTestpressFont, TestpressFont.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new TestpressFont(TestpressTypeface.DEFAULT);
    }

}
