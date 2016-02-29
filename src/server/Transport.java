package server;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;

public class Transport {

	private Workflow work = new Workflow();
	private SocketIOServer server;

	protected Transport(SocketIOServer server) {
		super();
		this.server = server;
	}

	private void sendParams(SocketIOClient client) {
		// Preprocessing
		client.sendEvent("preFilterParams", work.sendPreFilterParams());
		client.sendEvent("preRemoveParams", work.sendPreRemoveParams());
		client.sendEvent("preReplaceParams", work.sendPreReplaceParams());
		client.sendEvent("preDictionaryParams", work.sendPreDictionaryParams());

		// Spell correct
		client.sendEvent("spellImportance", work.sendSpellImportanceParams());
		client.sendEvent("spellSimilarity", work.sendSpellSimilarityParams());
		client.sendEvent("spellMinWordSize", work.sendSpellMinWordSizeParams());
		client.sendEvent("spellDictionaryParams", work.sendSpellDictionaryParams());

		// Composite
		client.sendEvent("compFrequentParams", work.sendCompFrequentParams());
		client.sendEvent("compUniqueParams", work.sendCompUniqueParams());
		client.sendEvent("compSizeParams", work.sendCompSizeParams());
		client.sendEvent("compOccParams", work.sendCompOccParams());
		client.sendEvent("compSplitParams", work.sendCompSplitParams());
		
		// Postprocess
		client.sendEvent("postFilterParams", work.sendPostFilterParams());
		client.sendEvent("postAllParams", work.sendPostAllParams());
		client.sendEvent("postReplaceParams", work.sendPostReplaceParams());
		client.sendEvent("postLengthParams", work.sendPostLengthParams());
		client.sendEvent("postSplitParams", work.sendPostSplitParams());
	}

	private void sendData(SocketIOClient client)
	{
		// Main
		client.sendEvent("mainData", work.sendOverview(0));

		// Send Pre data
		client.sendEvent("preFilterData", work.sendPreFilterHistogram());
		client.sendEvent("preFilterGrid", work.sendPreFilter());

		if(work.getSimpleRun())
		{
			// Send Spell data
			client.sendEvent("similarities", work.sendSimilarityHistogram());
			client.sendEvent("vocab", work.sendVocab());
			client.sendEvent("importance", work.sendPreVocabHistogram());

			// Send Composite data
			client.sendEvent("frequentGroups", work.sendFrequentGroups());
			client.sendEvent("frequentData", work.sendFrequentHistogram());
			client.sendEvent("uniqueGroups", work.sendUniqueGroups());
			client.sendEvent("uniqueData", work.sendUniqueHistogram());

			// Send Post data
			client.sendEvent("postFilterGrid", work.sendPostVocab());
			client.sendEvent("postFilterData", work.sendPostVocabHistogram());
			client.sendEvent("output", work.sendOverview(3));

			// Send Final data
			client.sendEvent("postImportantWords", work.sendPostImportant());
			client.sendEvent("postSalvageWords", work.sendPostSalvage());
		}

		if(work.getComplexRun())
		{
			// Send Final data
			client.sendEvent("postSalvageData", work.sendPostSalvageData());
			client.sendEvent("output", work.sendOverview(4));
		}
	}
	
	public void initialize() {
		// Connection
		server.addConnectListener(new ConnectListener() {

			public void onConnect(SocketIOClient client) {
				System.out.println("Connect");

				sendParams(client);
				sendData(client);
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

		server.addEventListener("applySpellImportedData", String.class, new DataListener<String>() {

			public void onData(SocketIOClient client, String data,
			                   AckRequest arg2) throws Exception {

				work.applySpellImport(data, client);
			}
		});

		server.addEventListener("applySpellCorrect", String.class, new DataListener<String>() {

			public void onData(SocketIOClient client, String data,
			                   AckRequest arg2) throws Exception {

				work.applySpellCorrect(data, client);
			}
		});

		server.addEventListener("applySpellMinWordSize", String.class, new DataListener<String>() {

			public void onData(SocketIOClient client, String data,
			                   AckRequest arg2) throws Exception {

				work.applySpellMinWordSize(Integer.parseInt(data), client);
			}
		});

		server.addEventListener("getCluster", String.class, new DataListener<String>() {

			public void onData(SocketIOClient client, String data,
			                   AckRequest arg2) throws Exception {
				
				client.sendEvent("cluster", work.sendCluster(data));
			}
		});
		
		server.addEventListener("getReplacements", String.class, new DataListener<String>() {

			public void onData(SocketIOClient client, String data,
			                   AckRequest arg2) throws Exception {
				
				client.sendEvent("replacements", work.sendReplacements(data));
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
		
		server.addEventListener("applyCompositeParams", String.class, new DataListener<String>() {

			public void onData(SocketIOClient client, String data,
			                   AckRequest arg2) throws Exception {
				
				work.applyCompositeParams(data, client);
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

		server.addEventListener("applyPostParams", String.class, new DataListener<String>() {

			public void onData(SocketIOClient client, String data,
			                   AckRequest arg2) throws Exception {
				
				work.applyPostParams(data, client);
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
