package in.testpress.util;

import android.content.Context;

import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import in.testpress.R;

public class ImageUtils {

    /**
     * Init the ImageLoader with custom configuration.
     *
     * @param context Context
     * @return Initialized ImageLoader.
     */
    public static ImageLoader initImageLoader(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context must not be null.");
        }
        ImageLoader imageLoader = ImageLoader.getInstance();
        if (imageLoader.isInited()) {
            return imageLoader;
        } else {
            DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                    .cacheOnDisk(true).cacheInMemory(true)
                    .resetViewBeforeLoading(true)
                    .imageScaleType(ImageScaleType.EXACTLY)
                    .showImageForEmptyUri(R.drawable.testpress_placeholder_icon)
                    .showImageOnFail(R.drawable.testpress_placeholder_icon)
                    .showImageOnLoading(R.drawable.testpress_placeholder_icon)
                    .displayer(new FadeInBitmapDisplayer(300)).build();

            ImageLoaderConfiguration config =
                    new ImageLoaderConfiguration.Builder(context.getApplicationContext())
                            .defaultDisplayImageOptions(defaultOptions)
                            .memoryCache(new WeakMemoryCache())
                            .diskCacheSize(500 * 1024 * 1024).build();

            imageLoader.init(config);
            return imageLoader;
        }
    }

    public static DisplayImageOptions getPlaceholdersOption() {
        return new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisk(true).resetViewBeforeLoading(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .showImageForEmptyUri(R.drawable.testpress_placeholder_icon)
                .showImageOnFail(R.drawable.testpress_placeholder_icon)
                .showImageOnLoading(R.drawable.testpress_placeholder_icon).build();
    }
}
