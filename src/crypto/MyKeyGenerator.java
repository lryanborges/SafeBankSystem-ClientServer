package crypto;

import java.security.NoSuchAlgorithmException;
import java.util.Random;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class MyKeyGenerator {

	public static SecretKey generateKeyAes() {

		SecretKey generatedKey = null;

		try {
			KeyGenerator generator = KeyGenerator.getInstance("AES");
			generatedKey = generator.generateKey();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return generatedKey;

	}

	public static String generateKeyVernam() {

		int size = 16;
		String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		StringBuilder randomString = new StringBuilder(size);
		Random random = new Random();

		for (int i = 0; i < size; i++) {
			int randomIndex = random.nextInt(characters.length());
			char randomChar = characters.charAt(randomIndex);
			randomString.append(randomChar);
		}
		
		return randomString.toString();
		
	}

	public static String generateKeyHMAC() {

		int size = 16;
		String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		StringBuilder randomString = new StringBuilder(size);
		Random random = new Random();

		for (int i = 0; i < size; i++) {
			int randomIndex = random.nextInt(characters.length());
			char randomChar = characters.charAt(randomIndex);
			randomString.append(randomChar);
		}
		
		return randomString.toString();
		
	}

}
