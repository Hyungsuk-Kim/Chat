package chat.example2;

import java.net.*;
import java.io.*;

public class ChatServerEx {
	private ServerSocket serverSocket;

	public ChatServerEx(int port) {
		try {
			serverSocket = new ServerSocket(port);
			System.out.println(port + " 포트에서 접속을 기다립니다.");
			start();
			
		} catch (IOException e) {
			System.out.println(e.getMessage());
			System.out.println("채팅 서버가 종료됩니다.");
			
		} finally {
			if (serverSocket != null) {
				try {
					serverSocket.close();
				} catch (IOException ioe) {	}
			}
		}
	}
	
	public void start() throws IOException {
		Socket socket;
		Thread serverThread;
		while (true) {
			socket = serverSocket.accept();
			System.out.println(socket.getInetAddress() + " 에서 접속했습니다.");
			serverThread = new ServerThread(this, socket);
			serverThread.start();
		}
	}

	public static void main(String[] args) {
		new ChatServerEx(5432);
	}
}

class ServerThread extends Thread {
	private ChatServerEx server;
	private Socket socket;
	private BufferedReader socketReader;
	private PrintWriter socketWriter;

	public ServerThread(ChatServerEx server, Socket socket) {
		this.server = server;
		this.socket = socket;
	}

	@Override
	public void run() {
		try {
			String message = null;
			
			socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			socketWriter = new PrintWriter(socket.getOutputStream(), true);
			
			while ((message = socketReader.readLine()) != null) {
				System.out.println("Client Message : " + message);
				socketWriter.println(message);
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
		} finally {
			try {
				socketReader.close();		
				socketWriter.close();
				socket.close();
				System.out.println(socket.getInetAddress() + " 접속이 종료되었습니다.");
			} catch (IOException ioe) {	}
		}
		
	}

	public void sendMessage(String message) {
		socketWriter.println(message);
	}
}
