package sample.utils;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import sample.data.model.Account;
import sample.data.model.Email;
import sample.data.model.Proxy;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class Logger {

	public enum UtilType {
		WEB_REGER("Web reger"),
		POSTER("Poster");

		private final String name;

		UtilType(String name) {
			this.name = name;
		}
	}

	private static final String LOG = "log.txt";
	private static final String BAD = "bad.txt";
	private static final String GOOD = "good.txt";
	private static final String GOOD_WITH_PROXY = "good with proxy.txt";
	private static final String GOOD_WITH_COOKIE = "good with cookie.txt";
	private static final String GOOD_WITH_COOKIE_JSON = "good with cookie json.txt";
	private static final String PROXY = "proxy.txt";
	private static final String ERROR = "error.txt";
	private static final String LOGINS = "logins.txt";
	private static final String CONFIG = "config.ini";
	private static final String LOG_FORMAT = "[%s] (Потік %s) %s\n";
	private final TextArea logField;
	private PrintWriter logWriter;
	private PrintWriter goodWriter;
	private PrintWriter goodWithProxyWriter;
	private PrintWriter goodWithCookieWriter;
	private PrintWriter goodWithCookieJsonWriter;
	private PrintWriter proxyWriter;
	private PrintWriter badWriter;
	private PrintWriter errorWriter;
	private PrintWriter loginWriter;
	private PrintWriter settingWriter;
	private SimpleDateFormat dateFormat;

	public Logger(TextArea logField) {
		this.logField = logField;
	}

	public void init(UtilType type) {
		try {
			File dir = createDir(type);
			logWriter = new PrintWriter(new File(dir, LOG));
			badWriter = new PrintWriter(new File(dir, BAD));
			goodWriter = new PrintWriter(new File(dir, GOOD));
			errorWriter = new PrintWriter(new File(dir, ERROR));
			switch (type) {
				case WEB_REGER:
					goodWithProxyWriter = new PrintWriter(new File(dir, GOOD_WITH_PROXY));
					goodWithCookieWriter = new PrintWriter(new File(dir, GOOD_WITH_COOKIE));
					proxyWriter = new PrintWriter(new File(dir, PROXY));
					loginWriter = new PrintWriter(new File(dir, LOGINS));
					settingWriter = new PrintWriter(CONFIG);
					break;
				case POSTER:
					goodWithProxyWriter = new PrintWriter(new File(dir, GOOD_WITH_PROXY));
					goodWithCookieWriter = new PrintWriter(new File(dir, GOOD_WITH_COOKIE));
					goodWithCookieJsonWriter = new PrintWriter(new File(dir, GOOD_WITH_COOKIE_JSON));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		dateFormat = new SimpleDateFormat("HH:mm:ss");
	}

	public synchronized void info(String msg) {
		int threadNumber = Utils.getThreadNumber(Thread.currentThread());
		info(msg, threadNumber);
	}

	public synchronized void info(String msg, int threadNumber) {
		String time = dateFormat.format(new Date(System.currentTimeMillis()));

		logWriter.write(String.format(LOG_FORMAT, time, threadNumber, msg));
		logWriter.flush();
		Platform.runLater(() -> logField.appendText(String.format(LOG_FORMAT, time, threadNumber, msg)));
		System.out.println(String.format(LOG_FORMAT, time, threadNumber, msg));
	}

	public synchronized void printGood(Account account) {
		goodWriter.println(account.getMospanoffFormat());
		goodWriter.flush();
	}

	public synchronized void printGood(Email email) {
		goodWriter.println(email.getFullInfo());
		goodWriter.flush();
	}

	public synchronized void printGoodWithProxy(Account account) {
		goodWithProxyWriter.println(account.getAccountWithProxy());
		goodWithProxyWriter.flush();
	}

	public void printGoodWithCookie(Account account) {
		goodWithCookieWriter.println(account.getAccountWithCookie());
		goodWithCookieWriter.flush();
	}

	public void printGoodWithCookieJson(Account account) {
		goodWithCookieJsonWriter.println(account.getAccountWithCookie());
		goodWithCookieJsonWriter.flush();
	}

	public synchronized void printProxy(Proxy proxy) {
		proxyWriter.println(proxy.toString());
		proxyWriter.flush();
	}

	public synchronized void printBad(Account account, String type) {
		badWriter.println(String.format("[%s] %s - %s:%s", type, account.getUserName(), account.getOldEmail(), account.getOldEmailPassword()));
		badWriter.flush();
	}

	public synchronized void printBad(Email email) {
		badWriter.println(String.format("%s:%s", email.getEmail(), email.getPassword()));
		badWriter.flush();
	}

	public synchronized void printBad(Account account) {
		badWriter.println(account.getMospanoffFormat());
		badWriter.flush();
	}

	public void printLogin(Account account) {
		loginWriter.println(account.getUserName());
		loginWriter.flush();
	}

	public synchronized void printError(Throwable throwable) {
		errorWriter.println(dateFormat.format(new Date(System.currentTimeMillis())));
		errorWriter.flush();
		throwable.printStackTrace(errorWriter);
	}

	public synchronized void saveSettings(Map<String, String> settings) {
		for (Map.Entry<String, String> setting : settings.entrySet()) {
			settingWriter.println(setting.getKey() + "=" + setting.getValue());
		}
		settingWriter.flush();
	}

	public synchronized void closeStreams() {
		logWriter.close();
		goodWriter.close();
		badWriter.close();
		errorWriter.close();
		if (goodWithProxyWriter != null) {
			goodWithProxyWriter.close();
		}
		if (proxyWriter != null) {
			proxyWriter.close();
		}
		if (loginWriter != null) {
			loginWriter.close();
		}
		if (goodWithCookieWriter != null) {
			goodWithCookieWriter.close();
		}
		if (settingWriter != null) {
			settingWriter.close();
		}
		if (goodWithCookieJsonWriter != null) {
			goodWithCookieJsonWriter.close();
		}
	}

	private File createDir(UtilType type) throws IOException {
		File resultsDir = new File("Results");
		if (!resultsDir.exists()) {
			Files.createDirectory(resultsDir.toPath());
		}

		File typeDir = new File(resultsDir, type.name);
		if (!typeDir.exists()) {
			Files.createDirectory(typeDir.toPath());
		}

		Date currentDate = new Date(System.currentTimeMillis());
		File parentDir = new File(typeDir, new SimpleDateFormat("dd.MM.yyyy HH.mm").format(currentDate));
		if (!parentDir.exists()) {
			Files.createDirectory(parentDir.toPath());
		}

		return parentDir;
	}
}
