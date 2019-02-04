package in.testpress.course.network;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Map;

import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
public class LeaderboardPagerTest {

    @Mock
    private TestpressCourseApiClient apiClient;

    @Mock
    private LeaderboardPager leaderboardPager;

    @Mock
    public Map<String, Object> queryParams;


    @Test
    public void testLeaderboardPager_checkGetItemCallsGetLeaderboardOrNot() {
        doCallRealMethod().when(leaderboardPager).getItems(Mockito.<Integer>anyInt(), Mockito.<Integer>anyInt());
        doCallRealMethod().when(leaderboardPager).setApiClient(apiClient);

        leaderboardPager.queryParams = queryParams;
        leaderboardPager.setApiClient(apiClient);
        leaderboardPager.getItems(Mockito.<Integer>anyInt(), Mockito.<Integer>anyInt());

        verify(apiClient, times(1)).getLeaderboard(
                ArgumentMatchers.<String, Object>anyMap()
        );
    }
}
