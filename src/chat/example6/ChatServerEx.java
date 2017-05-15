package chat.example6;

import java.net.*;
import java.io.*;
import java.util.*;
/*  << 서버에서 전송할 Protocol 정의 >>
 * 100: 새로운 참가자 입장을 알림 / 참가자 리스트 갱신 요청 (예: "100|홍길동|히딩크|쿠엘류")
 * 200: 특정 참가자 퇴장을 알림 / 참가자 리스트 갱신 요청 (예: "200|홍길동|쿠엘류")
 * 300: 대화 메시지 전송 / (예: "300|안녕하세요!")     */

public class ChatServerEx {
	private ServerSocket serverSocket;
	private List<ServerThread> threadPool;

	public ChatServerEx(int port) {
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
	
	/* 참가자 입장이나 퇴장시 참가자 전원에게 접속 리스트 전송
	 *  (예) 100|홍길동|히딩크|쿠엘류  or 200|홍길동|쿠엘류   */
	public void broadCastUserList(int header) {
		StringBuffer userList = new StringBuffer();
		userList.append(header);
		for (ServerThread serverThread : threadPool) {
			userList.append("|" + serverThread.getUserName());
		}
		broadCast(userList.toString());
	}
	
	/** 채팅 참가자 전원에게 메시지 전송  */
	public void broadCast(String message) {
		for (ServerThread serverThread : threadPool) {
			serverThread.sendMessage(message);
		}
	}
	
	/** 참가자 중 특정인에게만 전송  */
	public void singleCast(String targetUser, String message) {
		for (ServerThread serverThread : threadPool) {			
			if (serverThread.getUserName().equals(targetUser)) {
				serverThread.sendMessage(message);
			}
		}
	}
	
	/** 참가자 중 특정인을 제외하고 전송  */
	public void wangttaCast(String targetUser, String message) {
		for (ServerThread serverThread : threadPool) {
			if (!(serverThread.getUserName().equals(targetUser))) {
				serverThread.sendMessage(message);
			}
		}
	}

	public void addThread(ServerThread aThread) {
		threadPool.add(aThread);
	}

	public void removeThread(ServerThread aThread) {
		threadPool.remove(aThread);
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
	private String userName;	

	public ServerThread(ChatServerEx server, Socket socket) {
		this.server = server;
		this.socket = socket;
	}

	@Override
	public void run() {
		String message = null;
		try {
			socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			socketWriter = new PrintWriter(socket.getOutputStream(), true);
			
			while ((message = socketReader.readLine()) != null){
				parseProtocol(message);
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

	private void parseProtocol(String protocol) {
		StringTokenizer tokenizer = new StringTokenizer(protocol, "|");
		int protocolHeader = Integer.parseInt(tokenizer.nextToken());
		String message = null;
		String targetUser = null;

		switch (protocolHeader) {
			case 100: // 새 참가자 입장: UserList 갱신(broadCastUserList() 호출)
				userName = tokenizer.nextToken();
				server.addThread(this);
				server.broadCastUserList(100);
				server.broadCast("300|▣▣" + userName + "▣▣님이 입장하셨습니다.");
				break;
			case 200: // 기존 참가자 퇴장: UserList 갱신(broadCastUserList() 호출)
				userName = tokenizer.nextToken();
				server.removeThread(this);
				server.broadCastUserList(200);
				server.broadCast("300|▣▣" + userName + "▣▣님이 퇴장하셨습니다.");
				break;
			case 300: // 채팅 참가자 전원에게 메시지 전송(broadCast() 호출)
				message = tokenizer.nextToken();
				server.broadCast("300|[" + userName + "]님의 메시지:" + message);
				break;
			case 400: // 귓속말: 특정인에게만 전송(singleCast() 호출)
				targetUser = tokenizer.nextToken();
				message = tokenizer.nextToken();
				server.singleCast(targetUser, "300|[" + userName + "]님의 귓속말: " + message);
				server.singleCast(userName, "300|[" + userName + "]님의 귓속말: " + message);
				break;
			case 500: // 왕따: 특정인을 제외하고 전송(wangttaCase() 호출)
				targetUser = tokenizer.nextToken();
				message = tokenizer.nextToken();
				server.wangttaCast(targetUser, "300|[" + userName + "]님의 [" + targetUser +"]님을 제외한 메시지: " + message);
				break;
		}
	}

	public void sendMessage(String message) {
		socketWriter.println(message);
	}

	public String getUserName() {
		return userName;
	}
}
