package in.testpress.exam.ui;

import org.junit.Test;

import in.testpress.util.CommonTestUtils;

public class AccessCodeFragmentTest {

    private static final int NUMBER_OF_RETROFIT_CALLS = 1;

    @Test
    public void testAccessCodeFragment_getRetrofitCalls_returnCorrectValues() {
        CommonTestUtils.testGetRetrofitCallsReturnCorrectValues(
                new AccessCodeFragment(),
                NUMBER_OF_RETROFIT_CALLS
        );
    }
}
