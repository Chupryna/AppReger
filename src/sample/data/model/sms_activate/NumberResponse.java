package sample.data.model.sms_activate;

import java.util.ArrayList;
import java.util.List;

public class NumberResponse {

	private String number;
	private String idNum;
	private String error;
	private String apiCountryCode;
	private int countOfUsage = 0;
	private int badRegistrationCount = 0;
	private boolean endWorkWithNumber = false;
	private final List<String> receivedCodes = new ArrayList<>();

	public NumberResponse() {
	}

	public NumberResponse(String error) {
		this.error = error;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getIdNum() {
		return idNum;
	}

	public void setIdNum(String idNum) {
		this.idNum = idNum;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public boolean isSuccessfully() {
		return number != null && idNum != null;
	}

	public int getCountOfUsage() {
		return countOfUsage;
	}

	public void addCountOfUsage() {
		countOfUsage++;
	}

	public void addBadRegistration(int captchaCount) {
		badRegistrationCount++;
		if (badRegistrationCount < captchaCount) {
			return;
		}

		endWorkWithNumber = true;
	}

	public void setEndWorkWithNumber(boolean endWorkWithNumber) {
		this.endWorkWithNumber = endWorkWithNumber;
	}

	public boolean isEndWorkWithNumber() {
		return endWorkWithNumber;
	}

	public String getNumberWithoutCountryCode(String countryCode) {
		return number.replaceFirst(countryCode, "");
	}

	public String getApiCountryCode() {
		return apiCountryCode;
	}

	public void setApiCountryCode(String apiCountryCode) {
		this.apiCountryCode = apiCountryCode;
	}

	public List<String> getReceivedCodes() {
		return receivedCodes;
	}

	public void addReceiverCode(String code) {
		receivedCodes.add(code);
	}

	@Override
	public String toString() {
		return "NumberResponse{" +
				"tel=" + number +
				", idNum='" + idNum + '\'' +
				", error='" + error + '\'' +
				'}';
	}
}
