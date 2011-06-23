package ftsl;

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
import util.Logger;

public class Session {

	boolean stopWriting = true;
	int DEFAULT_VALUE = 1000;
	static final int LOGGING_PERIOD = 10;
	int sleepTime = DEFAULT_VALUE;
	int MAX_BUFFER_SIZE = 1000;
	FTSL_Logger logger;
	Timer timer = new Timer();

	
	/////////////////////////////////////////// Session Basic Info
	Socket socket;
	ObjectInputStream inputStream;
	ObjectOutputStream outputStream;
	String sessionID = "";
	
	/////////////////////////////////////////// Packets Info
	int lastSentPacketID = 0;
	int lastRecievedPacketID = 0;
	Vector<FTSLMessage> sentBuffer = new Vector<FTSLMessage>();
	HashMap<Integer, String> receivedBuffer = new HashMap<Integer, String>();
	// ///////////////////////////////////////// Messages Info
	int sendMessageID = 1;
	Vector<MessageInfo> SentMessagesInfo = new Vector<MessageInfo>();

	
	/* ****************************** Constructor */
	public Session() {

	}

	public Session(String server, int port) {
		try {

			Random rand = new Random();
			
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

			write();
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

	public Socket getSocket() {
		return socket;
	}

	public HashMap<Integer, String> getReceivedBuffer() {
		return receivedBuffer;
	}

	public void setReceivedBuffer(HashMap<Integer, String> receivedBuffer) {
		this.receivedBuffer = receivedBuffer;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
		try {
			this.inputStream = new ObjectInputStream(socket.getInputStream());
			this.outputStream = new ObjectOutputStream(socket.getOutputStream());

		} catch (IOException e) {
			// TODO Auto-generated catch block
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

	public void setLastRecievedPacketID(int lastRecievedPacketID) {
		lastRecievedPacketID = lastRecievedPacketID;
	}

	// ////////////////////////////////////////////// Operations

	public int increaseLastSentPacketID() {
		lastSentPacketID++;
		logger.logSessionInfo("LastSentPacketID", lastSentPacketID);
		return lastSentPacketID;
	}

	public int increaseSendMessageID() {
		sendMessageID++;
		logger.logSessionInfo("SendMessageID", sendMessageID);
		return sendMessageID;
	}
	
	public int increaseLastReceivedPacketID() {
		lastRecievedPacketID++;
		logger.logSessionInfo("LastRecievedPacketID", lastRecievedPacketID);
		return lastRecievedPacketID;
	}

	/* *************************** */

	public void keepSentPacket(int id, FTSLMessage packet) {
		sentBuffer.add(packet);
		logger.logSentMessage(packet);
	}

	public void addSentMessage(FTSLMessage message) {
		sentBuffer.add(message);
	}

	
	public void addreceivedMessage(int id, String message) {
		receivedBuffer.put(id, message);
	}

	/* **************************** */

	
	public void sendFTSLRequest() {
		FTSLHeader header = new FTSLHeader(sessionID, "FTSL_REQUEST", 0,
				lastRecievedPacketID, 0);

		FTSLMessage packet = new FTSLMessage(null, header);
		byte[] buffer = packet.toByte_();

		try {

			outputStream.write(buffer, 0, buffer.length);
			outputStream.flush();

		} catch (IOException e) {

		}

	}

	public void sendFTSLReply() {
		FTSLHeader header = new FTSLHeader(sessionID, "FTSL_REPLY", 0,
				lastRecievedPacketID, 0);

		FTSLMessage packet = new FTSLMessage(null, header);
		byte[] buffer = packet.toByte_();

		try {

			outputStream.write(buffer, 0, buffer.length);
			outputStream.flush();

		} catch (IOException e) {

		}

	}
	
	public byte[] createFTSLMessage() {
		FTSLHeader header = new FTSLHeader();
		header.setSID(sessionID);

		FTSLMessage message = new FTSLMessage(null, header);
		return message.toByte_();
	}
	
	/* **************************** */


	public void completMessageInfo() {

		SentMessagesInfo.lastElement().setEnd(lastSentPacketID);
		logger.logMessageInfo(SentMessagesInfo.lastElement());

	}

	public void addMessageInfo() {

		MessageInfo info = new MessageInfo();
		if (SentMessagesInfo.isEmpty())
			info.setStart(1);
			
		else 
			info.setStart(SentMessagesInfo.lastElement().getEnd()+1);
			
		info.setId(sendMessageID);	
		info.setEnd(lastSentPacketID);
		SentMessagesInfo.add(info);

	}

	
	public void addMessageInfo(MessageInfo info) {
		SentMessagesInfo.add(info);

	}
	public void updateMessageInfo(int pid) {

		SentMessagesInfo.lastElement().setIndex(pid);
	}
	
	/* **************************** */

	public void removeDeliveredMessages(int rpid) {
		int index = 0;
		int id = rpid;
		while (index < SentMessagesInfo.size()) {
			MessageInfo info = SentMessagesInfo.get(index);
			if (info.getEnd() != 0 & info.getEnd() <= rpid) {
				id = info.getEnd();
				SentMessagesInfo.remove(index);

			} else
				index = SentMessagesInfo.size();
		}

		index = 0;
		while (index < sentBuffer.size()) {
			FTSLMessage message = sentBuffer.get(index);
			if (message.getHeader().getPID() <= rpid) {
				sentBuffer.remove(index);

			} else
				index = sentBuffer.size();
		}

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

	/* **************************** */

	public int read(byte buffer[], int pos, int len) {

		int expectedID = lastRecievedPacketID + 1;
		if (receivedBuffer.containsKey(expectedID)) {
			byte[] tempBuffer = receivedBuffer.get(expectedID).getBytes();
			increaseLastReceivedPacketID();
			
			// should be checked again
			for (int i = 0; i < tempBuffer.length; i++)
				buffer[pos + i] = tempBuffer[i];

			return tempBuffer.length;

		} else {
			int read = -1;

			byte[] packet = new byte[len];

			try {
				read = inputStream.read(packet);
			} catch (IOException e) {
				e.printStackTrace();
			}

			int test = 1;
			while (read != -1 & test == 1) {
				int pSize = processInputPacket(packet);

				if (pSize == 0) {
					try {
						read = inputStream.read(packet);
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					test = 0;
					read = pSize;
					for (int i = 0; i < read; i++)
						buffer[pos + i] = packet[i];
					
				}
			}

			return read;
		}
	}
	
	public int processInputPacket(byte buffer[]) {

		String packet = new String(buffer);
		if (!packet.startsWith(FTSLHeader.protocol))
			return 0;

		int index = packet.indexOf("\n");
		String h = packet.substring(0, index);
		String b = packet.substring(index + 1);
		FTSLHeader header = FTSLHeader.valueOf_(h);
		byte[] body = new byte[header.getMessageSize()];
		byte[] tempBody = b.getBytes();
		
		for (int i = 0; i < header.getMessageSize(); i++)
			body[i] = tempBody[i];

		int result = processFTSLHeader(buffer);
		
		if (result == 0) {
			return 0;

		} else {

			if (tempBody == null || tempBody.length == 0)
					return 0;
				
			for (int i = 0; i < tempBody.length; i++)
				buffer[i] = tempBody[i];
		}
		return header.getMessageSize();
	}
	
	public int processFTSLHeader(byte[] buffer) {
		
		String packet = new String(buffer);
		int index = packet.indexOf("\n");
		String str = packet.substring(0, index);
		
		FTSLHeader header = FTSLHeader.valueOf_(str);
		
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

			} else {

				receivedBuffer.put(pid, packet);
				if (!receivedBuffer.containsKey(pid - 1)) {
					FTSLHeader h = new FTSLHeader(sid, "FTSL_NACK", pid,
							lastRecievedPacketID, 0);
					FTSLMessage ftslPacket = new FTSLMessage(null, h);

					try {

						outputStream.write(ftslPacket.toByte_());
						outputStream.flush();

					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				return 0;
			}

		} else if (flag.compareTo("REQ") == 0) {
			
			FTSLHeader h = new FTSLHeader(sid, "REP", 0,
					lastRecievedPacketID, 0);

			FTSLMessage ftslPacket = new FTSLMessage(null, h);

			try {
				outputStream.write(ftslPacket.toByte_());
				outputStream.flush();

			} catch (IOException e) {
				// TODO: handle exception
			}
			return -1;

		} else if (flag.compareTo("FTSL_REPLY") == 0) {

			removeDeliveredMessages(rpid);

			return -1;
		} else if (flag.compareTo("FTSL_NOTIFICATION") == 0) {

			// it doesn't happen in this side because that is the client
			// re-create the socket
			return -1;

		} else if (flag.compareTo("FTSL_ACK") == 0) {

			removeDeliveredMessages(rpid);
			// all messages are lost should be sent again
			index = 0;
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

			return -1;

		} else if (flag.compareTo("FTSL_NACK") == 0) {
			removeDeliveredMessages(rpid);
			// all messages are lost should be sent again
			index = 0;
			while (index < sentBuffer.size()) {
				FTSLMessage pkt= sentBuffer.get(index);
				if (pkt.getHeader().getPID() > rpid
						& pkt.getHeader().getPID() < pid) {
					try {
						Logger.log(":) :) :) Client is sending the lost messags");
						outputStream.write(pkt.toByte_());
						outputStream.flush();

					} catch (IOException e) {
						e.printStackTrace();	
					}
				}
				index++;
			}

			return -1;
		}
		return -1;
	}

	/* **************************** */

	public void write(byte[] buffer) {

		while (stopWriting == false);
		buffer = processOutputPacket(buffer);
		try {
			outputStream.write(buffer);
		} catch (IOException e) {
			HandleFailure();
		}

	}

	public void write() {

		// this is to send the session ID to the server
		byte[] buffer = createFTSLMessage();
		try {

			outputStream.write(buffer);
			outputStream.flush();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public byte[] processOutputPacket(byte[] packet) {

		// Logger.log("FTSL is writing in the output stream in the server side of the proxy.");

		increaseLastSentPacketID();

		FTSLHeader header = new FTSLHeader(sessionID, "APP", lastSentPacketID,
				lastRecievedPacketID, packet.length);

		// Logger.log("the header of the packet is: " + header.toString_());

		FTSLMessage pkt = new FTSLMessage(packet, header);
		byte[] buffer = pkt.toByte_();

		keepSentPacket(lastSentPacketID, pkt);

		return buffer;

	}
	
	public void flush() {  // the end of a stream of the message
		try {
			outputStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		addMessageInfo();
		sendMessageID++;
	}
	
	/* ********************************* */

	public void HandleFailure() {

		stopWriting = false;
		try {
			InetAddress address = socket.getInetAddress();
			int port = socket.getPort();
			socket.close();
			socket = new Socket(address, port);
			outputStream = new ObjectOutputStream(socket.getOutputStream());
			inputStream = new ObjectInputStream(socket.getInputStream());

		} catch (IOException e2) {
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			sleepTime = sleepTime * 2;
			HandleFailure();
		}

		FTSLHeader header = new FTSLHeader(sessionID, "FTSL_NOTIFICATION", 0,
				lastRecievedPacketID, -1);

		FTSLMessage packet = new FTSLMessage(null, header);
		byte[] buffer = packet.toByte_();

		try {

			outputStream.write(buffer, 0, buffer.length);
			outputStream.flush();
			logger.logSessionInfo("Socket", socket);

			while (inputStream.read() == -1)
				;

		} catch (IOException e) {
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			sleepTime = sleepTime * 2;
			HandleFailure();
		}

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


}
