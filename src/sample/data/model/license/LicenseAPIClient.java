package sample.data.model.license;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import sample.data.API;

import java.util.concurrent.TimeUnit;

public class LicenseAPIClient {

	private static final String BASE_URL = "http://reger-validation.ho.ua/";

	private static volatile LicenseAPIClient instance;
	private Retrofit apiClient = null;

	private LicenseAPIClient() {
	}

	public static LicenseAPIClient getInstance() {
		LicenseAPIClient localInstance = instance;
		if (localInstance == null) {
			synchronized (LicenseAPIClient.class) {
				localInstance = instance;
				if (localInstance == null) {
					instance = localInstance = new LicenseAPIClient();
				}
			}
		}

		return localInstance;
	}

	private Retrofit getAPIClient() {
		if (apiClient == null) {
			apiClient = createAPIClient();
		}

		return apiClient;

	}

	private Retrofit createAPIClient() {
		OkHttpClient client = new OkHttpClient.Builder()
				.readTimeout(60, TimeUnit.SECONDS)
				.writeTimeout(60, TimeUnit.SECONDS)
				.connectTimeout(60, TimeUnit.SECONDS)
				.build();

		return new Retrofit.Builder()
				.baseUrl(BASE_URL)
				.addConverterFactory(GsonConverterFactory.create())
				.client(client)
				.build();
	}

	public API getService() {
		return getAPIClient().create(API.class);
	}
}