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
	int recieveMessageID = 0;
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

	public synchronized void setRecieveMessageID(int recieveMessageID) {
		this.recieveMessageID = recieveMessageID;
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

	public int getRecieveMessageID() {
		return recieveMessageID;
	}

	public synchronized void setLastRecievedMessageID(int id) {
		recieveMessageID = id;

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
	
	public int increaseLastRecievedPacketID() {
		lastRecievedPacketID++;
		logger.logSessionInfo("LastRecievedPacketID", lastRecievedPacketID);
		return lastRecievedPacketID;
	}

	public int increaseMessageID() {
		recieveMessageID++;
		logger.logSessionInfo("RecieveMessageID", recieveMessageID);
		return recieveMessageID;
	}
	
	public void increaseRecieveMessageID() {
		recieveMessageID++;
		logger.logSessionInfo("RecieveMessageID", recieveMessageID);
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


	public int getLastRecievedMessageID() {
		return recieveMessageID;
	}

	public void updateSocket(Socket s) {
		this.socket = s;
		logger.logSessionInfo("Socket", socket);

		try {
			inputStream = new ObjectInputStream(s.getInputStream());
			outputStream = new ObjectOutputStream(s.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/* **************************** */

	public int read(byte buffer[], int pos, int len) {

		int expectedID = lastRecievedPacketID + 1;
		if (receivedBuffer.containsKey(expectedID)) {
			byte[] tempBuffer = receivedBuffer.get(expectedID).getBytes();
			increaseLastRecievedPacketID();
			processFTSLBody(receivedBuffer.get(expectedID));

			for (int i = 0; i < tempBuffer.length; i++)
				buffer[pos + i] = tempBuffer[i];

			return tempBuffer.length;

		} else {
			int read = -1;

			byte[] packet = new byte[len];

			try {
				read = inputStream.read(packet);
			} catch (IOException e) {
				return -1;
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
	
	public int processFTSLHeader(FTSLHeader header, byte[] b) {

		String flag = header.getFLAG();
		String sid = header.getSID();
		int pid = header.getPID();
		int mid = header.getMID();
		int rpid = header.getrPID();

		if (flag.compareTo("") == 0) {
			/*
			 * this packet is a http reply assumption: server side received all
			 * packets in the same order are sent and prepared their reply and
			 * rent the replies. 1- get the packet ID and clean the request from
			 * the buffer. but first it should check to see if there any packet
			 * with the pid less than this pid and has not been replied yet. in
			 * this case we should send those requests first and get their reply
			 */

			// Logger.log("the packet is received is a "+appInterface.getRecieveMessageType());

			// first thing we should check is to see if the packet is the right

			// if (LastRecievedPacketID - lastSentPacketID >= MAX_BUFFER_SIZE) {
			// sendFTSLReply();
			// }

			int expectedPID = lastRecievedPacketID + 1;
			if (pid == expectedPID) {
				// it is the right message, no need to check anything else
				increaseLastRecievedPacketID();
				processFTSLBody(new String(b));
				removeDeliveredMessages(rpid);

				return 1;

			} else {
				// Logger.log("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");

				receivedBuffer.put(pid, new String(b));
				if (!receivedBuffer.containsKey(pid - 1)) {
					FTSLHeader h = new FTSLHeader(sid, "FTSL_NACK", pid,
							lastRecievedPacketID, getLastRecievedMessageID());
					FTSLMessage ftslPacket = new FTSLMessage(null, h);

					try {

						outputStream.write(ftslPacket.toByte_());
						outputStream.flush();

					} catch (IOException e) {
						// TODO: handle exception
					}
				}
				return 0;
			}

		} else if (flag.compareTo("FTSL_REQUEST") == 0) {
			// this is a request for the last packet and message is received in
			// client side

			// Logger.log("2222222222222222222222222222222222");
			FTSLHeader h = new FTSLHeader(sid, "FTSL_REPLY", 0,
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

			// int index=0;
			// while(index<sentBuffer.size()){
			// FTSLMessage packet=sentBuffer.get(index);
			// if (packet.getHeader().getPID()>rpid)
			// {
			// try {
			// outputStream.write(packet.toByte_());
			// outputStream.flush();
			// } catch (IOException e) {
			// // TODO: handle exception
			// }
			// }
			// index++;
			// }

			return -1;
		} else if (flag.compareTo("FTSL_NOTIFICATION") == 0) {

			// it doesn't happen in this side because that is the client
			// re-create the socket
			return -1;

		} else if (flag.compareTo("FTSL_ACK") == 0) {

			removeDeliveredMessages(rpid);
			// all messages are lost should be sent again
			int index = 0;
			while (index < sentBuffer.size()) {
				FTSLMessage packet = sentBuffer.get(index);
				if (packet.getHeader().getPID() > rpid) {
					try {
						outputStream.write(packet.toByte_());
						outputStream.flush();

					} catch (IOException e) {
						// TODO: handle exception
					}
				}
				index++;
			}

			return -1;

		} else if (flag.compareTo("FTSL_NACK") == 0) {
			removeDeliveredMessages(rpid);
			// all messages are lost should be sent again
			int index = 0;
			while (index < sentBuffer.size()) {
				FTSLMessage packet = sentBuffer.get(index);
				if (packet.getHeader().getPID() > rpid
						& packet.getHeader().getPID() < pid) {
					try {
						Logger.log(":) :) :) Client is sending the lost messags");
						outputStream.write(packet.toByte_());
						outputStream.flush();

					} catch (IOException e) {
						// TODO: handle exception
					}
				}
				index++;
			}

			return -1;
		}
		return -1;
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

		int result = processFTSLHeader(header, body);
		byte[] tempBuffer = b.getBytes();
		if (tempBuffer == null || tempBuffer.length == 0)
			return 0;

		if (result == 0) {
			return 0;

		} else {
			for (int i = 0; i < tempBuffer.length; i++)
				buffer[i] = tempBuffer[i];
		}
		return header.getMessageSize();
	}

	public void processFTSLBody(String body) {

		int index = body.indexOf(" ");
		if (index != -1) {
			String m = body.substring(0, index);
			if (m.compareTo("HTTP/1.1") == 0) {
				increaseRecieveMessageID();
				return;
				// session.updateReceivedMessageInfo();
			}

		}

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
				lastRecievedPacketID, sendMessageID, packet.length);

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
			// TODO Auto-generated catch block
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
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
				// TODO Auto-generated catch block
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
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}

		// ClientFTSL.removeSession(sessionID);
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
