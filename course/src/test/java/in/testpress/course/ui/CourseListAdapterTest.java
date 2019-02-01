package in.testpress.course.ui;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.modules.junit4.PowerMockRunner;

import in.testpress.models.greendao.Course;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.doCallRealMethod;

@RunWith(PowerMockRunner.class)
public class CourseListAdapterTest {

    @Mock
    private CourseListAdapter courseListAdapter;

    @Mock
    TextView textView;

    @Mock
    Activity activity;

    @Mock
    Course course;


    @Test
    public void testSetTextToTextView_setTextOrNot() {
        doCallRealMethod().when(courseListAdapter).setTextToTextView(anyString(), (TextView) any());
        courseListAdapter.setTextToTextView("Test Text", textView);

        verify(textView, times(1)).setText(Mockito.anyString());
    }

    @Test
    public void test_toggleTextViewVisibility() {
        doCallRealMethod().when(courseListAdapter).toggleTextViewVisibility(anyBoolean(), (TextView) any());
        courseListAdapter.toggleTextViewVisibility(true, textView);

        verify(textView, times(1)).setVisibility(View.GONE);
    }

}
