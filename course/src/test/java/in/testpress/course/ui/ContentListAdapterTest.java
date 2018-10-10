package in.testpress.course.ui;

import android.graphics.Color;
import android.support.v4.content.ContextCompat;

import com.github.testpress.mikephil.charting.charts.PieChart;
import com.github.testpress.mikephil.charting.components.Legend;
import com.github.testpress.mikephil.charting.data.PieData;
import com.github.testpress.mikephil.charting.data.PieEntry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import in.testpress.course.R;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Color.class, ContextCompat.class })
public class ContentListAdapterTest {

    private static final int TEST_VIDEO_WATCHED_PERCENTAGE = 18;

    @Mock
    private ContentsListAdapter contentsListAdapter;

    @Mock
    private PieChart pieChart;

    @Mock
    private Legend legend;

    @Before
    public void setUp() {
        mockStatic(Color.class);
        when(Color.rgb(140, 234, 255)).thenReturn(9235199);

        mockStatic(ContextCompat.class);
        //noinspection ConstantConditions
        when(ContextCompat.getColor(null, R.color.testpress_green)).thenReturn(Color.GREEN);
        doCallRealMethod().when(contentsListAdapter)
                .getVideoProgressPieChartData(TEST_VIDEO_WATCHED_PERCENTAGE);
    }

    @Test
    public void test_displayVideoWatchedPercentage_displayingCorrectValues() {
        String expectedCenterText = TEST_VIDEO_WATCHED_PERCENTAGE + "%";
        doCallRealMethod().when(pieChart).setCenterText(expectedCenterText);
        doCallRealMethod().when(pieChart).getCenterText();
        doCallRealMethod().when(pieChart).setData(Matchers.any(PieData.class));
        doCallRealMethod().when(pieChart).getData();

        when(pieChart.getLegend()).thenReturn(legend);

        doCallRealMethod().when(contentsListAdapter)
                .displayVideoWatchedPercentage(pieChart, TEST_VIDEO_WATCHED_PERCENTAGE);

        contentsListAdapter.displayVideoWatchedPercentage(pieChart, TEST_VIDEO_WATCHED_PERCENTAGE);

        assertThat("Video watched percentage text can't be null.",
                pieChart.getCenterText(),
                is(notNullValue()));

        assertThat("Video watched percentage displayed needs to be same as given",
                pieChart.getCenterText().toString(),
                is(expectedCenterText));

        testVideoWatchedPercentageProgressValues(pieChart.getData());
    }

    @Test
    public void test_getVideoProgressPieChartData_returnCorrectValue() {
        PieData pieData = contentsListAdapter
                .getVideoProgressPieChartData(TEST_VIDEO_WATCHED_PERCENTAGE);

        testVideoWatchedPercentageProgressValues(pieData);
    }

    private void testVideoWatchedPercentageProgressValues(PieData pieData) {
        PieEntry entry1 = pieData.getDataSet().getEntryForIndex(0);

        assertThat("Video watched percentage progress value needs to be same as given",
                entry1.getValue(),
                is((float) TEST_VIDEO_WATCHED_PERCENTAGE));

        PieEntry entry2 = pieData.getDataSet().getEntryForIndex(1);
        assertThat("Unwatched video percentage progress value needs to be (100 - watched percentage).",
                entry2.getValue(),
                is(100 - (float) TEST_VIDEO_WATCHED_PERCENTAGE));
    }

}
