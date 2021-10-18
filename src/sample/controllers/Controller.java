package sample.controllers;

import io.appium.java_client.MobileElement;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.remote.MobileCapabilityType;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.*;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import sample.data.API;
import sample.data.DataGenerator;
import sample.data.DataSource;
import sample.data.model.Account;
import sample.data.model.Country;
import sample.data.model.Month;
import sample.data.model.Proxy;
import sample.data.model.sms_activate.NumberResponse;
import sample.data.model.sms_activate.SmsActivateAPI;
import sample.data.model.sms_activate.SmsService;
import sample.manager.CountryManager;
import sample.manager.InstagramManager;
import sample.manager.SmsApiManager;
import sample.utils.Constants;
import sample.utils.Logger;
import sample.utils.Utils;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static sample.utils.Constants.*;


public class Controller {

	public TextArea log;
	public Label allLabel;
	public Button driverBtn;
	public Button startBtn;
	public Button nextBtn;
	public Label badLabel;
	public Label goodLabel;
	public CheckBox changeEmailCb;
	public Label numberUALabel;
	public Button proxiesBtn;
	public CheckBox stopCb;
	public Label numberProxyLabel;
	public Label numberEmailsLabel;
	public TextField apiKeyEt;
	public TextField countryEt;
	public TextField threadsCountEt;
	public TextField operatorEt;
	public ComboBox<SmsService> smsServiceComboBox;
	public CheckBox makeProgrevCb;
	public Label numberFollowersLabel;
	public TextField smsCountOnNumberEt;
	public Button stopBtn;
	public TextField captchaCountOnNumberEt;
	public Label accountsOnNumberLabel;
	public TextField registrationCountEt;
	public CheckBox useRotationCb;
	public TextField rotationLinkEt;
	public TextField rotationTimeoutEt;
	public Label numberBioLabel;
	public CheckBox addBio;
	public ComboBox<String> namesComboBox;
	public CheckBox bindProxy;
	public TextField timeoutForChangingNumberEt;

	private InstagramManager instagramManager;
	private final DataSource dataSource = new DataSource();
	private final Random random = new Random();
	private ExecutorService executorService;
	private List<WebDriver> drivers;
	private File proxyPath;
	private File uaPath;
	private Logger logger;
	private int badCount = 0;
	private int goodCount = 0;
	private int allCount = 0;
	int baseSize = 0;
	private SmsApiManager smsApiManager;

	public void initialize() {
		nextBtn.setDisable(true);
		stopBtn.setDisable(true);
		logger = new Logger(log);
		instagramManager = new InstagramManager(logger);

		List<SmsService> smsServices = new ArrayList<>();
		smsServices.add(new SmsService("vak-sms.com", "https://vak-sms.com/", "c82385d8e9a74b0e9ef2936d36e7fb32"));
		smsServices.add(new SmsService("smshub.org", "https://smshub.org/", "57663Ud1fd3c7f5f48f47e50765f0bb4665f8e"));
		smsServices.add(new SmsService("sms-activate.ru", "https://sms-activate.ru/", "728d0de194977459ce9dc8673A702b9A"));
		smsServices.add(new SmsService("sms-online.pro", "https://sms-online.pro/", ""));
		smsServices.add(new SmsService("cheapsms.pro", "http://cheapsms.pro/", "c3503cd39c3a3c6ba4e0897a937a03da"));
		smsServices.add(new SmsService("5sim.net", "http://api1.5sim.net/", "a9f701bac3b14343a5b48bbc2d1bc545"));
		smsServiceComboBox.setItems(FXCollections.observableList(smsServices));

		List<String> names = new ArrayList<>();
		names.add("Змішані");
		names.add("Ру");
		names.add("Араби");
		names.add("Корейці");
		namesComboBox.setItems(FXCollections.observableList(names));

		loadSettings();
	}

