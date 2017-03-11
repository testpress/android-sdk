package in.testpress.ui;

import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import in.testpress.R;
import in.testpress.core.TestpressException;
import in.testpress.network.BaseResourcePager;
import in.testpress.util.ThrowableLoader;
import in.testpress.util.UIUtils;
import in.testpress.util.ViewUtils;

/**
 * Base fragment for displaying items in grid with swipe refresh facility & progress for loading &
 * empty view
 *
 * @param <E>
 */
public abstract class BaseGridFragment<E> extends Fragment
        implements LoaderManager.LoaderCallbacks<List<E>> {

    private static final String FORCE_REFRESH = "forceRefresh";

    /**
     * List items provided to {@link #onLoadFinished(Loader, List)}
     */
    protected List<E> items = Collections.emptyList();

    /**
     * Grid view
     */
    protected TableLayout tableLayout;

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
     * Is the grid currently shown?
     */
    protected boolean gridShown;

    protected TestpressException exception;
    boolean firstCallBack = true;
    boolean needRetryButton;
    protected BaseResourcePager<E> pager;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.testpress_grid_fragment, container, false);
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tableLayout = (TableLayout) view.findViewById(R.id.table_layout);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                forceRefresh();
            }
        });
        swipeRefreshLayout.setColorSchemeResources(R.color.testpress_color_primary);
        if(!gridShown) {
            // Show loading progress
            TypedValue typed_value = new TypedValue();
            getActivity().getTheme().resolveAttribute(
                    android.support.v7.appcompat.R.attr.actionBarSize, typed_value, true);
            swipeRefreshLayout.setProgressViewOffset(false, 0, getResources()
                    .getDimensionPixelSize(typed_value.resourceId));
            swipeRefreshLayout.setRefreshing(true);
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
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initItems();
    }

    protected void initItems() {
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
            showGrid(gridShown);
        }
    }

    @Override
    public Loader<List<E>> onCreateLoader(int id, Bundle bundle) {
        return new ThrowableLoader<List<E>>(getActivity(), items) {
            @Override
            public List<E> loadData() throws TestpressException {
                do {
                    getPager().next();
                    items = getPager().getResources();
                } while (getPager().hasNext());
                return items;
            }
        };
    }

    /**
     * Create pager that provides resources
     *
     * @return pager
     */
    protected abstract BaseResourcePager<E> getPager();

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    /**
     * Force a refresh of the items displayed ignoring any cached items
     */
    protected void forceRefresh() {
        getPager().reset();
        items.clear();
        final Bundle bundle = new Bundle();
        bundle.putBoolean(FORCE_REFRESH, true);
        refreshWithProgress(bundle);
    }

    public void refreshWithProgress() {
        refreshWithProgress(null);
    }

    private void refreshWithProgress(final Bundle args) {
        if (!isUsable()) {
            return;
        }
        getPager().reset();
        swipeRefreshLayout.setEnabled(true);
        showGrid(false);
        getLoaderManager().restartLoader(0, args, this);
    }

    /**
     * Get error message to display for exception
     *
     * @param exception
     * @return string resource id
     */
    protected abstract int getErrorMessage(final TestpressException exception);

    public void onLoadFinished(final Loader<List<E>> loader, final List<E> items) {
        if (!isUsable()) {
            return;
        }
        final TestpressException exception = getException(loader);
        if (exception != null) {
            this.exception = exception;
            int errorMessage = getErrorMessage(exception);
            if (!items.isEmpty()) {
                showError(errorMessage);
            }
            showGrid();
            getLoaderManager().destroyLoader(loader.getId());
            return;
        }
        this.exception = null;
        this.items = items;
        displayItems();
        showGrid();
    }

    protected List<E> getItems() {
        return items;
    }

    protected void displayItems() {
        if (!isUsable()) {
            return;
        }
        List<E> items = getItems();
        tableLayout.removeAllViews();
        if (items.isEmpty()) {
            setEmptyText();
            if (!needRetryButton) {
                retryButton.setVisibility(View.GONE);
            }
        } else {
            int noOfItemsAvailable = items.size();    //Number of items Available
            int itemPosition = 0;   // Item position in grid
            int tableWidth = getContext().getResources().getDisplayMetrics().widthPixels;
            int tableRowWidth =
                    tableWidth - (tableLayout.getPaddingLeft() + tableLayout.getPaddingRight());
            // Get column spacing based on screen density (column spacing - 20dp)
            int columnSpacing = (int) UIUtils.getPixelFromDp(getActivity(), 20);
            // Calculate number of columns can be add in a row
            int noOfColumns = tableRowWidth / (getChildColumnWidth() + columnSpacing);
            int noOfColumnsInLastRow = noOfItemsAvailable % noOfColumns;
            int noOfRows = (noOfItemsAvailable / noOfColumns) + (noOfColumnsInLastRow != 0 ? 1 : 0);
            tableLayout.setGravity(Gravity.CENTER_HORIZONTAL);
            for (int j = 0; j < noOfRows; j++) {
                TableRow row = new TableRow(getActivity());
                TableRow.LayoutParams lp;
                if (noOfColumnsInLastRow != 0 && j == noOfRows - 1) {
                    lp = new TableRow.LayoutParams(noOfColumnsInLastRow);
                } else {
                    lp = new TableRow.LayoutParams(noOfColumns);
                }
                row.setLayoutParams(lp);
                for (int i = 1; i <= noOfColumns; i++) {
                    if (noOfItemsAvailable == itemPosition) {
                        break;
                    }
                    View childView = getChildView(items.get(itemPosition), row);
                    TableRow.LayoutParams params = getLayoutParams();
                    if (i != 1) {
                        params.leftMargin = columnSpacing;
                    }
                    childView.setLayoutParams(params);
                    row.addView(childView);
                    itemPosition++;
                }
                row.setGravity(Gravity.CENTER_HORIZONTAL);
                tableLayout.addView(row, j);
            }
        }
    }

    /**
     * Set the grid to be shown
     */
    protected void showGrid() {
        showGrid(true);
    }

    protected boolean isItemsEmpty() {
        return items.isEmpty();
    }

    /**
     * Show & hide grid
     *
     * @param show
     * @return this fragment
     */
    public BaseGridFragment<E> showGrid(final boolean show) {
        if (!isUsable()) {
            return this;
        }
        gridShown = show;
        if (show) {
            if (isItemsEmpty()) {
                show(emptyView);
            } else {
                hide(emptyView).show(tableLayout);
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
     * Show exception in a Snackbar
     *
     * @param message
     */
    protected void showError(final int message) {
        if (!isUsable()) {
            return;
        }
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
     * Set empty text on grid fragment
     *
     * @param message
     * @return this fragment
     */
    protected BaseGridFragment<E> setEmptyText(final String message) {
        if (emptyView != null) {
            emptyTitleView.setText(message);
            emptyDescView.setVisibility(View.GONE);
        }
        return this;
    }

    /**
     * Set empty text on grid fragment
     *
     * @param resId
     * @return this fragment
     */
    protected BaseGridFragment<E> setEmptyText(@StringRes final int resId) {
        if (emptyView != null) {
            emptyTitleView.setText(resId);
            emptyDescView.setVisibility(View.GONE);
        }
        return this;
    }

    /**
     * Set empty text on grid fragment
     *
     * @param title
     * @return this fragment
     */
    protected BaseGridFragment<E> setEmptyText(final int title, final int description, final int left) {
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

    protected abstract TableRow.LayoutParams getLayoutParams();

    protected abstract int getChildColumnWidth();

    protected abstract View getChildView(E item, ViewGroup parent);

    protected abstract void setEmptyText();

    /**
     * Detach from grid view.
     */
    @Override
    public void onDestroyView() {
        emptyView = null;
        swipeRefreshLayout = null;
        tableLayout = null;
        super.onDestroyView();
    }

    /**
     * Is this fragment still part of an activity and usable from the UI-thread?
     *
     * @return true if usable on the UI-thread, false otherwise
     */
    protected boolean isUsable() {
        return getActivity() != null;
    }

    private BaseGridFragment<E> show(final View view) {
        ViewUtils.setGone(view, false);
        return this;
    }

    private BaseGridFragment<E> hide(final View view) {
        ViewUtils.setGone(view, true);
        return this;
    }

    @Override
    public void onLoaderReset(Loader<List<E>> loader) {
        // left empty by default
    }
}