package in.testpress.util;

import android.content.Context;

import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

public class ImageUtils {

    /**
     * Init the ImageLoader with custom configuration.
     *
     * @param context Context
     * @return Initialized ImageLoader.
     */
    public static ImageLoader initImageLoader(Context context) {
        ImageLoader imageLoader = ImageLoader.getInstance();
        if (imageLoader.isInited()) {
            return imageLoader;
        } else {
            DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                    .cacheOnDisk(true).cacheInMemory(true)
                    .imageScaleType(ImageScaleType.EXACTLY)
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

}
