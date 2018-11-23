package in.testpress.exam.ui;

import org.junit.Test;

import in.testpress.util.CommonTestUtils;

public class ReviewQuestionsActivityTest {

    private static final int NUMBER_OF_RETROFIT_CALLS = 2;

    @Test
    public void testReviewQuestionsActivity_getRetrofitCalls_returnCorrectValues() {
        CommonTestUtils.testGetRetrofitCallsReturnCorrectValues(
                new ReviewQuestionsActivity(),
                NUMBER_OF_RETROFIT_CALLS
        );
    }
}
