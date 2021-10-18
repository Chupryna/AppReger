package sample.data.model;

import java.util.List;

public class Country {

	private String APICode;
	private String name;
	private String countryCode;
	private List<String> operators;

	public Country() {
	}

	public Country(String APICode, String name, String countryCode, List<String> operators) {
		this.APICode = APICode;
		this.name = name;
		this.countryCode = countryCode;
		this.operators = operators;
	}

	public String getAPICode() {
		return APICode;
	}

	public void setAPICode(String APICode) {
		this.APICode = APICode;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public List<String> getOperators() {
		return operators;
	}

	public void setOperators(List<String> operators) {
		this.operators = operators;
	}

	public boolean isValid() {
		return !APICode.isEmpty() && !name.isEmpty(); //&& !countryCode.isEmpty();
	}

	public static Country EMPTY_COUNTRY() {
		return new Country();
	}
}
