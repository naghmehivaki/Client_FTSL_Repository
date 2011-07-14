package ftsl;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.security.auth.login.FailedLoginException;

import util.Logger;

public class Session {

	int MAX_WAIT_TIME = 1000; // seconds
	boolean stopReading = false;
	boolean stopWriting = false;
	int DEFAULT_VALUE = 1000;
	static final int LOGGING_PERIOD = 20;
	int sleepTime = DEFAULT_VALUE;
	int MAX_BUFFER_SIZE = 1000;
	FTSL_Logger logger;
	Timer timer = new Timer();
	int lastRPID=0;
	int lastEnd=0;
	boolean isTransactional = false;
	

	/////////////////////////////////////////// Session Basic Info
	Socket socket;
	ObjectInputStream inputStream;
	ObjectOutputStream outputStream;
	String sessionID = "";

	// ///////////////////////////////////////// Packets Info
	int lastSentPacketID = 0;
	int eoTheLastSentMessage = 0;
	int lastRecievedPacketID = 0;
	int eoTheLastRecievedMessage = 0;
	Vector<FTSLMessage> sentBuffer = new Vector<FTSLMessage>();
	HashMap<Integer, FTSLMessage> receivedBuffer = new HashMap<Integer, FTSLMessage>();
	// ///////////////////////////////////////// Messages Info
	int sendMessageID = 0;
	Vector<MessageInfo> SentMessagesInfo = new Vector<MessageInfo>();

	/* ****************************** Constructor */
	public Session() {

	}
	
