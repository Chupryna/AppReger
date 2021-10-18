package sample.data.model;

public class Proxy {

	public enum Type {
		HTTP, SOCKS
	}

	private String host;
	private String port;
	private String login;
	private String password;
	private Type type;
	private boolean isAvailable;
	private int connectionCount = 3;

	public Proxy() {
	}

	public Proxy(String host, String port, String login, String password, Type type) {
		this.host = host;
		this.port = port;
		this.login = login;
		this.password = password;
		this.type = type;
		isAvailable = false;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public boolean isAvailable() {
		return isAvailable;
	}

	public void setAvailable(boolean available) {
		if (isAvailable) {
			return;
		}

		if (available) {
			isAvailable = true;
		} else {
			connectionCount--;
		}

		if (connectionCount == 0) {
			isAvailable = false;
		}
	}

	public boolean isValid() {
		return !host.isEmpty() && !port.isEmpty() && type != null;
	}

	public boolean isAuthorized() {
		return !login.isEmpty() && !password.isEmpty();
	}

	@Override
	public String toString() {
		return String.format("%s:%s:%s:%s", host, port, login, password);
	}

	public String getInfo() {
		return String.format("{%s:%s - %s}", host, port, type.toString());
	}

	public String getAddress() {
		return String.format("%s:%s", host, port);
	}
}
