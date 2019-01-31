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
public class TestQuestionsPagerTest {

    @Mock
    private TestpressExamApiClient apiClient;

    @Mock
    private TestQuestionsPager testQuestionsPager;

    @Mock
    public Map<String, Object> queryParams;


    @Test
    public void testChapterPager_checkGetItemCallsGetQuestionsOrNot() {
        doCallRealMethod().when(testQuestionsPager).getItems(anyInt(), anyInt());
        doCallRealMethod().when(testQuestionsPager).setApiClient(apiClient);

        testQuestionsPager.queryParams = queryParams;
        testQuestionsPager.setApiClient(apiClient);
        testQuestionsPager.getItems(anyInt(), anyInt());

        verify(apiClient, times(1)).getQuestions( Mockito.<String>any(), ArgumentMatchers.<String, Object>anyMap());
    }
}

