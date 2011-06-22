import java.io.IOException;
import java.io.ObjectInputStream;

import ftsl.Session;
import ftsl.FTTCP.*;
import util.Logger;

public class httpClient_test1 {

	String server = "127.0.0.1";
	int port = 5000;
	Session session;

	public void run() {

		//Logger.log("Client started");

		session = new Session(server, port);

		//Logger.log("Client created a new session to the serve " + server+ " port " + port);

		(new write()).start();
		(new read()).start();

	}

	public class read extends Thread {

		public void run() {

			int read = 0;
			byte[] buffer = new byte[1024];

			int pos = 0;
			int len = 1024; 
			
			read = session.read(buffer, pos, len - pos);
			
			//Logger.log("***** \n"+ new String (buffer));

			while (read != -1) {
				boolean newResponse = handleHttpResponse(buffer, pos, read);
				if (newResponse) {
					pos = pos + read;
					// reply is ready
				} else {
					pos = 0;
				}

				read = session.read(buffer, pos, len - pos);

			}
		}
	}

	public boolean handleHttpResponse(byte[] buffer, int pos, int read) {

		byte[] packet = new byte[read];
		for (int i = 0; i < read; i++) {
			packet[i] = buffer[pos + i];
		}
		
		String str = new String(packet);
		if (str.startsWith("HTTP/1.1"))
			return true;
		else
			return false;
	}

	public class write extends Thread {

		public void run() {

			int index = 0;
			while (index < 1000000) {

				
				String str = "GET http://" + server + "/ HTTP/1.1\r\n";
				str = str + "Host: " + server + "\r\n";
				str = str + "\r\n";
				session.write(str.getBytes());
				session.flush();
				//Logger.log("Client wrote: \n" + str);

				index++;
				System.out.println(index);
				try {
					Thread.currentThread().sleep(1);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		//	session.close();

		}
	}

	public static void main(String[] args) {
		httpClient_test1 client = new httpClient_test1();
		client.run();

	}
}