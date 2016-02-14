package server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import processing.Grouping;
import processing.Helper;
import processing.Preprocess;
import processing.Regex;
import processing.Similarity;
import processing.Weighting;
import core.ImportCSV;
import core.Tag;
import core.json.gridCluster;
import core.json.gridHist;
import core.json.gridHistory;
import core.json.gridOverview;
import core.json.gridRepl;
import core.json.gridVocab;

public class Workflow {
	
	// Initialize variables and classes
	private static final Logger log = Logger.getLogger("Logger");
  	private Helper help = new Helper();
    private ImportCSV im = new ImportCSV();
    private Weighting weighting = new Weighting();
    private Regex regex = new Regex();
    private Similarity similarity = new Similarity();
    private Grouping grouping = new Grouping();

    
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Parameters
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    // Initial datasets
    private List<Tag> tags; 
    private Map<String, Double> vocabPre = new HashMap<String, Double>();
    
    // Initialize pipeline steps
    // The index selects the working copy. 0 = original
    private Preprocess preprocess = new Preprocess(1);
	
	// Spell correction
	private double spellImportance = 0;
	private double spellSimilarity = 0;
    private Map<String, Map<String, Double>> vocabClusters = new HashMap<String, Map<String, Double>>();
    private TreeMap<Double, Map<String, String>> simClusters = new TreeMap<Double, Map<String,String>>();
	
	// Composites
	private double groupFrequent = 0;
	private double groupUnique = 0;
	
	// Create new post vocab
    private Map<String, Double> vocabPost = new HashMap<String, Double>();
	
	// Postprocessing
	private double postFilter = 0;
    private List<String> importantWords = new ArrayList<String>();
	
	private List<String> postReplace = new ArrayList<String>();
	private List<String> postRemove = new ArrayList<String>();
	
    
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Load data - Dataset 0
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
	public void init()
	{
		log.info("Initialize\n");
		
	    // Load data
	    tags = im.importTags("reduced_music.csv");
	    
	    // Set tags for the first step
	    help.provideTagsForNextStep(tags, 0);

	    log.info("Data loaded\n");
	}
	
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Preprocessing - Dataset 1
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public void computePreprocessing()
	{
		help.resetStep(tags, 1);
		
		// Set to lower case
		preprocess.setToLowerCase(tags);
		
		// Remove characters
		preprocess.removeCharacters(tags);
		
		// Replace characters
		preprocess.replaceCharacters(tags);
		
		// Compute word frequency
		preprocess.computeWordFreq(tags);
		
		// Update tags
		preprocess.applyFilter(tags);
		
		// Create preFilter vocab
		weighting.vocab(tags, vocabPre, 1);
	}
	
	public void applyPreFilter(double threshold)
	{
		// Set threshold
		preprocess.setFilter(threshold);
		
		// Apply
		computePreprocessing();
	}
	
	public void applyPreRemove(String chars)
	{
		// Set characters for removal
		preprocess.setRemove(chars);
		
		// Apply
		computePreprocessing();
	}
	
	public void applyPreReplace(String json)
	{
		List<Map<String, Object>> map = help.jsonStringToList(json);
		
		preprocess.setReplace(map);
		
		// Apply
		computePreprocessing();
	}
	
	public void applyPreDictionary(String json)
	{
		List<Map<String, Object>> map = help.jsonStringToList(json);
		
		preprocess.setDictionary(map);
		
		// Apply
		computePreprocessing();
	}
	
	// Send Params
	public double sendPreFilterParams()
	{
		return preprocess.getFilter();
	}
	
	public String sendPreRemoveParams()
	{
		return preprocess.getRemove();
	}
	
	public List<String> sendPreReplaceParams()
	{
		return preprocess.getReplace();
	}
	
	public List<String> sendPreDictionaryParams()
	{
		return preprocess.getDictionary();
	}
	
