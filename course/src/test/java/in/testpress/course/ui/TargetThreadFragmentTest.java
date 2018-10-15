package in.testpress.course.ui;

import org.junit.Test;

import in.testpress.util.CommonTestUtils;

public class TargetThreadFragmentTest {

    private static final int NUMBER_OF_RETROFIT_CALLS = 2;

    @Test
    public void testTargetThreadFragment_getRetrofitCalls_returnCorrectValues() {
        CommonTestUtils.testGetRetrofitCallsReturnCorrectValues(
                new TargetThreadFragment(),
                NUMBER_OF_RETROFIT_CALLS
        );
    }
}
