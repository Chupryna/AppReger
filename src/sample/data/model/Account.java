package sample.data.model;

import com.google.gson.JsonObject;
import org.openqa.selenium.Cookie;
import sample.utils.MailConnectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Account {
	private EmailConnection emailConnection;
	private String userName = "";
	private String password = "";
	private String newEmail = "";
	private String newEmailPassword = "";
	private String oldEmail = "";
	private String oldEmailPassword = "";
	private String oldUserName = "";
	private String registrationDate = "";
	private String fullName = "";
	private int posts = 0;
	private int followers = 0;
	private int following = 0;
	private int followError = 0;
	private boolean canBeBlocked = true;
	private boolean blocked = false;
	private final MailConnectionUtils connectionUtils = MailConnectionUtils.getInstance();
	private final List<Cookie> cookies = new ArrayList<>();
	private Proxy proxy;
	private String userAgent;

	public Account() {
	}

	public Account(String username, String password, String newEmail, String newEmailPassword, String email, String emailPassword, EmailConnection emailConnection) {
		this.userName = username;
		this.password = password;
		this.newEmail = newEmail;
		this.newEmailPassword = newEmailPassword;
		this.oldEmail = email;
		this.oldEmailPassword = emailPassword;
		this.emailConnection = emailConnection;
	}

	public EmailConnection getEmailConnection() {
		return emailConnection;
	}

	public void setEmailConnection(EmailConnection emailConnection) {
		this.emailConnection = emailConnection;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getNewEmail() {
		return newEmail;
	}

	public void setNewEmail(String newEmail) {
		this.newEmail = newEmail;
	}

	public String getNewEmailPassword() {
		return newEmailPassword;
	}

	public void setNewEmailPassword(String newEmailPassword) {
		this.newEmailPassword = newEmailPassword;
	}

	public String getOldEmail() {
		return oldEmail;
	}

	public void setOldEmail(String oldEmail) {
		this.oldEmail = oldEmail;
	}

	public String getOldEmailPassword() {
		return oldEmailPassword;
	}

	public void setOldEmailPassword(String oldEmailPassword) {
		this.oldEmailPassword = oldEmailPassword;
	}

	public String getOldUserName() {
		return oldUserName;
	}

	public void setOldUserName(String oldUserName) {
		this.oldUserName = oldUserName;
	}

	public String getRegistrationDate() {
		return registrationDate;
	}

	public void setRegistrationDate(String registrationDate) {
		this.registrationDate = registrationDate;
	}

	public int getPosts() {
		return posts;
	}

	public void setPosts(int posts) {
		this.posts = posts;
	}

	public int getFollowers() {
		return followers;
	}

	public void setFollowers(int followers) {
		this.followers = followers;
	}

	public boolean isCanBeBlocked() {
		return canBeBlocked && !userName.isEmpty() && !password.isEmpty();
	}

	public void setCanBeBlocked(boolean canBeBlocked) {
		this.canBeBlocked = canBeBlocked;
	}

	public boolean isBlocked() {
		return blocked;
	}

	public void setBlocked(boolean blocked) {
		this.blocked = blocked;
	}

	public int getFollowing() {
		return following;
	}

	public int getFollowError() {
		return followError;
	}

	public List<Cookie> getCookies() {
		return cookies;
	}

	public Proxy getProxy() {
		return proxy;
	}

	public void setProxy(Proxy proxy) {
		this.proxy = proxy;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	@Override
	public String toString() {
		return String.format("%s\nOld username: %s, registration date: %s, posts: %s, followers: %s, Blocked - %s\n", getMospanoffFormat(),
				oldUserName, registrationDate, posts, followers, blocked);
	}

	public Email toNewEmail() {
		String domain = newEmail.substring(newEmail.lastIndexOf('@') + 1);
		EmailConnection emailConnection = connectionUtils.findConnectionInfoByDomain(domain);
		if (emailConnection == null) {
			emailConnection = connectionUtils.createNewConnectionInfo(domain);
		}
		return new Email(newEmail, newEmailPassword, emailConnection);
	}

	public Email toOldEmail() {
		String domain = oldEmail.substring(oldEmail.lastIndexOf('@') + 1);
		EmailConnection emailConnection = connectionUtils.findConnectionInfoByDomain(domain);
		if (emailConnection == null) {
			emailConnection = connectionUtils.createNewConnectionInfo(domain);
		}
		return new Email(oldEmail, oldEmailPassword, emailConnection);
	}

	public String getInfo() {
		return String.format("%s | %s | %s", registrationDate, posts, followers);
	}

	public String getMospanoffFormat() {
		if (newEmail.isEmpty() && newEmailPassword.isEmpty()) {
			return String.format("%s:%s", userName, password);
		}

		if (oldEmail.isEmpty() && oldEmailPassword.isEmpty()) {
			return String.format("%s:%s:%s:%s", userName, password, newEmail, newEmailPassword);
		}

		return String.format("%s:%s:%s:%s:%s:%s", userName, password, newEmail, newEmailPassword, oldEmail, oldEmailPassword);
	}

	public void addFollowing() {
		following++;
	}

	public void addFollowError() {
		followError++;
	}

	public void addCookie(Cookie cookie) {
		cookies.add(cookie);
	}

	public void addCookies(Set<Cookie> cookies) {
		for (Cookie cookie : cookies) {
			addCookie(cookie);
		}
	}

	private String getFormattedCookies() {
		StringBuilder builder = new StringBuilder();
		for (Cookie cookie : cookies) {
			builder.append(cookie.getName())
					.append("=")
					.append(cookie.getValue())
					.append(";");
		}
		return builder.toString();
	}

	private String getJsonCookie() {
		JsonObject jsonObject = new JsonObject();
		for (Cookie cookie : cookies) {
			jsonObject.addProperty(cookie.getName(), cookie.getValue());
		}
		return jsonObject.toString();
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getFullName() {
		return fullName;
	}

	public String getAccountWithProxy() {
		if (proxy == null) {
			return getMospanoffFormat();
		}

		return getMospanoffFormat() + "|" + proxy.toString() + "|" + userAgent;
	}

	public String getAccountWithCookie() {
		return getMospanoffFormat() + "|" + userAgent + "|" + getJsonCookie();
	}
}
