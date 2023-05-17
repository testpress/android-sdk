package `in`.testpress.ui

import `in`.testpress.network.RetrofitCall
import `in`.testpress.util.CommonUtils
import androidx.appcompat.app.AppCompatActivity
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.powermock.api.mockito.PowerMockito
import org.powermock.api.support.membermodification.MemberMatcher
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner

@RunWith(PowerMockRunner::class)
@PrepareForTest(AppCompatActivity::class, CommonUtils::class)
class BaseToolBarActivityTest {
    @Mock
    private lateinit var retrofitCall: RetrofitCall<*>
    @Test
    fun test_onStop_cancelAPIRequests_isCalled() {
        retrofitCall = Mockito.mock(RetrofitCall::class.java)
        PowerMockito.suppress(MemberMatcher.methodsDeclaredIn(AppCompatActivity::class.java))
        val activity = Mockito.mock(
            BaseToolBarActivity::class.java
        )
        val retrofitCalls: Array<RetrofitCall<*>> = arrayOf(retrofitCall)
        PowerMockito.`when`(activity.getRetrofitCalls())
            .thenReturn(retrofitCalls)
        PowerMockito.doCallRealMethod().`when`(activity).onDestroy()
        activity.onDestroy()
        Mockito.verify(activity, Mockito.times(1)).getRetrofitCalls()
        Mockito.verify(retrofitCall, Mockito.times(1)).cancel()
        PowerMockito.mockStatic(CommonUtils::class.java)
        try {
            PowerMockito.doNothing().`when`(
                CommonUtils::class.java,
                "cancelAPIRequests",
                retrofitCalls as Any
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Assert.fail()
        }
        activity.onDestroy()
        PowerMockito.verifyStatic(CommonUtils::class.java, Mockito.times(1))
        CommonUtils.cancelAPIRequests(retrofitCalls)
    }
}