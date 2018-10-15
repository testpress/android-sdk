package in.testpress.course.ui;

import org.junit.Test;

import in.testpress.exam.ui.BookmarksFragment;
import in.testpress.util.CommonTestUtils;

public class LeaderboardFragmentTest {

    private static final int NUMBER_OF_RETROFIT_CALLS = 1;

    @Test
    public void testLeaderboardFragment_getRetrofitCalls_returnCorrectValues() {
        CommonTestUtils.testGetRetrofitCallsReturnCorrectValues(
                new LeaderboardFragment(),
                NUMBER_OF_RETROFIT_CALLS
        );
    }
}