	// Send Data
	public String sendPreFilter()
	{
	    return help.objectToJsonString(preprocess.preparePreFilter());
	}
	
	public String sendPreFilterHistogram()
	{
	    return help.objectToJsonString(preprocess.preparePreFilterHistogram());
	}
	
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Spell Checking - Dataset 2
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public void clustering(int minWordSize)
	{
	    // compute similarities
	    similarity.withVocab(tags, vocabPre, preprocess.getWhitelistWords(), minWordSize, vocabClusters);
	    
	    // create similarity clusters
	    createSimClusters();
	}
	
	public void applyClustering(double threshold)
	{
		similarity.applyClusters(tags, threshold, vocabClusters, 2);
		
	    // Resolve errors from replacements
	    //help.correctTags(tags, 2);
	    
	    log.info("Clustering Finished\n");
	}
	
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Composites - Dataset 3
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public void grouping()
	{
		// Compute all word groups
	    grouping.group(tags, preprocess.getWhitelistGroups(), 3);
	    log.info("Grouping finished\n");
	}
	
	public void setGroupSize(int maxGroupSize)
	{
	    // Set max group size
	    grouping.setMaxGroupSize(maxGroupSize);
	}
	
	public void setWhitelist(List<String> whitelist)
	{
	    // Set whitelist
	    grouping.setWhitelist(whitelist);
	}
	
	public void applyGrouping()
	{
	    // Apply groups with current threshold
		grouping.applyGroups(tags);
	    log.info("Groups applied\n");
	    
	    // Split words
	    help.splitCompositeTagLast(tags, 3);
	    log.info("Tags splited\n");
	    
	    // Compute importance with the new words
	    weighting.vocab(tags, vocabPost, 3);
	    log.info("Weigthed\n");
	}

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Postprocessing - Dataset 4
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public void regex()
	{
		/////////////////////////////////
	    // Variable initialization  	    

	    //List<String> subjective = im.importCSV("dicts/subjective.txt");
	    List<String> synonyms = im.importCSV("dicts/synonyms.txt");
	    List<String> messedup = im.importCSV("dicts/messedgroups.txt");

		///////////////////////////////// 
	    // Algorithm
	    
	    // Find oneword groups and replace them by the group
	    regex.findGroups(tags, 4);
	    log.info("group searching finished\n");
	    
	    // Synonym replacing regex
	    regex.replaceCustomWords(tags, synonyms, 4);
	    log.info("Synonym replacement finished\n");
	    
	    Map<String, String> important_tags = null;
		// Word separation
	    // Find important words in the unimportant tags
	    regex.findImportantWords(tags, important_tags, 0.1, 4, false, 4);
	    log.info("Word separation finished\n");
	    
	    // Messed up tags replacement
	    regex.replaceCustomWords(tags, messedup, 4);
	    
	    // Weighting words as last step 
	    //.byWeightedMean(tags ,"third", false);
	    log.info("Last time importance\n");
	    
	    // Remove dashes
	    help.removeDashes(tags, 4);
	}

	public void computeImportantWords(double threshold)
	{
	    // Build popular tags dict on raw data
	    importantWords = help.getImportantTags(vocabPost, threshold);
	}
	
	public void removeSubjectiveWords(List<Tag> tags, List<String> subjective, Map<String, String> important_tags)
	{
	    // Remove subjective tags
	    for(String s: subjective)
	    {
	    	if(important_tags.containsKey(s))
	    	{
	        	important_tags.remove(s); 
	    	}
	    }
	}

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Send Methods
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public String sendOverview(int index)
	{
		Supplier<List<gridOverview>> supplier = () -> new ArrayList<gridOverview>();

	    List<gridOverview> tags_filtered = tags.stream()
	    		.map(p -> new gridOverview(p.getTag(index), p.getItem(), p.getImportance()))
	    		.collect(Collectors.toCollection(supplier));
	    
	    return help.objectToJsonString(tags_filtered); 
	}
	
