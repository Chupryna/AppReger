package sample.data;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;
import sample.data.model.license.LicenseResponse;
import sample.data.model.license.User;

import java.util.Map;

public interface API {

	@POST()
	Call<ResponseBody> rotateIP(@Url String url, @QueryMap Map<String, String> query);

	@POST("validation/db.php")
	Call<LicenseResponse> getLicense(@Body User user);
}
