package sample.utils;

import java.util.Observable;

public class MessageReceivedObservable extends Observable {
	private boolean isReceived = false;
	private String result = "";

	public boolean isReceived() {
		return isReceived;
	}

	public void setReceived(boolean received) {
		isReceived = received;
		setChanged();
		notifyObservers();
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
		setReceived(true);
	}
}