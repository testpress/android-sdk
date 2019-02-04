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
public class CoursePagerTest {

    @Mock
    private TestpressCourseApiClient apiClient;

    @Mock
    private CoursePager coursePager;

    @Mock
    public Map<String, Object> queryParams;


    @Test
    public void testCoursePager_checkGetItemCallsGetCoursesOrNot() {
        doCallRealMethod().when(coursePager).getItems(Mockito.<Integer>anyInt(), Mockito.<Integer>anyInt());
        doCallRealMethod().when(coursePager).setApiClient(apiClient);

        coursePager.queryParams = queryParams;
        coursePager.setApiClient(apiClient);
        coursePager.getItems(Mockito.<Integer>anyInt(), Mockito.<Integer>anyInt());

        verify(apiClient, times(1)).getCourses(
                ArgumentMatchers.<String, Object>anyMap(),
                Mockito.<String>any()
        );
    }
}
