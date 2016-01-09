package server;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;

import processing.lastFM.WorkflowLast;

public class Transport {

	//private WorkflowAbstract work = new WorkflowAbstract();
	private WorkflowLast work = new WorkflowLast();
	//private WorkflowMovie work = new WorkflowMovie();
	private SocketIOServer server;
	
	protected Transport(SocketIOServer server) {
		super();
		this.server = server;
	}

	public void initialize()
	{		
		work.init();
		
		// Basic processing
		work.removeReplace();
		
		// Create vocab
		work.weightVocab();
		
		// Compute groups
		work.grouping(3);
		
		// Connection
		server.addConnectListener(new ConnectListener() {
			
			public void onConnect(SocketIOClient client) {
				
				// Overview
				client.sendEvent("initalized", work.sendOverview());
				
				// Pre
				client.sendEvent("vocab", work.sendVocab());
				
				// Composite
				client.sendEvent("frequentGroups", work.sendFrequentGroups());
				client.sendEvent("uniqueGroups", work.sendUniqueGroups());
				
				// Post

			}
		});
		
		// Get history data
		server.addEventListener("getHistory", String.class, new DataListener<String>() {

			public void onData(SocketIOClient client, String data,
					AckRequest arg2) throws Exception {
				
				client.sendEvent("history", work.sendHistory(data));
			}
        });
		
		// Get all groups
		server.addEventListener("getGroups", String.class, new DataListener<String>() {

			public void onData(SocketIOClient client, String data,
					AckRequest arg2) throws Exception {
				
				work.grouping(Integer.parseInt(data));
				
				client.sendEvent("frequentGroups", work.sendFrequentGroups());
				client.sendEvent("uniqueGroups", work.sendUniqueGroups());
			}
        });
		
		// Apply groups to the original data set
		server.addEventListener("applyGroups", String.class, new DataListener<String>() {

			public void onData(SocketIOClient client, String data,
					AckRequest arg2) throws Exception {
				
				work.applyGrouping();
				client.sendEvent("overview", work.sendOverview());
			}
        });
	}
}
