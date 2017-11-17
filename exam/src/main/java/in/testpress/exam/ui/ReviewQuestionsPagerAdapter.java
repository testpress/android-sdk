package in.testpress.exam.ui;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.Collections;
import java.util.List;

import in.testpress.models.greendao.Language;
import in.testpress.models.greendao.ReviewItem;

class ReviewQuestionsPagerAdapter extends FragmentStatePagerAdapter {

    private  List<ReviewItem> reviewItems = Collections.emptyList();
    private Language selectedLanguage;
    private boolean positionsModified;

    public ReviewQuestionsPagerAdapter(FragmentManager fragmentManager,
                                       List<ReviewItem> reviewItems) {
        super(fragmentManager);
        this.reviewItems = reviewItems;
    }

    @Override
    public Fragment getItem(int position) {
        return ReviewQuestionsFragment.getInstance(reviewItems.get(position).getId(), selectedLanguage);
    }

    public void setReviewItems(List<ReviewItem> reviewItems) {
        this.reviewItems = reviewItems;
    }

    void setSelectedLanguage(Language selectedLanguage) {
        this.selectedLanguage = selectedLanguage;
    }

    @Override
    public int getCount() {
        return reviewItems.size();
    }

    //This method will call when we call notifyDataSetChanged
    @Override
    public int getItemPosition(Object object) {
        // Clear the fragments only if positions modified, just update the content otherwise.
        if (positionsModified) {
            return POSITION_NONE;
        }
        ((ReviewQuestionsFragment) object).update();
        return super.getItemPosition(object);
    }

    void notifyDataSetChanged(boolean positionsModified) {
        this.positionsModified = positionsModified;
        super.notifyDataSetChanged();
    }
}
