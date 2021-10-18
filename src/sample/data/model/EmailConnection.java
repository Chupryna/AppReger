package sample.data.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class EmailConnection {
	@SerializedName("Domains")
	@Expose
	private List<String> domains = null;
	@SerializedName("Type")
	@Expose
	private String type;
	@SerializedName("Hostname")
	@Expose
	private String hostname;
	@SerializedName("Port")
	@Expose
	private Integer port;
	@SerializedName("SocketType")
	@Expose
	private String socketType;
	@SerializedName("UserName")
	@Expose
	private String userName;

	private String reservedHostname;

	public List<String> getDomains() {
		return domains;
	}

	public void setDomains(List<String> domains) {
		this.domains = domains;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getSocketType() {
		return socketType;
	}

	public void setSocketType(String socketType) {
		this.socketType = socketType;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getReservedHostname() {
		return reservedHostname;
	}

	public void setReservedHostname(String reservedHostname) {
		this.reservedHostname = reservedHostname;
	}

	public void useReservedHostname() {
		hostname = reservedHostname;
	}

	@Override
	public String toString() {
		return "EmailConnection{" +
				"domains=" + domains +
				", type='" + type + '\'' +
				", hostname='" + hostname + '\'' +
				", port=" + port +
				", socketType='" + socketType + '\'' +
				", userName='" + userName + '\'' +
				'}';
	}
}