package sample.data.model;

public class EmailResult {

	private String result = "";
	private boolean isSuccessfully = false;
	private boolean isNeedReconnect = false;
	private boolean isInvalidCredentials = false;
	private Exception exception;

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public boolean isSuccessfully() {
		return isSuccessfully;
	}

	public void setSuccessfully(boolean successfully) {
		isSuccessfully = successfully;
	}

	public boolean isNeedReconnect() {
		return isNeedReconnect;
	}

	public void setNeedReconnect(boolean needReconnect) {
		isNeedReconnect = needReconnect;
	}

	public boolean isInvalidCredentials() {
		return isInvalidCredentials;
	}

	public void setInvalidCredentials(boolean invalidCredentials) {
		isInvalidCredentials = invalidCredentials;
	}

	public Exception getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}

	@Override
	public String toString() {
		return "EmailResult{" +
				"isSuccessfully=" + isSuccessfully +
				", isNeedReconnect=" + isNeedReconnect +
				", exception=" + exception +
				'}';
	}
}
