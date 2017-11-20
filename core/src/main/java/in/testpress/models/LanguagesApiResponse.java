package in.testpress.models;

import java.util.ArrayList;
import java.util.List;

import in.testpress.models.greendao.Language;

public class LanguagesApiResponse {

    private int count;
    private int per_page;
    private List<Language> results = new ArrayList<Language>();

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getPer_page() {
        return per_page;
    }

    public void setPer_page(int per_page) {
        this.per_page = per_page;
    }

    public List<Language> getResults() {
        return results;
    }

    public void setResults(List<Language> results) {
        this.results = results;
    }
}
