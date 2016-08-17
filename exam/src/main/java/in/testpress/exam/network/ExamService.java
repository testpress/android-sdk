package in.testpress.exam.network;

import java.util.Map;

import in.testpress.exam.models.Exam;
import in.testpress.exam.models.TestpressApiResponse;
import retrofit.http.EncodedPath;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Path;
import retrofit.http.QueryMap;

public interface ExamService {

    @GET(TestpressExamApiClient.URL_EXAMS_FRAG)
    TestpressApiResponse<Exam> getExams(@QueryMap Map<String, Object> options,
                                        @Header("Authorization") String authorization);

}


