package in.testpress.exam.ui;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.Collections;
import java.util.List;

import in.testpress.exam.models.AttemptItem;
import in.testpress.models.greendao.Exam;
import in.testpress.models.greendao.Language;

class TestQuestionPagerAdapter extends FragmentStatePagerAdapter {

    private  int numberOfPages = 0;
    private  List<AttemptItem> attemptItemList = Collections.emptyList();
    private Language selectedLanguage;
    private Exam exam;

    public TestQuestionPagerAdapter(FragmentManager fragmentManager,
                                    List<AttemptItem> attemptItemList,
                                    Language selectedLanguage, Exam exam) {

        super(fragmentManager);
        this.attemptItemList = attemptItemList;
        numberOfPages = attemptItemList.size();
        this.selectedLanguage = selectedLanguage;
        this.exam = exam;
    }

    public void setCount(int count) {
        this.numberOfPages = count;
    }

    @Override
    public Fragment getItem(int position) {
        return TestQuestionFragment
                .getInstance(attemptItemList.get(position), position + 1, selectedLanguage, exam);
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
