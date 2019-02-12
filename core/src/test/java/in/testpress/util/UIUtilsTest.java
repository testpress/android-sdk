package in.testpress.util;

import android.app.Activity;
import android.content.Context;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.matchers.Null;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.doCallRealMethod;
import static org.powermock.api.mockito.PowerMockito.when;


@RunWith(PowerMockRunner.class)
@PrepareForTest(UIUtils.class)
public class UIUtilsTest {

    @Mock
    Context context;

    @Mock
    Activity activity;

    @Test
    public void testGetActivity_returnNullWhenContextEqualsToNull() {
        Context context;
        context = null;
        Assert.assertEquals(null,UIUtils.getActivity(context));
    }

    @Test
    public void testGetActivity_returnFalse_whenGetActivityReturnsNull() {
        Assert.assertEquals(false, UIUtils.isActivityDestroyed(context));
    }

    @Test
    public void testIsActivityDestroyed_callsIsFinishingOrNot_whenActivityNotNullAndSDKVersionSmallerThanJELLY_BEAN_MR1() {

        PowerMockito.mockStatic(UIUtils.class);
        when(UIUtils.isActivityDestroyed(context)).thenCallRealMethod();
        when(UIUtils.getActivity(context)).thenReturn(activity);

        UIUtils.isActivityDestroyed(context);

        verify(activity, times(1)).isFinishing();
    }
}
