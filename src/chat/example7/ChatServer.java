package chat.example7;

import java.net.*;
import java.io.*;
import java.util.*;

public class ChatServer {
	private ServerSocket serverSocket;
	private List<ServerThread> threadPool;

	public ChatServer(int port) {
		threadPool = new ArrayList<ServerThread>(10);

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
		ServerThread serverThread;
		while (true) {
			socket = serverSocket.accept();
			System.out.println(socket.getInetAddress() + " 에서 접속했습니다.");
			serverThread = new ServerThread(this, socket);
			serverThread.start();
		}
	}

	public void broadCastData(Data protocol) {
		for (ServerThread serverThread : threadPool) {
			serverThread.sendData(protocol);
		}
	}

	public void addThread(ServerThread aThread) {
		threadPool.add(aThread);
	}

	public void removeThread(ServerThread aThread) {
		threadPool.remove(aThread);
	}

	public List<String> getUserList() {
		List<String> userList = new ArrayList<String>();
		for (ServerThread serverThread : threadPool) {
			userList.add(serverThread.getUserName());
		}
		return userList;
	}

	public static void main(String[] args) {
		new ChatServer(5432);
	}
}

class ServerThread extends Thread {
	private ChatServer server;
	private Socket socket;
	private ObjectInputStream socketOIS;
	private ObjectOutputStream socketOOS;
	private String userName;	

	public ServerThread(ChatServer server, Socket socket) {
		this.server = server;
		this.socket = socket;
	}

	@Override
	public void run() {
		try {
			socketOIS = new ObjectInputStream(socket.getInputStream());
			socketOOS = new ObjectOutputStream(socket.getOutputStream());

			Data data = null;
			while (true) {
				data = (Data) socketOIS.readObject();
				parseProtocol(data);
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
			
		} finally {
			try {
				socketOIS.close();		
				socketOOS.close();
				socket.close();
				System.out.println(socket.getInetAddress() + " 접속이 종료되었습니다.");
			} catch (IOException ioe) {	}
		}
	}

	private void parseProtocol(Data protocol) {
		int state = protocol.getState();
		switch (state) {
			case Data.FIRST_MSG: // 참가자 입장
				userName = protocol.getUserName();
				server.addThread(this);	
				protocol.setUserList(server.getUserList());
				break;
			case Data.LAST_MSG: // 참가자 퇴장
				server.removeThread(this);
				protocol.setUserList(server.getUserList());
				break;
			case Data.NORMAL_MSG:	// 보통 대화
			case Data.SINGLE_MSG:	// 귓속말
			case Data.WANGTTA_MSG: // 왕따
				break;
		}
		server.broadCastData(protocol);
	}

	public void sendData(Data protocol) {
		try {
			socketOOS.writeObject(protocol);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	public String getUserName() {
		return userName;
	}
}
