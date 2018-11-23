package in.testpress.util;

import junit.framework.Assert;

import in.testpress.network.RetrofitCall;
import in.testpress.ui.BaseFragment;
import in.testpress.ui.BaseToolBarActivity;

public class CommonTestUtils {

    public static void testGetRetrofitCallsReturnCorrectValues(BaseToolBarActivity activity,
                                                               int numberOfCalls) {

        testGetRetrofitCallsReturnCorrectValues(activity.getRetrofitCalls(), numberOfCalls);
    }

    public static void testGetRetrofitCallsReturnCorrectValues(BaseFragment fragment,
                                                               int numberOfCalls) {

        testGetRetrofitCallsReturnCorrectValues(fragment.getRetrofitCalls(), numberOfCalls);
    }

    public static void testGetDialogsReturnCorrectValues(BaseFragment fragment,
                                                         int numberOfDialogs) {

        Assert.assertEquals("Check number of Dialogs returned is " + numberOfDialogs,
                fragment.getDialogs().length,
                numberOfDialogs);
    }

    private static void testGetRetrofitCallsReturnCorrectValues(RetrofitCall[] retrofitCalls,
                                                                int numberOfCalls) {

        Assert.assertEquals("Check number of RetrofitCalls returned is " + numberOfCalls,
                retrofitCalls.length,
                numberOfCalls);
    }
}
