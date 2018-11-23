package in.testpress.exam.ui;

import org.junit.Test;

import in.testpress.util.CommonTestUtils;

public class ReviewStatsFragmentTest {

    private static final int NUMBER_OF_RETROFIT_CALLS = 1;

    @Test
    public void testReviewStatsFragment_getRetrofitCalls_returnCorrectValues() {
        CommonTestUtils.testGetRetrofitCallsReturnCorrectValues(
                new ReviewStatsFragment(),
                NUMBER_OF_RETROFIT_CALLS
        );
    }

}
