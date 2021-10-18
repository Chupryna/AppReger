package sample.manager;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import sample.data.model.sms_activate.NumberResponse;
import sample.data.model.sms_activate.SmsActivateAPI;
import sample.utils.Logger;
import sample.utils.Utils;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static sample.utils.Constants.*;


public class SmsApiManager {

	private final SmsActivateAPI smsActivateAPI;
	private final Map<Integer, NumberResponse> numbersStorage;
	private final Logger logger;
	private int receivedNumbersCount = 0;
	private NumberResponse numberResponse;

	public SmsApiManager(Logger logger, String smsService) {
		this.logger = logger;
		numbersStorage = new HashMap<>();
		smsActivateAPI = createApiClient(smsService);
	}

	public int getReceivedNumbersCount() {
		return receivedNumbersCount;
	}

	public void setReceivedNumbersCount(int receivedNumbersCount) {
		this.receivedNumbersCount = receivedNumbersCount;
	}

	public NumberResponse getNumberResponse(int threadNumber) {
		return numbersStorage.get(threadNumber);
	}

	public void putNumberResponse(int threadNumber, NumberResponse numberResponse) {
		numbersStorage.put(threadNumber, numberResponse);
	}

	public SmsActivateAPI createApiClient(String smsService) {
		OkHttpClient client = new OkHttpClient.Builder()
				.readTimeout(60, TimeUnit.SECONDS)
				.writeTimeout(60, TimeUnit.SECONDS)
				.connectTimeout(60, TimeUnit.SECONDS)
				.build();

		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(smsService)
				.addConverterFactory(GsonConverterFactory.create())
				.client(client)
				.build();

		return retrofit.create(SmsActivateAPI.class);
	}

