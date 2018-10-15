package in.testpress.exam.ui;

import org.junit.Test;

import in.testpress.util.CommonTestUtils;

public class TimeAnalyticsActivityTest {

    private static final int NUMBER_OF_RETROFIT_CALLS = 1;

    @Test
    public void testTimeAnalyticsActivity_getRetrofitCalls_returnCorrectValues() {
        CommonTestUtils.testGetRetrofitCallsReturnCorrectValues(
                new TimeAnalyticsActivity(),
                NUMBER_OF_RETROFIT_CALLS
        );
    }
}
