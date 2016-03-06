package server;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;

public class Transport {

	private Workflow work = new Workflow();
	private SocketIOServer server;

	protected Transport(SocketIOServer server) {
		super();
		this.server = server;
	}

	private void sendParams(SocketIOClient client) {
		System.out.println("params");

		client.sendEvent("dataLoaded", work.sendDataLoaded());
		client.sendEvent("isRunning", work.sendStatus());

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
		client.sendEvent("outputState", "Multiword Tags");

		// Send Final data
		client.sendEvent("postImportantWords", work.sendPostImportant());
		client.sendEvent("postSalvageWords", work.sendPostSalvage());

	}
	
	public void initialize() {
		// Connection
		server.addConnectListener(client -> {
			System.out.println("Connect");

			sendParams(client);
			sendData(client);
		});
		
		
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// Main
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		server.addEventListener("applyImportedData", String.class, (client, data, arg2) -> work.applyImportedData(data));

		server.addEventListener("applyImportedDataCount", String.class, (client, data, arg2) -> work.applyImportedDataCount(Integer.parseInt(data), client));
		
		server.addEventListener("applyImportedDataFinished", String.class, (client, data, arg2) -> work.applyImportedDataFinished(client));
		
		server.addEventListener("selectMode", String.class, (client, data, arg2) -> work.selectMode(data, client));

		server.addEventListener("computeWorkflow", String.class, (client, data, arg2) -> work.computeWorkflow(client));

		server.addEventListener("getParameters", String.class, (client, data, arg2) -> {

			sendParams(client);
			sendData(client);
		});
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// Preprocessing
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		// Apply characters to remove
		server.addEventListener("applyPreRemoveCharacters", String.class, (client, data, arg2) -> work.applyPreRemove(data, client));
		
		// Apply characters to replace
		server.addEventListener("applyPreReplaceCharacters", String.class, (client, data, arg2) -> work.applyPreReplace(data, client));
		
		// Apply dictionary
		server.addEventListener("applyPreImportedData", String.class, (client, data, arg2) -> work.applyPreDictionary(data, client));
		
		// Apply filter
		server.addEventListener("applyPrefilter", String.class, (client, data, arg2) -> work.applyPreFilter(Integer.parseInt(data), client));
		
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// Spell Checking
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////

		server.addEventListener("applySpellImportedData", String.class, (client, data, arg2) -> work.applySpellImport(data, client));

		server.addEventListener("applySpellCorrect", String.class, (client, data, arg2) -> work.applySpellCorrect(data, client));

		server.addEventListener("applySpellMinWordSize", String.class, (client, data, arg2) -> work.applySpellMinWordSize(Integer.parseInt(data), client));

		server.addEventListener("getCluster", String.class, (client, data, arg2) -> client.sendEvent("cluster", work.sendCluster(data)));
		
		server.addEventListener("getReplacements", String.class, (client, data, arg2) -> client.sendEvent("replacements", work.sendReplacements(data)));

		server.addEventListener("getReplacementData", String.class, (client, data, arg2) -> client.sendEvent("replacementData", work.sendReplacementData(data)));

		//////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// Composites
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////

		server.addEventListener("applyFrequentThreshold", String.class, (client, data, arg2) -> work.applyCompositeFrequent(Double.parseDouble(data), client));
		
		server.addEventListener("applyUniqueThreshold", String.class, (client, data, arg2) -> work.applyCompositeUnique(Double.parseDouble(data), client));
		
		server.addEventListener("applyCompositeParams", String.class, (client, data, arg2) -> work.applyCompositeParams(data, client));

		//////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// Postprocessing - Dataset 4
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////

		server.addEventListener("applyPostFilter", String.class, (client, data, arg2) -> work.applyPostFilter(Double.parseDouble(data), client));
		
		server.addEventListener("applyPostReplace", String.class, (client, data, arg2) -> work.applyPostReplace(data, client));

		server.addEventListener("applyPostParams", String.class, (client, data, arg2) -> work.applyPostParams(data, client));
		
		server.addEventListener("applySalvaging", String.class, (client, data, arg2) -> work.applySalvaging(client));
		
		server.addEventListener("computeSalvaging", String.class, (client, data, arg2) -> {

			work.computeSalvaging(client);

			client.sendEvent("postSalvageData", work.sendPostSalvageData());
		});
		
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// Rest
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////

		// Get history data
		server.addEventListener("getHistory", String.class, (client, data, arg2) -> client.sendEvent("history", work.sendHistory(data)));
	}
}
