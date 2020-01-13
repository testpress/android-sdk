package in.testpress.course.util;

import android.util.Log;

import java.util.List;

import in.testpress.course.enums.CourseType;
import in.testpress.models.greendao.Course;
import in.testpress.models.greendao.CourseDao;

public class ManageCourseStates {
    private CourseType type;
    private CourseDao courseDao;
    private List<Course> courses;

    public ManageCourseStates(CourseType type, CourseDao courseDao) {
        this.type = type;
        this.courseDao = courseDao;
    }

    public void updateCoursesWithLocalState(List<Course> courses) {
        for (Course course: courses) {
            List<Course> coursesFromDB = courseDao.queryBuilder()
                    .where(CourseDao.Properties.Id.eq(course.getId())).list();

            if (!coursesFromDB.isEmpty()) {
                Course courseFromDB = coursesFromDB.get(0);
                course.setIsMyCourse(courseFromDB.getIsMyCourse());
            }
        }
    }

}
