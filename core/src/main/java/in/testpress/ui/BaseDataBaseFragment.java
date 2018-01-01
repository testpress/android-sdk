package in.testpress.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;

import org.greenrobot.greendao.AbstractDao;

import java.util.List;

import in.testpress.core.TestpressException;
import in.testpress.network.BaseResourcePager;
import in.testpress.util.ThrowableLoader;

/**
 *
 * @param <T> Entity type
 * @param <K> Primary key (PK) type; use Void if entity does not have exactly one PK
 */
public abstract class BaseDataBaseFragment<T, K> extends BaseListViewFragment<T> {

    protected BaseResourcePager<T> pager;

    protected abstract BaseResourcePager<T> getPager();

    protected abstract AbstractDao<T, K> getDao();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getListAdapter().notifyDataSetChanged();
    }

    protected void displayDataFromDB() {
        Log.d("displayDataFromDB","Adapter notifyDataSetChanged");
        getListAdapter().notifyDataSetChanged();

        if (isItemsEmpty()) {
            setEmptyText();
            retryButton.setVisibility(View.GONE);
        }
    }

    @Override
    protected boolean isItemsEmpty() {
        return getDao().count() == 0;
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public Loader<List<T>> onCreateLoader(int id, Bundle args) {
        return new ThrowableLoader<List<T>>(getActivity(), items) {
            @Override
            public List<T> loadData() throws TestpressException {
                do {
                    getPager().next();
                    items = getPager().getResources();
                } while (getPager().hasNext());
                return items;
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<List<T>> loader, List<T> courses) {
        final TestpressException exception = getException(loader);
        if (exception != null) {
            this.exception = exception;
            int errorMessage = getErrorMessage(exception);
            if (!isItemsEmpty()) {
                showError(errorMessage);
            }
            showList();
            getLoaderManager().destroyLoader(loader.getId());
            return;
        }

        this.exception = null;
        this.items = courses;
        if (!courses.isEmpty()) {
            getDao().insertOrReplaceInTx(courses);
        }
        displayDataFromDB();
        showList();
    }

    @Override
    public void refreshWithProgress() {
        getPager().reset();
        super.refreshWithProgress();
    }

}
