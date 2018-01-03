package in.testpress.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import in.testpress.R;
import in.testpress.core.TestpressException;
import in.testpress.util.SingleTypeAdapter;
import in.testpress.util.ThrowableLoader;
import in.testpress.util.ViewUtils;

/**
 * Base fragment for displaying a list of items with swipe refresh facility & progress for loading
 *
 * @param <E>
 */
public abstract class BaseListViewFragment<E> extends Fragment
        implements LoaderManager.LoaderCallbacks<List<E>> {

    private static final String FORCE_REFRESH = "forceRefresh";

    /**
     * @param args bundle passed to the loader by the LoaderManager
     * @return true if the bundle indicates a requested forced refresh of the
     * items
     */
    protected static boolean isForceRefresh(final Bundle args) {
        return args != null && args.getBoolean(FORCE_REFRESH, false);
    }

    /**
     * List items provided to {@link #onLoadFinished(Loader, List)}
     */
    protected List<E> items = Collections.emptyList();

    /**
     * List view
     */
    protected ListView listView;

    /**
     * Empty view
     */
    protected View emptyView;
    protected TextView emptyTitleView;
    protected TextView emptyDescView;
    protected Button retryButton;

    /**
     * Swipe Refresh Layout
     */
    protected SwipeRefreshLayout swipeRefreshLayout;

    /**
     * Is the list currently shown?
     */
    protected boolean listShown;

    protected TestpressException exception;
    protected boolean firstCallBack = true;
    boolean needRetryButton;

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Init loader only for the first time of creating the fragment after that if user come to
        // the same tab don't init the loader
        if (firstCallBack) {
            getLoaderManager().initLoader(0, null, this);
            firstCallBack = false;
        } else {
            if (items.isEmpty()) {
                if (exception != null){
                    // Set error message in empty view
                    getErrorMessage(exception);
                } else {
                    // Set no items message in empty view
                    setEmptyText();
                    if (!needRetryButton) {
                        retryButton.setVisibility(View.GONE);
                    }
                }
            }
            setListShown(listShown);
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.testpress_list, container, false);
    }

    /**
     * Detach from list view.
     */
    @Override
    public void onDestroyView() {
        emptyView = null;
        swipeRefreshLayout = null;
        listView = null;
        super.onDestroyView();
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        listView = (ListView) view.findViewById(android.R.id.list);
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                onListItemClick((ListView) parent, view, position, id);
            }
        });
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshWithProgress();
            }
        });
        swipeRefreshLayout.setColorSchemeResources(R.color.testpress_color_primary);
        if(!listShown) {
            // Show loading progress
            TypedValue typed_value = new TypedValue();
            getActivity().getTheme().resolveAttribute(android.support.v7.appcompat.R.attr.actionBarSize, typed_value, true);
            swipeRefreshLayout.setProgressViewOffset(false, 0, getResources().getDimensionPixelSize(typed_value.resourceId));
            if (isItemsEmpty()) {
                swipeRefreshLayout.setRefreshing(true);
            }
        }
        emptyView = view.findViewById(R.id.empty_container);
        emptyTitleView = (TextView) view.findViewById(R.id.empty_title);
        emptyDescView = (TextView) view.findViewById(R.id.empty_description);
        retryButton = (Button) view.findViewById(R.id.retry_button);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshWithProgress();
            }
        });
        configureList(getActivity(), getListView());
    }

    /**
     * Configure list after view has been created
     *
     * @param activity
     * @param listView
     */
    protected void configureList(final Activity activity, final ListView listView) {
        listView.setAdapter(createAdapter());
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    /**
     * Force a refresh of the items displayed ignoring any cached items
     */
    protected void forceRefresh() {
        final Bundle bundle = new Bundle();
        bundle.putBoolean(FORCE_REFRESH, true);
        refresh(bundle);
    }

    /**
     * Refresh the fragment's list
     */
    public void refresh() {
        refresh(null);
    }

    private void refresh(final Bundle args) {
        if (!isUsable()) {
            return;
        }
        getLoaderManager().restartLoader(0, args, this);
    }

    public void clearItemsAndRefresh() {
        items.clear();
        getListAdapter().getWrappedAdapter().setItems(items);
        refreshWithProgress();
    }

    /**
     * Get error message to display for exception
     *
     * @param exception
     * @return string resource id
     */
    protected abstract int getErrorMessage(final TestpressException exception);

    public void onLoadFinished(final Loader<List<E>> loader, final List<E> items) {
        final TestpressException exception = getException(loader);
        if (exception != null) {
            this.exception = exception;
            int errorMessage = getErrorMessage(exception);
            if (!items.isEmpty()) {
                showError(errorMessage);
            }
            showList();
            getLoaderManager().destroyLoader(loader.getId());
            return;
        }
        this.exception = null;
        updateItems(items);
    }

    public void updateItems(List<E> items) {
        this.items = items;
        if (items.isEmpty()) {
            setEmptyText();
            if (!needRetryButton) {
                retryButton.setVisibility(View.GONE);
            }
        }
        getListAdapter().getWrappedAdapter().setItems(items.toArray());
        showList();
    }

    protected abstract void setEmptyText();

    /**
     * Create adapter to display items
     *
     * @return adapter
     */
    protected HeaderFooterListAdapter<SingleTypeAdapter<E>> createAdapter() {
        final SingleTypeAdapter<E> wrapped = createAdapter(items);
        return new HeaderFooterListAdapter<SingleTypeAdapter<E>>(getListView(),
                wrapped);
    }

    /**
     * Create adapter to display items
     *
     * @param items
     * @return adapter
     */
    protected abstract SingleTypeAdapter<E> createAdapter(final List<E> items);

    /**
     * Set the list to be shown
     */
    protected void showList() {
        setListShown(true);
    }

    @Override
    public void onLoaderReset(Loader<List<E>> loader) {
        // left empty by default
    }

    /**
     * Show exception in a Snackbar
     *
     * @param message
     */
    protected void showError(final int message) {
        Snackbar.make(swipeRefreshLayout, message, Snackbar.LENGTH_SHORT).show();
    }

    /**
     * Get exception from loader if it provides one by being a
     * {@link ThrowableLoader}
     *
     * @param loader
     * @return exception or null if none provided
     */
    protected TestpressException getException(final Loader<List<E>> loader) {
        if (loader instanceof ThrowableLoader) {
            return ((ThrowableLoader<List<E>>) loader).clearException();
        } else {
            return null;
        }
    }

    /**
     * Refresh the list with the progress bar showing
     */
    public void refreshWithProgress() {
        swipeRefreshLayout.setEnabled(true);
        setListShown(false);
        refresh();
    }

    /**
     * Get {@link ListView}
     *
     * @return listView
     */
    public ListView getListView() {
        return listView;
    }

    /**
     * Get list adapter
     *
     * @return list adapter
     */
    @SuppressWarnings("unchecked")
    protected HeaderFooterListAdapter<SingleTypeAdapter<E>> getListAdapter() {
        if (listView != null) {
            return (HeaderFooterListAdapter<SingleTypeAdapter<E>>) listView.getAdapter();
        }
        return null;
    }

    /**
     * Set list adapter to use on list view
     *
     * @param adapter
     * @return this fragment
     */
    protected BaseListViewFragment<E> setListAdapter(final ListAdapter adapter) {
        if (listView != null) {
            listView.setAdapter(adapter);
        }
        return this;
    }

    private BaseListViewFragment<E> show(final View view) {
        ViewUtils.setGone(view, false);
        return this;
    }

    private BaseListViewFragment<E> hide(final View view) {
        ViewUtils.setGone(view, true);
        return this;
    }

    protected boolean isItemsEmpty() {
        return items.isEmpty();
    }

    /**
     * Set list shown or show progress bar
     *
     * @param shown
     * @return this fragment
     */
    public BaseListViewFragment<E> setListShown(final boolean shown) {
        if (!isUsable()) {
            return this;
        }
        listShown = shown;
        if (shown) {
            if (isItemsEmpty()) {
                show(emptyView);
            } else {
                hide(emptyView).show(listView);
            }
            swipeRefreshLayout.setRefreshing(false);
        } else {
            hide(emptyView);
            swipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(true);
                }
            });
        }
        return this;
    }

    /**
     * Set empty text on list fragment
     *
     * @param message
     * @return this fragment
     */
    protected BaseListViewFragment<E> setEmptyText(final String message) {
        if (emptyView != null) {
            emptyTitleView.setText(message);
            emptyDescView.setVisibility(View.GONE);
        }
        return this;
    }

    /**
     * Set empty text on list fragment
     *
     * @param resId
     * @return this fragment
     */
    protected BaseListViewFragment<E> setEmptyText(final int resId) {
        if (emptyView != null) {
            emptyTitleView.setText(resId);
            emptyDescView.setVisibility(View.GONE);
        }
        return this;
    }

    /**
     * Set empty text on list fragment
     *
     * @param title
     * @return this fragment
     */
    protected BaseListViewFragment<E> setEmptyText(final int title, final int description, final int left) {
        if (emptyView != null) {
            if (isItemsEmpty()) {
                swipeRefreshLayout.setEnabled(false);
            }
            emptyTitleView.setText(title);
            emptyTitleView.setCompoundDrawablesWithIntrinsicBounds(left, 0, 0, 0);
            emptyDescView.setText(description);
            retryButton.setVisibility(View.VISIBLE);
        }
        return this;
    }

    /**
     * Callback when a list view item is clicked
     *
     * @param l
     * @param v
     * @param position
     * @param id
     */
    public void onListItemClick(final ListView l, final View v,
                                final int position, final long id) {
    }

    /**
     * Is this fragment still part of an activity and usable from the UI-thread?
     *
     * @return true if usable on the UI-thread, false otherwise
     */
    protected boolean isUsable() {
        return getActivity() != null;
    }
}