package in.testpress.core;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.Gson;

import in.testpress.util.FontUtils;

public class TestpressFont {

    public static final Double DEFAULT_FONT_SIZE = 16.0;
    public static final Double DEFAULT_LINE_MULTIPLIER = 1.4;
    public enum TestpressTypeface { DEFAULT, MONOSPACE, SANS_SERIF, SERIF }
    private String fontAssetPath;
    private Integer builtInTypefaceIdentifier;
    private Double size;
    private Double lineMultiplier;

    /**
     * Init TestpressFont with given Typeface & default font size & lineMultiplier.
     *
     * @param testpressTypeface TestpressTypeface, which is an enum of inbuilt Typefaces.
     */
    public TestpressFont(@NonNull TestpressTypeface testpressTypeface) {
        this(testpressTypeface, null, null);
    }

    /**
     * Init TestpressFont with given font asset path & default font size & lineMultiplier.
     *
     * @param fontAssetPath Custom font path which stored in your assets folder.
     *                      <br> eg. "verdana.ttf" or "font/verdana.ttf"
     */
    public TestpressFont(@NonNull String fontAssetPath) {
        this(fontAssetPath, null, null);
    }

    /**
     * Init TestpressFont with given font size & lineMultiplier if it is NonNull & NonZero,
     * otherwise init with default values.
     *
     * @param size Primary font size.
     *             <br> eg. 16.0
     * @param lineMultiplier Primary line(spacing) multiplier.
     *                       <br> eg. 1.4
     */
    public TestpressFont(Double size, Double lineMultiplier) {
        this(TestpressTypeface.DEFAULT, size, lineMultiplier);
    }

    /**
     * Init TestpressFont with given Typeface. And with given font size & lineMultiplier
     * if it is NonNull & NonZero, default values otherwise.
     *
     * @param testpressTypeface TestpressTypeface, which is an enum of inbuilt Typefaces.
     * @param size Primary font size.
     *             <br> eg. 16.0
     * @param lineMultiplier Primary line(spacing) multiplier.
     *                       <br> eg. 1.4
     */
    public TestpressFont(TestpressTypeface testpressTypeface, Double size, Double lineMultiplier) {
        setBuiltInTypefaceIdentifier(testpressTypeface);
        setSize(size);
        setLineMultiplier(lineMultiplier);
    }

    /**
     * Init TestpressFont with given font asset path. And with given font size & lineMultiplier
     * if it is NonNull & NonZero, default values otherwise.
     *
     * @param fontAssetPath Custom font path which stored in your assets folder.
     *                      <br> eg. "verdana.ttf" or "font/verdana.ttf"
     * @param size Primary font size.
     *             <br> eg. 16.0
     * @param lineMultiplier Primary line(spacing) multiplier.
     *                       <br> eg. 1.4
     */
    public TestpressFont(@NonNull String fontAssetPath, Double size, Double lineMultiplier) {
        setFontAssetPath(fontAssetPath);
        setSize(size);
        setLineMultiplier(lineMultiplier);
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

    public Double getSize() {
        return size;
    }

    public void setSize(Double size) {
        if (size == null || size == 0) {
            this.size = DEFAULT_FONT_SIZE;
        } else {
            this.size = size;
        }
    }

    public Double getLineMultiplier() {
        return lineMultiplier;
    }

    void setLineMultiplier(Double lineMultiplier) {
        if (lineMultiplier == null || lineMultiplier == 0) {
            this.lineMultiplier = DEFAULT_LINE_MULTIPLIER;
        } else {
            this.lineMultiplier = lineMultiplier;
        }
    }

    public static String serialize(TestpressFont testpressFont) {
        if (testpressFont == null) {
            testpressFont = new TestpressFont(TestpressTypeface.DEFAULT, DEFAULT_FONT_SIZE,
                    DEFAULT_LINE_MULTIPLIER);
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
        return new TestpressFont(TestpressTypeface.DEFAULT, DEFAULT_FONT_SIZE, DEFAULT_LINE_MULTIPLIER);
    }

}
