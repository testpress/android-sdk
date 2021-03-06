package in.testpress.exam.ui;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.Collections;
import java.util.List;

import in.testpress.exam.models.AttemptItem;
import in.testpress.models.greendao.Language;

class TestQuestionPagerAdapter extends FragmentStatePagerAdapter {

    private  int numberOfPages = 0;
    private  List<AttemptItem> attemptItemList = Collections.emptyList();
    private Language selectedLanguage;

    public TestQuestionPagerAdapter(FragmentManager fragmentManager,
                                    List<AttemptItem> attemptItemList,
                                    Language selectedLanguage) {

        super(fragmentManager);
        this.attemptItemList = attemptItemList;
        numberOfPages = attemptItemList.size();
        this.selectedLanguage = selectedLanguage;
    }

    public void setCount(int count) {
        this.numberOfPages = count;
    }

    @Override
    public Fragment getItem(int position) {
        return TestQuestionFragment
                .getInstance(attemptItemList.get(position), position + 1, selectedLanguage);
    }

    @Override
    public int getCount() {
        return numberOfPages;
    }

    //This method will call when we call notifyDataSetChanged
    @Override
    public int getItemPosition(Object object) {
        ((TestQuestionFragment) object).update();
        return super.getItemPosition(object);
    }

}
