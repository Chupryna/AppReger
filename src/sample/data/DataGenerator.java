package sample.data;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class DataGenerator {

	private static final int PASS_LENGTH = 10;
	private static final int EMAIL_LENGTH = 12;
	private static final List<String> surnames = new ArrayList<>();
	private static final List<String> names = new ArrayList<>();
	private static final List<String> ruNames = new ArrayList<>();
	private static final List<String> arabNames = new ArrayList<>();
	private static final List<String> koreaNames = new ArrayList<>();
	private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
	private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private static final String DIGITS = "0123456789";
	private static final String NON_CHARACTER_SYMBOL = "!^%.$";
	private static final List<String> categories = new ArrayList<>(Arrays.asList(LOWER, UPPER, DIGITS));
	private static final Random random = new Random(System.nanoTime());

	private DataGenerator() {
	}

	public static void init() {
		File namesFile = new File("resources/names.txt");
		File surnamesFile = new File("resources/surnames.txt");
		File ruNamesFile = new File("resources/ruWoman.txt");
		File arabsFile = new File("resources/arabs.txt");
		File koreaFile = new File("resources/korea.txt");

		Thread thread = new Thread(() -> {
			try (BufferedReader namesReader = new BufferedReader(new FileReader(namesFile));
				 BufferedReader surnamesReader = new BufferedReader(new FileReader(surnamesFile));
				 BufferedReader arabsReader = new BufferedReader(new InputStreamReader(new FileInputStream(arabsFile), StandardCharsets.UTF_8));
				 BufferedReader koreaReader = new BufferedReader(new InputStreamReader(new FileInputStream(koreaFile), StandardCharsets.UTF_8));
				 BufferedReader ruNamesReader = new BufferedReader(new InputStreamReader(new FileInputStream(ruNamesFile), StandardCharsets.UTF_8))) {

				String line;
				while ((line = namesReader.readLine()) != null) {
					names.add(line);
				}
				while ((line = surnamesReader.readLine()) != null) {
					surnames.add(line);
				}
				while ((line = ruNamesReader.readLine()) != null) {
					ruNames.add(line);
				}
				while ((line = arabsReader.readLine()) != null) {
					arabNames.add(line);
				}
				while ((line = koreaReader.readLine()) != null) {
					koreaNames.add(line);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		thread.start();
	}

	public synchronized static String getPassword() {
		StringBuilder password = new StringBuilder(PASS_LENGTH);
		password.append(LOWER.charAt(random.nextInt(LOWER.length() - 1)));
		password.append(UPPER.charAt(random.nextInt(UPPER.length() - 1)));
		password.append(DIGITS.charAt(random.nextInt(DIGITS.length() - 1)));
//		password.append(NON_CHARACTER_SYMBOL.charAt(random.nextInt(NON_CHARACTER_SYMBOL.length() - 1)));
		for (int i = 3; i < PASS_LENGTH; i++) {
			String category = categories.get(random.nextInt(categories.size()));
			password.append(category.charAt(random.nextInt(category.length())));
		}

		return password.toString();
	}

	public synchronized static String getUsername() {
		return '_' +
				names.get(random.nextInt(names.size() - 1)) +
				'.' +
				surnames.get(random.nextInt(surnames.size() - 1)) +
				DIGITS.charAt(random.nextInt(DIGITS.length() - 1));
	}

	public synchronized static String getRuFullName() {
		return ruNames.get(random.nextInt(ruNames.size() - 1));
	}

	public synchronized static String getArabName() {
		return arabNames.get(random.nextInt(arabNames.size() - 1));
	}

	public synchronized static String getKoreaName() {
		return koreaNames.get(random.nextInt(koreaNames.size() - 1));
	}

	public static String getFullName() {
		return names.get(random.nextInt(names.size() - 1))
				+ " "
				+ surnames.get(random.nextInt(surnames.size()));
	}

	public static String getFakeEmail() {
		StringBuilder email = new StringBuilder(EMAIL_LENGTH);
		for (int i = 0; i < EMAIL_LENGTH; i++) {
			String category = categories.get(random.nextInt(categories.size()));
			email.append(category.charAt(random.nextInt(category.length())));
			if (i == 7) {
				email.append("_");
			}
		}

		return email.toString() + "@gmail.com";
	}

	public static String getMailLoginByFullName(String fullName) {
		return fullName.replace(" ", "_") + random.nextInt(100);
	}
}
