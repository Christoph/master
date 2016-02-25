package server;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;

public class Transport {

	private Workflow work = new Workflow();
	private SocketIOServer server;
	private Boolean devMode = false;
	
	protected Transport(SocketIOServer server) {
		super();
		this.server = server;
	}

	private void sendParams(SocketIOClient client) {
		// Main
		client.sendEvent("mainData", work.sendOverview(0));
		
		// Preprocessing
		client.sendEvent("preFilterParams", work.sendPreFilterParams());
		client.sendEvent("preRemoveParams", work.sendPreRemoveParams());
		client.sendEvent("preReplaceParams", work.sendPreReplaceParams());
		client.sendEvent("preDictionaryParams", work.sendPreDictionaryParams());

		// Spell correct
		client.sendEvent("spellImportance", work.sendSpellImportanceParams());
		client.sendEvent("spellSimilarity", work.sendSpellSimilarityParams());
		client.sendEvent("spellDictionaryParams", work.sendSpellDictionaryParams());

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
		client.sendEvent("postSplitParams", work.sendPostSplitParams());
	}
	
	public void initialize() {
		// Connection
		server.addConnectListener(new ConnectListener() {

			public void onConnect(SocketIOClient client) {
				System.out.println("Connect");

				if (devMode) {
					work.init(client);

					sendParams(client);

					work.computePreprocessing(client);
				} else {
					sendParams(client);
				}
			}
		});
		
		
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// Main
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		server.addEventListener("applyImportedData", String.class, new DataListener<String>() {

			public void onData(SocketIOClient client, String data,
			                   AckRequest arg2) throws Exception {
				
				work.applyImportedData(data);
			}
		});

		server.addEventListener("applyImportedDataCount", String.class, new DataListener<String>() {

			public void onData(SocketIOClient client, String data,
			                   AckRequest arg2) throws Exception {
				
				work.applyImportedDataCount(Integer.parseInt(data));
			}
		});
		
		server.addEventListener("applyImportedDataFinished", String.class, new DataListener<String>() {

			public void onData(SocketIOClient client, String data,
			                   AckRequest arg2) throws Exception {
				
				work.applyImportedDataFinished(client);
			}
		});
		
		server.addEventListener("runAll", String.class, new DataListener<String>() {

			public void onData(SocketIOClient client, String data,
			                   AckRequest arg2) throws Exception {

				work.runAll(client);
			}
		});

		//////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// Preprocessing
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		// Apply characters to remove
		server.addEventListener("applyPreRemoveCharacters", String.class, new DataListener<String>() {

			public void onData(SocketIOClient client, String data,
			                   AckRequest arg2) throws Exception {
				
				work.applyPreRemove(data, client);
			}
		});
		
		// Apply characters to replace
		server.addEventListener("applyPreReplaceCharacters", String.class, new DataListener<String>() {

			public void onData(SocketIOClient client, String data,
			                   AckRequest arg2) throws Exception {
				
				work.applyPreReplace(data, client);
			}
		});
		
		// Apply dictionary
		server.addEventListener("applyPreImportedData", String.class, new DataListener<String>() {

			public void onData(SocketIOClient client, String data,
			                   AckRequest arg2) throws Exception {
				
				work.applyPreDictionary(data, client);
			}
		});
		
		// Apply filter
		server.addEventListener("applyPrefilter", String.class, new DataListener<String>() {

			public void onData(SocketIOClient client, String data,
			                   AckRequest arg2) throws Exception {
				
				work.applyPreFilter(Integer.parseInt(data), client);
			}
		});
		
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// Spell Checking
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////

		// Compute clusters
		server.addEventListener("applySpellImportedData", String.class, new DataListener<String>() {

			public void onData(SocketIOClient client, String data,
			                   AckRequest arg2) throws Exception {

				work.applySpellImport(data, client);
			}
		});
		// Compute clusters
		server.addEventListener("applySpellImportance", String.class, new DataListener<String>() {

			public void onData(SocketIOClient client, String data,
			                   AckRequest arg2) throws Exception {
				
				work.applySpellImportance(Double.parseDouble(data), client);
			}
		});
		
		// Compute clusters
		server.addEventListener("applySpellSimilarity", String.class, new DataListener<String>() {

			public void onData(SocketIOClient client, String data,
			                   AckRequest arg2) throws Exception {
				
				work.applySpellSimilarity(Double.parseDouble(data), client);
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
				
				work.applyCompositeFrequent(Double.parseDouble(data), client);
			}
		});
		
		server.addEventListener("applyUniqueThreshold", String.class, new DataListener<String>() {

			public void onData(SocketIOClient client, String data,
			                   AckRequest arg2) throws Exception {
				
				work.applyCompositeUnique(Double.parseDouble(data), client);
			}
		});
		
		server.addEventListener("applyMaxSize", String.class, new DataListener<String>() {

			public void onData(SocketIOClient client, String data,
			                   AckRequest arg2) throws Exception {
				
				work.applyCompositeSize(Integer.parseInt(data), client);
			}
		});
		
		server.addEventListener("applyCompSplit", String.class, new DataListener<String>() {

			public void onData(SocketIOClient client, String data,
			                   AckRequest arg2) throws Exception {
				
				work.applyCompositeSplit(Boolean.parseBoolean(data), client);
			}
		});
		
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// Postprocessing - Dataset 4
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////

		server.addEventListener("applyPostFilter", String.class, new DataListener<String>() {

			public void onData(SocketIOClient client, String data,
			                   AckRequest arg2) throws Exception {
				
				work.applyPostFilter(Double.parseDouble(data), client);
			}
		});
		
		server.addEventListener("applyPostReplace", String.class, new DataListener<String>() {

			public void onData(SocketIOClient client, String data,
			                   AckRequest arg2) throws Exception {
				
				work.applyPostReplace(data, client);
			}
		});
		
		server.addEventListener("applyPostLength", String.class, new DataListener<String>() {

			public void onData(SocketIOClient client, String data,
			                   AckRequest arg2) throws Exception {
				
				work.applyPostLength(Integer.parseInt(data), client);
			}
		});
		
		server.addEventListener("applyPostAll", String.class, new DataListener<String>() {

			public void onData(SocketIOClient client, String data,
			                   AckRequest arg2) throws Exception {
				
				work.applyPostAll(Boolean.parseBoolean(data), client);
			}
		});
		
		server.addEventListener("applyPostSplit", String.class, new DataListener<String>() {

			public void onData(SocketIOClient client, String data,
			                   AckRequest arg2) throws Exception {
				
				work.applyPostSplit(Boolean.parseBoolean(data), client);
			}
		});
		
		server.addEventListener("applySalvaging", String.class, new DataListener<String>() {

			public void onData(SocketIOClient client, String data,
			                   AckRequest arg2) throws Exception {
				
				work.applySalvaging(client);
			}
		});
		
		server.addEventListener("computeSalvaging", String.class, new DataListener<String>() {

			public void onData(SocketIOClient client, String data,
			                   AckRequest arg2) throws Exception {
				
				work.computeSalvaging(client);
				
				client.sendEvent("postSalvageData", work.sendPostSalvageData());
			}
		});
		
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// Rest
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////

		
		// Get output data
		server.addEventListener("getOutputData", String.class, new DataListener<String>() {

			public void onData(SocketIOClient client, String data,
			                   AckRequest arg2) throws Exception {
				
				if (data.equals("output")) {
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
