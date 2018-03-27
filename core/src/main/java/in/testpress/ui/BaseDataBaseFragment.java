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

    @Override
    public Loader<List<T>> onCreateLoader(int id, Bundle args) {
        return new DBItemsLoader<>(this, items);
    }

    private static class DBItemsLoader<T, K> extends ThrowableLoader<List<T>> {

        private BaseDataBaseFragment<T, K> fragment;

        DBItemsLoader(BaseDataBaseFragment<T, K> fragment, List<T> data) {
            super(fragment.getContext(), data);
            this.fragment = fragment;
        }

        @Override
        public List<T> loadData() throws TestpressException {
            do {
                fragment.getPager().next();
            } while (fragment.getPager().hasNext());
            return fragment.getPager().getResources();
        }
    }

    @Override
    public void onLoadFinished(Loader<List<T>> loader, List<T> items) {
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
        this.items = items;
        if (!items.isEmpty()) {
            getDao().insertOrReplaceInTx(items);
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
