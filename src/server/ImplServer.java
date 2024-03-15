package server;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import javax.crypto.SecretKey;

import crypto.Encrypter;
import crypto.Hasher;
import crypto.MyKeyGenerator;
import datagrams.Message;
import model.BankAccount;

public class ImplServer implements Runnable {

	private Socket clientSocket;
	private Socket clientServerSocket;
	ObjectInputStream input;
	ObjectOutputStream output;
	ObjectOutputStream fileOutput;
	ObjectInputStream fileInput;
	String file = "src/server/accounts.txt";
	boolean connected = false;
	private String HMACKey;
	private String VernamKey;
	private SecretKey AESKey;
	private HashSet<BankAccount> accounts;
	
	public ImplServer(Socket client) {
		this.clientSocket = client;
		
		AESKey = MyKeyGenerator.generateKeyAes();
		HMACKey = MyKeyGenerator.generateKeyHMAC();
		VernamKey = MyKeyGenerator.generateKeyVernam();
	}

	@Override
	public void run() {
		
		try {
			input = new ObjectInputStream(clientSocket.getInputStream());
			
			BankAccount account = null; // vai apontar pra conta q eu vou gerenciar qnd logar
			
			String moneyFormat = "%.2f";
			
			Message<BankAccount> acknowledgmentMsg = (Message<BankAccount>) input.readObject(); // primera msg é só de reconhecimento de nova conexão
			clientServerSocket = new Socket("", (int) (10000 + acknowledgmentMsg.getClientSendingMsg()));
			output = new ObjectOutputStream(clientServerSocket.getOutputStream());
			
			System.out.println("-----------------------------");
			System.out.println("\tNOVO CLIENTE");
			System.out.println("-----------------------------");
			System.out.println("Cliente HMAC: " + HMACKey);
			System.out.println("Cliente Vernam: " + VernamKey);
			System.out.println("Cliente AES: " + AESKey);
			System.out.println("-----------------------------");
			
			// envia as 3 chaves assim q o cliente faz conexão
			output.writeObject(getHMACKey()); 
			output.writeObject(getVernamKey());
			output.writeObject(getAESKey());
			
			while(true) {
				Message received = (Message) input.readObject();
				
				accounts = getFileAccounts(); // é a lista de todas as contas (basicamente o arquivo)
				fileOutput = new ObjectOutputStream(new FileOutputStream(file)); // true p modo append pra acumular dados para além de uma execução
				
				if(!connected) {
					Message<BankAccount> receivedMsg = (Message<BankAccount>) received;
					
					switch(receivedMsg.getOperation()) {
					case 1:
						
						String encryptedCpf = receivedMsg.getContent().getCpf();
						String encryptedPassword = receivedMsg.getContent().getPassword();
						
						String cpf = fullDecrypt(encryptedCpf);
						String password = fullDecrypt(encryptedPassword);
						
						String login = cpf + "/" + password;
						//String realHMAC = Hasher.hMac(HMACKey, login);		
						
						account = findAccount(cpf, password);
						if(account != null) {
							connected = true;	
							System.out.println("Conta logada: " + account.getClientName() + ", CPF: " + account.getCpf());
							System.out.println("Conectado através do Cliente: " + receivedMsg.getClientSendingMsg());
							
							String response = "Conectado com sucesso...";
							response = fullEncrypt(response);
							
							Message<String> message = new Message<String>(0, response);
							output.writeObject(message);
							
						} else {
							System.out.println("Cliente não cadastrado.");
						}	
						
						break;
					case 2:
						// registrar
						
						BankAccount newAccount = receivedMsg.getContent();
						
						String decryptedCpf = fullDecrypt(newAccount.getCpf());
						String decryptedName = fullDecrypt(newAccount.getClientName());
						String decryptedPassword = fullDecrypt(newAccount.getPassword());
						String decryptedAddress = fullDecrypt(newAccount.getAddress());
						String decryptedCelNumber = fullDecrypt(newAccount.getCelNumber());
						
						createBankAccount(new BankAccount(decryptedCpf, decryptedName, decryptedPassword, decryptedAddress, decryptedCelNumber));
						
						break;
					default:
						System.out.println("................");	
					}
				} else { // qnd já tiver conectado(connected == true)
					
					BankAccount operatingAccount = findAccountByCpf(account.getCpf());
					account = operatingAccount;
					accounts.remove(operatingAccount); // tira a conta q eu to gerenciando da lista/arquivo
					
					Message<String> receivedMsg = (Message<String>) received;
					
					String decryptedMsg = fullDecrypt(receivedMsg.getContent());
					String realHMAC = Hasher.hMac(HMACKey, decryptedMsg);
					
					if(realHMAC.equals(receivedMsg.getMessageHmac())) {
						double value;
						String response;
						String HMAC;
						String encryptedResponse;
						switch(receivedMsg.getOperation()) {
						case 1:
							response = "Saldo: R$" + String.format(moneyFormat, account.getMoney());
							
							HMAC = Hasher.hMac(HMACKey, response);
							encryptedResponse = fullEncrypt(response);
							
							output.writeObject(new Message<String>(1, encryptedResponse, HMAC));
							
							break;
						case 2:
							value = Double.parseDouble(decryptedMsg);
							
							if(account.getMoney() >= value) {
								account.setMoney(account.getMoney() - value);
								response = "Saque de R$" + String.format(moneyFormat, value) + " realizado.";
							} else {
								response = "Valor na conta insuficiente.";
							}
							
							HMAC = Hasher.hMac(HMACKey, response);
							encryptedResponse = fullEncrypt(response);
							
							output.writeObject(new Message<String>(2, encryptedResponse, HMAC));
							
							break;
						case 3:
							value = Double.parseDouble(decryptedMsg);
							
							if(value >= 0) {
								account.setMoney(account.getMoney() + value);
								response = "Deposito de R$" + String.format(moneyFormat, value) + " realizado.";
							} else {
								response = "Não é possível depositar um valor negativo.";
							}
							
							HMAC = Hasher.hMac(HMACKey, response);
							encryptedResponse = fullEncrypt(response);
							
							output.writeObject(new Message<String>(3, encryptedResponse, HMAC));
							break;
						case 4:
							String[] part = decryptedMsg.split("/");
							value = Double.parseDouble(part[1]);
							
							BankAccount receiver = findAccountByCpf(part[0]);
							
							if(receiver != null) {
								if(account.getMoney() >= value && value >= 0) {
									receiver.setMoney(receiver.getMoney() + value);
									account.setMoney(account.getMoney() - value);
									response = "Transferência de R$" + value + " realizado para a chave " + part[0];
								} else {
									response = "Valor da transferência não permitido. Verifique seu saldo.";
								}
							} else {
								response = "Chave pix não encontrada";
							}
							
							HMAC = Hasher.hMac(HMACKey, response);
							encryptedResponse = fullEncrypt(response);
							
							output.writeObject(new Message<String>(4, encryptedResponse, HMAC));
									
							break;
						case 5:
							
							String[] parts = decryptedMsg.split("/");
							value = account.getMoney();
							response = "";
							
							if(parts[0].equalsIgnoreCase("1")) {
								for(int i = 1; i <= 12; i++) {
									value = value + ((value * 0.5) / 100);  // rendendo 0.5% ao mês
									if(i % 3 == 0 && i != 9) {
										response = response + "\n" + i + "º mês: R$" + String.format(moneyFormat, value);	
									}
								}	
							} else if(parts[0].equalsIgnoreCase("2")) {
								double toFixed = Double.parseDouble(parts[1]);
								double current = value - toFixed;
								
								for(int i = 1; i <= 12; i++) {
									current = current + ((current * 0.5) / 100);  // rendendo 1.5% ao mês
									toFixed = toFixed + ((current * 1.5) / 100);
									value = current + toFixed;
									if(i % 3 == 0 && i != 9) {
										response = response + "\n" + i + "º mês: R$" + String.format(moneyFormat, current) + 
												" corrente, R$" + String.format(moneyFormat, toFixed) + 
												" fixa. Total: R$" + String.format(moneyFormat, value);	
									}
								}
							}
							
							HMAC = Hasher.hMac(HMACKey, response);
							encryptedResponse = fullEncrypt(response);
							
							output.writeObject(new Message<String>(5, encryptedResponse, HMAC));
							
							break;
						default:
							System.out.println("................");	
						}
					} else {
						System.out.println();
						System.out.println("Mensagem inválida! Cliente não autenticado.");
					}
					
					accounts.add(account); // volta a conta q eu tinha removido, agora com possiveis modificações
				}
				
				for(BankAccount acc : accounts) { // add toda o set de contas de volta pro arquivo
					if(acc != null) {
						fileOutput.writeObject(acc); 
						//System.out.println("Colocado no arquivo: " + acc.getClientName() + ". Valor: " + acc.getMoney());	
					}
				}	
				
				fileOutput.close();
			}
			
		} catch (IOException | ClassNotFoundException | InvalidKeyException | NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
	}
	
	public String fullEncrypt(String message) {
		
		String encrypted = Encrypter.vernamEncrypt(VernamKey, message);
		encrypted = Encrypter.aesEncrypt(AESKey, encrypted);
		
		return encrypted;
	}
	
	public String fullDecrypt(String encryptedMessage) {
		
		String decrypted = Encrypter.aesDecrypt(AESKey, encryptedMessage); // retorna nulo se a chave n for a mesma da criptografia
		decrypted = Encrypter.vernamDecrypt(VernamKey, decrypted);
		
		return decrypted;
	}
	
	public void createBankAccount(BankAccount newAccount) throws IOException {
		
		//fileOutput.writeObject(newAccount); // criar a conta em si, adicionando no arquivo de contas
		accounts.add(newAccount);
		
		System.out.println("Nova conta registrada! Cliente: " + newAccount.getClientName());
		
	}
	
	public BankAccount findAccount(String cpf, String password) {
	
		for(BankAccount findedAccount : accounts) {
			if(findedAccount != null) {
				if(findedAccount.getCpf().equals(cpf) && findedAccount.getPassword().equals(password)) {
					System.out.println("Login com sucesso: " + findedAccount.getClientName());
					return findedAccount;
				}	
			}
		}
	
		return null;
	}
	
	public BankAccount findAccountByCpf(String cpf) {
		
		for(BankAccount findedAccount : accounts) {
			if(findedAccount != null) {
				if(findedAccount.getCpf().equals(cpf)) {
					return findedAccount;
				}	
			}
		}
	
		return null;
	}
	
	public HashSet<BankAccount> getFileAccounts(){
		
		boolean eof = false;
		HashSet<BankAccount> accounts = new HashSet<BankAccount>();
		
		try {
			fileInput = new ObjectInputStream(new FileInputStream(file));
			
			while(!eof) {
				BankAccount account = (BankAccount) fileInput.readObject();
				accounts.add(account);
			}
			
		} catch (IOException e) {
			eof = true;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return accounts;
	}

	public String getHMACKey() {
		return HMACKey;
	}

	public String getVernamKey() {
		return VernamKey;
	}

	public SecretKey getAESKey() {
		return AESKey;
	}
	
}
