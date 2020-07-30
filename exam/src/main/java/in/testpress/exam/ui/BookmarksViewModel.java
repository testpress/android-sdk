package in.testpress.exam.ui;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class BookmarksViewModel extends ViewModel {

    public MutableLiveData<Boolean> isHidden = new MutableLiveData<>();
}
