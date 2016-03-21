package server;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.corundumstudio.socketio.SocketIOClient;

import core.json.gridHistory;
import processing.Composite;
import processing.Helper;
import processing.Postprocess;
import processing.Preprocess;
import processing.Spellcorrect;
import processing.Weighting;
import core.Tag;
import core.json.gridOverview;

public class Workflow {
	
	// Initialize variables and classes
	private Helper help = new Helper();
	private Weighting weighting = new Weighting();

	private int globalID = 0;
	private int count, packages;
	private int lastAppliedStep = 0;

	private Boolean running = false;
	private String mode = "";
	private Boolean dataLoaded = false;
	private Boolean runs = false;

	private Boolean preDirty = false;
	private Boolean spellDirty = false;
	private Boolean compDirty = false;


	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Parameters
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	// Data
	private List<List<Tag>> tags = new ArrayList<>();
	private List<Map<String, Double>> vocabs = new ArrayList<>();

	private Map<String, Long> tagsFreq = new HashMap<>();

	private List<String> whitelistWords = new ArrayList<>();
	private List<String> whitelistGroups = new ArrayList<>();
	private List<String> whitelistVocab = new ArrayList<>();
	private List<String> blacklist = new ArrayList<>();

	// Initialize pipeline steps
	// The index selects the working copy. 0 = original
	private Preprocess preprocess;
	private Spellcorrect spellcorrect;
	private Composite composite;
	private Postprocess postprocess;


	public Workflow() {
		for (int i = 0; i < 5; i++) {
			tags.add(new ArrayList<>());
		}

		for (int i = 0; i < 5; i++) {
			vocabs.add(new HashMap<>());
		}

		preprocess = new Preprocess(blacklist);
		spellcorrect = new Spellcorrect(whitelistWords, whitelistGroups, whitelistVocab);
		composite = new Composite(whitelistGroups);
		postprocess = new Postprocess();
	}

	public void computeWorkflow(SocketIOClient client)
	{
		System.out.println("computeWorkflow");

		if(!runs)
		{
			runs = true;

			if (preDirty) {
				client.sendEvent("computePre", "started");
				client.sendEvent("computeSpell", "started");
				client.sendEvent("computeComp", "started");
				computePreprocessing(client);
				client.sendEvent("computePre", "finished");

				preDirty = false;
				spellDirty = true;
			}

			if (spellDirty) {
				client.sendEvent("computeSpell", "started");
				client.sendEvent("computeComp", "started");
				computeSpellCorrect(client);
				client.sendEvent("computeSpell", "finished");

				spellDirty = false;
				compDirty = true;
			}

			if (compDirty) {
				client.sendEvent("computeComp", "started");
				computeGroups(client);
				client.sendEvent("computeComp", "finished");

				compDirty = false;
			}

			runs = false;
		}
	}

	public void sendParams(SocketIOClient client) {
		// Preprocessing
		client.sendEvent("preFilterParams", sendPreFilterParams());
		client.sendEvent("preRemoveParams", sendPreRemoveParams());
		client.sendEvent("preReplaceParams", sendPreReplaceParams());
		client.sendEvent("preDictionaryParams", sendPreDictionaryParams());

		// Spell correct
		client.sendEvent("spellImportance", sendSpellImportanceParams());
		client.sendEvent("spellSimilarity", sendSpellSimilarityParams());
		client.sendEvent("spellMinWordSize", sendSpellMinWordSizeParams());
		client.sendEvent("spellDictionaryParams", sendSpellDictionaryParams());

		// Composite
		client.sendEvent("compFrequentParams", sendCompFrequentParams());
		client.sendEvent("compUniqueParams", sendCompUniqueParams());
		client.sendEvent("compSizeParams", sendCompSizeParams());
		client.sendEvent("compOccParams", sendCompOccParams());
		client.sendEvent("compSplitParams", sendCompSplitParams());

		// Postprocess
		client.sendEvent("postFilterParams", sendPostFilterParams());
		client.sendEvent("postAllParams", sendPostAllParams());
		client.sendEvent("postReplaceParams", sendPostReplaceParams());
		client.sendEvent("postRemoveParams", sendPostRemoveParams());
		client.sendEvent("postLengthParams", sendPostLengthParams());
		client.sendEvent("postSplitParams", sendPostSplitParams());
	}

