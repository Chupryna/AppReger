package sample.manager;

import sample.utils.Logger;

import java.util.Random;

public class InstagramManager {

	private final Random random = new Random();
	private final Logger logger;

	public InstagramManager(Logger logger) {
		this.logger = logger;
	}

}
