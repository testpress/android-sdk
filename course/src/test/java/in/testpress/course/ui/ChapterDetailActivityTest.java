package in.testpress.course.ui;

import org.junit.Test;

import in.testpress.util.CommonTestUtils;

public class ChapterDetailActivityTest {

    private static final int NUMBER_OF_RETROFIT_CALLS = 2;

    @Test
    public void testChapterDetailActivity_getRetrofitCalls_returnCorrectValues() {
        CommonTestUtils.testGetRetrofitCallsReturnCorrectValues(
                new ChapterDetailActivity(),
                NUMBER_OF_RETROFIT_CALLS
        );
    }
}
