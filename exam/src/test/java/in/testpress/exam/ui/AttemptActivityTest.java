package in.testpress.exam.ui;

import org.junit.Test;

import in.testpress.util.CommonTestUtils;

public class AttemptActivityTest {

    private static final int NUMBER_OF_RETROFIT_CALLS = 2;

    @Test
    public void testAttemptActivity_getRetrofitCalls_returnCorrectValues() {
        CommonTestUtils.testGetRetrofitCallsReturnCorrectValues(
                new AttemptsActivity(),
                NUMBER_OF_RETROFIT_CALLS
        );
    }
}
