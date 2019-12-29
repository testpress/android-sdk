package in.testpress.v2_4;

import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import in.testpress.core.TestpressException;
import in.testpress.v2_4.models.ApiResponse;
import retrofit2.Response;

/**
 * Generic resource pager for elements with an id that can be paged
 *
 * @param <T>
 */
public abstract class BaseResourcePager<T, L> {

    protected ApiResponse<T> apiResponse;

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
    protected final Map<Object, L> resources = new LinkedHashMap<>();

    /**
     * Query Params to be passed
     */
    public Map<String, Object> queryParams = new LinkedHashMap<>();

    /**
     * Are more pages available?
     */
    protected boolean hasMore;

    /**
     * Reset the number of the next page to be requested from {@link #next()}
     * and clear all stored state
     *
     * @return this pager
     */
    public BaseResourcePager<T, L> reset() {
        page = 1;
        return clear();
    }

    /**
     * Clear all stored resources and have the next call to {@link #next()} load
     * all previously loaded pages using count variable
     *
     * @return this pager
     */
    public BaseResourcePager<T, L> clear() {
        count = Math.max(1, page - 1);
        page = 1;
        resources.clear();
        apiResponse = null;
        hasMore = true;
        clearQueryParams();
        return this;
    }

    public BaseResourcePager<T, L> clearResources() {
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
    public List<L> getResources() {
        return new ArrayList<L>(resources.values());
    }

    public void setResources(List<L> items) {
        for(L item : items) {
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
                Response<ApiResponse<T>> retrofitResponse = getResponse(page, -1);
                List<L> resources;
                if (retrofitResponse.isSuccessful()) {
                    apiResponse = retrofitResponse.body();
                    resources = getItems(apiResponse.getResults());
                } else {
                    hasMore = false;
                    throw TestpressException.httpError(retrofitResponse);
                }
                Log.d("BaseResourcePager", "next: " + resources.size());
                emptyPage = resources.isEmpty();
                if (emptyPage)
                    break;
                for (L resource : resources) {
                    resource = register(resource);
                    if (resource == null)
                        continue;
                    this.resources.put(getId(resource), resource);
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
        return apiResponse == null || apiResponse.getNext() != null;
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
    protected L register(final L resource) {
        return resource;
    }

    /**
     * Get id for resource
     *
     * @param resource
     * @return id
     */
    protected abstract Object getId(L resource);

    /**
     * Create iterator to return given page and size
     *
     * @param page
     * @param size
     * @return iterator
     */
    public abstract Response<ApiResponse<T>> getResponse(final int page, final int size)
            throws IOException;

    public abstract List<L> getItems(T resultResponse);

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

    public T getListResponse() {
        return apiResponse.getResults();
    }
}

