package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Bank {
	
	private static ServerSocket serverSocket;

	public static void main(String[] args) {
		
		try {
			serverSocket = new ServerSocket(10000);
			
			while(true) {
				Socket connectedSocket = serverSocket.accept();				
				ImplServer server = new ImplServer(connectedSocket);
				Thread serverThread = new Thread(server);
				serverThread.start();
			}
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
