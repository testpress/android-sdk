package in.testpress.exam.ui;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;

import java.util.Collections;
import java.util.List;

import in.testpress.exam.models.greendao.ReviewItem;

class ReviewQuestionsPagerAdapter extends FragmentStatePagerAdapter {

    private  List<ReviewItem> reviewItems = Collections.emptyList();

    public ReviewQuestionsPagerAdapter(FragmentManager fragmentManager,
                                       List<ReviewItem> reviewItems) {
        super(fragmentManager);
        this.reviewItems = reviewItems;
    }

    @Override
    public Fragment getItem(int position) {
        return ReviewQuestionsFragment.getInstance(reviewItems.get(position).getId());
    }

    public void setReviewItems(List<ReviewItem> reviewItems) {
        this.reviewItems = reviewItems;
    }

    @Override
    public int getCount() {
        return reviewItems.size();
    }

    @Override
    public int getItemPosition(Object object){
        return PagerAdapter.POSITION_NONE;
    }

}