	public void sendData(SocketIOClient client)
	{
		//Vocab Data
		client.sendEvent("rawVocabSize", vocabs.get(0).size());
		client.sendEvent("preVocabSize", vocabs.get(1).size());
		client.sendEvent("spellVocabSize", vocabs.get(2).size());
		client.sendEvent("compVocabSize", vocabs.get(3).size());
		client.sendEvent("postVocabSize", vocabs.get(4).size());

		//Dataset Data
		client.sendEvent("rawDataset", tags.get(0).size());
		client.sendEvent("preDataset", tags.get(1).size());
		client.sendEvent("spellDataset", tags.get(2).size());
		client.sendEvent("compDataset", tags.get(3).size());
		client.sendEvent("postDataset", tags.get(4).size());

		// Send Pre data
		client.sendEvent("preFilterData", sendPreFilterHistogram());
		client.sendEvent("preFilterGrid", sendPreFilter());

		// Send Spell data
		client.sendEvent("similarities", sendSimilarityHistogram());
		client.sendEvent("vocab", sendVocab());
		client.sendEvent("importance", sendPreVocabHistogram());
		client.sendEvent("replacementData", sendReplacements(0.5));

		// Send Composite data
		client.sendEvent("frequentGroups", sendFrequentGroups());
		client.sendEvent("frequentData", sendFrequentHistogram());
		client.sendEvent("uniqueGroups", sendUniqueGroups());
		client.sendEvent("uniqueData", sendUniqueHistogram());

		// Send Post data
		client.sendEvent("postFilterGrid", sendPostVocab());
		client.sendEvent("postFilterData", sendPostVocabHistogram());
		client.sendEvent("output", sendOverview(3));
		client.sendEvent("resultVocab", sendVocab(3));
		client.sendEvent("outputState", "Multiword Tags");

		// Send Final data
		client.sendEvent("postImportantWords", sendPostImportant());

	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Load data - Dataset 0
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void applyImportedData(String json) {
		List<Map<String, Object>> map = help.jsonStringToList(json);
		String item, name, weight;

		if (packages < count) {
			packages++;

			for (Map<String, Object> aMap : map) {
				try {
					// Starting ID with 1 to be database ready
					globalID++;

					item = String.valueOf(aMap.get("item"));
					name = String.valueOf(aMap.get("tag"));
					weight = String.valueOf(aMap.get("weight"));

					tags.get(0).add(new Tag(globalID, item, name.toLowerCase().replaceAll(" +"," "), Double.parseDouble(weight), 0, 0));
				} catch (Exception e) {
					System.out.println(aMap);
				}
			}
		}
	}
	
	public void applyImportedDataFinished(SocketIOClient client) {
		// Compute word frequency
		help.wordFrequency(tags.get(0), tagsFreq);
		weighting.vocab(tags.get(0), vocabs.get(0));

		client.sendEvent("rawDataset", tags.get(0).size());
		client.sendEvent("rawVocabSize", vocabs.get(0).size());

		// Send that the data is loaded
		dataLoaded = true;
		client.sendEvent("dataLoaded", sendDataLoaded());
		System.out.println("DataLoaded");
	}
	
	public void applyImportedDataCount(int count, SocketIOClient client) {
		tags.forEach(List<Tag>::clear);

		globalID = 0;
		
		this.count = count;
		this.packages = 0;

		running = false;
		client.sendEvent("isRunning",sendStatus());
	}

	public void selectMode(String data, SocketIOClient client) {
		if(data.equals("free") || data.equals("linked") ||data.equals("guided"))
		{
			client.sendEvent("initRunning","started");

			mode = data;
			client.sendEvent("selectedMode",sendMode());

			// Wait a little bit for the angular ng-if statement
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			sendParams(client);
			sendData(client);

			client.sendEvent("initRunning","finished");

			computeWorkflow(client);
		}

		if(data.equals("reconnect"))
		{
			client.sendEvent("initRunning","started");

			// Mode stays
			client.sendEvent("selectedMode",sendMode());

			// Wait a little bit for the angular ng-if statement
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			sendParams(client);
			sendData(client);

			client.sendEvent("initRunning","finished");
		}

		if(!running)
		{
			running = true;
		}
	}

	public String sendStatus() {
		return running.toString();
	}

	public String sendMode() {
		return mode;
	}

	public String sendDataLoaded() {
		return dataLoaded.toString();
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Preprocessing - Dataset 1
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public void computePreprocessing(SocketIOClient client) {
		help.resetStep(tags, 1);

		// Remove tags
		preprocess.applyFilter(tags.get(1), tagsFreq);
		
		// Remove characters
		preprocess.removeCharacters(tags.get(1));
		
		// Replace characters
		preprocess.replaceCharacters(tags.get(1));

		// Remove blacklisted words
		help.removeBlacklistedWords(tags.get(1), blacklist);

		// Create preFilter vocab
		weighting.vocab(tags.get(1), vocabs.get(1));

		// Cluster words for further use
		spellcorrect.clustering(tags.get(1), vocabs.get(1), whitelistVocab);
		
		client.sendEvent("preDataset", tags.get(1).size());
		client.sendEvent("preVocabSize", vocabs.get(1).size());
		client.sendEvent("similarities", sendSimilarityHistogram());
		client.sendEvent("vocab", sendVocab());
		client.sendEvent("importance", sendPreVocabHistogram());
	}
	
	// Apply changes
	public void applyPreFilter(int threshold, SocketIOClient client) {
		// Set threshold
		preprocess.setFilter(threshold);

		preDirty = true;
	}
	
	public void applyPreRemove(String chars, SocketIOClient client) {
		// Set characters for removal
		preprocess.setRemove(chars);

		preDirty = true;
	}
	
	public void applyPreReplace(String json, SocketIOClient client) {
		List<Map<String, Object>> map = help.jsonStringToList(json);
		
		preprocess.setReplace(map);

		preDirty = true;
	}
	
	public void applyPreDictionary(String json, SocketIOClient client) {
		List<Map<String, Object>> map = help.jsonStringToList(json);
		
		preprocess.setDictionary(map);

		preDirty = true;
	}
	
	// Send Params
	public double sendPreFilterParams() {
		return preprocess.getFilter();
	}
	
	public String sendPreRemoveParams() {
		return preprocess.getRemove();
	}
	
	public List<String> sendPreReplaceParams() {
		return preprocess.getReplace();
	}
	
	public List<String> sendPreDictionaryParams() {
		return blacklist;
	}
	
	// Send Data
	public String sendPreFilter() {
		return help.objectToJsonString(preprocess.preparePreFilter(tagsFreq));
	}
	
	public String sendPreFilterHistogram() {
		return help.objectToJsonString(preprocess.preparePreFilterHistogram(tagsFreq));
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Spell Correction - Dataset 2
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public void computeSpellCorrect(SocketIOClient client) {
		// Reset current stage
		help.resetStep(tags, 2);

		// Apply clustering
		spellcorrect.applyClustering(tags.get(2), vocabs.get(1));
		
		// Compute further data
		composite.group(tags.get(2));

		// New Vocab
		weighting.vocab(tags.get(2), vocabs.get(2));

		client.sendEvent("spellDataset", tags.get(2).size());
		client.sendEvent("spellVocabSize", vocabs.get(2).size());
		client.sendEvent("frequentGroups", sendFrequentGroups());
		client.sendEvent("frequentData", sendFrequentHistogram());
		client.sendEvent("uniqueGroups", sendUniqueGroups());
		client.sendEvent("uniqueData", sendUniqueHistogram());
		client.sendEvent("replacementData", sendReplacements(0.5));
	}
	
	// Apply changes
	public void applySpellCorrect(String json, SocketIOClient client) {
		List<Map<String, Object>> map = help.jsonStringToList(json);
		double imp, sim;

		if(map.get(0).get("importance").equals(1) || map.get(0).get("importance").equals(0))
		{
			imp = ((Integer) map.get(0).get("importance"));
		}
		else
		{
			imp = ((Double) map.get(0).get("importance"));
		}

		if(map.get(0).get("similarity").equals(1) || map.get(0).get("similarity").equals(0))
		{
			sim = ((Integer) map.get(0).get("similarity"));
		}
		else
		{
			sim = ((Double) map.get(0).get("similarity"));
		}

		spellcorrect.setSpellImportance(imp);
		spellcorrect.setSpellSimilarity(sim);

		spellDirty = true;
	}

	public void applySpellImport(String json, SocketIOClient client) {
		List<Map<String, Object>> map = help.jsonStringToList(json);

		spellcorrect.setDictionary(map);

		spellDirty = true;
	}

	public void applySpellMinWordSize(int minWordSize, SocketIOClient client) {
		// Set minWordSize
		spellcorrect.setMinWordSize(minWordSize);

		spellDirty = true;
	}

	// Send Params
	public double sendSpellImportanceParams() {
		return spellcorrect.getSpellImportance();
	}
	
	public double sendSpellSimilarityParams() {
		return spellcorrect.getSpellSimilarity();
	}
	
	public int sendSpellMinWordSizeParams() {
		return spellcorrect.getMinWordSize();
	}

	public List<String> sendSpellDictionaryParams() {
		List<String> temp = new ArrayList<>();

		temp.addAll(whitelistWords);
		temp.addAll(whitelistGroups);

		return temp;
	}

	// Send Data
	public String sendCluster(String tag) {
		return help.objectToJsonString(spellcorrect.prepareCluster(tag, vocabs.get(1)));
	}
	
	public String sendSimilarityHistogram() {
		return help.objectToJsonString(spellcorrect.prepareSimilarityHistogram());
	}
	
	private double getSim(List<Map<String, Object>> map) {
		double sim;
		if(map.get(0).get("similarity").equals(0))
		{
			sim = ((Integer) map.get(0).get("similarity"));
		}
		else if(map.get(0).get("similarity").equals(1))
		{
			sim = ((Integer) map.get(0).get("similarity"));
		}
		else
		{
			sim = ((Double) map.get(0).get("similarity"));
		}
		return sim;
	}

	public String sendReplacements(double sim)
	{
		return help.objectToJsonString(spellcorrect.prepareReplacements(sim, vocabs.get(1)));
	}

	public String sendReplacementData(String json)
	{
		List<Map<String, Object>> map = help.jsonStringToList(json);

		double imp, sim;

		if(map.get(0).get("importance").equals(0)) return "";

		if(map.get(0).get("importance").equals(1))
		{
			imp = ((Integer) map.get(0).get("importance"));
		}
		else
		{
			imp = ((Double) map.get(0).get("importance"));
		}

		sim = getSim(map);

		return help.objectToJsonString(spellcorrect.prepareReplacementData(sim, imp, vocabs.get(1)));
	}

	public String sendVocab() {
		return help.objectToJsonString(help.prepareVocab(vocabs.get(1)));
	}

	public String sendPreVocabHistogram() {
		return help.objectToJsonString(help.prepareVocabHistogram(vocabs.get(1)));
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Composites - Dataset 3
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public void computeGroups(SocketIOClient client) {
		help.resetStep(tags, 3);
		
		// Compute all word groups
		composite.applyGroups(tags.get(3));

		// Provide further data
		weighting.vocab(tags.get(3), vocabs.get(3));

		client.sendEvent("postFilterGrid", sendPostVocab());
		client.sendEvent("postFilterData", sendPostVocabHistogram());

		client.sendEvent("compVocabSize", vocabs.get(3).size());
		client.sendEvent("compDataset", tags.get(3).size());
		client.sendEvent("output", sendOverview(3));
		client.sendEvent("resultVocab", sendVocab(3));
		client.sendEvent("outputState", "Multiword Tags");

		prepareSalvaging(client);
	}
	
	// Apply changes
	public void applyCompositeFrequent(double threshold, SocketIOClient client) {
		// Set threshold
		composite.setFrequentThreshold(threshold);
		
		compDirty = true;

		// Send new grid information after a change of threshold
		client.sendEvent("uniqueGroups", sendUniqueGroups());
	}
	
	public void applyCompositeUnique(double threshold, SocketIOClient client) {
		// Set threshold
		composite.setJaccardThreshold(threshold);

		compDirty = true;

		// Send new grid information after a change of threshold
		client.sendEvent("frequentGroups", sendFrequentGroups());
	}
	
	public void applyCompositeParams(String json, SocketIOClient client) {
		List<Map<String, Object>> map = help.jsonStringToList(json);

		composite.setMaxGroupSize((Integer) map.get(0).get("maxGroupSize"));
		composite.setMinOccurrence((Integer) map.get(0).get("minOcc"));
		composite.setSplit((Boolean) map.get(0).get("split"));

		compDirty = true;
	}
	
	// Send Params
	public double sendCompFrequentParams() {
		return composite.getFrequentThreshold();
	}
	
	public double sendCompUniqueParams() {
		return composite.getJaccardThreshold();
	}
	
	public int sendCompSizeParams() {
		return composite.getMaxGroupSize();
	}
	
	public int sendCompOccParams() {
		return composite.getMinOccurrence();
	}

	public Boolean sendCompSplitParams() {
		return composite.getSplit();
	}

	// Send Data
	public String sendFrequentGroups() {
		return help.objectToJsonString(composite.prepareFrequentGroups());
	}
	
	public String sendUniqueGroups() {
		return help.objectToJsonString(composite.prepareUniqueGroups());
	}
	
	public String sendFrequentHistogram() {
		return help.objectToJsonString(composite.prepareFrequentHistogram());
	}
	
	public String sendUniqueHistogram() {
		return help.objectToJsonString(composite.prepareUniqueHistogram());
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Postprocessing - Dataset 4
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public void prepareSalvaging(SocketIOClient client) {
		postprocess.updateImportantWords(vocabs.get(3));

		client.sendEvent("postImportantWords", sendPostImportant());
	}
	
	public void computeSalvaging(SocketIOClient client) {
		client.sendEvent("postSalvaging", "true");
		postprocess.computeSalvaging(vocabs.get(3));
		client.sendEvent("postSalvaging", "false");

		client.sendEvent("postSalvageData", sendPostSalvageData());
	}
	
	// Apply changes
	public void applySalvaging(SocketIOClient client) {
		help.resetStep(tags, 4);
		
		client.sendEvent("computePost", "started");

		computeSalvaging(client);
		postprocess.applySalvaging(tags.get(4));

		client.sendEvent("computePost", "finished");

		weighting.vocab(tags.get(4), vocabs.get(4));

		client.sendEvent("postVocabSize", vocabs.get(4).size());
		client.sendEvent("postDataset", tags.get(4).size());
		client.sendEvent("output", sendOverview(4));
		client.sendEvent("resultVocab", sendVocab(4));
		client.sendEvent("outputState", "Finalize");
	}
	
	public void applyPostFilter(double threshold, SocketIOClient client) {
		// Set threshold
		postprocess.setPostFilter(threshold);

		postprocess.updateImportantWords(vocabs.get(3));

		client.sendEvent("postImportantWords", sendPostImportant());
	}
	
	public void applyPostReplace(String json, SocketIOClient client) {
		List<Map<String, Object>> map = help.jsonStringToList(json);

		postprocess.setPostReplace(map);
	}

	public void applyPostRemove(String json, SocketIOClient client) {
		List<Map<String, Object>> map = help.jsonStringToList(json);

		postprocess.setPostRemove(map);
	}

	public void applyPostParams(String json, SocketIOClient client) {
		List<Map<String, Object>> map = help.jsonStringToList(json);

		postprocess.setMinWordLength((Integer) map.get(0).get("minWordLength"));
		postprocess.setSplitTags((Boolean) map.get(0).get("split"));
		postprocess.setUseAllWords((Boolean) map.get(0).get("useAll"));
	}
	
	// Send Params
	public double sendPostFilterParams() {
		return postprocess.getPostFilter();
	}
	
	public int sendPostLengthParams() {
		return postprocess.getMinWordLength();
	}
	
	public Boolean sendPostAllParams() {
		return postprocess.getUseAllWords();
	}
	
	public List<String> sendPostReplaceParams() {
		return postprocess.getPostReplace();
	}

	public List<String> sendPostRemoveParams() {
		return postprocess.getPostRemove();
	}

	public Boolean sendPostSplitParams() {
		return postprocess.getSplitTags();
	}
	
	// Send Data
	public String sendPostVocab() {
		return help.objectToJsonString(help.prepareVocab(vocabs.get(3)));
	}
	
	public String sendPostVocabHistogram() {
		return help.objectToJsonString(help.prepareVocabHistogram(vocabs.get(3)));
	}
	
	public String sendPostImportant() {
		return help.objectToJsonString(postprocess.prepareImportantWords());
	}

	public String sendPostSalvageData() {
		return help.objectToJsonString(postprocess.prepareSalvagedData());
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Header
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Overview
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String sendOverview(int index) {
		// Each send to the overview sets the history provider correctly
		lastAppliedStep = index;

		// Send history data
		Supplier<List<gridOverview>> supplier = ArrayList::new;

		List<gridOverview> tags_filtered = tags.get(index).stream()
				.map(p -> new gridOverview(p.getTag(), p.getItem(), p.getWeight(), p.getChanged()))
				.collect(Collectors.toCollection(supplier));

		return help.objectToJsonString(tags_filtered);
	}

	public String sendVocab(int index) {
		return help.objectToJsonString(help.prepareVocab(vocabs.get(index)));
	}

	public String sendHistory(String json) {
		List<gridHistory> tags_filtered = new ArrayList<>();

		List<Map<String, Object>> map = help.jsonStringToList(json);

		String tag = (String) map.get(0).get("tag");
		String item = (String) map.get(0).get("item");
		int id = 0;
		List<String> temp = new ArrayList<>(5);

		temp.add("");
		temp.add("");
		temp.add("");
		temp.add("");
		temp.add("");

		// Get ID
		for(Tag t: tags.get(lastAppliedStep))
		{
			if(t.getTag().equals(tag) && t.getItem().equals(item))
			{
				id = t.getId();
			}
		}

		for(int i = 0; i < 5; i++)
		{
			for(Tag t: tags.get(i))
			{
				if(t.getId() == id)
				{
					temp.set(i, t.getTag());
				}
			}
		}

		tags_filtered.add(new gridHistory(temp));

	    return help.objectToJsonString(tags_filtered);
	}
}
