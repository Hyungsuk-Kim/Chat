package chat.example4;

import java.awt.*;
import java.awt.event.*;
import java.net.*;

import java.io.*;

public class ChatClientGUIEx extends Frame implements ActionListener {
	private static final long serialVersionUID = 1L;
	private Socket socket;
	private PrintWriter socketWriter;
	private BufferedReader socketReader;

	private TextArea taChatArea;
	private TextField tfMessage;
	private Button btnSend;
	private Panel sendPanel;

	public ChatClientGUIEx(String host, int port) {
		super("허접 채팅방"); 
		connect(host, port);
		launchFrame();
	}

	private void connect(String host, int port) {
		try {
			socket = new Socket(host, port);
			socketWriter = new PrintWriter(socket.getOutputStream(), true);
			socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			new ClientThread().start();
			System.out.println("서버에 접속하였습니다.(" + host + ":" + port + ")");
		} catch (IOException e) {
			System.out.println("서버 접속 실패 : " + e.getMessage());
			System.exit(-1);
		}
	}
	
	private void disconnect() {
		if (socket != null) {
			try {
				System.out.println("서버 접속을 종료합니다.");
				socketWriter.close();	socketReader.close();
				socket.close();
			} catch(IOException ioe) {
				System.out.println(ioe.getMessage());
			}
		}
	}

	private void launchFrame() {
		taChatArea = new TextArea();
		tfMessage = new TextField(30);
		btnSend = new Button("SEND");
		sendPanel = new Panel();
		
		sendPanel.add(tfMessage);
		sendPanel.add(btnSend);
		
		this.add(taChatArea, BorderLayout.CENTER);
		this.add(sendPanel, BorderLayout.SOUTH);
		
		this.setVisible(true);
		this.setSize(300, 200);
		
		btnSend.addActionListener(this);
		this.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e){
				disconnect();
				dispose();
			}

		});
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnSend){
			socketWriter.println(tfMessage.getText());
			tfMessage.setText("");
		}
	}

	public static void main(String[] args) {
		new ChatClientGUIEx("localhost", 5432);
	}

	class ClientThread extends Thread {
		@Override
		public void run() {
			String broadCastMessage = null;
			try {
				while ((broadCastMessage = socketReader.readLine()) != null) {
					taChatArea.append(broadCastMessage + "\n");
				}
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		}
	}
}	