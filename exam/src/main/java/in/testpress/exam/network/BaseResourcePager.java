package in.testpress.exam.network;


import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import in.testpress.core.TestpressException;
import in.testpress.exam.models.TestpressApiResponse;
import retrofit2.Response;


/**
 * Generic resource pager for elements with an id that can be paged
 *
 * @param <E>
 */
public abstract class BaseResourcePager<E> {

    protected TestpressExamApiClient apiClient;
    protected TestpressApiResponse<E> response;

    /**
     * Next page to request
     */
    protected int page = 1;

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

    public BaseResourcePager(final TestpressExamApiClient apiClient) {
        this.apiClient = apiClient;
    }

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
                for (E resource : resourcePage) {
                    resource = register(resource);
                    if (resource == null)
                        continue;
                    resources.put(getId(resource), resource);
                }
                page++;
            }
            // Set count value to 1 if first load request made after call clear()
            if (count > 1) {
                count = 1;
            }

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

