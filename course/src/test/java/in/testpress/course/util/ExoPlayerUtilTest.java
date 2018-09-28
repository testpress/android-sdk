package in.testpress.course.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;

import static in.testpress.course.network.TestpressCourseApiClient.LAST_POSITION;
import static in.testpress.course.network.TestpressCourseApiClient.TIME_RANGES;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExoPlayerUtilTest {

    private static final float TEST_START_POSITION = 111.111f;
    private static final float TEST_CURRENT_POSITION = 222.222f;

    @Mock
    private ExoPlayerUtil exoPlayerUtilMocked;

    @Test
    public void testExoPlayerUtil_getVideoAttemptParameters_returnCorrectValue() {

        when(exoPlayerUtilMocked.getCurrentPosition()).thenReturn(TEST_CURRENT_POSITION);

        Mockito.doCallRealMethod().when(exoPlayerUtilMocked).setStartPosition(TEST_START_POSITION);
        exoPlayerUtilMocked.setStartPosition(TEST_START_POSITION);

        Mockito.doCallRealMethod().when(exoPlayerUtilMocked).getVideoAttemptParameters();
        Map<String, Object> parameters = exoPlayerUtilMocked.getVideoAttemptParameters();
        float lastPosition = (float) parameters.get(LAST_POSITION);

        assertThat("last_position field needs to present on the video attempt parameters",
                lastPosition,
                is(notNullValue()));

        assertThat("last_position value needs to same as current content position of exo player",
                lastPosition,
                is(TEST_CURRENT_POSITION));

        String[][] timeRanges = (String[][]) parameters.get(TIME_RANGES);

        assertThat("time_ranges field needs to present on the video attempt parameters",
                timeRanges,
                is(notNullValue()));

        assertThat("time_ranges start position needs to be start position given to exo player",
                timeRanges[0][0],
                is(String.valueOf(TEST_START_POSITION)));

        assertThat("time_ranges current position needs to be current content position of exo player",
                timeRanges[0][1],
                is(String.valueOf(lastPosition)));
    }

}
