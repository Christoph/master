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
		
		// Composite
		client.sendEvent("compFrequentParams", work.sendCompFrequentParams());
		client.sendEvent("compUniqueParams", work.sendCompUniqueParams());
		client.sendEvent("compSizeParams", work.sendCompSizeParams());
		client.sendEvent("compSplitParams", work.sendCompSplitParams());
		
		// Postprocess
		client.sendEvent("postFilterParams", work.sendPostFilterParams());
		client.sendEvent("postAllParams", work.sendPostAllParams());
		client.sendEvent("postReplaceParams", work.sendPostReplaceParams());
		client.sendEvent("postLengthParams", work.sendPostLengthParams());
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
		client.sendEvent("importance", work.sendPreVocabHistogram());
	}
	
	private void sendCompositeData(SocketIOClient client)
	{
		client.sendEvent("frequentGroups", work.sendFrequentGroups());
		client.sendEvent("frequentData", work.sendFrequentHistogram());
		client.sendEvent("uniqueGroups", work.sendUniqueGroups());
		client.sendEvent("uniqueData", work.sendUniqueHistogram());
	}
	
	private void sendPostprocessData(SocketIOClient client)
	{
		client.sendEvent("postFilterGrid", work.sendPostVocab());
		client.sendEvent("postFilterData", work.sendPostVocabHistogram());
		client.sendEvent("postImportantWords", work.sendPostImportant());
		client.sendEvent("postSalvageWords", work.sendPostSalvage());
	}
	
	public void initialize()
	{		
		// This should be done after data import
		work.init();

		// Compute everything with default values
		work.computePreprocessing();
		work.computeSpellCorrect();
		work.computeGroups();
		work.prepareSalvaging();
		
		// Connection
		server.addConnectListener(new ConnectListener() {
			
			public void onConnect(SocketIOClient client) {
				System.out.println("Connect");
				// Broadcast parameters
				sendParams(client);
				
				// Send all the data
				sendPreprocessData(client);
				sendSpellcorrectData(client);
				sendCompositeData(client);
				sendPostprocessData(client);
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
			}
        });
		
		// Compute clusters
		server.addEventListener("applySpellSimilarity", String.class, new DataListener<String>() {

			public void onData(SocketIOClient client, String data,
					AckRequest arg2) throws Exception {
				
				work.applySpellSimilarity(Double.parseDouble(data));
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

		server.addEventListener("applyFrequentThreshold", String.class, new DataListener<String>() {

			public void onData(SocketIOClient client, String data,
					AckRequest arg2) throws Exception {
				
				work.applyCompositeFrequent(Double.parseDouble(data));
			}
        });
		
		server.addEventListener("applyUniqueThreshold", String.class, new DataListener<String>() {

			public void onData(SocketIOClient client, String data,
					AckRequest arg2) throws Exception {
				
				work.applyCompositeUnique(Double.parseDouble(data));
			}
        });
		
		server.addEventListener("applyMaxSize", String.class, new DataListener<String>() {

			public void onData(SocketIOClient client, String data,
					AckRequest arg2) throws Exception {
				
				work.applyCompositeSize(Integer.parseInt(data));
			}
        });
		
	    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
	    // Postprocessing - Dataset 4
	    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
		server.addEventListener("applyPostFilter", String.class, new DataListener<String>() {

			public void onData(SocketIOClient client, String data,
					AckRequest arg2) throws Exception {
				
				work.applyPostFilter(Double.parseDouble(data));
				
				client.sendEvent("output", work.sendOverview(4));
				System.out.println(data);
			}
        });
		
		server.addEventListener("applyPostReplace", String.class, new DataListener<String>() {

			public void onData(SocketIOClient client, String data,
					AckRequest arg2) throws Exception {
				
				work.applyPostReplace(data);
				
				client.sendEvent("output", work.sendOverview(4));
				System.out.println(data);
			}
        });
		
		server.addEventListener("applyPostLength", String.class, new DataListener<String>() {

			public void onData(SocketIOClient client, String data,
					AckRequest arg2) throws Exception {
				
				work.applyPostLength(Integer.parseInt(data));
				
				client.sendEvent("output", work.sendOverview(4));
				System.out.println(data);
			}
        });
		
		server.addEventListener("applyPostAll", String.class, new DataListener<String>() {

			public void onData(SocketIOClient client, String data,
					AckRequest arg2) throws Exception {
				
				work.applyPostAll(Boolean.parseBoolean(data));
				
				client.sendEvent("output", work.sendOverview(4));
				System.out.println(data);
			}
        });
		
		server.addEventListener("applySalvaging", String.class, new DataListener<String>() {

			public void onData(SocketIOClient client, String data,
					AckRequest arg2) throws Exception {
				
				work.applySalvaging();
				
				client.sendEvent("output", work.sendOverview(4));
				System.out.println(data);
			}
        });
		
		server.addEventListener("computeSalvaging", String.class, new DataListener<String>() {

			public void onData(SocketIOClient client, String data,
					AckRequest arg2) throws Exception {
				
				work.computeSalvaging();
				
				client.sendEvent("output", work.sendOverview(4));
				System.out.println(data);
			}
        });
		
	    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
	    // Rest
	    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
		
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
