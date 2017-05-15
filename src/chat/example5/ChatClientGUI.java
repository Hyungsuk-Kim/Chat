package chat.example5;

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;

public class ChatClientGUI extends Frame implements ActionListener {
	private static final long serialVersionUID = 1L;
	private Socket socket;
	private PrintWriter socketWriter;
	private BufferedReader socketReader;

	private TextArea taChatArea;
	private TextField tfMessage;
	private Button btnSend, btnConn, btnDisconn;

	// For Dialog
	private Dialog conDialog;
	private TextField tfHostName, tfChatName;
	private Button btnOK, btnCancel;

	private String hostName;
	private int port;
	private String chatName;


	public ChatClientGUI(String host, int port) {
		super("허접 채팅방"); 
		this.hostName = host;
		this.port = port;
		//connect(host, port);
		launchFrame();
	}

	private void connect(String host, int port) {
		try {
			socket = new Socket(host, port);
			socketWriter = new PrintWriter(socket.getOutputStream(), true);
			socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			socketWriter.println(chatName);	//
			new ClientThread().start();
			changeBtnState(true);		//

			System.out.println("서버에 접속하였습니다.(" + host + ":" + port + ")");
		} catch (IOException e) {
			System.out.println("서버 접속 실패 : " + e.getMessage());
			System.exit(-1);
		}
	}
	
	private void disconnect() {
		if(socket != null) {
			try {
				System.out.println("서버 접속을 종료합니다.");
				socketWriter.close();	
				socketReader.close();
				socket.close();
				changeBtnState(false);
			} catch(IOException ioe) {
				System.out.println(ioe.getMessage());
			}
		}
	}

	private void launchFrame() {
		taChatArea = new TextArea("", 10, 40);
		tfMessage = new TextField("", 40);
		btnSend = new Button("보내기");
		btnConn = new Button("서버연결");
		btnDisconn = new Button("연결종료");
		
		Panel pSouth = new Panel();
		pSouth.add(tfMessage);
		pSouth.add(btnSend);
		pSouth.setBackground(Color.darkGray);

		Panel pNorth = new Panel();
		pNorth.add(btnConn);
		pNorth.add(btnDisconn);
		pNorth.setBackground(Color.orange);

		Panel pCenter = new Panel(new BorderLayout());
		pCenter.add(new Label("   대화 내용"), BorderLayout.NORTH);
		pCenter.add(taChatArea, BorderLayout.CENTER);

		add(pCenter, BorderLayout.CENTER);
		add(pSouth, BorderLayout.SOUTH);
		add(pNorth, BorderLayout.NORTH);
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				disconnect();
				System.exit(0);
			}
		});

		tfMessage.addActionListener(this);
		btnSend.addActionListener(this);
		btnConn.addActionListener(this);
		btnDisconn.addActionListener(this);

		setSize(500, 350);
	    Dimension dm = this.getToolkit().getScreenSize();
		int x = dm.width/2 - this.getWidth()/2;
		int y = dm.height/2 - this.getHeight()/2;
	    setLocation(x, y);
		setResizable(false);
		setVisible(true);

		taChatArea.setEditable(false);
		tfMessage.requestFocus();
		changeBtnState(false);

		launchDialog();
	}

	public void launchDialog() {
		conDialog = new Dialog(this, "연결설정", true);

		Panel pLabel = new Panel(new GridLayout(2, 1));     
		Panel pTextField = new Panel(new GridLayout(2, 1));
		Panel pCenter = new Panel();
		Panel pSouth = new Panel();

		Label lbHostName = new Label("호스트명", Label.LEFT);
		tfHostName = new TextField("localhost", 20);
		Label lbChatName = new Label("대화명", Label.LEFT);
		tfChatName = new TextField(20);
				
		btnOK = new Button("확인");
		btnCancel = new Button("취소");
		
		pLabel.add(lbHostName);
		pLabel.add(lbChatName);
		pTextField.add(tfHostName);
		pTextField.add(tfChatName);
		pCenter.add(pLabel);
		pCenter.add(pTextField);
		pSouth.add(btnOK);
		pSouth.add(btnCancel);
		conDialog.add(pCenter, BorderLayout.CENTER);
		conDialog.add(pSouth, BorderLayout.SOUTH);	

		conDialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				conDialog.setVisible(false);
			}
		});
		
		btnOK.addActionListener(this);
		btnCancel.addActionListener(this);

		Point point = this.getLocation();  //Point class 는 x,y좌표를 설정
		conDialog.setLocation(point.x+130, point.y+120);

		conDialog.pack();
		conDialog.setVisible(true);
		tfHostName.requestFocus();
	}

	private void changeBtnState(boolean conn) {
		btnConn.setEnabled(!conn);
		btnDisconn.setEnabled(conn);
		btnSend.setEnabled(conn);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source==btnConn) {
			tfHostName.setText(hostName);
			tfChatName.setText(chatName);
			Point point = this.getLocation();  //Point class 는 x,y좌표를 설정
			conDialog.setLocation(point.x+150, point.y+150);
			conDialog.setVisible(true);
		} else if (source==btnDisconn) {
			disconnect();
			taChatArea.append("서버와의 접속을 종료하였습니다.\n");
		} else if (source==btnOK) {
			this.hostName = tfHostName.getText().trim();
			this.chatName = tfChatName.getText().trim();
			conDialog.setVisible(false);
			connect(hostName, port);
		} else if (source==btnCancel) {
			conDialog.setVisible(false);
		} else if (source==tfMessage || source == btnSend) {
			socketWriter.println(tfMessage.getText());
			tfMessage.setText("");
			tfMessage.requestFocus();
		}
	}

	public static void main(String[] args) {
		new ChatClientGUI("localhost", 5432);
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