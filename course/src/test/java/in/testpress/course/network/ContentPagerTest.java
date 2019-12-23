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
public class ContentPagerTest {

    @Mock
    private TestpressCourseApiClient apiClient;

    @Mock
    private ContentPager contentPager;

    @Mock
    public Map<String, Object> queryParams;


    @Test
    public void testContentPager_checkGetItemCallsGetContentOrNot() {
        doCallRealMethod().when(contentPager).getItems(Mockito.<Integer>anyInt(), Mockito.<Integer>anyInt());
        doCallRealMethod().when(contentPager).setApiClient(apiClient);

        contentPager.queryParams = queryParams;
        contentPager.setApiClient(apiClient);
        contentPager.getItems(Mockito.<Integer>anyInt(), Mockito.<Integer>anyInt());

        verify(apiClient, times(1)).getContents(
                Mockito.<String>any(),
                ArgumentMatchers.<String, Object>anyMap()
        );
    }
}
