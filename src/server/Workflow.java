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

	private Boolean running = false;
	private String mode = "";
	private Boolean dataLoaded = false;

	private Boolean preDirty = false;
	private Boolean spellDirty = false;
	private Boolean compDirty = false;
	private Boolean postDirty = false;

	private String stopwords = "a,able,about,across,after,all,almost,also,am,among,an,and,any,are,as,at,be,because,been,but,by,can,cannot,could,dear,did,do,does,either,else,ever,every,for,from,get,got,had,has,have,he,her,hers,him,his,how,however,i,if,in,into,is,it,its,just,least,let,like,likely,may,me,might,most,must,my,neither,no,nor,not,of,off,often,on,only,or,other,our,own,rather,said,say,says,she,should,since,so,some,than,that,the,their,them,then,there,these,they,this,tis,to,too,twas,us,wants,was,we,were,what,when,where,which,while,who,whom,why,will,with,would,yet,you,your";
	private List<String> defaultReplace = new ArrayList<>();


	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Parameters
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	// Data
	private List<List<Tag>> tags = new ArrayList<>();

	private Map<String, Long> tagsFreq = new HashMap<>();
	private Map<String, Double> vocabPre = new HashMap<>();
	private Map<String, Double> vocabPost = new HashMap<>();

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

		defaultReplace.add("-, ");
		defaultReplace.add("_, ");
		defaultReplace.add(":, ");
		defaultReplace.add(";, ");
		defaultReplace.add("/, ");

		preprocess = new Preprocess(blacklist);
		spellcorrect = new Spellcorrect(whitelistWords, whitelistGroups, whitelistVocab);
		composite = new Composite(whitelistGroups);
		postprocess = new Postprocess();
	}

	public void computeWorkflow(SocketIOClient client)
	{
		System.out.println("computeWorkflow");

		if(preDirty)
		{
			client.sendEvent("computePre", "started");
			client.sendEvent("computeSpell", "started");
			client.sendEvent("computeComp", "started");
			computePreprocessing(client);
			client.sendEvent("computePre", "finished");

			preDirty = false;
			spellDirty = true;
		}

		if(spellDirty)
		{
			client.sendEvent("computeSpell", "started");
			client.sendEvent("computeComp", "started");
			computeSpellCorrect(client);
			client.sendEvent("computeSpell", "finished");

			spellDirty = false;
			compDirty = true;
		}

		if(compDirty)
		{
			client.sendEvent("computeComp", "started");
			computeGroups(client);
			client.sendEvent("computeComp", "finished");

			compDirty = false;
		}

		if(postDirty)
		{
			computeSalvaging(client);
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
		client.sendEvent("postLengthParams", sendPostLengthParams());
		client.sendEvent("postSplitParams", sendPostSplitParams());
	}

	public void sendData(SocketIOClient client)
	{
		// Send Pre data
		client.sendEvent("preFilterData", sendPreFilterHistogram());
		client.sendEvent("preFilterGrid", sendPreFilter());

		// Send Spell data
		client.sendEvent("similarities", sendSimilarityHistogram());
		client.sendEvent("vocab", sendVocab());
		client.sendEvent("importance", sendPreVocabHistogram());

		// Send Composite data
		client.sendEvent("frequentGroups", sendFrequentGroups());
		client.sendEvent("frequentData", sendFrequentHistogram());
		client.sendEvent("uniqueGroups", sendUniqueGroups());
		client.sendEvent("uniqueData", sendUniqueHistogram());

		// Send Post data
		client.sendEvent("postFilterGrid", sendPostVocab());
		client.sendEvent("postFilterData", sendPostVocabHistogram());
		client.sendEvent("output", sendOverview(3));
		client.sendEvent("outputState", "Multiword Tags");

		// Send Final data
		client.sendEvent("postImportantWords", sendPostImportant());
		client.sendEvent("postSalvageWords", sendPostSalvage());

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

					tags.get(0).add(new Tag(globalID, item, name, Double.parseDouble(weight), 0));
				} catch (Exception e) {
					System.out.println(aMap);
				}
			}
		}
	}
	
	public void applyImportedDataFinished(SocketIOClient client) {
		// Set to lower case
		help.setToLowerCase(tags.get(0));
		
		// Compute word frequency
		help.wordFrequency(tags.get(0), tagsFreq);

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

		if(data.equals("guided"))
		{
			client.sendEvent("initRunning","started");

			if(!running) applyDefaults();

			mode = data;
			client.sendEvent("selectedMode",sendMode());

			sendParams(client);
			sendData(client);

			client.sendEvent("initRunning","finished");
		}

		if(data.equals("free") || data.equals("linked"))
		{
			client.sendEvent("initRunning","started");

			mode = data;
			client.sendEvent("selectedMode",sendMode());

			sendParams(client);
			sendData(client);

			client.sendEvent("initRunning","finished");

			preDirty = true;
			computeWorkflow(client);
		}

		if(data.equals("reconnect"))
		{
			client.sendEvent("initRunning","started");

			// Mode stays
			client.sendEvent("selectedMode",sendMode());

			sendParams(client);
			sendData(client);

			client.sendEvent("initRunning","finished");
		}

		running = true;
		client.sendEvent("isRunning",sendStatus());
	}

	private void applyDefaults() {
		// Preprocessing
		preprocess.setDefaultReplace(defaultReplace);

		preprocess.setRemove("'");

		blacklist.clear();
		Collections.addAll(blacklist, stopwords.split(","));

		// Spell correct
		spellcorrect.setSpellImportance(0.7);
		spellcorrect.setSpellSimilarity(0.7);
		spellcorrect.setMinWordSize(3);

		// Composite
		composite.setFrequentThreshold(0.35);
		composite.setJaccardThreshold(0.7);

		// Postprocessing
		postprocess.setPostFilter(0.25);
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
		weighting.vocab(tags.get(1), vocabPre);

		// Cluster words for further use
		spellcorrect.clustering(tags.get(1), vocabPre, whitelistVocab);
		
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
		spellcorrect.applyClustering(tags.get(2), vocabPre);
		
		// Compute further data
		composite.group(tags.get(2));
		
		client.sendEvent("frequentGroups", sendFrequentGroups());
		client.sendEvent("frequentData", sendFrequentHistogram());
		client.sendEvent("uniqueGroups", sendUniqueGroups());
		client.sendEvent("uniqueData", sendUniqueHistogram());
		client.sendEvent("replacements", sendReplacements());
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
		return help.objectToJsonString(spellcorrect.prepareCluster(tag, vocabPre));
	}
	
	public String sendSimilarityHistogram() {
		return help.objectToJsonString(spellcorrect.prepareSimilarityHistogram());
	}
	
	public int sendReplacements(String json) {
		List<Map<String, Object>> map = help.jsonStringToList(json);

		double imp, sim;

		if(map.get(0).get("importance").equals(0)) return 0;

		if(map.get(0).get("importance").equals(1))
		{
			imp = ((Integer) map.get(0).get("importance"));
		}
		else
		{
			imp = ((Double) map.get(0).get("importance"));
		}

		sim = getSim(map);

		return spellcorrect.prepareReplacements(sim, imp, vocabPre);
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

		return help.objectToJsonString(spellcorrect.prepareReplacementData(sim, imp, vocabPre));
	}
	
	public int sendReplacements() {
		double sim = spellcorrect.getSpellSimilarity();
		double imp = spellcorrect.getSpellImportance();

		return spellcorrect.prepareReplacements(sim, imp, vocabPre);
	}

	public String sendVocab() {
		return help.objectToJsonString(help.prepareVocab(vocabPre));
	}

	public String sendPreVocabHistogram() {
		return help.objectToJsonString(help.prepareVocabHistogram(vocabPre));
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Composites - Dataset 3
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public void computeGroups(SocketIOClient client) {
		help.resetStep(tags, 3);
		
		// Compute all word groups
		composite.applyGroups(tags.get(3));

		// Provide further data
		weighting.vocab(tags.get(3), vocabPost);

		client.sendEvent("postFilterGrid", sendPostVocab());
		client.sendEvent("postFilterData", sendPostVocabHistogram());

		client.sendEvent("output", sendOverview(3));
		client.sendEvent("outputState", "Multiword Tags");

		prepareSalvaging(client);
	}
	
	// Apply changes
	public void applyCompositeFrequent(double threshold, SocketIOClient client) {
		// Set threshold
		composite.setFrequentThreshold(threshold);
		
		compDirty = true;
	}
	
	public void applyCompositeUnique(double threshold, SocketIOClient client) {
		// Set threshold
		composite.setJaccardThreshold(threshold);

		compDirty = true;
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
		postprocess.initializeSalvaging(vocabPost);
		
		client.sendEvent("postImportantWords", sendPostImportant());
		client.sendEvent("postSalvageWords", sendPostSalvage());
	}
	
	public void computeSalvaging(SocketIOClient client) {
		postprocess.computeSalvaging(vocabPost);
		
		client.sendEvent("postSalvageData", sendPostSalvageData());
	}
	
	// Apply changes
	public void applySalvaging(SocketIOClient client) {
		help.resetStep(tags, 4);
		
		postprocess.applySalvaging(tags.get(4));
		
		client.sendEvent("output", sendOverview(4));
		client.sendEvent("outputState", "Finalize");
	}
	
	public void applyPostFilter(double threshold, SocketIOClient client) {
		// Set threshold
		postprocess.setPostFilter(threshold);
		
		postDirty = true;
	}
	
	public void applyPostReplace(String json, SocketIOClient client) {
		List<Map<String, Object>> map = help.jsonStringToList(json);
		
		postprocess.setPostReplace(map);

		postDirty = true;
	}

	public void applyPostParams(String json, SocketIOClient client) {
		List<Map<String, Object>> map = help.jsonStringToList(json);

		postprocess.setMinWordLength((Integer) map.get(0).get("minWordLength"));
		postprocess.setSplitTags((Boolean) map.get(0).get("split"));
		postprocess.setUseAllWords((Boolean) map.get(0).get("useAll"));

		postDirty = true;
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
	
	public Boolean sendPostSplitParams() {
		return postprocess.getSplitTags();
	}
	
	// Send Data
	public String sendPostVocab() {
		return help.objectToJsonString(help.prepareVocab(vocabPost));
	}
	
	public String sendPostVocabHistogram() {
		return help.objectToJsonString(help.prepareVocabHistogram(vocabPost));
	}
	
	public String sendPostImportant() {
		return help.objectToJsonString(postprocess.prepareImportantWords());
	}
	
	public String sendPostSalvage() {
		return help.objectToJsonString(postprocess.prepareSalvageWords());
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
		Supplier<List<gridOverview>> supplier = ArrayList::new;

		List<gridOverview> tags_filtered = tags.get(index).stream()
				.map(p -> new gridOverview(p.getTag(), p.getItem(), p.getWeight()))
				.collect(Collectors.toCollection(supplier));

		return help.objectToJsonString(tags_filtered);
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
		for(Tag t: tags.get(3))
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
