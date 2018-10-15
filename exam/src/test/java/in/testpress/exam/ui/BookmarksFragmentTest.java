package in.testpress.exam.ui;

import org.junit.Test;

import in.testpress.util.CommonTestUtils;

public class BookmarksFragmentTest {

    private static final int NUMBER_OF_RETROFIT_CALLS = 3;

    @Test
    public void testBookmarksFragment_getRetrofitCalls_returnCorrectValues() {
        CommonTestUtils.testGetRetrofitCallsReturnCorrectValues(
                new BookmarksFragment(),
                NUMBER_OF_RETROFIT_CALLS
        );
    }
}
