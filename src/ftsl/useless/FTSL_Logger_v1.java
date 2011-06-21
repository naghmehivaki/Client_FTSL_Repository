package ftsl.useless;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import ftsl.FTTCP.*;

public class FTSL_Logger_v1 extends Thread{
	
	public String fileName="FTSL.log";
	
	
	public void log(HashMap<String, Session> sessions){
				
		File f = new File(fileName);
		FileOutputStream out=null;
		try {
			if (!f.exists())
				f.createNewFile();
			out = new FileOutputStream(f);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Set<String> sessionIDs= sessions.keySet();
		Iterator<String> it=sessionIDs.iterator();
		while(it.hasNext()){
			String log="";
			String sid=it.next();
			Session session=sessions.get(sid);
			///////////////////////////////////////////////// Log Session
			
			try {
			
				log="Session ID: "+session.getSessionID()+"\n";
				log=log+"Socket: "+ session.getSocket().getInetAddress()+ " " +session.getSocket().getPort() +"\n";
				log=log+"lastSentPacketID: "+ session.getLastSentPacketID()+"\n";
				log=log+"LastRecievedPacketID: "+ session.getLastRecievedPacketID()+"\n";
				log=log+"sendMessageID: "+ session.getSendMessageID()+"\n";
				log=log+"recieveMessageID: "+ session.getRecieveMessageID()+"\n";
				
//				MessageInfo info=session.getLastReceivedMessageInfo();
//				log=log+"LastReceivedMessageInfo: "+ info.getStart()+" "+info.getIndex()+" "+info.getEnd()+" "+info.getId()+"\n";
				log=log+"SentMessagesInfo\n";
				int index=0;
				Vector<MessageInfo> SentMessagesInfo=session.getSentMessagesInfo();
				while(index<SentMessagesInfo.size()){
					MessageInfo info=SentMessagesInfo.get(index);
					log=log+info.getStart()+" "+info.getIndex()+" "+info.getEnd()+" "+info.getId()+"\n";
					index++;
				}
				
				log=log+"sentBuffer\n";

				index=0;
				Vector<FTSLMessage> sentBuffer = session.getSentBuffer();
				while(index<sentBuffer.size()){
					FTSLMessage packet=sentBuffer.get(index);
					log=log+packet.toString_()+"*****\n";
					index++;
				}
				out.write(log.getBytes());
				
				
			} catch (Exception e) {
				// TODO: handle exception
			}
			
			///////////////////////////////////////////////// Log session	
		}	
	}
	
	public HashMap<String, Session> init(){
	
		HashMap<String, Session> sessions=new HashMap<String, Session>();
		
		BufferedReader reader = null;

		try {
			String log;
			reader = new BufferedReader(new FileReader(fileName));
			if (reader==null)
					return null;
			
			log = reader.readLine();
			if (log==null)
				return null;
			
			while (log!=null){
				
				Session session=new Session();
				int index=0;
				index=log.indexOf(" ");
				if (log.substring(0,index) == "Session ID:"){
					session.setSessionID(log.substring(index+1));
				}
				log = reader.readLine();
				index=log.indexOf(" ");
				if (log.substring(0,index) == "Socket:"){
					log=log.substring(index+1);
					index=log.indexOf(" ");
					Socket socket=new Socket(log.substring(0, index), Integer.valueOf(log.substring(index+1)));
					session.setSocket(socket);
					
				}
				log = reader.readLine();
				index=log.indexOf(" ");
				if (log.substring(0,index) == "lastSentPacketID:"){
					session.setLastSentPacketID(Integer.valueOf(log.substring(index+1)));

				}
				log = reader.readLine();
				index=log.indexOf(" ");
				if (log.substring(0,index) == "LastRecievedPacketID:"){
					session.setLastRecievedPacketID(Integer.valueOf(log.substring(index+1)));
				}
				
				log = reader.readLine();
				index=log.indexOf(" ");
				if (log.substring(0,index) == "sendMessageID:"){
					session.setSendMessageID(Integer.valueOf(log.substring(index+1)));
				}
				log = reader.readLine();
				index=log.indexOf(" ");
				if (log.substring(0,index) == "recieveMessageID:"){
					session.setRecieveMessageID(Integer.valueOf(log.substring(index+1)));
				}
//				log = reader.readLine();
//				index=log.indexOf(" ");
//				if (log.substring(0,index) == "LastReceivedMessageInfo:"){
//					MessageInfo info=session.getLastReceivedMessageInfo();
//
//					log=log.substring(index+1);
//					index=log.indexOf(" ");
//					info.setStart(Integer.valueOf(log.substring(0,index)));
//					log=log.substring(index+1);
//					index=log.indexOf(" ");
//					info.setIndex(Integer.valueOf(log.substring(0,index)));
//					log=log.substring(index+1);
//					index=log.indexOf(" ");
//					info.setEnd(Integer.valueOf(log.substring(0,index)));
//					info.setId(Integer.valueOf(log.substring(index+1)));
//					
//					session.setLastReceivedMessageInfo(info);
//				}
				log = reader.readLine();
				if (log=="SentMessagesInfo"){
					Vector<MessageInfo> sentMessagesInfo=session.getSentMessagesInfo();
					log = reader.readLine();

					while (log != "sentBuffer"){
						MessageInfo info=new MessageInfo();

						index=log.indexOf(" ");
						info.setStart(Integer.valueOf(log.substring(0,index)));
						log=log.substring(index+1);
						index=log.indexOf(" ");
						info.setIndex(Integer.valueOf(log.substring(0,index)));
						log=log.substring(index+1);
						index=log.indexOf(" ");
						info.setEnd(Integer.valueOf(log.substring(0,index)));
						info.setId(Integer.valueOf(log.substring(index+1)));
						sentMessagesInfo.add(info);
						
						log = reader.readLine();
					}
					session.setSentMessagesInfo(sentMessagesInfo);
					
				}
					
				log = reader.readLine();

			    Vector<FTSLMessage> sentBuffer = session.getSentBuffer();
			    String str="";
			    while (log!=null && log != "SessionID:"){
			    	if (log!="*****"){
			    		str=str+log+"\n";
			    	}else {
			    		FTSLMessage packet=FTSLMessage.valueOf_(str);
			    		sentBuffer.add(packet);
			    	}
					log = reader.readLine();

			    }
				
			}		

		}catch (Exception e) {
			// TODO: handle exception
		}
		
		return sessions;
		
	}
	
	
	

}
