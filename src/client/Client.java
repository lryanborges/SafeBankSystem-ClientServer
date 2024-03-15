package client;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.crypto.SecretKey;

public class Client {

	static int clientNumber;
	static boolean isInvader;
	private Socket socket;
	private ServerSocket serverSocket;
	private static boolean conexion = false;
	static SecretKey aesKey;
	static String HMACKey;
	static String vernamKey;
	
	public Client(int num) {
		Client.clientNumber = num;
		Client.isInvader = false;
		run();
	}
	
	public Client(int num, boolean isInvader) {
		Client.clientNumber = num;
		Client.isInvader = isInvader;
		run();
	}
	
	public void run() {
		
		try {
			System.out.println("\tCLIENTE " + clientNumber);
			System.out.println("-----------------------------");
			socket = new Socket("127.0.0.1", 10000);
			ImplClient client = new ImplClient(socket);
			Thread clientThread = new Thread(client);
			clientThread.start();
			
			serverSocket = new ServerSocket(10000 + clientNumber);
			Socket connected = serverSocket.accept();
			ClientMessageReceiver receiver = new ClientMessageReceiver(connected, client);
			Thread receiverThread = new Thread(receiver);
			receiverThread.start();
			
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
	}
	
	public static boolean isConexion() {
		return conexion;
	}
	public static void setConexion(boolean conexion) {
		Client.conexion = conexion;
	}

	public static SecretKey getAesKey() {
		return aesKey;
	}

	public static String getHMACKey() {
		return HMACKey;
	}

	public static String getVernamKey() {
		return vernamKey;
	}

	public static void main(String[] args) {
		new Client(2);
	}
	
}
