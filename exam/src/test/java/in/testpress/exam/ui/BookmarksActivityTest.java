package in.testpress.exam.ui;

import org.junit.Test;

import in.testpress.util.CommonTestUtils;

public class BookmarksActivityTest {

    private static final int NUMBER_OF_RETROFIT_CALLS = 4;

    @Test
    public void testBookmarksActivity_getRetrofitCalls_returnCorrectValues() {
        CommonTestUtils.testGetRetrofitCallsReturnCorrectValues(
                new BookmarksActivity(),
                NUMBER_OF_RETROFIT_CALLS
        );
    }
}
