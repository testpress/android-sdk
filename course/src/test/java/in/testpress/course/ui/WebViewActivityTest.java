package in.testpress.course.ui;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import android.webkit.CookieManager;

import junit.framework.Assert;


@RunWith(RobolectricTestRunner.class)
public class WebViewActivityTest {

    @Test
    public void testClearCookies() {
        CookieManager.getInstance().setCookie("http://example.com/", "key=value");
        Assert.assertTrue(CookieManager.getInstance().hasCookies());
        WebViewActivity.clearCookies(ApplicationProvider.getApplicationContext());

        Assert.assertFalse(CookieManager.getInstance().hasCookies());
    }

}
