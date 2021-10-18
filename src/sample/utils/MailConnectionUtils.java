package sample.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import sample.data.model.EmailConnection;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Collections;
import java.util.List;

public class MailConnectionUtils {

	private static final String SERVERS_JSON = "resources/settings.json";
	private static volatile MailConnectionUtils instance = null;
	private volatile List<EmailConnection> emailConnections;

	private MailConnectionUtils() {
	}

	public static MailConnectionUtils getInstance() {
		MailConnectionUtils localInstance = instance;
		if (localInstance == null) {
			synchronized (MailConnectionUtils.class) {
				localInstance = instance;
				if (localInstance == null) {
					instance = localInstance = new MailConnectionUtils();
				}
			}
		}

		return localInstance;
	}

	public void init() {
		Thread loadJson = new Thread(() -> {
			try {
				emailConnections = new Gson().fromJson(new FileReader(SERVERS_JSON),
						new TypeToken<List<EmailConnection>>() {}.getType());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				System.out.println("Дані поштових сервісів не завантажено. Відсутній :" + SERVERS_JSON);
			}
		});
		loadJson.start();
	}

	public EmailConnection findConnectionInfoByDomain(String domain) {
		return emailConnections.stream()
				.filter(info -> info.getDomains().contains(domain))
				.findFirst()
				.orElse(null);
	}

	public EmailConnection createNewConnectionInfo(String domain) {
		EmailConnection connection =  new EmailConnection();
		connection.setDomains(Collections.singletonList(domain));
		connection.setType("imap");
		connection.setHostname("imap." + domain);
		connection.setReservedHostname("mail." + domain);
		connection.setPort(143);
		connection.setSocketType("plain");
		connection.setUserName("%EMAILADDRESS%");

		return connection;
	}
}
