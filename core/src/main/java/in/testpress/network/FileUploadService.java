package in.testpress.network;

import in.testpress.models.FileDetails;
import okhttp3.MultipartBody;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface FileUploadService {

    @Multipart
    @POST("/api/v2.2/image_upload/")
    RetrofitCall<FileDetails> upload(@Part MultipartBody.Part body);
}
