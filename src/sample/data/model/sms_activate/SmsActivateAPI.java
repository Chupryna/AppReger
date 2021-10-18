package sample.data.model.sms_activate;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface SmsActivateAPI {

	enum Error {
		ApiKeyNotFound("apiKeyNotFound"),
		NoNumber("NO_NUMBERS"),
		NoMoney("NO_BALANCE");

		public String message;

		Error(String message) {
			this.message = message;
		}
	}

	enum Status {
		SMS_WAS_SENT("1"),
		GET_NEW_CODE("3"),
		END_OF_USING("6"),
		NUMBER_IS_BANNED("8");

		public String code;

		Status(String code) {
			this.code = code;
		}
	}

	enum StatusResponse {
		ACCESS_READY, ACCESS_RETRY_GET, ACCESS_ACTIVATION, ACCESS_CANCEL, NO_ACTIVATION, STATUS_CANCEL, BAD_STATUS
	}

	String SERVICE = "ig";

	@GET("stubs/handler_api.php?action=getBalance")
	Call<ResponseBody> getBalance(@Query("api_key") String apiKey);

	@GET("stubs/handler_api.php?action=getNumbersStatus")
	Call<ResponseBody> getCountNumber(@Query("api_key") String apiKey,
											 @Query("country") String country,
											 @Query("operator") String operator);

	@GET("stubs/handler_api.php?action=getNumber")
	Call<ResponseBody> getNumber(@Query("api_key") String apiKey,
								   @Query("service") String service,
								   @Query("country") String country,
								   @Query("operator") String operator);

	@GET("stubs/handler_api.php?action=getStatus")
	Call<ResponseBody> getStatus(@Query("api_key") String apiKey,
								 @Query("id") String id);

	@GET("stubs/handler_api.php?action=setStatus")
	Call<ResponseBody> setStatus(@Query("api_key") String apiKey,
								 @Query("status") String status,
								 @Query("id") String id);

//	@GET("api/getBalance/")
//	Call<BalanceResponse> getBalance(@Query("apiKey") String apiKey);
//
//	@GET("api/getCountNumber/")
//	Call<CountNumberResponse> getCountNumber(@Query("apiKey") String apiKey,
//											 @Query("service") String service,
//											 @Query("country") String country,
//											 @Query("operator") String operator);
//
//	@GET("api/getNumber/")
//	Call<NumberResponse> getNumber(@Query("apiKey") String apiKey,
//								   @Query("service") String service,
//								   @Query("country") String country,
//								   @Query("operator") String operator);
}
