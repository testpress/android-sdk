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
public class CommentsPagerTest {

    @Mock
    private TestpressExamApiClient apiClient;

    @Mock
    private CommentsPager commentsPager;

    @Mock
    public Map<String, Object> queryParams;


    @Test
    public void testChapterPager_checkGetItemCallsGetCommentsOrNot() {
        doCallRealMethod().when(commentsPager).getItems(anyInt(), anyInt());
        doCallRealMethod().when(commentsPager).setApiClient(apiClient);

        commentsPager.queryParams = queryParams;
        commentsPager.setApiClient(apiClient);
        commentsPager.getItems(anyInt(), anyInt());

        verify(apiClient, times(1)).getComments(
                Mockito.<String>any(),
                ArgumentMatchers.<String, Object>anyMap()
        );
    }
}
