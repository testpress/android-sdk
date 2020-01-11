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

    public ManageCourseStates(CourseType type, CourseDao courseDao, List<Course> courses) {
        this.type = type;
        this.courseDao = courseDao;
        this.courses = courses;
    }

    private void updateState(Course course, boolean state) {
        switch (type) {
            case MY_COURSE:
                course.setIsMyCourse(state);
                break;
            case PRODUCT_COURSE:
                course.setIsProduct(state);
                break;
        }
    }

    private void unAssignAllCourses() {
        List<Course> coursesFromDB = courseDao.queryBuilder().list();
        for (Course course: coursesFromDB) {
            updateState(course, false);
        }
        courseDao.insertOrReplaceInTx(coursesFromDB);
    }

    private void assignCourses(List<Course> courses) {
        for (Course course: courses) {
            updateState(course, true);
        }
        courseDao.insertOrReplaceInTx(courses);
    }

    private void cleanCourses() {
        courseDao.queryBuilder()
                .where(
                        CourseDao.Properties.IsMyCourse.notEq(true),
                        CourseDao.Properties.IsProduct.notEq(true)
                )
                .buildDelete().executeDeleteWithoutDetachingEntities();
        courseDao.detachAll();
    }

    private void updateCoursesWithLocalState(List<Course> courses) {
        for (Course course: courses) {
            List<Course> coursesFromDB = courseDao.queryBuilder()
                    .where(CourseDao.Properties.Id.eq(course.getId())).list();

            if (!coursesFromDB.isEmpty()) {
                Course courseFromDB = coursesFromDB.get(0);
                course.setIsMyCourse(courseFromDB.getIsMyCourse());
                course.setIsProduct(courseFromDB.getIsProduct());
            }
        }
    }

    public void removeCourses() {
        /*
        * Since both products API and courses API sends courses, we need to distinguish and remove courses that are
        * neither user's nor product's.
        * */
        unAssignAllCourses();
        updateCoursesWithLocalState(courses);
        assignCourses(courses);
        cleanCourses();
    }
}
