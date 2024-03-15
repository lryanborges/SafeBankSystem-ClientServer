package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.SecretKey;

import crypto.Encrypter;
import crypto.Hasher;
import crypto.MyKeyGenerator;
import datagrams.Message;

public class ClientMessageReceiver implements Runnable {

	private Socket socket;
	private ImplClient myClient;
	
	public ClientMessageReceiver(Socket socket, ImplClient client){
		this.socket = socket;
		this.myClient = client;
	}
	
	@Override
	public void run() {
		
		try {
			ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
			
			Client.HMACKey = (String) input.readObject();
			Client.vernamKey = (String) input.readObject();
			Client.aesKey = (SecretKey) input.readObject();	
			
			if(Client.isInvader) { // se ele for classificado com invasor, gera chaves novas
				Client.HMACKey = MyKeyGenerator.generateKeyHMAC();
			} 
			
			System.out.println("Cliente HMAC: " + Client.HMACKey);
			System.out.println("Cliente Vernam: " + Client.vernamKey);
			System.out.println("Cliente AES: " + Client.aesKey);
			
			while(true) {
				Message<String> message = (Message<String>) input.readObject();
				
				if(!Client.isConexion()) {
					if(message.getOperation() == 0) { // oper == 0 é p login
						Client.setConexion(true); // p definir o client como logado
						
						String decryptedMsg = fullDecrypt(message.getContent());
						
						System.out.println();
						System.out.println("Servidor: " + decryptedMsg);
					}
				} else { // qnd ja tiver logado
					
					String decryptedMsg = fullDecrypt(message.getContent());
					String realHMAC = Hasher.hMac(Client.getHMACKey(), decryptedMsg);
					
					if(realHMAC.equals(message.getMessageHmac())) {
						//System.out.println("Mensagem válida.");
						System.out.println();
						System.out.println("Servidor: " + decryptedMsg);
					} else {
						System.out.println("Servidor falso.");
					}
					
				}
			}
			
		} catch (IOException | ClassNotFoundException | InvalidKeyException | NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
	}
	
	public String fullEncrypt(String message) {
		
		String encrypted = Encrypter.vernamEncrypt(Client.getVernamKey(), message);
		encrypted = Encrypter.aesEncrypt(Client.getAesKey(), encrypted);
		
		return encrypted;
	}
	
	public String fullDecrypt(String encryptedMessage) {
		
		String decrypted = Encrypter.aesDecrypt(Client.getAesKey(), encryptedMessage);
		decrypted = Encrypter.vernamDecrypt(Client.getVernamKey(), decrypted);
		
		return decrypted;
	}
	
}
