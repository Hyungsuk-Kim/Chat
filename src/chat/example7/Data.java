package chat.example7;

import java.io.*;
import java.util.*;

/*  Server와 Client 사이에서 주고 받을 객체로 채팅 정보가 저장된다. */
public class Data implements Serializable {
	private static final long serialVersionUID = 1L;
	private int state;					// Client의 상태를 나타냄  ex) 처음 접속, 접속 종료 등
	private String userName;		// 메시지를 보내는 Client의 이름
	private String targetName;	// 귓속말이나 왕따의 대상 이름
	private String message;		// Client가 보내는 메시지
	private List<String> userList;			// 현재 접속자 리스트를 저장

	/* Client의 상태를 지정하는 상수  */
	public static final int FIRST_MSG = 100;		// 처음 접속
	public static final int LAST_MSG = 200;		// 접속 종료
	public static final int NORMAL_MSG = 300;	// 보통 대화
	public static final int SINGLE_MSG = 400;	// 귓속말
	public static final int WANGTTA_MSG = 500;	// 왕따

	/* FIRST_MSG, LAST_MSG 용 생성자	 */
	public Data (int state, String userName) {		
		this.state = state;
		this.userName = userName;
	}
	/* NORMAL_MSG 용 생성자 */
	public Data (int state, String userName, String message) {	
		this(state, userName);
		this.message = message;
	}
	/* SINGLE_MSG, WANGTTA_MSG 용 생성자 */
	public Data (int state, String userName, String targetName, String message) {
		this(state, userName, message);
		this.targetName = targetName;
	}

	public int getState() {
		return state;
	}

	public String getUserName() {
		return userName;
	}

	public String getTargetName() {
		return targetName;
	}

	public String getMessage() {
		return message;
	}

	public List<String> getUserList() {
		return userList;
	}

	public void setUserList(List<String> userList) {
		this.userList = userList;
	}

	@Override
	public String toString() {
		return "Data: " + state + "|" + userName + "|" 
				+ targetName + "|" + message + "|" + userList; 
	}
}