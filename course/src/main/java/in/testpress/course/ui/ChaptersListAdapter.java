package in.testpress.course.ui;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import in.testpress.core.TestpressSdk;
import in.testpress.course.R;
import in.testpress.models.greendao.Chapter;
import in.testpress.util.ImageUtils;
import in.testpress.util.SingleTypeAdapter;

import static in.testpress.course.TestpressCourse.CHAPTER_SLUG;
import static in.testpress.course.TestpressCourse.COURSE_ID;

class ChaptersListAdapter extends SingleTypeAdapter<Chapter> {

    private final Activity activity;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private String courseId;
    private String parentId;
    private OnChapterClickListener chapterClickListener;

    ChaptersListAdapter(Activity activity, String courseId, String parentId, OnChapterClickListener chapterClickListener) {
        super(activity.getLayoutInflater(), R.layout.testpress_chapters_list_item);
        this.activity = activity;
        this.courseId = courseId;
        this.parentId = parentId;
        this.imageLoader = ImageUtils.initImageLoader(activity);
        this.options = ImageUtils.getPlaceholdersOption();
        this.chapterClickListener = chapterClickListener;
    }

    @Override
    public int getCount() {
        return  (int) Chapter.getParentChaptersQueryBuilder(activity, courseId, parentId).count();
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    @Override
    public Chapter getItem(int position) {
        return Chapter.getParentChaptersQueryBuilder(activity, courseId, parentId).listLazy().get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }


    @Override
    protected int[] getChildViewIds() {
        return new int[] {
            R.id.chapter_title, R.id.thumbnail_image, R.id.white_foreground, R.id.lock, R.id.chapter_item_layout
        };
    }

    private void displayThumbnail(Chapter chapter) {
        if (chapter.getImage() == null || chapter.getImage().isEmpty()) {
            setGone(1, true);
        } else {
            setGone(1, false);
            imageLoader.displayImage(chapter.getImage(), imageView(1), options);
        }
    }

    private void showOrLockChapter(Chapter chapter) {
        if (chapter.getIsLocked()) {
            setGone(2, false);
            setGone(3, false);
            view(4).setClickable(false);
        } else {
            setGone(2, true);
            setGone(3, true);
        }
    }

    private void addOnClickForChapter(final Chapter chapter) {
        if (!chapter.getIsLocked()) {
            view(4).setClickable(true);
            view(4).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    chapterClickListener.onChapterClick(chapter);
                }
            });
        }
    }

    @Override
    protected void update(final int position, final Chapter chapter) {
        textView(0).setTypeface(TestpressSdk.getRubikMediumFont(activity));
        setText(0, chapter.getName());
        displayThumbnail(chapter);
        showOrLockChapter(chapter);
        addOnClickForChapter(chapter);
    }

    public interface OnChapterClickListener {
        void onChapterClick(Chapter chapter);
    }

}
