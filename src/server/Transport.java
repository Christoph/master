package server;

import com.corundumstudio.socketio.SocketIOServer;

public class Transport {

	private Workflow work = new Workflow();
	private SocketIOServer server;

	protected Transport(SocketIOServer server) {
		super();
		this.server = server;
	}


	public void initialize() {
		// Connection
		server.addConnectListener(client -> {
			System.out.println("Connect");

			client.sendEvent("dataLoaded", work.sendDataLoaded());
			client.sendEvent("isRunning", work.sendStatus());

			client.sendEvent("mainData", work.sendOverview(0));
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

			work.sendParams(client);
			work.sendData(client);
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

		// Get Blacklist
		server.addEventListener("getBlacklist", String.class, (client, data, arg2) -> client.sendEvent("preDictionaryParams", work.sendPreDictionaryParams()));

		//////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// Spell Checking
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////

		server.addEventListener("applySpellImportedData", String.class, (client, data, arg2) -> work.applySpellImport(data, client));

		server.addEventListener("applySpellCorrect", String.class, (client, data, arg2) -> work.applySpellCorrect(data, client));

		server.addEventListener("applySpellMinWordSize", String.class, (client, data, arg2) -> work.applySpellMinWordSize(Integer.parseInt(data), client));

		server.addEventListener("getCluster", String.class, (client, data, arg2) -> client.sendEvent("cluster", work.sendCluster(data)));

		server.addEventListener("getReplacements", String.class, (client, data, arg2) -> client.sendEvent("replacementData", work.sendReplacements(Double.parseDouble(data))));

		server.addEventListener("getReplacementData", String.class, (client, data, arg2) -> client.sendEvent("replacementData", work.sendReplacementData(data)));

		// Get Whitelist
		server.addEventListener("getWhitelist", String.class, (client, data, arg2) -> client.sendEvent("spellDictionaryParams", work.sendSpellDictionaryParams()));

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

		server.addEventListener("applyPostRemove", String.class, (client, data, arg2) -> work.applyPostRemove(data, client));

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
