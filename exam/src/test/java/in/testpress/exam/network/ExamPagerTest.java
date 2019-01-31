package in.testpress.exam.network;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Map;

import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
public class ExamPagerTest {

    @Mock
    private TestpressExamApiClient apiClient;

    @Mock
    private ExamPager examPager;

    @Mock
    public Map<String, Object> queryParams;


    @Test
    public void testChapterPager_checkGetItemCallsGetExamsOrNot() {
        doCallRealMethod().when(examPager).getItems(anyInt(), anyInt());
        doCallRealMethod().when(examPager).setApiClient(apiClient);

        examPager.queryParams = queryParams;
        examPager.setApiClient(apiClient);
        examPager.getItems(anyInt(), anyInt());

        verify(apiClient, times(1)).getExams(ArgumentMatchers.<String, Object>anyMap());
    }
}

