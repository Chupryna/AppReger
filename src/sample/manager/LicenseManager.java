package sample.manager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import sample.data.model.license.AuthCallback;
import sample.data.model.license.LicenseAPIClient;
import sample.data.model.license.LicenseResponse;
import sample.data.model.license.User;

import javax.annotation.Nonnull;

public class LicenseManager {

	private static volatile LicenseManager instance;

	private LicenseManager() {
	}

	public static LicenseManager getInstance() {
		LicenseManager localInstance = instance;
		if (localInstance == null) {
			synchronized (LicenseManager.class) {
				localInstance = instance;
				if (localInstance == null) {
					instance = localInstance = new LicenseManager();
				}
			}
		}

		return localInstance;
	}

	public void checkLicense(User user, AuthCallback callback) {
		LicenseAPIClient.getInstance().getService().getLicense(user).enqueue(new Callback<LicenseResponse>() {
			@Override
			public void onResponse(@Nonnull Call<LicenseResponse> call, @Nonnull Response<LicenseResponse> response) {
				int responseCode = response.code();
				System.out.println(responseCode);
				System.out.println(response.body());

				if (responseCode != 200) {
					callback.onFailure(new Exception("Error " + responseCode));
					return;
				}

				if (response.body() != null) {
					callback.onSuccess(response.body());
				} else {
					callback.onFailure(new Exception("Response body is empty"));
				}
			}

			@Override
			public void onFailure(@Nonnull Call<LicenseResponse> call, @Nonnull Throwable throwable) {
				throwable.printStackTrace();
				callback.onFailure(throwable);
			}
		});
	}
}
