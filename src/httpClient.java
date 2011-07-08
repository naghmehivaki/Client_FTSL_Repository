
import ftsl.MessageHandler;
import ftsl.Session;
import util.Logger;

public class httpClient {

	String server = "127.0.0.1";
	int port = 5000;
	Session session;

	public void run() {

		//Logger.log("Client started");
		
		session = new Session(server, port);
		
		//Logger.log("Client created a new session to the serve "+ server+ " port "+ port);
		
		(new write()).start();	
		(new read()).start();

	}

	public class read extends Thread {		
		
		public read() {
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
			MessageHandler msgHandler= session.read(buffer, pos, len - pos);
			read = msgHandler.getSize();
			if (msgHandler.isEom())
				msgHandler.confirm();
			
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
				
				msgHandler= session.read(buffer, pos, len - pos);
				read = msgHandler.getSize();
				if (msgHandler.isEom())
					msgHandler.confirm();

			}
			Logger.log("client doesn't read anything now ...");
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
	
		if (method.compareTo("HTTP/1.1") == 0)
			return true;
		else
			return false;
	}


	public class write extends Thread {

		public write() {		
		}

		public void run() {

			int index = 0;

			while (index < 10000) {

				String str = "GET http://" + server + "/ HTTP/1.1\r\n";
				session.write(str.getBytes());

				//Logger.log("Client wrote: \n" + str);
			
				str = "Host: " + server + "\r\n";
				str = str + "\r\n";
				session.write(str.getBytes( ), true);
				
				System.out.println("Client wrote: \n" + index);
				//Logger.log("Client wrote: \n" + str);
				try {
					Thread.currentThread().sleep(1);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				index++;
			}
		}
	}

	public static void main(String[] args) {
		httpClient client = new httpClient();
		client.run();

	}
}