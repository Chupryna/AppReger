package sample.data.model.license;

public interface AuthCallback {
	void onSuccess(LicenseResponse licenseResponse);
	void onFailure(Throwable throwable);
}
