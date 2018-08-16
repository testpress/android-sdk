package in.testpress.course.ui;

import android.app.ProgressDialog;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.TextView;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.modules.junit4.PowerMockRunner;

import in.testpress.core.TestpressException;
import in.testpress.models.greendao.Course;
import in.testpress.network.BaseResourcePager;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.doCallRealMethod;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
public class CourseListAdapterTest {

    @Mock
    private CourseListAdapter courseListAdapter;

    @Mock
    TextView textView;

    @Mock
    BaseResourcePager baseResourcePager;

    @Mock
    TestpressException testpressException;

    @Mock
    Course course;

    @Mock
    AlertDialog wantToCancelDialog;

    @Mock
    ProgressDialog progressDialog;

    @Mock
    AlertDialog retryDialog;

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

    @Test
    public void testGetIncrementBy() {
        doCallRealMethod().when(courseListAdapter).getIncrementBy((BaseResourcePager) any());
        when(baseResourcePager.getTotalItemsCount()).thenReturn(240);
        when(baseResourcePager.getPerPage()).thenReturn(200);
        baseResourcePager.page = 3;

        Assert.assertEquals(40, courseListAdapter.getIncrementBy(baseResourcePager));
    }

    @Test
    public void testHandleError_shouldNotCallInitializeDialogBox_whenExceptionIsCancelled() {
        doCallRealMethod().when(courseListAdapter).handleError((Course) any(), (TestpressException) any());
        when(testpressException.isCancelled()).thenReturn(true);

        courseListAdapter.handleError(course, testpressException);

        verify(courseListAdapter, times(0)).initializeDialogBox(anyInt(), anyInt(), (Course) any());
    }

    @Test
    public void testHandleError_shouldCallInitializeDialogBox_whenExceptionIsNotCancelled() {
        doCallRealMethod().when(courseListAdapter).handleError((Course) any(), (TestpressException) any());
        when(testpressException.isCancelled()).thenReturn(false);

        courseListAdapter.handleError(course, testpressException);

        verify(courseListAdapter, times(1)).initializeDialogBox(anyInt(), anyInt(), (Course) any());
    }

    @Test
    public void testCancelLoadersIfAnyIsCalled_whenProgressBar_CancelDialog_RetryDialog_areShowing_allShouldBeDismissed() {
        doCallRealMethod().when(courseListAdapter).cancelLoadersIfAny();
        doCallRealMethod().when(courseListAdapter).setWantToCancelDialog((AlertDialog) any());
        doCallRealMethod().when(courseListAdapter).setRetryDialog((AlertDialog) any());
        doCallRealMethod().when(courseListAdapter).setProgressDialog((ProgressDialog) any());
        when(wantToCancelDialog.isShowing()).thenReturn(true);
        when(retryDialog.isShowing()).thenReturn(true);
        when(progressDialog.isShowing()).thenReturn(true);
        courseListAdapter.setWantToCancelDialog(wantToCancelDialog);
        courseListAdapter.setRetryDialog(retryDialog);
        courseListAdapter.setProgressDialog(progressDialog);

        courseListAdapter.cancelLoadersIfAny();

        verify(wantToCancelDialog, times(1)).dismiss();
        verify(retryDialog, times(1)).dismiss();
        verify(progressDialog, times(1)).dismiss();
    }
}
