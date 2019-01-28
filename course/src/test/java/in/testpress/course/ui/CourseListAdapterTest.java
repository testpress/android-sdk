package in.testpress.course.ui;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;

import in.testpress.models.greendao.CourseCredit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doCallRealMethod;

@RunWith(PowerMockRunner.class)
public class CourseListAdapterTest {

    @Mock
    private CourseListAdapter courseListAdapter;

    private int mTotalContent;

    private void setTotalContent(int totalContent){
        mTotalContent = totalContent;
    }

    @Test
    public void testCalculateCourseProgressPercentage(){
        CourseCredit courseCredit = new CourseCredit();

        courseCredit.setTotalUniqueVideoAttempts(10);
        courseCredit.setTotalUniqueHtmlAttempts(20);
        courseCredit.setTotalUniqueQuizAttempts(30);
        courseCredit.setTotalUniqueExamAttempts(40);
        courseCredit.setTotalUniqueAttachmentAttempts(50);
        setTotalContent(200);

        doCallRealMethod().when(courseListAdapter)
                .calculateCourseProgressPercentage(courseCredit, mTotalContent);
        int progressPercentage = courseListAdapter.calculateCourseProgressPercentage(courseCredit, mTotalContent);

        assertEquals(75, progressPercentage);
    }

    @Test
    public void testCalculateCourseProgressPercentage_whenTotalContentIsEqualToZero(){
        CourseCredit courseCredit = new CourseCredit();

        courseCredit.setTotalUniqueVideoAttempts(10);
        courseCredit.setTotalUniqueHtmlAttempts(20);
        courseCredit.setTotalUniqueQuizAttempts(30);
        courseCredit.setTotalUniqueExamAttempts(40);
        courseCredit.setTotalUniqueAttachmentAttempts(50);
        setTotalContent(0);

        doCallRealMethod().when(courseListAdapter)
                .calculateCourseProgressPercentage(courseCredit, mTotalContent);
        int progressPercentage = courseListAdapter.calculateCourseProgressPercentage(courseCredit, mTotalContent);

        assertEquals(0, progressPercentage);
    }

    @Test
    public void testCalculateCourseProgressPercentage_whenAllAttemptEqualToZero(){
        CourseCredit courseCredit = new CourseCredit();

        courseCredit.setTotalUniqueVideoAttempts(0);
        courseCredit.setTotalUniqueHtmlAttempts(0);
        courseCredit.setTotalUniqueQuizAttempts(0);
        courseCredit.setTotalUniqueExamAttempts(0);
        courseCredit.setTotalUniqueAttachmentAttempts(0);
        setTotalContent(20);

        doCallRealMethod().when(courseListAdapter)
                .calculateCourseProgressPercentage(courseCredit, mTotalContent);
        int progressPercentage = courseListAdapter.calculateCourseProgressPercentage(courseCredit, mTotalContent);

        assertEquals(0, progressPercentage);
    }

    @Test
    public void testCalculateCourseProgressPercentage_whenCourseCreditIsNull(){

        CourseCredit courseCredit;
        courseCredit = null;

        doCallRealMethod().when(courseListAdapter)
                .calculateCourseProgressPercentage(courseCredit, mTotalContent);
        int progressPercentage = courseListAdapter.calculateCourseProgressPercentage(courseCredit, mTotalContent);

        assertEquals(0, progressPercentage);
    }
}
