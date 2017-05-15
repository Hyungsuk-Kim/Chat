package chat.example6;

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import java.util.StringTokenizer;
/*  << 클라이언트에서 전송할 Protocol 정의 >>
 * 100: 대화 참여 / 대화명 전송  (예: "100|홍길동")
 * 200: 대화 종료 / 대화명 전송  (예: "200|이순신")
 * 300: 전체 전송 / 채팅 참가자 전원에게 메시지 전송 요청 (예: "300|안녕하세요")
 * 400: 귓속말 / 참가자 중 특정인에게만 전송 요청 (예: "400|히딩크|귓속말입니다")
 * 500: 왕따 / 참자가 중 특정인을 제외하고 전송 요청 (예: "500|부시맨|부시맨싫어요") */

public class ChatClientGUI extends Frame implements ActionListener, ItemListener {
	private static final long serialVersionUID = 1L;
	private Socket socket;
	private PrintWriter socketWriter;
	private BufferedReader socketReader;

	private TextArea taChatArea;
	private TextField tfMessage;
	private Button btnSend, btnConn, btnDisconn;
	private List lstUsers;
	private Choice choOption;

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
		launchFrame();
	}

	private void connect(String host, int port) {
		try {
			socket = new Socket(host, port);
			socketWriter = new PrintWriter(socket.getOutputStream(), true);
			socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			socketWriter.println("100|" + chatName);
			//socketWriter.println(chatName);

			new ClientThread().start();
			changeBtnState(true);		

			System.out.println("서버에 접속하였습니다.(" + host + ":" + port + ")");
			
		} catch (IOException e) {
			System.out.println("서버 접속 실패 : " + e.getMessage());
			System.exit(-1);
		}
	}
	
	private void disconnect() {
		if (socket != null) {
			try {
				socketWriter.println("200|" + chatName);
				
				System.out.println("서버 접속을 종료합니다.");
				socketWriter.close();	socketReader.close();
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
		lstUsers = new List(10, false);
		choOption = new Choice();
		choOption.add("방전체");
		choOption.add("귓속말");
		choOption.add("왕따");

		Panel pSouth = new Panel();
		pSouth.add(choOption);
		pSouth.add(tfMessage);
		pSouth.add(btnSend);
		pSouth.setBackground(Color.darkGray);

		Panel pNorth = new Panel();
		pNorth.add(btnConn);
		pNorth.add(btnDisconn);
		pNorth.setBackground(Color.orange);

		Panel pCenter = new Panel(new BorderLayout());
		Panel pCenterCenter = new Panel(new BorderLayout());
		Panel pCenterEast = new Panel(new BorderLayout());
		pCenterCenter.add(new Label("   대화 내용"), BorderLayout.NORTH);
		pCenterCenter.add(taChatArea, BorderLayout.CENTER);
		pCenterEast.add(new Label("접속자 리스트", Label.CENTER), BorderLayout.NORTH);
		pCenterEast.add(lstUsers, BorderLayout.CENTER);
		pCenter.add(pCenterCenter, BorderLayout.CENTER);
		pCenter.add(pCenterEast, BorderLayout.EAST);

		add(pSouth, BorderLayout.SOUTH);
		add(pCenter, BorderLayout.CENTER);
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
		lstUsers.addItemListener(this);
		choOption.addItemListener(this);

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

	private final int ALL = 0;
	private final int WHISPER = 1;
	private final int WANGTTA = 2;

	/** 선택된 기능에 따라 protocol 메시지를 만듬 */
	private String makeProtocol(String msg) {
		String protocol = null;
		String selectedUser = lstUsers.getSelectedItem();

		switch (choOption.getSelectedIndex()) {
			case ALL:
				protocol = "300|" + msg;
				break;
			case WHISPER:
				protocol = "400|" + selectedUser + "|" + msg;
				break;
			case WANGTTA:
				protocol = "500|" + selectedUser + "|" + msg;
				break;
		}
		return protocol;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source==btnConn) {
			tfHostName.setText(hostName);
			tfChatName.setText(chatName);
			Point point = this.getLocation();  // Point class 는 x,y 좌표를 설정
			conDialog.setLocation(point.x+150, point.y+150);
			conDialog.setVisible(true);
		} else if (source==btnDisconn) {
			disconnect();
			lstUsers.removeAll();
			taChatArea.append("서버와의 접속을 종료하였습니다.\n");
		} else if (source==btnOK) {
			this.hostName = tfHostName.getText().trim();
			this.chatName = tfChatName.getText().trim();
			conDialog.setVisible(false);
			connect(hostName, port);
		} else if (source==btnCancel) {
			conDialog.setVisible(false);
		} else if (source==tfMessage || source == btnSend) {
			// makeProtocol() 메소드를 사용하여 만든 프로토콜 메시지를 전송
			socketWriter.println(makeProtocol(tfMessage.getText()));
			tfMessage.setText("");
			tfMessage.requestFocus();
		}
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if ((choOption.getSelectedIndex()!=ALL) && (lstUsers.getSelectedIndex()==-1)) { 
			taChatArea.append("== 먼저 UserList에서 대상자를 선택해 주세요! ==\n");
			choOption.select(ALL);
		}
	}

	public static void main(String[] args) {
		new ChatClientGUI("localhost", 5432);
	}

	class ClientThread extends Thread {
		@Override
		public void run() {
			String broadCastMessage = null;
			StringTokenizer tokenizer = null;
			String protocolHeader = null;

			try {
				while ((broadCastMessage = socketReader.readLine()) != null) {
					tokenizer = new StringTokenizer(broadCastMessage, "|");
					protocolHeader = tokenizer.nextToken();
					if (protocolHeader.equals("100") || protocolHeader.equals("200")) {
						lstUsers.removeAll();
						while (tokenizer.hasMoreTokens()) {
							lstUsers.add(tokenizer.nextToken());
						}
					} else if (protocolHeader.equals("300")) {
						taChatArea.append(tokenizer.nextToken() + "\n");
					}
				}
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		}
	}
}	
