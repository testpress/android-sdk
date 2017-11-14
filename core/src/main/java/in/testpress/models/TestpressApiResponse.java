package in.testpress.models;

import java.util.ArrayList;
import java.util.List;

public class TestpressApiResponse<T> {
    private Integer count;
    private String next;
    private String previous;
    private Integer perPage;
    private List<T> results = new ArrayList<T>();

    /**
     *
     * @return
     * The count
     */
    public Integer getCount() {
        return count;
    }

    /**
     *
     * @param count
     * The count
     */
    public void setCount(Integer count) {
        this.count = count;
    }

    /**
     *
     * @return
     * The next
     */
    public String getNext() {
        return next;
    }

    /**
     *
     * @param next
     * The next
     */
    public void setNext(String next) {
        this.next = next;
    }

    /**
     *
     * @return
     * The previous
     */
    public Object getPrevious() {
        return previous;
    }

    /**
     *
     * @param previous
     * The previous
     */
    public void setPrevious(String previous) {
        this.previous = previous;
    }

    /**
     *
     * @return
     * The perPage
     */
    public Integer getPerPage() {
        return perPage;
    }

    /**
     *
     * @param perPage
     * The per_page
     */
    public void setPerPage(Integer perPage) {
        this.perPage = perPage;
    }

    /**
     *
     * @return
     * The results
     */
    public List<T> getResults() {
        return results;
    }

    /**
     *
     * @param results
     * The results
     */
    public void setResults(List<T> results) {
        this.results = results;
    }

    public boolean hasMore() {
        return !next.equals("null");
    }
}