	public String sendVocab()
	{
	    List<gridVocab> tags_filtered = new ArrayList<gridVocab>();
	    
	    for(String s: Helper.sortByComparatorDouble(vocabPre).keySet())
	    {
	    	tags_filtered.add(new gridVocab(s, vocabPre.get(s)));
	    }

	    return help.objectToJsonString(tags_filtered);
	}
	
	public String sendCluster(String tag)
	{
	    List<gridCluster> tags_filtered = new ArrayList<gridCluster>();
	    
	    if(vocabClusters.containsKey(tag))
	    {
		    for(String s: vocabClusters.get(tag).keySet())
		    {
		    	tags_filtered.add(new gridCluster(s, vocabClusters.get(tag).get(s)));
		    }
	    }

	    return help.objectToJsonString(tags_filtered);
	}
	
	public String sendImportanceHistogram()
	{
	    List<gridHist> hist = new ArrayList<gridHist>();
	    Map<Double, Long> temp = new HashMap<Double, Long>();
	    
	    for(Entry<String, Double> c: vocabPre.entrySet())
	    {
    		if(temp.containsKey(c.getValue()))
    		{
    			temp.put(c.getValue(), temp.get(c.getValue()) + 1);
    		}
    		else
    		{
    			temp.put(c.getValue(), (long) 1);
    		}

	    }
	    
	    for(double d: temp.keySet())
	    {
	    	hist.add(new gridHist(d, temp.get(d)));
	    }

	    return help.objectToJsonString(hist);
	}
	
	public String sendSimilarityHistogram()
	{
	    List<gridHist> hist = new ArrayList<gridHist>();
	    Map<Double, Long> temp = new HashMap<Double, Long>();
	    
	    for(Entry<String, Map<String, Double>> c: vocabClusters.entrySet())
	    {
	    	for(double s: c.getValue().values())
	    	{ 
	    		if(temp.containsKey(s))
	    		{
	    			temp.put(s, temp.get(s) + 1);
	    		}
	    		else
	    		{
	    			temp.put(s, (long) 1);
	    		}
	    	}
	    }
	    
	    for(double d: temp.keySet())
	    {
	    	hist.add(new gridHist(d, temp.get(d)));
	    }

	    return help.objectToJsonString(hist);
	}
	
	// Send 100 entries around the threshold
	public String sendReplacements(double threshold)
	{
	    List<gridRepl> repl = new ArrayList<gridRepl>();

	    double core = simGet(threshold);
	    double lower = core;
	    double higher = core;
	    Boolean init = false;
	    
	    // Add core replacements
	    addElementsToRepl(repl, core);
	    
	    // Add at least one layer around the center until the total number of entries rises above 101
	    while(repl.size() <= 101 || init == false)
	    {
	    	init = true;
	    	
	        Entry<Double, Map<String, String>> high = simClusters.higherEntry(higher);
	        
	        if(high != null)
	        {
		        higher = high.getKey();
			    addElementsToRepl(repl, higher);
	        }

	        Entry<Double, Map<String, String>> low = simClusters.lowerEntry(lower);
	        
	        if(low != null)
	        {
		        lower = low.getKey();
			    addElementsToRepl(repl, lower);
	        }
	    }
		
	    return help.objectToJsonString(repl);
	}
	
	private void addElementsToRepl(List<gridRepl> repl, double key)
	{
	    for(Entry<String, String> e: simClusters.get(key).entrySet())
	    {
	    	repl.add(new gridRepl(e.getKey(), e.getValue(), key));
	    }
	}
	
	// Get nearest entry from the similarity tree map
	private double simGet(double key) {
	    Map<String, String> value = simClusters.get(key);
	    double entry = key;

	    if (value == null) {
	        Entry<Double, Map<String, String>> floor = simClusters.floorEntry(key);
	        Entry<Double, Map<String, String>> ceiling = simClusters.ceilingEntry(key);

	        if ((key - floor.getKey()) < (ceiling.getKey() - key)) {
	            value = floor.getValue();
	            entry = floor.getKey();
	        } else {
	            value = ceiling.getValue();
	            entry = ceiling.getKey();
	        }
	    }

	    return entry;
	}
	
