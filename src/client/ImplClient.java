package client;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

import crypto.Encrypter;
import crypto.Hasher;
import datagrams.Message;
import model.BankAccount;

public class ImplClient implements Runnable {

	Socket socket;
	
	public ImplClient(Socket socket) {
		this.socket = socket;
	}
	
	@Override
	public void run() {
		
		try {
			
			ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
			Scanner scan = new Scanner(System.in);
	
			output.writeObject(new Message<String>(100, "Reconhecimento de conexão...", Client.clientNumber));
			
			while(!Client.isConexion()) {
				
				scan.nextLine();
				
				System.out.println("-----------------------------");
				System.out.println("\tBANCO SEGURO");
				System.out.println("-----------------------------");
				System.out.println("[1] - Login");
				System.out.println("[2] - Registrar-se");
				System.out.println("-----------------------------");
				System.out.print("Opção: ");
				int opc = scan.nextInt();
				scan.nextLine();
				
				switch(opc) {
				case 1:
					System.out.println("-----------------------------");
					System.out.print("CPF: ");
					String loginCpf = scan.nextLine();
					System.out.print("Senha: ");
					String loginPassword = scan.nextLine();
					
					String login = loginCpf + "/" + loginPassword;
					
					String HMAC = Hasher.hMac(Client.getHMACKey(), login);
					loginCpf = fullEncrypt(loginCpf);
					loginPassword = fullEncrypt(loginPassword);
					
					BankAccount loginAccount = new BankAccount(loginCpf, loginPassword);
					
					output.writeObject(new Message<BankAccount>(1, loginAccount, Client.clientNumber));
					
					break;
				case 2:
					System.out.println("-----------------------------");
					System.out.print("Nome: ");
					String newName = scan.nextLine();
					System.out.print("CPF: ");
					String newCpf = scan.nextLine();
					System.out.print("Senha: ");
					String newPassword = scan.nextLine();
					System.out.print("Endereço: ");
					String newAddress = scan.nextLine();
					System.out.print("Número de celular: ");
					String newCelNumber = scan.nextLine();
					
					newCpf = fullEncrypt(newCpf);
					newName = fullEncrypt(newName);
					newPassword = fullEncrypt(newPassword);
					newAddress = fullEncrypt(newAddress);
					newCelNumber = fullEncrypt(newCelNumber);
					
					BankAccount newAccount = new BankAccount(newCpf, newName, newPassword, newAddress, newCelNumber);
					
					output.writeObject(new Message<BankAccount>(2, newAccount));
					
					break;
				default:
					System.out.println("Opção inválida.");
				}
				
				scan.nextLine();
			}
			
			while(Client.isConexion()) {
				
				scan.nextLine();
				
				System.out.println("-----------------------------");
				System.out.println("\tBANCO SEGURO");
				System.out.println("-----------------------------");
				System.out.println("[1] - Verificar saldo");
				System.out.println("[2] - Sacar valor");
				System.out.println("[3] - Depositar valor");
				System.out.println("[4] - Transferência bancária");
				System.out.println("[5] - Investimentos");
				System.out.println("-----------------------------");
				System.out.print("Opção: ");
				int opc = scan.nextInt();
				scan.nextLine();
				
				String value;
				String HMAC;
				String encryptedMsg;
				switch(opc) {
				case 1:
					System.out.println("-----------------------------");
					System.out.println("\tSALDO DA CONTA");
					System.out.println("-----------------------------");
					value = "Verificar saldo";
					
					HMAC = Hasher.hMac(Client.getHMACKey(), value);
					encryptedMsg = fullEncrypt(value);
					
					output.writeObject(new Message<String>(1, encryptedMsg, HMAC));
					
					break;
				case 2:
					System.out.println("-----------------------------");
					System.out.println("\tSAQUE");
					System.out.println("-----------------------------");
					System.out.print("Valor do saque: ");
					value = scan.nextLine();
					
					HMAC = Hasher.hMac(Client.getHMACKey(), value);
					encryptedMsg = fullEncrypt(value);
					
					output.writeObject(new Message<String>(2, encryptedMsg, HMAC));
					break;
				case 3: 
					System.out.println("-----------------------------");
					System.out.println("\tDEPOSITO");
					System.out.println("-----------------------------");
					System.out.print("Valor do deposito: ");
					value = scan.nextLine();
					
					HMAC = Hasher.hMac(Client.getHMACKey(), value);
					encryptedMsg = fullEncrypt(value);
					
					output.writeObject(new Message<String>(3, encryptedMsg, HMAC));
					break;
				case 4:
					System.out.println("-----------------------------");
					System.out.println("\tTRANSFERÊNCIA");
					System.out.println("-----------------------------");
					System.out.print("Pix do destinatário: ");
					String pix = scan.nextLine();
					System.out.print("Valor da transferência: ");
					value = scan.nextLine();
					
					String concat = pix + "/" + value;
					
					HMAC = Hasher.hMac(Client.getHMACKey(), concat);
					encryptedMsg = fullEncrypt(concat);
					
					output.writeObject(new Message<String>(4, encryptedMsg, HMAC));
					break;
				case 5:
					System.out.println("-----------------------------");
					System.out.println("\tINVESTIMENTOS");
					System.out.println("-----------------------------");
					System.out.println("[1] - Verificar previsões");
					System.out.println("[2] - Investir na renda fixa");
					System.out.println("-----------------------------");
					System.out.print("Opção: ");
					String investOpc = scan.nextLine();
					
					if(investOpc.equalsIgnoreCase("1")) {
						value = "1/Verificar investimentos";
					} else if(investOpc.equalsIgnoreCase("2")) {
						System.out.println("-----------------------------");
						System.out.println("Valor pra investir: ");
						value = scan.nextLine();
						value = "2/" + value;
					} else {
						value = "";
						System.out.println("Opção inválida.");
					}
					
					HMAC = Hasher.hMac(Client.getHMACKey(), value);
					encryptedMsg = fullEncrypt(value);
					
					output.writeObject(new Message<String>(5, encryptedMsg, HMAC));
					
					break;
				default:
					System.out.println("Opção inválida.");
				}
				
			}
			
		} catch (IOException | InvalidKeyException | NoSuchAlgorithmException e) {
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