	@FXML
	public void start() {
		if (countryEt.getText().isEmpty()) {
			showErrorDialog("Введіть код країни");
			return;
		}

		int threadsCount;
		try {
			threadsCount = Integer.parseInt(threadsCountEt.getText());
		} catch (NumberFormatException e) {
			showErrorDialog("Введіть кількість потоків");
			return;
		}

//		apiKeyEt.setText(smsServiceComboBox.getSelectionModel().getSelectedItem().getApiKey());

		smsApiManager = new SmsApiManager(logger, smsServiceComboBox.getSelectionModel().getSelectedItem().getLink());
		badCount = 0;
		goodCount = 0;
		allCount = 0;
		logger.init(Logger.UtilType.WEB_REGER);
		disableButtons(true);
		updateCounts();
		baseSize = dataSource.getBaseSize();
		drivers = new ArrayList<>();

		executorService = Executors.newFixedThreadPool(threadsCount);
		for (int i = 0; i < threadsCount; i++) {
			executorService.submit(this::regAccount);
		}

		createSettings();
	}

	private void regAccount() {
		int threadNumber = Utils.getThreadNumber(Thread.currentThread());
		String apiKey = apiKeyEt.getText();
		String countryCode = countryEt.getText();
		String operator = operatorEt.getText().isEmpty() ? "any" : operatorEt.getText();

		Account account = new Account();
		try {
			URL serverUrl = new URL(Constants.SERVER_URL);
			DesiredCapabilities cap = new DesiredCapabilities();

			cap.setCapability(MobileCapabilityType.PLATFORM_NAME, "Android");
			cap.setCapability(MobileCapabilityType.PLATFORM_VERSION, "7");
			cap.setCapability(MobileCapabilityType.APP, "D:\\Projects\\App Reger\\inst 182.apk");
//			cap.setCapability(MobileCapabilityType.DEVICE_NAME, "emulator-5554");
			cap.setCapability("DeviceIPAddress", "127.0.0.1:62001");
			cap.setCapability("appPackage", "com.instagram.android");
			cap.setCapability("appActivity", ".activity.MainTabActivity");
			cap.setCapability(MobileCapabilityType.FULL_RESET, "true");
			cap.setCapability(MobileCapabilityType.NEW_COMMAND_TIMEOUT, "0");

			logger.info("Запуск драйвера");
			AndroidDriver<MobileElement> driver = new AndroidDriver<>(serverUrl, cap);
			WebDriverWait driverWait = new WebDriverWait(driver, 30);
			driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
			drivers.add(driver);

			logger.info("Відкрито застосунок Instagram");
			driver.findElement(By.id(OPEN_REG_BTN)).click();
			logger.info("Відкрито екран реєстрації");

			NumberResponse numberResponse = smsApiManager.getNumberResponse(threadNumber);
			if (numberResponse == null || numberResponse.isEndWorkWithNumber()) {
				smsApiManager.putNumberResponse(threadNumber, null);
				smsApiManager.getNewNumber(apiKey, countryCode, operator, threadNumber);
			} else {
				smsApiManager.setStatus(apiKey, numberResponse, SmsActivateAPI.Status.GET_NEW_CODE, SmsActivateAPI.StatusResponse.ACCESS_RETRY_GET,
						"Номер " + numberResponse.getNumber() + " очікує нове повідомлення", threadNumber);
			}

			int timer = 0;
			do {
				Utils.sleep(5000);
				timer++;
			}
			while (smsApiManager.getNumberResponse(threadNumber) == null && timer < 30);
			numberResponse = smsApiManager.getNumberResponse(threadNumber);

			if (numberResponse == null) {
				logger.info("Number response is Null");
				nextRegistration(driver);
				return;
			}

			if (!numberResponse.isSuccessfully()) {
				logger.info(numberResponse.getError());
				driver.quit();
				return;
			}

			if (numberResponse.isEndWorkWithNumber()) {
				nextRegistration(driver);
				return;
			}

			driver.findElement(By.id(COUNTRY_DIALOG_BTN)).click();
			Country country = CountryManager.getInstance().getCountryByApiCode(numberResponse.getApiCountryCode());
			driver.findElement(By.id(COUNTRY_FIELD_ID)).sendKeys(country.getName());
			driver.findElement(By.id(COUNTRY_LIST_ID)).findElements(By.className(COUNTRY_LIST_ITEM_CLASS)).get(0).click();
			String number = numberResponse.getNumberWithoutCountryCode(country.getCountryCode());

			driver.findElement(By.id(PHONE_FIELD_ID)).sendKeys(number);
			driver.findElement(By.id(SEND_CODE_BTN_ID)).click();
			try {
				driverWait.until(ExpectedConditions.visibilityOfElementLocated((By.id(CODE_FIELD_ID))));
				logger.info("Код відправлено");
			} catch (TimeoutException e) {
				logger.info("Не вдалось відправити код\n");
				nextRegistration(driver);
				return;
			}

			final boolean[] isCodeReceived = {false};
			//Очікування коду
			for (int i = 1; i < TIME_FOR_WAIT_SMS; i++) {
				smsApiManager.getStatus(apiKey, numberResponse, driver, isCodeReceived, threadNumber);
				Utils.sleep(5000);

				if (isCodeReceived[0]) {
					driver.findElement(By.id(NEXT_BTN_ID)).click();
					break;
				}

				if (numberResponse.isEndWorkWithNumber()) {
					nextRegistration(driver);
					return;
				}

//				try {
//					if (i % (TIME_FOR_WAIT_SMS / 3) == 0) {
//						driver.findElement(By.cssSelector(SEND_CODE_AGAIN_BTN)).click();
//						try {
//							driverWait.until(webDriver -> webDriver.findElements(By.cssSelector(RESEND_CODE_POPUP)));
//							logger.info("Відправили код знову");
//						} catch (TimeoutException e) {
//							driver.findElement(By.cssSelector(SEND_CODE_AGAIN_BTN)).click();
//						}
//					}
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
			}

			if (!isCodeReceived[0]) {
				logger.info("Код не отримано");
				smsApiManager.setStatus(apiKey, numberResponse, SmsActivateAPI.Status.NUMBER_IS_BANNED, SmsActivateAPI.StatusResponse.ACCESS_CANCEL, "Номер скасовано\n", threadNumber);
				numberResponse.setEndWorkWithNumber(true);
				if (numberResponse.getReceivedCodes().isEmpty()) {
					smsApiManager.setReceivedNumbersCount(smsApiManager.getReceivedNumbersCount() - 1);
				}

				nextRegistration(driver);
				return;
			}

			setNameAndPassword(account, driver);
			setDateOfBirth(driver);
			setUsername(account, driver);

			try {
				driverWait.until(ExpectedConditions.visibilityOfElementLocated(By.id(SKIP_BTN_ID))).click();
				logger.info("Аккаунт успішно зареєстровано " + (numberResponse.getCountOfUsage() + 1));
			} catch (Exception e) {
				logger.printError(e);
				e.printStackTrace();
				writeBadResult(account, driver, smsApiManager.getNumberResponse(Utils.getThreadNumber(Thread.currentThread())));
				return;
			}

			//Прибрати діалог
			try {
				driver.findElement(By.id(SKIP_DIALOG_BTN_ID)).click();
				Utils.sleep(1000);
			} catch (NoSuchElementException ignored) {
			}

			//Пропустити аватар
			try {
				driver.findElement(By.id(SKIP_BTN_ID)).click();
				Utils.sleep(1000);
			} catch (Exception e) {
				e.printStackTrace();
			}

			//Пропустити рекомендовані
			try {
				driver.findElement(By.id(ACTION_BAR_BTN_ID)).click();
			} catch (NoSuchElementException ignored) {
			}

			if (addBio.isSelected()) {
				setBio(driver, driverWait);
			}

			numberResponse.addCountOfUsage();
			String smsCountOnNumber = smsCountOnNumberEt.getText();
			if (!smsCountOnNumber.isEmpty()) {
				if (numberResponse.getCountOfUsage() >= Integer.parseInt(smsCountOnNumber)) {
					numberResponse.setEndWorkWithNumber(true);
				}
			}
			writeGoodResult(account, driver);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private void setNameAndPassword(Account account, AndroidDriver<MobileElement> driver) {
		String fullName;
		int index = namesComboBox.getSelectionModel().getSelectedIndex();
		switch (index) {
			case 1:
				fullName = DataGenerator.getRuFullName();
				break;
			case 2:
				fullName = DataGenerator.getArabName();
				break;
			case 3:
				fullName = DataGenerator.getKoreaName();
				break;
			default:
				fullName = DataGenerator.getFullName();
		}
		account.setPassword(DataGenerator.getPassword());
		account.setFullName(fullName);

		driver.findElement(By.id(FULL_NAME_FIELD_ID)).sendKeys(account.getFullName());
		logger.info("Встановлено ім'я: " + account.getFullName());
		Utils.sleep(1000);

		driver.findElement(By.id(PASSWORD_FIELD_ID)).sendKeys(account.getPassword());
		logger.info("Встановлено пароль: " + account.getPassword());
		Utils.sleep(1000);

		driver.findElement(By.id(GO_TO_BIRTHDAY_BTN_ID)).click();
		Utils.sleep(1000);
	}

	private void setDateOfBirth(AndroidDriver<MobileElement> driver) {
		MobileElement dap = driver.findElement(By.id(DATE_PICKERS_ID));
		List<MobileElement> datePickers = dap.findElements(By.className(DATE_PICKER_CLASS));

		int day = 1 + random.nextInt(28);
		MobileElement dayField = datePickers.get(0).findElement(By.id(DATE_PICKER_FIELD));
		dayField.click();
		dayField.clear();
		dayField.sendKeys(String.valueOf(day));
		Utils.sleep(1000);

		int month = 1 + random.nextInt(11);
		MobileElement monthField = datePickers.get(1).findElement(By.id(DATE_PICKER_FIELD));
		monthField.click();
		monthField.clear();
		monthField.sendKeys(Month.getMonthByNumber(month));
		Utils.sleep(1000);

		int year = 1970 + random.nextInt(30);
		MobileElement yearField = datePickers.get(2).findElement(By.id(DATE_PICKER_FIELD));
		yearField.click();
		yearField.clear();
		yearField.sendKeys(String.valueOf(year));
		Utils.sleep(1000);
		yearField.click();

		logger.info(String.format("Встановлено дату народження: %02d.%02d.%d", day, month, year));
		driver.findElement(By.id(NEXT_BTN_ID)).click();
	}

	private void setUsername(Account account, AndroidDriver<MobileElement> driver) {
		try {
			driver.findElement(By.id(CHANGE_USERNAME_ID)).click();
		} catch (Exception e) {
			String username = DataGenerator.getUsername();
			driver.findElement(By.id(USERNAME_FIELD)).sendKeys(username);
			account.setUserName(username);
		}

		if (account.getUserName().isEmpty()) {
			MobileElement usernameField = driver.findElement(By.id(USERNAME_FIELD));
			int index = namesComboBox.getSelectionModel().getSelectedIndex();

			if (index > 0) {
				String username = usernameField.getText();
				account.setUserName(username);
			} else {
				String date = String.format("%02d%02d", Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
						Calendar.getInstance().get(Calendar.MONTH) + 1);
				account.setUserName(usernameField.getText().replaceAll("\\d", "") + "_" + date);
				usernameField.clear();
				usernameField.sendKeys(account.getUserName());
			}
		}
		logger.info("Встановлено username: " + account.getUserName());

		Utils.sleep(1000);
		driver.findElement(By.id(NEXT_BTN_ID)).click();
	}

	private void setBio(AndroidDriver<MobileElement> driver, WebDriverWait longDriverWait) {
		logger.info("Встановлюємо біо");
	}

	private void updateCounts() {
		double goodPercent = allCount == 0 ? 0 : (double) goodCount / allCount * 100;
		double badPercent = allCount == 0 ? 0 : 100.0 - goodPercent;
		String goodText = String.format("%d (%.1f%%)", goodCount, goodPercent);
		String badText = String.format("%d (%.1f%%)", badCount, badPercent);

		badLabel.setText(badText);
		goodLabel.setText(goodText);
		allLabel.setText(String.valueOf(allCount));
		accountsOnNumberLabel.setText(String.format("%.1f", goodCount == 0 ? 0 : (double) goodCount / smsApiManager.getReceivedNumbersCount()));

		String registrationCount = registrationCountEt.getText();
		if (!registrationCount.isEmpty() && allCount >= Integer.parseInt(registrationCount)) {
			stop();
		}
	}

	private void showStopDialog() {
		logger.closeStreams();
		disableButtons(false);
		for (WebDriver driver : drivers) {
			try {
				driver.quit();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		Alert message = new Alert(Alert.AlertType.WARNING);
		message.setTitle("Результат");
		message.setHeaderText("Робота зупинена");
		message.show();
	}

	private void showErrorDialog(String msg) {
		Alert message = new Alert(Alert.AlertType.ERROR);
		message.setTitle("Помилка");
		message.setHeaderText(msg);
		message.show();
	}

	private void writeGoodResult(Account account, AndroidDriver<MobileElement> driver) {
//		account.addCookies(driver.manage().getCookies());

		logger.info("Аккаунт " + account.getUserName() + " успішно зареєстровано\n");
		logger.printGood(account);
//		logger.printGoodWithProxy(account);
//		logger.printGoodWithCookie(account);
		logger.printLogin(account);
		goodCount++;
		allCount++;
		Platform.runLater(this::updateCounts);

		nextRegistration(driver);
	}

	private void writeBadResult(Account account, AndroidDriver<MobileElement> driver, NumberResponse numberResponse) {
		logger.info("Аккаунт злетів на капчу\n");
		logger.printBad(account);
//		logger.printProxy(account.getProxy());
		badCount++;
		allCount++;
		numberResponse.addBadRegistration(Integer.parseInt(captchaCountOnNumberEt.getText()));
		Platform.runLater(this::updateCounts);

		nextRegistration(driver);
	}

	private void nextRegistration(AndroidDriver<MobileElement> driver) {
		driver.quit();
		drivers.remove(driver);

		if (useRotationCb.isSelected()) {
			rotateIP(rotationLinkEt.getText(), Utils.getThreadNumber(Thread.currentThread()));
		}

		while (stopCb.isSelected()) {
			Utils.sleep(10000);
		}
		executorService.submit(this::regAccount);
	}

	private void rotateIP(String link, int threadNumber) {
		logger.info("Виконуємо ротацію IP", threadNumber);

		String baseUrl;
		Map<String, String> queryParamsMap = new HashMap<>();

		if (link.contains("&")) {
			String[] splitLink = link.split("\\?");
			baseUrl = splitLink[0] + "/";
			String[] params = splitLink[1].split("&");

			for (String param : params) {
				String[] p = param.split("=");
				queryParamsMap.put(p[0], p[1]);
			}
		} else {
			baseUrl = link + "/";
		}

		OkHttpClient client = new OkHttpClient.Builder()
				.readTimeout(60, TimeUnit.SECONDS)
				.writeTimeout(60, TimeUnit.SECONDS)
				.connectTimeout(60, TimeUnit.SECONDS)
				.build();

		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(baseUrl)
				.client(client)
				.build();

		API api = retrofit.create(API.class);
		api.rotateIP("", queryParamsMap).enqueue(new Callback<ResponseBody>() {
			@Override
			public void onResponse(@Nonnull Call<ResponseBody> call, @Nonnull Response<ResponseBody> response) {
				if (response.body() != null) {
					try {
						String body = response.body().string();
						logger.info(body, threadNumber);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			@Override
			public void onFailure(@Nonnull Call<ResponseBody> call, @Nonnull Throwable throwable) {
				logger.info("Rotate failed", threadNumber);
				throwable.printStackTrace();
			}
		});
		Utils.sleep(Integer.parseInt(rotationTimeoutEt.getText()) * 1000);
	}

	private void disableButtons(boolean disable) {
		startBtn.setDisable(disable);
		nextBtn.setDisable(!disable);
		stopBtn.setDisable(!disable);
		if (disable) {
			log.setText("");
		}
	}

	@FXML
	private void stop() {
		executorService.shutdownNow();
		showStopDialog();
	}

	@FXML
	private void nextAction() {
		executorService.submit(this::regAccount);
	}

	@FXML
	private void loadProxies(ActionEvent actionEvent) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Завантажити proxy...");
		fileChooser.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("TXT(*.txt)", "*.txt"),
				new FileChooser.ExtensionFilter("Все файли (*.*)", "*.*"));
		File proxies = fileChooser.showOpenDialog(((Node) actionEvent.getSource()).getScene().getWindow());

		if (proxies != null) {
			proxyPath = proxies;
			dataSource.loadProxies(proxies, Proxy.Type.HTTP);
			numberProxyLabel.setText(String.valueOf(dataSource.getProxiesListSize()));
		}
	}

	@FXML
	private void loadUserAgents(ActionEvent actionEvent) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Завантажити UA...");
		fileChooser.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("TXT(*.txt)", "*.txt"),
				new FileChooser.ExtensionFilter("Все файли (*.*)", "*.*"));
		File userAgents = fileChooser.showOpenDialog(((Node) actionEvent.getSource()).getScene().getWindow());

		if (userAgents != null) {
			uaPath = userAgents;
			dataSource.loadUserAgents(userAgents);
			numberUALabel.setText(String.valueOf(dataSource.getUserAgentsSize()));
		}
	}

	@FXML
	private void loadEmails(ActionEvent actionEvent) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Завантажити пошти...");
		fileChooser.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("TXT(*.txt)", "*.txt"),
				new FileChooser.ExtensionFilter("Все файли (*.*)", "*.*"));
		File emails = fileChooser.showOpenDialog(((Node) actionEvent.getSource()).getScene().getWindow());

		if (emails != null) {
			dataSource.loadEmails(emails);
			numberEmailsLabel.setText(String.valueOf(dataSource.getEmailsSize()));
		}
	}

	@FXML
	private void loadFollowers(ActionEvent actionEvent) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Завантажити...");
		fileChooser.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("TXT(*.txt)", "*.txt"),
				new FileChooser.ExtensionFilter("Все файли (*.*)", "*.*"));
		File followers = fileChooser.showOpenDialog(((Node) actionEvent.getSource()).getScene().getWindow());

