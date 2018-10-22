package in.testpress.ui;

import android.app.Dialog;
import android.support.v4.app.Fragment;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import in.testpress.network.RetrofitCall;
import in.testpress.util.CommonUtils;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.doCallRealMethod;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.support.membermodification.MemberMatcher.methodsDeclaredIn;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ CommonUtils.class })
public class BaseFragmentTest {

    @Mock private BaseFragment fragment;

    @Before
    public void setUp() {
        PowerMockito.suppress(methodsDeclaredIn(Fragment.class));
        PowerMockito.mockStatic(CommonUtils.class);
    }

    @Test
    public void testBaseFragment_onDestroyView_cancelAPIRequests_isCalled() {
        RetrofitCall[] retrofitCalls = new RetrofitCall[] {};
        when(fragment.getRetrofitCalls()).thenReturn(retrofitCalls);

        doCallRealMethod().when(fragment).onDestroyView();
        fragment.onDestroyView();

        verify(fragment, times(1)).getRetrofitCalls();
        PowerMockito.verifyStatic(times(1));
        CommonUtils.cancelAPIRequests(retrofitCalls);
    }

    @Test
    public void testBaseFragment_onDestroyView_dismissDialogs_isCalled() {
        Dialog[] dialogs = new Dialog[] {};
        when(fragment.getDialogs()).thenReturn(dialogs);

        doCallRealMethod().when(fragment).onDestroyView();
        fragment.onDestroyView();

        verify(fragment, times(1)).getDialogs();
        PowerMockito.verifyStatic(times(1));
        CommonUtils.dismissDialogs(dialogs);
    }

}
