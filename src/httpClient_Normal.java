import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import util.Logger;

public class httpClient_Normal {

	String server = "10.3.3.171";
	int port = 5000;
	Socket socket=null;
	ObjectInputStream inputStream=null;
	ObjectOutputStream outputStream=null;

	public void run() {

		//Logger.log("Client started");

		try {
			socket = new Socket(server, port);
			outputStream= new ObjectOutputStream(socket.getOutputStream());
			outputStream.flush();
			inputStream= new ObjectInputStream(socket.getInputStream());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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
			
			try {
				read = inputStream.read(buffer, pos, len - pos);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			

			while (read != -1) {
				//Logger.log("***** \n"+ new String (buffer));
				boolean newResponse = handleHttpResponse(buffer, pos, read);
//				if (newResponse) {
//					pos = pos + read;
//					// reply is ready
//				} else {
//					pos = 0;
//				}

				try {
					read = inputStream.read(buffer, pos, len - pos);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

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
				
			
				try {
					outputStream.write(str.getBytes());
					outputStream.flush();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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
		httpClient_Normal client = new httpClient_Normal();
		client.run();

	}
}