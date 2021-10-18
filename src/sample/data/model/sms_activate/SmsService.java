package sample.data.model.sms_activate;

public class SmsService {

	private final String name;
	private final String link;
	private final String apiKey;

	public SmsService(String name, String link, String apiKey) {
		this.name = name;
		this.link = link;
		this.apiKey = apiKey;
	}

	public String getName() {
		return name;
	}

	public String getLink() {
		return link;
	}

	public String getApiKey() {
		return apiKey;
	}

	@Override
	public String toString() {
		return name;
	}
}
