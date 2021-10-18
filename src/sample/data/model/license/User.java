package sample.data.model.license;

import com.google.gson.annotations.SerializedName;

public class User {

	@SerializedName("login")
	private String login;
	@SerializedName("password")
	private String password;
	@SerializedName("hardwareId")
	private String hardwareId;

	public User(String login, String password, String hardwareId) {
		this.login = login;
		this.password = password;
		this.hardwareId = hardwareId;
	}
}
