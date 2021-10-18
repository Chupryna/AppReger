package sample.data.model;

import java.util.Objects;

public class Email {
	private String email;
	private String password;
	private String fullName;
	private String dateOfBirth;
	private EmailConnection emailConnection;

	public Email() {
	}

	public Email(String email, String password) {
		this(email, password, null);
	}

	public Email(String email, String password, EmailConnection emailConnection) {
		this.email = email;
		this.password = password;
		this.emailConnection = emailConnection;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(String dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public EmailConnection getEmailConnection() {
		return emailConnection;
	}

	public void setEmailConnection(EmailConnection emailConnection) {
		this.emailConnection = emailConnection;
	}

	public boolean isValid() {
		return !email.isEmpty() && !password.isEmpty() && emailConnection != null;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Email email1 = (Email) o;
		return Objects.equals(email, email1.email) &&
				Objects.equals(password, email1.password);
	}

	@Override
	public int hashCode() {
		return Objects.hash(email, password);
	}

	@Override
	public String toString() {
		return email + ":" + password;
	}

	public String getFullInfo() {
		return String.format("%s:%s|%s:%s", email, password, fullName, dateOfBirth);
	}
}