	public void getNewNumber(String apiKey, String country, String operator, int threadNumber) {
		smsActivateAPI.getBalance(apiKey).enqueue(new Callback<ResponseBody>() {
			@Override
			public void onResponse(@Nonnull Call<ResponseBody> call, @Nonnull Response<ResponseBody> response) {
				int responseCode = response.code();
				if (responseCode != 200) {
					logger.info("Error " + responseCode, threadNumber);
				}

				if (response.body() == null) {
					return;
				}

				try {
					String body = response.body().string();
					System.out.println(body);

					if (body.contains(":")) {
						try {
							double balance = Double.parseDouble(body.split(":")[1]);
							logger.info(String.format("Баланс %.0f руб", balance), threadNumber);
						} catch (NumberFormatException e) {
							logger.info("NUMBER FORMAT!!!!", threadNumber);
							e.printStackTrace();
						}
						getCountNumber(apiKey, country, operator, threadNumber, COUNT_TRY_GET_COUNT_NUMBER);
					} else {
						NumberResponse numberResponse = new NumberResponse();
						numberResponse.setError(body);
						numberResponse.setEndWorkWithNumber(true);
						numbersStorage.put(threadNumber, numberResponse);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onFailure(@Nonnull Call<ResponseBody> call, @Nonnull Throwable throwable) {
				throwable.printStackTrace();
			}
		});
	}

	private void getCountNumber(String apiKey, String country, String operator, int threadNumber, int tryCount) {
		smsActivateAPI.getCountNumber(apiKey, country, operator).enqueue(new Callback<ResponseBody>() {
			@Override
			public void onResponse(@Nonnull Call<ResponseBody> call, @Nonnull Response<ResponseBody> response) {
				int responseCode = response.code();
				if (responseCode != 200) {
					logger.info("Error " + responseCode, threadNumber);
				}

				if (response.body() == null) {
					return;
				}

				try {
					int numberCount = 0;
					String body = response.body().string();
					System.out.println(body);

					String[] services = body.split(",");
					for (String service : services) {
						if (service.contains("ig")) {
							numberCount = Integer.parseInt(service.split(":")[1].replaceAll("\\D", ""));
						}
					}

					if (numberCount < 1) {
						if (tryCount > 0) {
							logger.info("Немає доступних номерів. Повторна перевірка", threadNumber);
							Utils.sleep(3000);
							getCountNumber(apiKey, country, operator, threadNumber, tryCount - 1);
							return;
						}

						NumberResponse numberResponse = new NumberResponse();
						numberResponse.setError("Немає доступних номерів");
						numberResponse.setEndWorkWithNumber(true);
						numbersStorage.put(threadNumber, numberResponse);
					} else {
						logger.info(String.format("Доступно %d номерів", numberCount), threadNumber);
						getNumber(apiKey, country, operator, threadNumber);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onFailure(@Nonnull Call<ResponseBody> call, @Nonnull Throwable throwable) {
				NumberResponse numberResponse = new NumberResponse();
				numberResponse.setError("Помилка запиту номера");
				numbersStorage.put(threadNumber, numberResponse);
				logger.printError(throwable);
			}
		});
	}

	private void getNumber(String apiKey, String country, String operator, int threadNumber) {
		smsActivateAPI.getNumber(apiKey, SmsActivateAPI.SERVICE, country, operator).enqueue(new Callback<ResponseBody>() {
			@Override
			public void onResponse(@Nonnull Call<ResponseBody> call, @Nonnull Response<ResponseBody> response) {
				int responseCode = response.code();
				if (responseCode != 200) {
					logger.info("Error " + responseCode, threadNumber);
				}

				if (response.body() == null) {
					return;
				}

				numberResponse = new NumberResponse();
				try {
					String body = response.body().string();
					System.out.println(body);
					if (body.contains("ACCESS_NUMBER")) {
						String[] strings = body.split(":");
						numberResponse.setIdNum(strings[1]);
						numberResponse.setNumber(strings[2]);
						numberResponse.setApiCountryCode(country);
						logger.info("Отримали номер " + numberResponse.getNumber(), threadNumber);
						receivedNumbersCount++;
					} else if (body.contains(SmsActivateAPI.Error.NoMoney.message)) {
						numberResponse.setError("Не достатньо коштів для отримання номеру");
						numberResponse.setEndWorkWithNumber(true);
					} else if (body.contains(SmsActivateAPI.Error.NoNumber.message)) {
						logger.info("Не вдалось отримати номер. Повторна спроба", threadNumber);
						numberResponse = null;
						Utils.sleep(10000);
						getNumber(apiKey, country, operator, threadNumber);
					} else {
						numberResponse.setEndWorkWithNumber(true);
					}
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					numbersStorage.put(threadNumber, numberResponse);
				}
			}

			@Override
			public void onFailure(@Nonnull Call<ResponseBody> call, @Nonnull Throwable throwable) {
				logger.info("Помилка отримання номеру. Повторна спроба", threadNumber);
				numberResponse = null;
				getNumber(apiKey, country, operator, threadNumber);
				logger.printError(throwable);
			}
		});
	}

	public void getStatus(String apiKey, NumberResponse numberResponse, WebDriver driver, boolean[] isCodeReceived, int threadNumber) {
		smsActivateAPI.getStatus(apiKey, numberResponse.getIdNum()).enqueue(new Callback<ResponseBody>() {
			@Override
			public void onResponse(@Nonnull Call<ResponseBody> call, @Nonnull Response<ResponseBody> response) {
				int responseCode = response.code();
				if (responseCode != 200) {
					logger.info("Error " + responseCode, threadNumber);
				}

				if (response.body() == null) {
					return;
				}

				try {
					String body = response.body().string();
					System.out.println(body);

					if (body.contains("STATUS_OK")) {
						String code = body.split(":")[1];
						logger.info("Код отримано: " + code, threadNumber);
						if (numberResponse.getReceivedCodes().contains(code)) {
							setStatus(apiKey, numberResponse, SmsActivateAPI.Status.GET_NEW_CODE, SmsActivateAPI.StatusResponse.ACCESS_RETRY_GET,
									"Повтор коду. Чекаємо новий код на номер " + numberResponse.getNumber(), threadNumber);
							return;
						}

						driver.findElement(By.id(CODE_FIELD_ID)).sendKeys(code);
						isCodeReceived[0] = true;
						numberResponse.addReceiverCode(code);
					} else if (body.equals(SmsActivateAPI.StatusResponse.NO_ACTIVATION.name()) ||
							body.equals(SmsActivateAPI.StatusResponse.STATUS_CANCEL.name())) {
						logger.info("Термін дії номеру минув\n", threadNumber);
						numberResponse.setEndWorkWithNumber(true);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onFailure(@Nonnull Call<ResponseBody> call, @Nonnull Throwable throwable) {
				throwable.printStackTrace();
			}
		});
	}

	public void setStatus(String apiKey, NumberResponse numberResponse, SmsActivateAPI.Status status,
						   SmsActivateAPI.StatusResponse statusResponse, String msg, int threadNumber) {
		smsActivateAPI.setStatus(apiKey, status.code, numberResponse.getIdNum()).enqueue(new Callback<ResponseBody>() {
			@Override
			public void onResponse(@Nonnull Call<ResponseBody> call, @Nonnull Response<ResponseBody> response) {
				int responseCode = response.code();
				if (responseCode != 200) {
					logger.info("Error " + responseCode, threadNumber);
				}

				if (response.body() == null) {
					return;
				}

				try {
					String body = response.body().string();
					System.out.println(body);
					if (body.equals(statusResponse.name())) {
						logger.info(msg, threadNumber);
					} else if (body.equals(SmsActivateAPI.StatusResponse.NO_ACTIVATION.name()) ||
							body.equals(SmsActivateAPI.StatusResponse.STATUS_CANCEL.name()) ||
							body.equals(SmsActivateAPI.StatusResponse.BAD_STATUS.name())) {
						logger.info("Термін дії номеру минув\n", threadNumber);
						numberResponse.setEndWorkWithNumber(true);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onFailure(@Nonnull Call<ResponseBody> call, @Nonnull Throwable throwable) {
				throwable.printStackTrace();
			}
		});
	}
}
