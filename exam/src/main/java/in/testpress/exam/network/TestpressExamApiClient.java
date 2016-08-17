package in.testpress.exam.network;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import in.testpress.core.TestpressSdk;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

public class TestpressExamApiClient {

    private final RestAdapter restAdapter;

    /**
     * Exams List URL
     */
    public static final String URL_EXAMS_FRAG =  "/api/v2.2/exams/";

    /**
     * Query Params
     */
    public static final String SEARCH_QUERY = "q";
    public static final String STATE = "state";
    public static final String PAGE = "page";

    public TestpressExamApiClient() {
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();

        restAdapter = new RestAdapter.Builder()
                .setEndpoint(TestpressSdk.BASE_URL)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setConverter(new GsonConverter(gson))
                .build();
    }

    public static String getAuthToken() {
        return "JWT " + TestpressSdk.getAuthToken();
    }

    public ExamService getExamService() {
        return restAdapter.create(ExamService.class);
    }
}