		if (followers != null) {
			dataSource.loadFollowers(followers);
			numberFollowersLabel.setText(String.valueOf(dataSource.getFollowersSize()));
		}
	}

	@FXML
	private void loadBio(ActionEvent actionEvent) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Завантажити...");
		fileChooser.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("TXT(*.txt)", "*.txt"),
				new FileChooser.ExtensionFilter("Все файли (*.*)", "*.*"));
		File bio = fileChooser.showOpenDialog(((Node) actionEvent.getSource()).getScene().getWindow());

		if (bio != null) {
			dataSource.loadBio(bio);
			numberBioLabel.setText(String.valueOf(dataSource.getBioSize()));
		}
	}

	private void createSettings() {
		Map<String, String> settings = new HashMap<>();
		if (proxyPath != null) {
			settings.put("proxy", proxyPath.getAbsolutePath());
		}

		if (uaPath != null) {
			settings.put("ua", uaPath.getAbsolutePath());
		}

		settings.put("threads", threadsCountEt.getText());
		settings.put("country", countryEt.getText());
		settings.put("sms", smsCountOnNumberEt.getText());
		settings.put("captcha", captchaCountOnNumberEt.getText());
		settings.put("name", String.valueOf(namesComboBox.getSelectionModel().getSelectedIndex()));
		settings.put("apiKey", apiKeyEt.getText());
		settings.put("smsService", String.valueOf(smsServiceComboBox.getSelectionModel().getSelectedIndex()));

		logger.saveSettings(settings);
	}

	private void loadSettings() {
		Map<String, String> settings = dataSource.loadSettings();
		String proxy = settings.get("proxy");
		String ua = settings.get("ua");
		String threads = settings.get("threads");
		String country = settings.get("country");
		String sms = settings.get("sms");
		String captcha = settings.get("captcha");
		String name = settings.get("name");
		String apiKey = settings.get("apiKey");
		String smsService = settings.get("smsService");

		if (proxy != null) {
			proxyPath = new File(proxy);
			dataSource.loadProxies(proxyPath, Proxy.Type.HTTP);
			numberProxyLabel.setText(String.valueOf(dataSource.getProxiesListSize()));
		}

		if (ua != null) {
			uaPath = new File(ua);
			dataSource.loadUserAgents(uaPath);
			numberUALabel.setText(String.valueOf(dataSource.getUserAgentsSize()));
		}

		if (threads != null) {
			threadsCountEt.setText(threads);
		}

		if (country != null) {
			countryEt.setText(country);
		}

		if (sms != null) {
			smsCountOnNumberEt.setText(sms);
		}

		if (captcha != null) {
			captchaCountOnNumberEt.setText(captcha);
		}

		if (name != null) {
			try {
				int index = Integer.parseInt(name);
				namesComboBox.getSelectionModel().select(index);
			} catch (NumberFormatException ignored) {
			}

		}

		if (apiKey != null) {
			apiKeyEt.setText(apiKey);
		}

		if (smsService != null) {
			try {
				int index = Integer.parseInt(smsService);
				smsServiceComboBox.getSelectionModel().select(index);
			} catch (NumberFormatException ignored) {
			}
		}
	}
}