	public Session(String server, int port, boolean t, int time) {
		try {
			
			isTransactional=t;
			MAX_WAIT_TIME=time;

			sessionID = String.valueOf(System.currentTimeMillis())
					+ String.valueOf((new Random()).nextInt(10000));
			// Logger.log("Session Id "+ sessionID +
			// " is assigned to the session.");

			socket = new Socket(server, port);
			// Logger.log("Client session created new socket "+
			// socket.toString() + " to server "+server);

			outputStream = new ObjectOutputStream(socket.getOutputStream());
			outputStream.flush();
			inputStream = new ObjectInputStream(socket.getInputStream());

			writeSessionID();
			logger = new FTSL_Logger(sessionID);

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		logger.logSession(this);
		timer.scheduleAtFixedRate(new logTask(this, logger),
				LOGGING_PERIOD * 1000, LOGGING_PERIOD * 1000);
	}
	
	public Session(String server, int port, boolean t) {
		try {
			
			isTransactional=t;

			sessionID = String.valueOf(System.currentTimeMillis())
					+ String.valueOf((new Random()).nextInt(10000));
			// Logger.log("Session Id "+ sessionID +
			// " is assigned to the session.");

			socket = new Socket(server, port);
			// Logger.log("Client session created new socket "+
			// socket.toString() + " to server "+server);

			outputStream = new ObjectOutputStream(socket.getOutputStream());
			outputStream.flush();
			inputStream = new ObjectInputStream(socket.getInputStream());

			writeSessionID();
			logger = new FTSL_Logger(sessionID);

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		logger.logSession(this);
		timer.scheduleAtFixedRate(new logTask(this, logger),
				LOGGING_PERIOD * 1000, LOGGING_PERIOD * 1000);
	}
	
	public Session(String server, int port, int time) {
		try {
			
			MAX_WAIT_TIME=time;

			sessionID = String.valueOf(System.currentTimeMillis())
					+ String.valueOf((new Random()).nextInt(10000));
			// Logger.log("Session Id "+ sessionID +
			// " is assigned to the session.");

			socket = new Socket(server, port);
			// Logger.log("Client session created new socket "+
			// socket.toString() + " to server "+server);

			outputStream = new ObjectOutputStream(socket.getOutputStream());
			outputStream.flush();
			inputStream = new ObjectInputStream(socket.getInputStream());

			writeSessionID();
			logger = new FTSL_Logger(sessionID);

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		logger.logSession(this);
		timer.scheduleAtFixedRate(new logTask(this, logger),
				LOGGING_PERIOD * 1000, LOGGING_PERIOD * 1000);
	}
	
	public Session(String server, int port) {
		try {

			sessionID = String.valueOf(System.currentTimeMillis())
					+ String.valueOf((new Random()).nextInt(10000));
			// Logger.log("Session Id "+ sessionID +
			// " is assigned to the session.");

			socket = new Socket(server, port);
			// Logger.log("Client session created new socket "+
			// socket.toString() + " to server "+server);

			outputStream = new ObjectOutputStream(socket.getOutputStream());
			outputStream.flush();
			inputStream = new ObjectInputStream(socket.getInputStream());

			writeSessionID();
			logger = new FTSL_Logger(sessionID);

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		logger.logSession(this);
		timer.scheduleAtFixedRate(new logTask(this, logger),
				LOGGING_PERIOD * 1000, LOGGING_PERIOD * 1000);
	}

	/* ****************************** setters and getters */	
	public void setTimeut(int time){
		MAX_WAIT_TIME=time;
	}
	public boolean isTransactional() {
		return isTransactional;
	}

	public void setTransactional(boolean isTransactional) {
		this.isTransactional = isTransactional;
	}

	public int getEoTheLastSentMessage() {
		return eoTheLastSentMessage;
	}

	public void setEoTheLastSentMessage(int e) {
		this.eoTheLastSentMessage = e;
	}

	public int getEoTheLastRecievedMessage() {
		return eoTheLastRecievedMessage;
	}

	public void setEoTheLastRecievedMessage(int e) {
		this.eoTheLastRecievedMessage = e;
	}

	public Socket getSocket() {
		return socket;
	}

	public HashMap<Integer, FTSLMessage> getReceivedBuffer() {
		return receivedBuffer;
	}

	public void setReceivedBuffer(HashMap<Integer, FTSLMessage> receivedBuffer) {
		this.receivedBuffer = receivedBuffer;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
		try {
			this.inputStream = new ObjectInputStream(socket.getInputStream());
			this.outputStream = new ObjectOutputStream(socket.getOutputStream());

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public synchronized void setSendMessageID(int sendMessageID) {
		this.sendMessageID = sendMessageID;
	}

	public String getSessionID() {
		return sessionID;
	}

	public void setSessionID(String sessionID) {
		this.sessionID = sessionID;

	}

	public int getLastSentPacketID() {
		return lastSentPacketID;
	}

	public synchronized void setLastSentPacketID(int id) {
		this.lastSentPacketID = id;

	}

	public int getLastRecievedPacketID() {
		return this.lastRecievedPacketID;
	}

	public synchronized void setLastRecievedPacket(int id) {
		lastRecievedPacketID = id;

	}

	public Vector<FTSLMessage> getSentBuffer() {
		return sentBuffer;
	}

	public synchronized void setSentBuffer(Vector<FTSLMessage> sentBuffer) {
		this.sentBuffer = sentBuffer;
	}

	public int getSendMessageID() {
		return sendMessageID;
	}

	public synchronized void setLastSentMessageID(int id) {
		this.sendMessageID = id;

	}

	public Vector<MessageInfo> getSentMessagesInfo() {
		return SentMessagesInfo;
	}

	public void setSentMessagesInfo(Vector<MessageInfo> sentMessagesInfo) {
		SentMessagesInfo = sentMessagesInfo;
	}

	public void setLastRecievedPacketID(int id) {
		lastRecievedPacketID = id;
	}

	// ////////////////////////////////////////////// Operations

	public int increaseLastSentPacketID() {
		lastSentPacketID++;
		logger.logSessionInfo("LastSentPacketID", lastSentPacketID);
		return lastSentPacketID;
	}

	public void increaseSendMessageID() {
		sendMessageID++;
		logger.logSessionInfo("SendMessageID", sendMessageID);
	}

	public int increaseLastReceivedPacketID() {
		lastRecievedPacketID++;
		logger.logSessionInfo("LastRecievedPacketID", lastRecievedPacketID);
		return lastRecievedPacketID;
	}

	/* *************************** */

	public void keepSentPacket(FTSLMessage packet) {
		sentBuffer.add(packet);
		logger.logSentMessage(packet);
	}

	public void addSentMessage(FTSLMessage message) {
		sentBuffer.add(message);
	}

	public void addReceivedMessage(int id, FTSLMessage message) {
		receivedBuffer.put(id, message);
	}

	/* **************************** */

	public void sendFTSLRequest() {
		FTSLHeader header = new FTSLHeader(sessionID, "FTSL_REQUEST", 0,
				lastRecievedPacketID);

		FTSLMessage packet = new FTSLMessage(null, header);
		byte[] buffer = packet.toByte_();

		try {

			outputStream.write(buffer, 0, buffer.length);
			outputStream.flush();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void sendFTSLReply() {
		FTSLHeader header = new FTSLHeader(sessionID, "FTSL_REPLY", 0,
				lastRecievedPacketID);

		FTSLMessage packet = new FTSLMessage(null, header);
		byte[] buffer = packet.toByte_();

		try {

			outputStream.write(buffer, 0, buffer.length);
			outputStream.flush();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/* **************************** */

	public void addMessageInfo() {

		MessageInfo info = new MessageInfo();
		if (SentMessagesInfo.size()==0)
			info.setStart(1);

		else
			info.setStart(lastEnd + 1);

		info.setId(sendMessageID);
		info.setEnd(lastSentPacketID);
		lastEnd=lastSentPacketID;
		SentMessagesInfo.add(info);
		logger.logMessageInfo(info);

	}

	public void addMessageInfo(MessageInfo info) {
		SentMessagesInfo.add(info);

	}

	/* **************************** */

	public int removeDeliveredMessages(int rpid) {
		int id = rpid;

		if (rpid > lastRPID) {
			lastRPID=rpid;
			int index = 0;
			
			/*
			 * It finds the id of the message is less or 
			 * equals rpid but it is also the end of an 
			 * application layer message.
			 */
			
			while (index < SentMessagesInfo.size()) {
				MessageInfo info = SentMessagesInfo.get(index);
				
				if (info.getEnd() <= rpid) {
					id = info.getEnd();
					SentMessagesInfo.remove(index);
				} else
					index = SentMessagesInfo.size();
			}
			
			index = 0;
			while (index < sentBuffer.size()) {
				FTSLMessage message = sentBuffer.get(index);
				if (message.getHeader().getPID() <= id) {
					sentBuffer.remove(index);

				} else
					index = sentBuffer.size();
			}
		}
		return id;

	}

	/* **************************** */

	public void updateSocket(Socket s) {
		this.socket = s;
		logger.logSessionInfo("Socket", socket);

		try {
			inputStream = new ObjectInputStream(s.getInputStream());
			outputStream = new ObjectOutputStream(s.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/* ************************************ */
	
	public MessageHandler read(byte buffer[], int pos, int len) {

		while (stopReading == true){
			System.out.println(":(((((((((((( 0");
		}

		int expectedID = lastRecievedPacketID + 1;

		if (receivedBuffer.containsKey(expectedID)) {
			FTSLMessage message= receivedBuffer.get(expectedID);
			byte[] tempBuffer = message.toByte_();
			processInputPacket(tempBuffer);
		
			for (int i = 0; i < tempBuffer.length; i++)
				buffer[pos + i] = tempBuffer[i];

			MessageHandler msgHandler=new MessageHandler(this, expectedID,message.getProperties());
			return msgHandler;

		} else {
			int read = 0;
			byte[] packet = new byte[len];

			while (read == 0) {
				try {
					read = inputStream.read(packet);
				} catch (IOException e) {
					read=0;
				}
			}

			int test = 1;
			MessageProperties msgProperties=new MessageProperties();
			while (read != -1 & test == 1) {
				
				if (read !=0)
					msgProperties = processInputPacket(packet);

				if (msgProperties.getSize() == 0) {
					try {
						while (stopReading == true){
							System.out.println(":(((((((((((( 0");
						}
						read = inputStream.read(packet);
					} catch (IOException e) {

						e.printStackTrace();
						read=0;
					}
				} else {
					test = 0;
					read = msgProperties.getSize();
					for (int i = 0; i < read; i++)
						buffer[pos + i] = packet[i];
				}
				if (read == -1)
					read=0;
			}
			Logger.log("####### returning it");
			MessageHandler msgHandler=new MessageHandler(this, expectedID, msgProperties);
			return msgHandler;
	}
	}
	
	public MessageHandler read(byte buffer[], int pos, int len, int time) {

		MAX_WAIT_TIME=time;
		while (stopReading == true){
			System.out.println(":(((((((((((( 0");
		}

		int expectedID = lastRecievedPacketID + 1;

		if (receivedBuffer.containsKey(expectedID)) {
			FTSLMessage message=receivedBuffer.get(expectedID);
			byte[] tempBuffer = message.toByte_();
			processInputPacket(tempBuffer);
		
			for (int i = 0; i < tempBuffer.length; i++)
				buffer[pos + i] = tempBuffer[i];

			MessageHandler msgHandler=new MessageHandler(this, expectedID,message.getProperties());
			return msgHandler;


		} else {
			int read = 0;
			byte[] packet = new byte[len];

			while (read == 0) {
				try {
					read = inputStream.read(packet);
				} catch (IOException e) {
					read=0;
				}
			}

			int test = 1;
			MessageProperties msgProperties=new MessageProperties();
			while (read != -1 & test == 1) {
				
				if (read !=0)
					msgProperties = processInputPacket(packet);

				if (msgProperties.getSize() == 0) {
					try {
						while (stopReading == true){
							System.out.println(":(((((((((((( 0");
						}
						read = inputStream.read(packet);
					} catch (IOException e) {

						e.printStackTrace();
						read=0;
					}
				} else {
					test = 0;
					read = msgProperties.getSize();
					for (int i = 0; i < read; i++)
						buffer[pos + i] = packet[i];
				}
				if (read == -1)
					read=0;
			}
			Logger.log("####### returning it");
			MessageHandler msgHandler=new MessageHandler(this, expectedID, msgProperties);
			return msgHandler;
		}
	}
	
	public void confirm(){
		eoTheLastRecievedMessage=lastRecievedPacketID;
	}
	public void confirm(int messageID){
		eoTheLastRecievedMessage=lastRecievedPacketID;
	}
	
	public MessageProperties processInputPacket(byte buffer[]) {

		MessageProperties msgProperties=new MessageProperties();
		String packet = new String(buffer);
		if (!packet.startsWith(FTSLHeader.protocol))
			return msgProperties;

		int result = processFTSLHeader(buffer);

		if (result == 0) {
			return msgProperties;

		} else {

			int index = packet.indexOf("\n");
			String str= packet.substring(0,index);
			index= str.indexOf("|");
			String h = str.substring(0, index-1);
			String p = str.substring(index+2);
			String b = packet.substring(index + 1);

			byte[] tempBody = b.getBytes();
			for (int i = 0; i < tempBody.length; i++)
				buffer[i] = tempBody[i];

			msgProperties = MessageProperties.valueOf_(p);
			return msgProperties;
		}
	}

	public int processFTSLHeader(byte[] buffer) {

		String packet = new String(buffer);
		FTSLMessage ftslMessage= FTSLMessage.valueOf_(buffer);
		FTSLHeader header=ftslMessage.getHeader();
		
		String flag = header.getFLAG();
		String sid = header.getSID();
		int pid = header.getPID();
		int rpid = header.getrPID();
	
		if (flag.compareTo("APP") == 0) {

			int expectedPID = lastRecievedPacketID + 1;
			if (pid == expectedPID) {

				// it is the right message, no need to check anything else
				increaseLastReceivedPacketID();
				removeDeliveredMessages(rpid);
				return 1;

			} else if (pid<expectedPID){
				return 0;
				
			}else {
				receivedBuffer.put(pid, FTSLMessage.valueOf_(packet));
				if (!receivedBuffer.containsKey(pid - 1)) {
					FTSLHeader h = new FTSLHeader(sid, "NAK", pid,
							lastRecievedPacketID);
					FTSLMessage ftslPacket = new FTSLMessage(null, h);

					try {

						outputStream.write(ftslPacket.toByte_());
						outputStream.flush();

					} catch (IOException e) {
						e.printStackTrace();
						if (socket.isConnected()==false)
							HandleFailure();
					}
				}
				return 0;
			}

		} else if (flag.compareTo("REQ") == 0) {

			FTSLHeader h = new FTSLHeader(sid, "REP", 0, lastRecievedPacketID);

			FTSLMessage ftslPacket = new FTSLMessage(null, h);

			try {
				outputStream.write(ftslPacket.toByte_());
				outputStream.flush();

			} catch (IOException e) {
				HandleFailure();
			}
			return 0;

		} else if (flag.compareTo("REP") == 0) {

			removeDeliveredMessages(rpid);

			int index = 0;
			while (index < sentBuffer.size()) {
				FTSLMessage pkt = sentBuffer.get(index);
				if (pkt.getHeader().getPID() > rpid) {
					try {
						outputStream.write(pkt.toByte_());
						outputStream.flush();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				index++;
			}
			return 0;

		} else if (flag.compareTo("NTF") == 0) {
	
			System.out.println("####### client received a notification from the server");
			removeDeliveredMessages(rpid);
			//Logger.log("rpid: "+rpid);
			int index = 0;
			
			while (index < sentBuffer.size()) {
				FTSLMessage pkt = sentBuffer.get(index);
				if (pkt.getHeader().getPID() > rpid) {
					try {
						//Logger.log("######## Client is re sending some messages"+pkt.getHeader().getPID());
						outputStream.write(pkt.toByte_());
						outputStream.flush();	
					} catch (IOException e) {
						stopWriting=true;
						stopReading=true;
						HandleFailure();
						stopWriting=false;
						break;
					}
				}
				index++;
			}

			return 0;

		} else if (flag.compareTo("ACK") == 0) {

			int id = removeDeliveredMessages(rpid);
			return 0;

		} else if (flag.compareTo("NAK") == 0) {
			removeDeliveredMessages(rpid);
			// all messages are lost should be sent again
			int index = 0;
			while (index < sentBuffer.size()) {
				FTSLMessage pkt = sentBuffer.get(index);
				int id = pkt.getHeader().getPID();
				if (id > rpid & id < pid) {
					try {
						outputStream.write(pkt.toByte_());
						outputStream.flush();

					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				index++;
			}

			return 0;
		}
		return 0;
	}

	/* **************************** */

	public void write(byte[] buffer) {

		while (stopWriting == true);
		buffer = processOutputPacket(buffer);
		try {
			outputStream.write(buffer);
			outputStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
			stopReading = true;
			stopWriting = true;
			HandleFailure();
			stopWriting = false;
		}
	}

	public void write(byte[] buffer, boolean endOfMsg) {

		while (stopWriting == true);
	
		buffer = processOutputPacket(buffer, endOfMsg);
		try {
			outputStream.write(buffer);
			outputStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
			stopReading = true;
			stopWriting = true;
			HandleFailure();
			stopWriting = false;
		}
		if (endOfMsg) {
			eoTheLastSentMessage=lastSentPacketID;
			addMessageInfo();
			increaseSendMessageID();
		}
	}
	public void write(byte[] buffer, boolean endOfMsg, int time) {

		MAX_WAIT_TIME=time;
		while (stopWriting == true);
	
		buffer = processOutputPacket(buffer, endOfMsg);
		try {
			outputStream.write(buffer);
			outputStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
			stopReading = true;
			stopWriting = true;
			HandleFailure();
			stopWriting = false;
		}
		if (endOfMsg) {
			addMessageInfo();
 		}
	}
	
	public void write(byte[] buffer, int time) {

		MAX_WAIT_TIME=time;
		
		while (stopWriting == true);
		buffer = processOutputPacket(buffer);
		try {
			outputStream.write(buffer);
			outputStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
			stopReading = true;
			stopWriting = true;
			HandleFailure();
			stopWriting = false;
		}
	}


	public void writeSessionID() {

		// this is to send the session ID to the server
		FTSLHeader header = new FTSLHeader();
		header.setSID(sessionID);

		FTSLMessage message = new FTSLMessage(null, header);
		message.toByte_();
		
		try {

			outputStream.write(message.toByte_());
			outputStream.flush();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public byte[] processOutputPacket(byte[] packet) {
		
		increaseLastSentPacketID();
		FTSLHeader header = new FTSLHeader(sessionID, "APP", lastSentPacketID,
				lastRecievedPacketID);
		MessageProperties properties= new MessageProperties(packet.length);
		FTSLMessage pkt = new FTSLMessage(header, properties, packet);
		byte[] buffer = pkt.toByte_();

		keepSentPacket(pkt);
		return buffer; 
	}
	public byte[] processOutputPacket(byte[] packet, boolean endOfMsg) {

		increaseLastSentPacketID();
		FTSLHeader header = new FTSLHeader(sessionID, "APP", lastSentPacketID,
				lastRecievedPacketID);
		MessageProperties properties= new MessageProperties(packet.length, endOfMsg);
		FTSLMessage pkt = new FTSLMessage(header, properties, packet);
		byte[] buffer = pkt.toByte_();

		keepSentPacket(pkt);

		return buffer;

	}

	public void eom() { // the end of a stream of the message
		addMessageInfo();
		increaseSendMessageID();
	}

	/* ********************************* */

	public void HandleFailure() {

		System.out.println("failure handling");
		boolean done = false;

		try {
			InetAddress address = socket.getInetAddress();
			int port = socket.getPort();
			socket.close();
			socket = new Socket(address, port);
			outputStream = new ObjectOutputStream(socket.getOutputStream());
			outputStream.flush();
			inputStream = new ObjectInputStream(socket.getInputStream());

		} catch (IOException e2) {
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			sleepTime = sleepTime * 2;
			if (sleepTime > MAX_WAIT_TIME * 1000)
				System.exit(1);

			HandleFailure();
			done = true;
		}

		if (done==false) {
			FTSLHeader header = new FTSLHeader(sessionID, "NTF", 0,
					lastRecievedPacketID);

			FTSLMessage packet = new FTSLMessage(null, header);
			byte[] buffer = packet.toByte_();
			
			try {

				outputStream.write(buffer, 0, buffer.length);
				outputStream.flush();
				logger.logSessionInfo("Socket", socket);

				/*
				 * notification should be read here to re-send all
				 * the lost messages before sending any new message. 
				 */
				
				inputStream.read(buffer);
				stopReading=false;
				processFTSLHeader(buffer);

			} catch (IOException e) {
				e.printStackTrace();
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				sleepTime = sleepTime * 2;
				if (sleepTime > MAX_WAIT_TIME * 1000)
					System.exit(1);
				HandleFailure();
			}
		}

		sleepTime = DEFAULT_VALUE;

	}

	/* ********************************* */
	public int close() {
		try {
			inputStream.close();
			outputStream.close();
			socket.close();
			return 1;
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
	}

	/* ********************************* */
	class logTask extends TimerTask {
		Session session;
		FTSL_Logger logger;

		public logTask(Session s, FTSL_Logger l) {
			session = s;
			logger = l;
		}
		public void run() {
			logger.log(session);
		}
	}
	
	
	
//	public void commit(){
//	
//}
//
//public void commit(int messageID){
//	
//}
//
//public void abort(){
//	
//}
//public void abort(int messageID){
//	
//}


}
