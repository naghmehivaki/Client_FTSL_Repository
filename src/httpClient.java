import java.io.IOException;
import java.io.ObjectInputStream;

import ftsl.FTTCP.*;
import util.Logger;

public class httpClient {

	String server = "10.3.3.171";
	int port = 5000;
	Session session;

	public void run() {

		//Logger.log("Client started");
		
		session = new Session(server, port);
		
		//Logger.log("Client created a new session to the serve "+ server+ " port "+ port);
		
//		OutputChannel out = session.getOutputChannel();
//		InputChannel in = session.getInputChannel();
		
		
		
		(new write()).start();		
		
		(new read()).start();

	}

	public class read extends Thread {
		//InputChannel in;
		
		
		public read() {
			//in = i;
		}

		public void run() {
//			byte[] buffer = new byte[100];
//
//			int read = 0;
//			read = in.read(buffer);
//
//			while (read != -1) {
//				Logger.log("Client read :\n"+new String(buffer));
//				read = in.read(buffer);
//			}
//		}
			
			
			int read = 0;
			byte[] buffer = new byte[1024];
			int pos = 0;
			int len = 1024;
			read = session.read(buffer, pos, len - pos);
			
			while (read != -1) {
//				boolean newRequest = processInputbuffer(buffer, pos, read);
//				//Logger.log("newRequest= "+newRequest);
//				if (newRequest & pos != 0) {
//					
//					byte[] reply = new byte[pos];
//					for (int i = 0; i < pos; i++) {
//						reply[i] = buffer[i];
//					}
//					//Logger.log("Reply is: \n"+new String(reply));
//
//					// reply is ready.
//					
//					for (int i = 0; i < read; i++) {
//						buffer[i] = buffer[pos + i];
//					}
//					pos = read;
//				} else {
//					pos = pos + read;
//				}
				
				read = session.read(buffer, pos, len - pos);

			}
		}			
	}
	
	public boolean processInputbuffer(byte[] buffer, int pos, int read) {

		
		//Logger.log("pos: "+pos+" read: "+read+" buffer size: "+buffer.length);
		byte[] packet = new byte[read];
		for (int i = 0; i < read; i++) {
			packet[i] = buffer[pos + i];
		}

		//Logger.log("Server read: \n"+new String (packet));

		String str = new String(packet);
		int index = str.indexOf(" ");

		if (index <= 0)
			return false;

		String method = str.substring(0, index);
		//Logger.log("method is: "+method);
		// if (method == "OPTIONS" || method == "GET" || method == "HEAD" ||
		// method == "POST" || method == "PUT" || method == "DELETE" || method
		// == "TRACE" || method == "CONNECT")
		if (method.compareTo("HTTP/1.1") == 0)
			return true;
		else
			return false;
	}


	public class write extends Thread {
		//OutputChannel out;

		public write() {
			//out = o;
		}

		public void run() {

			int index = 0;

			while (index < 1000) {

				String str = "GET http://" + server + "/ HTTP/1.1\r\n";
				//session.write(str.getBytes());

				//Logger.log("Client wrote: \n"+str);
			
				str = str+"Host: " + server + "\r\n";
				str = str + "\r\n";
				session.write(str.getBytes());
				System.out.println("Client sent: "+System.currentTimeMillis());

				//Logger.log("Client wrote: \n"+str);

				index++;

			}

		}
	}

	public static void main(String[] args) {
		httpClient client = new httpClient();
		client.run();

	}
}