package in.testpress.util;

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import com.nostra13.universalimageloader.core.ImageLoader;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class ImageUtilsTest {

    @Test
    public void testInitImageLoader_whenImageLoaderNotInitialized() throws Exception {
        if (ImageLoader.getInstance().isInited()) {
            ImageLoader.getInstance().destroy();
        }
        ImageUtils.initImageLoader(InstrumentationRegistry.getContext());
        assertEquals("ImageLoader not initialized", true, ImageLoader.getInstance().isInited());
    }

    @Test
    public void testInitImageLoader_whenImageLoaderAlreadyInitialized() throws Exception {
        testInitImageLoader_whenImageLoaderNotInitialized();
        ImageUtils.initImageLoader(InstrumentationRegistry.getContext());
        assertEquals("ImageLoader not initialized", true, ImageLoader.getInstance().isInited());
    }

}
