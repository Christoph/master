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
		work.weightPreVocab();
		
		// Compute clusters
		work.clustering(3);
		
		// Compute groups
		work.grouping();
		
		// Connection
		server.addConnectListener(new ConnectListener() {
			
			public void onConnect(SocketIOClient client) {
				
				// Overview
				client.sendEvent("initalized", work.sendOverview());
				
				// Pre
				// Send replacements
				client.sendEvent("replacements", work.sendReplacements(0.65));
				// Send vocab
				client.sendEvent("vocab", work.sendVocab());
				// Send importance
				client.sendEvent("importance", work.sendImportanceHistogram());
				// Send similarities
				client.sendEvent("similarities", work.sendSimilarityHistogram());
				
				// Composite
				client.sendEvent("frequentGroups", work.sendFrequentGroups());
				client.sendEvent("uniqueGroups", work.sendUniqueGroups());
				
				client.sendEvent("frequentStrength", work.sendFrequentHistogram());
				client.sendEvent("uniqueStrength", work.sendUniqueHistogram());
				
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
		
		// Compute clusters
		server.addEventListener("clustering", String.class, new DataListener<String>() {

			public void onData(SocketIOClient client, String data,
					AckRequest arg2) throws Exception {
				
				work.clustering(Integer.parseInt(data));
			}
        });
		
		// Get corresponding cluster
		server.addEventListener("getCluster", String.class, new DataListener<String>() {

			public void onData(SocketIOClient client, String data,
					AckRequest arg2) throws Exception {
				
				client.sendEvent("cluster", work.sendCluster(data));
			}
        });
		
		// Get replacements around threshold
		server.addEventListener("getReplacements", String.class, new DataListener<String>() {

			public void onData(SocketIOClient client, String data,
					AckRequest arg2) throws Exception {
				
				client.sendEvent("replacements", work.sendReplacements(Double.parseDouble(data)));
			}
        });
		
		// Apply clustering threshold
		server.addEventListener("applyClustering", String.class, new DataListener<String>() {

			public void onData(SocketIOClient client, String data,
					AckRequest arg2) throws Exception {
				
				work.applyClustering(Double.parseDouble(data));
				client.sendEvent("overview", work.sendOverview());
				
				work.grouping();
				client.sendEvent("frequentGroups", work.sendFrequentGroups());
				client.sendEvent("uniqueGroups", work.sendUniqueGroups());
			}
        });
		
		// Get all groups
		server.addEventListener("getGroups", String.class, new DataListener<String>() {

			public void onData(SocketIOClient client, String data,
					AckRequest arg2) throws Exception {
				
				work.setGroupSize(Integer.parseInt(data));
				work.grouping();
				
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
		
		// getImportantWords
		server.addEventListener("getPostVocab", String.class, new DataListener<String>() {

			public void onData(SocketIOClient client, String data,
					AckRequest arg2) throws Exception {
				
				work.weightPostVocab();
				
				client.sendEvent("postVocab", work.sendPostVocab());
			}
        });
	}
}