	private void createSimClusters()
	{
		String head = "";
		String key = "";
		double value = 0;
		
	    for(Entry<String, Map<String, Double>> c: vocabClusters.entrySet())
	    {
	    	head = c.getKey();
	    	
	    	for(Entry<String, Double> e: c.getValue().entrySet())
	    	{
	    		key = e.getKey();
	    		value = e.getValue();
	    		
	    		if(simClusters.containsKey(value))
	    		{
	    			simClusters.get(value).put(head, key);
	    		}
	    		else
	    		{
	    			// Create inner map
	    			simClusters.put(value, new HashMap<String, String>());
	    			
	    			// Add first line
	    			simClusters.get(value).put(head, key);
	    		}
	    	}
	    }
	}

	/*
	public String sendHistory(String data) {
		
	    Supplier<List<gridHistory>> supplier = () -> new ArrayList<gridHistory>();
	    List<Integer> ids = new ArrayList<Integer>();
	    
	    if(data.length()>0)
	    {
		    String[] temp = data.split(",");
		    
		    for(String s: temp)
		    {
		    	ids.add(Integer.parseInt(s));
		    }
	    }
	    
	    List<gridHistory> tags_filtered = tags.stream()
	    		.filter(p -> ids.contains(p.getID()) )
	    		.map(p -> new gridHistory(p.getHistory()))
	    		.collect(Collectors.toCollection(supplier));
	    
	    return help.objectToJsonString(tags_filtered);
	}
	*/
	
	public String sendFrequentGroups()
	{
		return grouping.getFrequentGroupsJSON();
	}
	
	public String sendUniqueGroups()
	{
		return grouping.getUniqueGroupsJSON();
	}
	
	public String sendFrequentHistogram()
	{
		return grouping.getFrequentHistogramJSON();
	}
	
	public String sendUniqueHistogram()
	{
		return grouping.getUniqueHistogramJSON();
	}
	
	public String sendPostVocab()
	{
	    List<gridVocab> tags_filtered = new ArrayList<gridVocab>();
	    
	    for(String s: vocabPost.keySet())
	    {
	    	tags_filtered.add(new gridVocab(s, vocabPost.get(s)));
	    }

	    return help.objectToJsonString(tags_filtered);
	}
	

	
	public String sendPostImportanceHistogram()
	{
	    List<gridHist> hist = new ArrayList<gridHist>();
	    Map<Double, Long> temp = new HashMap<Double, Long>();
	    
	    for(Entry<String, Double> c: vocabPost.entrySet())
	    {
    		if(temp.containsKey(c.getValue()))
    		{
    			temp.put(c.getValue(), temp.get(c.getValue()) + 1);
    		}
    		else
    		{
    			temp.put(c.getValue(), (long) 1);
    		}

	    }
	    
	    for(double d: temp.keySet())
	    {
	    	hist.add(new gridHist(d, temp.get(d)));
	    }

	    return help.objectToJsonString(hist);
	}
	
	public String sendImportantWords()
	{
	    List<gridVocab> tags_filtered = new ArrayList<gridVocab>();
	    
	    for(String s: importantWords)
	    {
	    	tags_filtered.add(new gridVocab(s, 0));
	    }

	    return help.objectToJsonString(tags_filtered);
	}
	
	public String sendFinal()
	{
		Supplier<List<gridOverview>> supplier = () -> new ArrayList<gridOverview>();

	    List<gridOverview> tags_filtered = tags.stream()
	    		.map(p -> new gridOverview(p.getTag(4), p.getItem(), p.getImportance()))
	    		.collect(Collectors.toCollection(supplier));
	    
	    return help.objectToJsonString(tags_filtered); 
	}
}
