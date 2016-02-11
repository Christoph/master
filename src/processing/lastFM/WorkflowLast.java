package processing.lastFM;

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
import processing.Similarity;
import processing.Weighting;
import core.ImportCSV;
import core.json.gridCluster;
import core.json.gridHist;
import core.json.gridHistory;
import core.json.gridOverview;
import core.json.gridRepl;
import core.json.gridVocab;
import core.tags.TagLast;
import core.tags.TagsToCSV;

public class WorkflowLast {
	
	// Initialize variables and classes
	private static final Logger log = Logger.getLogger("Logger");
  	private HelperLast help = new HelperLast();
    private ImportCSV im = new ImportCSV();
    private WeightingLast weighting = new WeightingLast();
    private Weighting weightingGeneral = new Weighting();
    private RegexLast regex = new RegexLast();
    private Similarity similarity = new Similarity();
    Grouping grouping = new Grouping();
    
    // Data
    private List<TagLast> tags;
    private Map<String, Double> tagsFreq = new HashMap<String, Double>();
    private Map<String, Double> vocabPre = new HashMap<String, Double>();
    private Map<String, Double> vocabPost = new HashMap<String, Double>();
    private Map<String, Map<String, Double>> vocabClusters = new HashMap<String, Map<String, Double>>();
    private TreeMap<Double, Map<String, String>> simClusters = new TreeMap<Double, Map<String,String>>();
    
    private List<String> whitelistWords = new ArrayList<String>();
    private List<String> whitelistGroups = new ArrayList<String>();
    private String remove = "";
    private List<String> blacklist = new ArrayList<String>();
	
    private List<String> articles = im.importCSV("dicts/article.txt");
    private List<String> preps = im.importCSV("dicts/prep.txt");
    private List<String> custom = im.importCSV("dicts/custom.txt");
    private List<String> replace = new ArrayList<String>();
    
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Initialization
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
	public void init()
	{
		log.info("Initialize\n");
		
	    // Load data
	    tags = im.importLastTags("raw_subset_tags.csv");
	    
	    // Set first history step
	    help.addHistoryStep(tags);

	    log.info("Data loaded\n");
	}
	
	//OLD
	
	public void removeReplace()
	{
	    blacklist.addAll(preps);
	    blacklist.addAll(articles);
	    blacklist.addAll(custom);
	    blacklist.add("");
	    
	    remove = "'";
	    
	    replace.add("-, ");
	    replace.add("_, ");
	    replace.add(":, ");
	    replace.add(";, ");
	    replace.add("/, ");
		
	    // Characters
	    help.setToLowerCase(tags);
	    help.removeCharacters(tags, remove);
	    help.replaceCharacters(tags, replace);
	    log.info("Character editing finished\n");
	    
	    // Blacklist
	    help.removeBlacklistedWords(tags, blacklist);
	    log.info("Blacklist finished\n");
	}
	
	
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Preprocessing
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void preFilter()
	{
	    weightingGeneral.vocabByFrequency(tags, tagsFreq);
	}
	
	public void applyCharactersToRemove(String chars)
	{
		// Preparing characters to be used as regex
		remove = "["+chars+"]";
		
	    help.removeCharacters(tags, remove);
	}
	
	public void applyCharactersToReplace(String json)
	{
		List<Map<String, Object>> map = help.jsonStringToList(json);
		
		replace.clear();

		for(int i = 0; i < map.size(); i++)
		{
		    replace.add(map.get(i).get("replace")+","+map.get(i).get("by"));
		}
		
		help.replaceCharacters(tags, replace);
	}
	
	public void applyDictionaryData(String json)
	{
		List<Map<String, Object>> map = help.jsonStringToList(json);
		String tag;

		for(int i = 0; i < map.size(); i++)
		{
			tag = map.get(i).get("tag")+"";
			
			if(tag.contains(" "))
			{
				whitelistGroups.add(tag);
				
				for(String s: tag.split(" "))
				{
					whitelistWords.add(s);
				}
			}
			else
			{
				if(!whitelistWords.contains(tag)) whitelistWords.add(tag);
			}
		}
	}
	
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Spell Checking
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public void clustering(int minWordSize)
	{
	    // compute similarities
	    similarity.withVocab(tags, vocabPre, whitelistWords, minWordSize, vocabClusters);
	    
	    // create similarity clusters
	    createSimClusters();
	}
	
	public void weightPreVocab()
	{
	    weighting.vocabByImportance(tags, vocabPre, "weighting_nlp", false);
	    log.info("Weighting finished\n");
	}
	
