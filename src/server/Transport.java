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
		
		work.preFilter();
		
		// Basic processing
		work.removeReplace();
		
		// Create vocab
		work.weightPreVocab();
		
		// Compute clusters
		work.clustering(3);
		
		// Compute groups
		work.grouping();
		
		work.weightPostVocab();
		
		work.computeImportantWords(0.25);
		
		// Connection
		server.addConnectListener(new ConnectListener() {
			
			public void onConnect(SocketIOClient client) {
				
				// Overview
				client.sendEvent("connected", "");
			}
		});
		
		// Get preprocessing data
		server.addEventListener("getPreprocessingData", String.class, new DataListener<String>() {

			public void onData(SocketIOClient client, String data,
					AckRequest arg2) throws Exception {
				
				if(data.equals("preFilterData"))
				{
					client.sendEvent("preFilterData", work.sendPreFilterHistogram());
				}
				if(data.equals("preFilterGrid"))
				{
					client.sendEvent("preFilterGrid", work.sendPreFilter());
				}
			}
        });
		
		// Apply characters to remove
		server.addEventListener("applyRemoveCharacters", String.class, new DataListener<String>() {

			public void onData(SocketIOClient client, String data,
					AckRequest arg2) throws Exception {
				
				work.applyCharactersToRemove(data);
			}
        });
		
		// Apply characters to replace
		server.addEventListener("applyReplaceCharacters", String.class, new DataListener<String>() {

			public void onData(SocketIOClient client, String data,
					AckRequest arg2) throws Exception {
				
				work.applyCharactersToReplace(data);
			}
        });
		
		// Apply characters to replace
		server.addEventListener("applyImportedData", String.class, new DataListener<String>() {

			public void onData(SocketIOClient client, String data,
					AckRequest arg2) throws Exception {
				
				work.applyDictionaryData(data);
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
		
		// Get spell checking data
		server.addEventListener("getSpellcheckingData", String.class, new DataListener<String>() {

			public void onData(SocketIOClient client, String data,
					AckRequest arg2) throws Exception {
				
				if(data.equals("similarities"))
				{
					client.sendEvent("similarities", work.sendSimilarityHistogram());
				}
				if(data.equals("vocab"))
				{
					client.sendEvent("vocab", work.sendVocab());
				}
				if(data.equals("importance"))
				{
					client.sendEvent("importance", work.sendImportanceHistogram());
				}
			}
        });
		
		// Get cluster of tag
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
		
		// Get composite data
		server.addEventListener("getCompositeData", String.class, new DataListener<String>() {

			public void onData(SocketIOClient client, String data,
					AckRequest arg2) throws Exception {
				
				if(data.equals("frequentData"))
				{
					client.sendEvent("frequentData", work.sendFrequentHistogram());
				}
				if(data.equals("frequentGroups"))
				{
					client.sendEvent("frequentGroups", work.sendFrequentGroups());
				}
				if(data.equals("uniqueData"))
				{
					client.sendEvent("uniqueData", work.sendUniqueHistogram());
				}
				if(data.equals("uniqueGroups"))
				{
					client.sendEvent("uniqueGroups", work.sendUniqueGroups());
				}
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
		
		// Get postprocessing data
		server.addEventListener("getPostprocessingData", String.class, new DataListener<String>() {

			public void onData(SocketIOClient client, String data,
					AckRequest arg2) throws Exception {
				
				if(data.equals("postFilterData"))
				{
					client.sendEvent("postFilterData", work.sendPostImportanceHistogram());
				}
				if(data.equals("postFilterGrid"))
				{
					client.sendEvent("postFilterGrid", work.sendPostVocab());
				}
				if(data.equals("importantWords"))
				{
					client.sendEvent("importantWords", work.sendImportantWords());
				}
			}
        });
		
		// Get output data
		server.addEventListener("getOutputData", String.class, new DataListener<String>() {

			public void onData(SocketIOClient client, String data,
					AckRequest arg2) throws Exception {
				
				if(data.equals("output"))
				{
					client.sendEvent("output", work.sendFinal());
				}
			}
        });
	}
}
