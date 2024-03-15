package crypto;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class Encrypter {

	public static String vernamEncrypt(String privateKey, String message) {
		StringBuilder vernamMessage = new StringBuilder();
		
		for(int i = 0; i < message.length(); i++) {
			char caracter = message.charAt(i);
			char caracKey = privateKey.charAt(i % privateKey.length()); // vai pegando o proximo caractere com modulo p chave ir sendo repetida
			char encryptedCaractere = (char)(caracter ^ caracKey); // xor de caracMessage com caracKey de um em um
			vernamMessage.append(encryptedCaractere); // junta tudo
		}
		
		return vernamMessage.toString();
	}
	
	public static String vernamDecrypt(String privateKey, String message) {
		return vernamEncrypt(privateKey, message); // pq vernam é simetrico
	}
	
	public static String aesEncrypt(SecretKey aesKey, String message) {
		
		byte[] bytesEncryptedMessage;
		Cipher cipher;
		String encryptedMessage = null;
		
		try {
			cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, aesKey);
			
			bytesEncryptedMessage = cipher.doFinal(message.getBytes()); // aq ja terminou a cifragem, poderia passar pra String
			
			encryptedMessage = Base64.getEncoder().encodeToString(bytesEncryptedMessage); // so q eu boto logo na base 64 pra acelerar
			
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
		}
		
		return encryptedMessage;
	}
	
	public static String aesDecrypt(SecretKey aesKey, String encryptedMessage) {
		
		byte[] bytesEncryptedMessage = Base64.getDecoder().decode(encryptedMessage); // tirar da base 64 e passar pra array de bytes
		Cipher decipher;
		String decryptedMessage = null;
		
		try {
			decipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			decipher.init(Cipher.DECRYPT_MODE, aesKey); 
			
			byte[] bytesDecryptedMessage = decipher.doFinal(bytesEncryptedMessage);
			decryptedMessage = new String(bytesDecryptedMessage);
			
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
		}
		
		return decryptedMessage;
	}
	
}