	public void applyClustering(double threshold)
	{
		similarity.applyClusters(tags, threshold, vocabClusters);
		
	    // Resolve errors from replacements
	    help.correctTagsAndIDs(tags);
	    
	    // Compute importance with the new words
	    weighting.vocabByImportance(tags, vocabPost, "grouping", false);
	    
	    // Add history step
	    help.addHistoryStep(tags);
	    log.info("Clustering Finished\n");
	}
	
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Composites
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public void grouping()
	{
		// Compute all word groups
	    grouping.group(tags, whitelistGroups);
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
	    help.splitCompositeTagLast(tags);
	    log.info("Tags splited\n");
	    
	    // Compute importance with the new words
	    weighting.vocabByImportance(tags, vocabPost, "grouping", false);
	    log.info("Weigthed\n");
	    
	    // Add history step
	    help.addHistoryStep(tags);
	}

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Postprocessing
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public void regex()
	{
		/////////////////////////////////
	    // Variable initialization  	    
	    TagsToCSV writer_tags = new TagsToCSV("tags_Regex.csv");
	    
	    //List<String> subjective = im.importCSV("dicts/subjective.txt");
	    List<String> synonyms = im.importCSV("dicts/synonyms.txt");
	    List<String> messedup = im.importCSV("dicts/messedgroups.txt");

		///////////////////////////////// 
	    // Algorithm
	    
	    // Find oneword groups and replace them by the group
	    regex.findGroups(tags, true);
	    log.info("group searching finished\n");
	    
	    // Synonym replacing regex
	    regex.replaceCustomWords(tags, synonyms,"synonyms");
	    log.info("Synonym replacement finished\n");
	    
	    // Important words
	    //important_tags = getImportantWords(tags, threshold, minWordLength);
	    //log.info("important tag exttraction finished\n");
		
	    // Remove subjective tags from important words
	    //removeSubjectiveWords(tags, subjective, important_tags);
	    log.info("removing subjective words finished\n");
	    
	    // Word separation
	    // Find important words in the unimportant tags
	    //regex.findImportantWords(tags, important_tags, threshold, minWordLength, false);
	    log.info("Word separation finished\n");
	    
	    // Messed up tags replacement
	    regex.replaceCustomWords(tags, messedup,"cleaning");
	    
	    // Weighting words as last step 
	    weighting.byWeightedMean(tags ,"third", false);
	    log.info("Last time importance\n");
	    
	    // Remove dashes
	    help.removeDashes(tags);
	    
	    // Output
	    writer_tags.writeTagListCustomWeight(tags);
	}
	
	public void weightPostVocab()
	{
	    weighting.vocabByImportance(tags, vocabPost, "weighting_regex", false);
	    log.info("Weighting finished\n");
	}

	public void computeImportantWords(double threshold, int minWordLength)
	{
	    // Build popular tags dict on raw data
	    //importantWords = help.getImportantTags(tags, threshold, minWordLength);
	}
	
	public void removeSubjectiveWords(List<TagLast> tags, List<String> subjective, Map<String, String> important_tags)
	{
	    TagsToCSV writer_important_filtered = new TagsToCSV("important_tags_filtered.csv");
		
	    // Remove subjective tags
	    for(String s: subjective)
	    {
	    	if(important_tags.containsKey(s))
	    	{
	        	important_tags.remove(s); 
	    	}
	    }
	    
	    writer_important_filtered.writeImportantTags(important_tags);
	}

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Send Methods
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public String sendOverview()
	{
	    Supplier<List<gridOverview>> supplier = () -> new ArrayList<gridOverview>();

	    List<gridOverview> tags_filtered = tags.stream()
	    		.map(p -> new gridOverview(p.getTagName(), p.getImportance(), p.getCarrierName(), p.getID()))
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
	
	public String sendPreFilter()
	{
	    List<gridVocab> tags_filtered = new ArrayList<gridVocab>();
	    
	    for(String s: tagsFreq.keySet())
	    {
	    	tags_filtered.add(new gridVocab(s, tagsFreq.get(s)));
	    }

	    return help.objectToJsonString(tags_filtered);
	}
	
	public String sendPreFilterHistogram()
	{
	    List<gridHist> hist = new ArrayList<gridHist>();
	    Map<Double, Long> temp = new HashMap<Double, Long>();
	    
	    for(Entry<String, Double> c: tagsFreq.entrySet())
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
}
