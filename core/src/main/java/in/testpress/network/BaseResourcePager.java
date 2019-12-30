package in.testpress.network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import in.testpress.core.TestpressRetrofitRequest;
import in.testpress.core.TestpressCallback;
import in.testpress.core.TestpressException;
import in.testpress.models.TestpressApiResponse;
import retrofit2.Response;

/**
 * Generic resource pager for elements with an id that can be paged
 *
 * @param <E>
 */
public abstract class BaseResourcePager<E> {

    protected TestpressApiResponse<E> response;

    /**
     * Next page to request
     */
    public int page = 1;

    /**
     * Number of pages to request
     */
    protected int count = 1;

    /**
     * All resources retrieved
     */
    protected final Map<Object, E> resources = new LinkedHashMap<Object, E>();

    /**
     * Query Params to be passed
     */
    public Map<String, Object> queryParams = new LinkedHashMap<String, Object>();

    /**
     * Are more pages available?
     */
    protected boolean hasMore;

    protected RetrofitCall<TestpressApiResponse<E>> retrofitCall;

    /**
     * Reset the number of the next page to be requested from {@link #next()}
     * and clear all stored state
     *
     * @return this pager
     */
    public BaseResourcePager<E> reset() {
        page = 1;
        return clear();
    }

    /**
     * Clear all stored resources and have the next call to {@link #next()} load
     * all previously loaded pages using count variable
     *
     * @return this pager
     */
    public BaseResourcePager<E> clear() {
        count = Math.max(1, page - 1);
        page = 1;
        resources.clear();
        response = null;
        hasMore = true;
        clearQueryParams();
        return this;
    }

    public BaseResourcePager<E> clearResources() {
        resources.clear();
        return this;
    }

    /**
     * Get number of resources loaded into this pager
     *
     * @return number of resources
     */
    public int size() {
        return resources.size();
    }

    /**
     * Get resources
     *
     * @return resources
     */
    public List<E> getResources() {
        return new ArrayList<E>(resources.values());
    }

    public void setResources(List<E> items) {
        for(E item : items) {
            resources.put(getId(item), item);
        }
    }

    /**
     * Get the next page of resources
     *
     * @return true if more pages
     * @throws TestpressException
     */
    public boolean next() throws TestpressException {
        boolean emptyPage = false;
        try {
            for (int i = 0; i < count && hasNext(); i++) {
                Response<TestpressApiResponse<E>> retrofitResponse = getItems(page, -1);
                List<E> resourcePage;
                if (retrofitResponse.isSuccessful()) {
                    response = retrofitResponse.body();
                    resourcePage = response.getResults();
                } else {
                    hasMore = false;
                    throw TestpressException.httpError(retrofitResponse);
                }
                emptyPage = resourcePage.isEmpty();
                if (emptyPage)
                    break;
                storeReesource(resourcePage);
                page++;
            }
            resetPageCount();
        } catch (Exception e) {
            hasMore = false;
            if (e instanceof IOException) {
                throw TestpressException.networkError((IOException) e);
            } else {
                throw TestpressException.unexpectedError(e);
            }
        }
        hasMore = hasNext() && !emptyPage;
        return hasMore;
    }

    private void storeReesource(List<E> resourcePage) {
        for (E resource : resourcePage) {
            resource = register(resource);
            if (resource == null)
                continue;
            resources.put(getId(resource), resource);
        }
    }

    private void resetPageCount() {
        // Set count value to 1 if first load request made after call clear()
        if (count > 1) {
            count = 1;
        }
    }


    public RetrofitCall<TestpressApiResponse<E>> fetchItemsAsync(
            final TestpressRetrofitRequest<E> retrofitRequest, final TestpressCallback<List<E>> callback) {
        retrofitCall = retrofitRequest.getRetrofitCall(page, -1).enqueue(new TestpressCallback<TestpressApiResponse<E>>() {
            @Override
            public void onSuccess(TestpressApiResponse<E> result) {
                response = result;
                List<E> resourcePage = response.getResults();

                if (resourcePage.isEmpty()) {
                    hasMore = false;
                } else {
                    storeReesource(resourcePage);
                    page++;

                    if (hasNext()) {
                        fetchItemsAsync(retrofitRequest, callback);
                        return;
                    }

                    hasMore = hasNext();
                }
                resetPageCount();
                callback.onSuccess(getResources());
            }

            @Override
            public void onException(TestpressException exception) {
                hasMore = false;
                callback.onException(exception);
            }
        });
        return retrofitCall;
    }

    public boolean hasNext() {
        return response == null || response.getNext() != null;
    }

    /**
     * Set hasMore
     *
     * @param hasMore Are more pages available to request
     */
    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }

    /**
     * Are more pages available to request?
     *
     * @return true if the last call to {@link #next()} returned true, false
     *         otherwise
     */
    public boolean hasMore() {
        return hasMore;
    }

    /**
     * Callback to register a fetched resource before it is stored in this pager
     * <p>
     * Sub-classes may override
     *
     * @param resource
     * @return resource
     */
    protected E register(final E resource) {
        return resource;
    }

    /**
     * Get id for resource
     *
     * @param resource
     * @return id
     */
    protected abstract Object getId(E resource);

    /**
     * Create iterator to return given page and size
     *
     * @param page
     * @param size
     * @return iterator
     */
    public abstract Response<TestpressApiResponse<E>> getItems(final int page, final int size)
            throws IOException;

    public Object getQueryParams(String key) {
        return queryParams.get(key);
    }

    public void setQueryParams(String key, Object value) {
        queryParams.put(key, value);
    }

    public void removeQueryParams(String key) {
        queryParams.remove(key);
    }

    public void clearQueryParams() {
        queryParams.clear();
    }
}

