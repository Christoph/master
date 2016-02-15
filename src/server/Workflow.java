package server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import processing.Grouping;
import processing.Helper;
import processing.Preprocess;
import processing.Regex;
import processing.Spellcorrect;
import processing.Weighting;
import core.ImportCSV;
import core.Tag;
import core.json.gridHist;
import core.json.gridOverview;
import core.json.gridVocab;

public class Workflow {
	
	// Initialize variables and classes
	private static final Logger log = Logger.getLogger("Logger");
  	private Helper help = new Helper();
    private ImportCSV im = new ImportCSV();
    private Weighting weighting = new Weighting();
    private Regex regex = new Regex();
    private Grouping grouping = new Grouping();

    
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Parameters
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    // Initial datasets
    private List<Tag> tags; 
    private Map<String, Double> vocabPre = new HashMap<String, Double>();
    private Map<String, Double> vocabPost = new HashMap<String, Double>();
    
    // Initialize pipeline steps
    // The index selects the working copy. 0 = original
    private Preprocess preprocess = new Preprocess(1);
    private Spellcorrect spellcorrect = new Spellcorrect(2);

	// Composites
	private double groupFrequent = 0;
	private double groupUnique = 0;
	
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
		
		// Cluster words for further use
		spellcorrect.clustering(tags, vocabPre, preprocess.getWhitelistWords());
	}
	
	// Apply changes
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
    // Spell Correction - Dataset 2
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public void computeSpellCorrect()
	{
		help.resetStep(tags, 2);
		
		spellcorrect.applyClustering(tags);
	}
	
	// Apply changes
	public void applySpellImportance(double threshold)
	{
		// Set threshold
		spellcorrect.setSpellImportance(threshold);
		
		// Apply
		computeSpellCorrect();
	}
	
	public void applySpellSimilarity(double threshold)
	{
		// Set threshold
		spellcorrect.setSpellSimilarity(threshold);
		
		// Apply
		computeSpellCorrect();
	}
	
	// Send Params
	public double sendSpellImportanceParams()
	{
		return spellcorrect.getSpellImportance();
	}
	
	public double sendSpellSimilarityParams()
	{
		return spellcorrect.getSpellSimilarity();
	}
	
	// Send Data
	public String sendCluster(String tag)
	{
	    return help.objectToJsonString(spellcorrect.prepareCluster(tag));
	}
	
	public String sendSimilarityHistogram()
	{
	    return help.objectToJsonString(spellcorrect.prepareSimilarityHistogram());
	}
	
	public String sendReplacements(double threshold)
	{
	    return help.objectToJsonString(spellcorrect.prepareReplacements(threshold));
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
