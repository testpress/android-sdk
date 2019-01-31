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
public class ChapterPagerTest {

    @Mock
    private TestpressCourseApiClient apiClient;

    @Mock
    private ChapterPager chapterPager;

    @Mock
    public Map<String, Object> queryParams;


    @Test
    public void testChapterPager_checkGetItemCallsGetChaptersOrNot() {
        doCallRealMethod().when(chapterPager).getItems(Mockito.<Integer>anyInt(), Mockito.<Integer>anyInt());
        doCallRealMethod().when(chapterPager).setApiClient(apiClient);

        chapterPager.queryParams = queryParams;
        chapterPager.setApiClient(apiClient);
        chapterPager.getItems(Mockito.<Integer>anyInt(), Mockito.<Integer>anyInt());

        verify(apiClient, times(1)).getChapters(
                Mockito.<String>any(),
                ArgumentMatchers.<String, Object>anyMap(),
                Mockito.<String>any()
        );
    }
}
