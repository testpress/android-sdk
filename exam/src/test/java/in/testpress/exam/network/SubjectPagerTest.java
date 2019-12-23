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
public class SubjectPagerTest {

    @Mock
    private TestpressExamApiClient apiClient;

    @Mock
    private SubjectPager subjectPager;

    @Mock
    public Map<String, Object> queryParams;


    @Test
    public void testSubjectPager_checkGetItemCallsGetSubjectsOrNot() {
        doCallRealMethod().when(subjectPager).getItems(anyInt(), anyInt());
        doCallRealMethod().when(subjectPager).setApiClient(apiClient);

        subjectPager.queryParams = queryParams;
        subjectPager.setApiClient(apiClient);
        subjectPager.getItems(anyInt(), anyInt());

        verify(apiClient, times(1)).getSubjects( Mockito.<String>any(), ArgumentMatchers.<String, Object>anyMap());
    }
}

