package server;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;

public class Transport {

	//private WorkflowAbstract work = new WorkflowAbstract();
	private Workflow work = new Workflow();
	//private WorkflowMovie work = new WorkflowMovie();
	private SocketIOServer server;
	
	protected Transport(SocketIOServer server) {
		super();
		this.server = server;
	}

	private void sendParams(SocketIOClient client)
	{
		// Preprocessing
		client.sendEvent("preFilterParams", work.sendPreFilterParams());
		client.sendEvent("preRemoveParams", work.sendPreRemoveParams());
		client.sendEvent("preReplaceParams", work.sendPreReplaceParams());
		client.sendEvent("preDictionaryParams", work.sendPreDictionaryParams());
		
		// Postprocessing
		client.sendEvent("spellImportance", work.sendSpellImportanceParams());
		client.sendEvent("spellSimilarity", work.sendSpellSimilarityParams());
	}
	
	private void sendPreprocessData(SocketIOClient client)
	{
		client.sendEvent("preFilterData", work.sendPreFilterHistogram());
		client.sendEvent("preFilterGrid", work.sendPreFilter());
	}
	
	private void sendSpellcorrectData(SocketIOClient client)
	{
		client.sendEvent("similarities", work.sendSimilarityHistogram());
		client.sendEvent("vocab", work.sendVocab());
		client.sendEvent("importance", work.sendImportanceHistogram());
	}
	
	public void initialize()
	{		
		// This should be done after data import
		work.init();

		// Compute everything with default values
		work.computePreprocessing();
		work.computeSpellCorrect();
		
		// Connection
		server.addConnectListener(new ConnectListener() {
			
			public void onConnect(SocketIOClient client) {
				System.out.println("Connect");
				// Broadcast parameters
				sendParams(client);
				
				// Send all the data
				sendPreprocessData(client);
				sendSpellcorrectData(client);
			}
		});
		
	    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
	    // Preprocessing
	    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		// Apply characters to remove
		server.addEventListener("applyRemoveCharacters", String.class, new DataListener<String>() {

			public void onData(SocketIOClient client, String data,
					AckRequest arg2) throws Exception {
				
				work.applyPreRemove(data);
				
				sendSpellcorrectData(client);
			}
        });
		
		// Apply characters to replace
		server.addEventListener("applyReplaceCharacters", String.class, new DataListener<String>() {

			public void onData(SocketIOClient client, String data,
					AckRequest arg2) throws Exception {
				
				work.applyPreReplace(data);
				
				sendSpellcorrectData(client);
			}
        });
		
		// Apply dictionary
		server.addEventListener("applyImportedData", String.class, new DataListener<String>() {

			public void onData(SocketIOClient client, String data,
					AckRequest arg2) throws Exception {
				
				work.applyPreDictionary(data);
				
				sendSpellcorrectData(client);
			}
        });
		
		// Apply filter
		server.addEventListener("applyPrefilter", String.class, new DataListener<String>() {

			public void onData(SocketIOClient client, String data,
					AckRequest arg2) throws Exception {
				
				work.applyPreFilter(Double.parseDouble(data));
				
				sendSpellcorrectData(client);
			}
        });
		
	    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
	    // Spell Checking
	    //////////////////////////////////////////////////////////////////////////////////////////////////////////////

		// Compute clusters
		server.addEventListener("applySpellImportance", String.class, new DataListener<String>() {

			public void onData(SocketIOClient client, String data,
					AckRequest arg2) throws Exception {
				
				work.applySpellImportance(Double.parseDouble(data));
				
				client.sendEvent("output", work.sendOverview(2));
				System.out.println(data);
			}
        });
		
		// Compute clusters
		server.addEventListener("applySpellSimilarity", String.class, new DataListener<String>() {

			public void onData(SocketIOClient client, String data,
					AckRequest arg2) throws Exception {
				
				work.applySpellSimilarity(Double.parseDouble(data));
				
				client.sendEvent("output", work.sendOverview(2));
				System.out.println(data);
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
		
	    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
	    // Composites
	    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		// Get composite data
		server.addEventListener("getCompositeData", String.class, new DataListener<String>() {

			public void onData(SocketIOClient client, String data,
					AckRequest arg2) throws Exception {
				
				if(data.equals("frequentData"))
				{
					//client.sendEvent("frequentData", work.sendFrequentHistogram());
				}
				if(data.equals("frequentGroups"))
				{
					//client.sendEvent("frequentGroups", work.sendFrequentGroups());
				}
				if(data.equals("uniqueData"))
				{
					//client.sendEvent("uniqueData", work.sendUniqueHistogram());
				}
				if(data.equals("uniqueGroups"))
				{
					//client.sendEvent("uniqueGroups", work.sendUniqueGroups());
				}
			}
        });
		
		// Apply groups to the original data set
		server.addEventListener("applyGroups", String.class, new DataListener<String>() {

			public void onData(SocketIOClient client, String data,
					AckRequest arg2) throws Exception {
				
				//work.applyGrouping();
				//client.sendEvent("overview", work.sendOverview());
			}
        });
		
		// Get postprocessing data
		server.addEventListener("getPostprocessingData", String.class, new DataListener<String>() {

			public void onData(SocketIOClient client, String data,
					AckRequest arg2) throws Exception {
				
				if(data.equals("postFilterData"))
				{
					//client.sendEvent("postFilterData", work.sendPostImportanceHistogram());
				}
				if(data.equals("postFilterGrid"))
				{
					//client.sendEvent("postFilterGrid", work.sendPostVocab());
				}
				if(data.equals("importantWords"))
				{
					//client.sendEvent("importantWords", work.sendImportantWords());
				}
			}
        });
		
		// Get output data
		server.addEventListener("getOutputData", String.class, new DataListener<String>() {

			public void onData(SocketIOClient client, String data,
					AckRequest arg2) throws Exception {
				
				if(data.equals("output"))
				{
					//client.sendEvent("output", work.sendFinal());
				}
			}
        });
		
		// Get history data
		server.addEventListener("getHistory", String.class, new DataListener<String>() {

			public void onData(SocketIOClient client, String data,
					AckRequest arg2) throws Exception {
				
				//client.sendEvent("history", work.sendHistory(data));
			}
        });
	}
}
