package in.testpress.exam.network;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Map;

import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
public class CategoryPagerTest {

    @Mock
    private TestpressExamApiClient apiClient;

    @Mock
    private CategoryPager categoryPager;

    @Mock
    public Map<String, Object> queryParams;


    @Test
    public void testChapterPager_checkGetItemCallsGetCategoriesOrNot() {
        doCallRealMethod().when(categoryPager).getItems(anyInt(), anyInt());
        doCallRealMethod().when(categoryPager).setApiClient(apiClient);

        categoryPager.queryParams = queryParams;
        categoryPager.setApiClient(apiClient);
        categoryPager.getItems(anyInt(), anyInt());

        verify(apiClient, times(1)).getCategories(ArgumentMatchers.<String, Object>anyMap());
    }
}
