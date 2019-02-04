package in.testpress.exam.network;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Map;

import in.testpress.v2_4.models.BookmarksListResponse;

import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
public class BookmarksPagerTest {

    @Mock
    private TestpressExamApiClient apiClient;

    @Mock
    private BookmarksPager bookmarksPager;

    @Mock
    private BookmarksListResponse bookmarksListResponse;

    @Mock
    public Map<String, Object> queryParams;


    @Test
    public void testBookmarksPager_checkGetItemCallsGetBookmarksOrNot() {

        doCallRealMethod().when(bookmarksPager).getItems(bookmarksListResponse);
        doCallRealMethod().when(bookmarksListResponse).getBookmarks();

        bookmarksPager.queryParams = queryParams;
        bookmarksPager.getItems(bookmarksListResponse);

        verify(bookmarksListResponse, times(1)).getBookmarks();
    }
}

