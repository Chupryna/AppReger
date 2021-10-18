package sample.data.model.sms_activate;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class BalanceResponse {

	@SerializedName("balance")
	@Expose
	private Double balance;
	@SerializedName("error")
	@Expose
	private String error;

	public Double getBalance() {
		return balance;
	}

	public void setBalance(Double balance) {
		this.balance = balance;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	@Override
	public String toString() {
		return "BalanceResponse{" +
				"balance=" + balance +
				", error='" + error + '\'' +
				'}';
	}
}