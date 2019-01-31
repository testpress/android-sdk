package in.testpress.exam.network;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Map;

import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
public class AttemptsPagerTest {

    @Mock
    private TestpressExamApiClient apiClient;

    @Mock
    private AttemptsPager attemptsPager;

    @Mock
    public Map<String, Object> queryParams;


    @Test
    public void testChapterPager_checkGetItemCallsGetAttemptsOrNot() {
        doCallRealMethod().when(attemptsPager).getItems(anyInt(), anyInt());
        doCallRealMethod().when(attemptsPager).setApiClient(apiClient);

        attemptsPager.queryParams = queryParams;
        attemptsPager.setApiClient(apiClient);
        attemptsPager.getItems(anyInt(), anyInt());

        verify(apiClient, times(1)).getAttempts(
                Mockito.<String>any(),
                ArgumentMatchers.<String, Object>anyMap()
        );
    }
}
