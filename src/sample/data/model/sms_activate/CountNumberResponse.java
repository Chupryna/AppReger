package sample.data.model.sms_activate;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CountNumberResponse {
	@SerializedName("ig")
	@Expose
	private Integer ig;
	@SerializedName("price")
	@Expose
	private Double price;
	@SerializedName("error")
	@Expose
	private String error;

	public Integer getIg() {
		return ig;
	}

	public void setIg(Integer ig) {
		this.ig = ig;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	@Override
	public String toString() {
		return "CountNumberResponse{" +
				"ig=" + ig +
				", price=" + price +
				", error='" + error + '\'' +
				'}';
	}
}
