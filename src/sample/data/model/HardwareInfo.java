package sample.data.model;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class HardwareInfo {

	private static final String ENCRYPTION_KEY = "2s6v9y$B?E(H+MbQ";

	private String osSerialNumber;
	private String osInstallDate;
	private String baseboardSerialNumber;
	private String processorID;
	private String username;

	public void init() {
		osSerialNumber = executeCommand(new String[]{"CMD", "/C", "WMIC OS GET SerialNumber"});
		osInstallDate = executeCommand(new String[]{"CMD", "/C", "WMIC OS GET Installdate"}).replaceAll("\\..+", "");
		baseboardSerialNumber = executeCommand(new String[]{"CMD", "/C", "WMIC BASEBOARD GET SerialNumber"});
		processorID = executeCommand(new String[]{"CMD", "/C", "WMIC CPU GET ProcessorId"});
		username = executeCommand(new String[]{"CMD", "/C", "WMIC COMPUTERSYSTEM GET Name"});
	}

	private String executeCommand(String[] command) {
		StringBuilder commandResult = new StringBuilder();

		try {
			Process process = Runtime.getRuntime().exec(command);
			process.getOutputStream().close();

			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				commandResult.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return commandResult.toString().trim().replaceAll(" +", ":").split(":")[1];
	}

	public String getHardwareKey() {
		try {
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			SecretKey secretKey = new SecretKeySpec(ENCRYPTION_KEY.getBytes(), "AES");
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);

			cipher.update(osSerialNumber.getBytes(StandardCharsets.UTF_8));
			cipher.update(osInstallDate.getBytes(StandardCharsets.UTF_8));
			cipher.update(baseboardSerialNumber.getBytes(StandardCharsets.UTF_8));
			cipher.update(processorID.getBytes(StandardCharsets.UTF_8));
			byte[] encryptedMessageBytes = cipher.doFinal(username.getBytes(StandardCharsets.UTF_8));
//			System.out.println(new String(encryptedMessageBytes));

			return new String(encryptedMessageBytes);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException |
				IllegalBlockSizeException | InvalidKeyException e) {
			e.printStackTrace();
		}

		return "";
	}
}
