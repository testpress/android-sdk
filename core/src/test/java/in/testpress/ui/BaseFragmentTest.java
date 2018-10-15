package in.testpress.ui;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import in.testpress.network.RetrofitCall;
import in.testpress.util.CommonUtils;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.doCallRealMethod;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.support.membermodification.MemberMatcher.methodsDeclaredIn;


@RunWith(PowerMockRunner.class)
@PrepareForTest({ BaseFragment.class, CommonUtils.class })
public class BaseFragmentTest {

    @Mock
    private RetrofitCall retrofitCall;

    @Test
    public void testBaseFragment_onStop_cancelAPIRequests_isCalled() {
        PowerMockito.suppress(methodsDeclaredIn(Fragment.class));
        BaseFragment fragment = mock(BaseFragment.class);

        RetrofitCall[] retrofitCalls = new RetrofitCall[] { retrofitCall };

        when(fragment.getRetrofitCalls()).thenReturn(retrofitCalls);

        doCallRealMethod().when(fragment).onDestroyView();
        fragment.onDestroyView();

        verify(fragment, times(1)).getRetrofitCalls();
        verify(retrofitCall, times(1)).cancel();

        PowerMockito.mockStatic(CommonUtils.class);
        try {
            doNothing().when(CommonUtils.class, "cancelAPIRequests", (Object) retrofitCalls);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

        fragment.onDestroyView();

        PowerMockito.verifyStatic(times(1));
        CommonUtils.cancelAPIRequests(retrofitCalls);
    }
}
