package in.testpress.course.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import java.util.List;

import in.testpress.core.TestpressSDKDatabase;
import in.testpress.course.R;
import in.testpress.models.greendao.Chapter;
import in.testpress.models.greendao.ChapterDao;
import in.testpress.ui.BaseToolBarActivity;

import static in.testpress.course.TestpressCourse.COURSE_ID;
import static in.testpress.course.TestpressCourse.PARENT_ID;

public class ChaptersGridActivity extends BaseToolBarActivity {

    public static final String ACTIONBAR_TITLE = "title";

    public static Intent createIntent(String title, String courseId, String parentId,
                                      Context context) {
        Intent intent = new Intent(context, ChaptersGridActivity.class);
        intent.putExtra(ACTIONBAR_TITLE, title);
        intent.putExtra(COURSE_ID, courseId);
        intent.putExtra(PARENT_ID, parentId);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testpress_container_layout);
        ChaptersGridFragment fragment = new ChaptersGridFragment();
        Bundle bundle = getIntent().getExtras();
        String title = bundle.getString(ACTIONBAR_TITLE);
        if (title != null && !title.isEmpty()) {
            //noinspection ConstantConditions
            getSupportActionBar().setTitle(title);
        }
        fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment)
                .commitAllowingStateLoss();
    }

    @Override
    protected Bundle getDataToSetResult() {
        Bundle data = super.getDataToSetResult();
        // If chapter list have parent then pass parent's parent id & course id
        List<Chapter> chapters = null;
        try {
            @SuppressLint("RestrictedApi")
            ChaptersGridFragment fragment =
                    (ChaptersGridFragment) getSupportFragmentManager().getFragments().get(0);

            chapters = fragment.getItems();
        } catch (Exception e) {
        }
        if (chapters != null && !chapters.isEmpty()) {
            Integer parentId = chapters.get(0).getParentId();
            if (parentId != null) {
                ChapterDao chapterDao = TestpressSDKDatabase.getChapterDao(this);
                List<Chapter> parentChapterList = chapterDao.queryBuilder()
                        .where(ChapterDao.Properties.Id.eq(parentId))
                        .list();

                if (parentChapterList.isEmpty()) {
                    return data;
                }
                Chapter parentChapter = parentChapterList.get(0);
                data.putString(COURSE_ID, parentChapter.getCourseId().toString());
                data.putString(PARENT_ID, String.valueOf(parentChapter.getParentId()));
            }
        }
        return data;
    }

}
