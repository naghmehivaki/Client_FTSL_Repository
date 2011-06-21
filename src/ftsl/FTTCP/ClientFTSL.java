package ftsl.FTTCP;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import ftsl.FTHTTP.AppInterfaceImp;
import util.Logger;

public class ClientFTSL {

	
	static boolean initialized = false;


	static HashMap<String, Session> sessions = new HashMap<String, Session>();
	static FTSL_Logger logger=new FTSL_Logger();


	///////////////////////////////////////////////////////////

	public static synchronized Session getSession(String sid){
		return sessions.get(sid);
	}
	
	public static synchronized void putSession(String sid,Session session){
		sessions.put(sid, session);
	}
	
	public static void init() {

	//	sessions = logger.init();
		if (sessions != null){
			cleanUp();
		}
		
		initialized = true;
	}

	///////////////////////////

	public static void cleanUp() {
		// remove all incomplete messages in the sessions
		
		Set<String> s = sessions.keySet();
		Iterator<String> it= s.iterator();
		while(it.hasNext()){
			String sid=it.next();
			Session session=getSession(sid);
			//session.setLastRecievedPacketID(session.getLastReceivedMessageInfo().getEnd());
			Vector<MessageInfo> SentMessagesInfo =	session.getSentMessagesInfo();
			int pid=0;
			int mid=0;
			MessageInfo info=SentMessagesInfo.lastElement();
			if (info.getEnd()==0){
				SentMessagesInfo.remove(info);
				pid=info.getStart()-1;
				mid=info.getId()-1;
				session.setLastSentPacketID(pid);
				session.setSendMessageID(mid);
				updateSession(sid,session);
			}	
		}			
	}
	// ////////////////////////////////////////////


	public static void keepSession(Session session){
//		if (initialized == false)
//			init();
//		
		putSession(session.getSessionID(), session);
	}
	public static void removeSession(String sid){
		sessions.remove(sid);
		//logger.log(sessions);
	}
	
	/////////////////////////////////////////////////////////////
	 public static synchronized void updateSession(String sid, Session session) {
		 	sessions.remove(sid);
			sessions.put(sid, session);
			//logger.log(sessions);
	 }


}