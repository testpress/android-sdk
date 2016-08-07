package in.testpress.exam.ui;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.Collections;
import java.util.List;

import in.testpress.exam.models.AttemptItem;

class TestQuestionPagerAdapter extends FragmentPagerAdapter {

    private  int numberOfPages = 0;
    private  List<AttemptItem> attemptItemList = Collections.emptyList();

    public TestQuestionPagerAdapter(FragmentManager fragmentManager, List<AttemptItem> attemptItemList) {
        super(fragmentManager);
        this.attemptItemList = attemptItemList;
        numberOfPages = attemptItemList.size();
    }

    public void setCount(int count) {
        this.numberOfPages = count;
    }

    @Override
    public Fragment getItem(int position) {
        return TestQuestionFragment.getInstance(attemptItemList.get(position), position + 1);
    }

    @Override
    public int getCount() {
        return numberOfPages;
    }

}
