package processing.lastFM;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import processing.Grouping;
import processing.Similarity;
import core.ImportCSV;
import core.json.gridCluster;
import core.json.gridHist;
import core.json.gridHistory;
import core.json.gridOverview;
import core.json.gridVocab;
import core.tags.TagLast;
import core.tags.TagsToCSV;

public class WorkflowLast {
	
	// Initialize variables and classes
	private static final Logger log = Logger.getLogger("Logger");
  	private HelperLast help = new HelperLast();
    private ImportCSV im = new ImportCSV();
    private WeightingLast weighting = new WeightingLast();
    private RegexLast regex = new RegexLast();
    private Similarity similarity = new Similarity();
    Grouping grouping = new Grouping();
    
    // Data
    private List<TagLast> tags;
    private List<TagLast> groupingTags = new ArrayList<TagLast>();
    private Map<String, Double> vocabPre = new HashMap<String, Double>();
    private Map<String, Double> vocabPost = new HashMap<String, Double>();
    private Map<String, Map<String, Double>> vocabClusters = new HashMap<String, Map<String, Double>>();
    private Map<String, String> importantWords = new HashMap<String, String>();
    
    private List<String> whitelistWords = new ArrayList<String>();
    private List<String> whitelistGroups = new ArrayList<String>();
    private List<String> remove = new ArrayList<String>();
    private List<String> blacklist = new ArrayList<String>();
	
    private List<String> articles = im.importCSV("dicts/article.txt");
    private List<String> preps = im.importCSV("dicts/prep.txt");
    private List<String> custom = im.importCSV("dicts/custom.txt");
    private List<String> replace = new ArrayList<String>();
    
	public void init()
	{
		log.info("Initialize\n");
		
	    // Load data
	    tags = im.importLastTags("raw_subset_tags.csv");
	    
	    // Set first history step
	    help.addHistoryStep(tags);

	    log.info("Data loaded\n");
	}
	
	public void clustering(int minWordSize)
	{
	    // Similarity replacement
	    similarity.withVocab(tags, vocabPre, whitelistWords, minWordSize, vocabClusters);
	}
	
	public void removeReplace()
	{
	    blacklist.addAll(preps);
	    blacklist.addAll(articles);
	    blacklist.addAll(custom);
	    blacklist.add("");
	    
	    remove.add("'");
	    
	    replace.add("-, ");
	    replace.add("_, ");
	    replace.add(":, ");
	    replace.add(";, ");
	    replace.add("/, ");
		
	    // Characters
	    help.removeReplaceCharactersAndLowerCase(tags, remove, replace);
	    log.info("Character editing finished\n");
	    
	    // Blacklist
	    help.removeBlacklistedWords(tags, blacklist);
	    log.info("Blacklist finished\n");
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
	
	public void grouping(int maxGroupSize)
	{
		// TODO: Initialize all temp datasets -> performance
		// Temporary dataset
		groupingTags.clear();
		
		for(TagLast t: tags)
		{
			// TODO: Change grouping code to List<String> -> Performance
			groupingTags.add(new TagLast(t));
		}
	    
	    // Reset old groups
	    grouping.resetGroups();
	    
	    // Set max group size
	    grouping.setMaxGroupSize(maxGroupSize);
	    
		// Find word groups
	    grouping.group(groupingTags, whitelistGroups, 2);
	    log.info("Grouping finished\n");
	}
	
	public void applyGrouping()
	{
	    // Apply groups with current threshold
		tags.clear();
		for(TagLast t: groupingTags)
		{
			tags.add(new TagLast(t));
		}
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

	
	public void regex()
	{
		/////////////////////////////////
	    // Variable initialization  	    
	    TagsToCSV writer_tags = new TagsToCSV("tags_Regex.csv");
	    
	    Map<String, String> important_tags;
	    
	    List<String> subjective = im.importCSV("dicts/subjective.txt");
	    List<String> synonyms = im.importCSV("dicts/synonyms.txt");
	    List<String> messedup = im.importCSV("dicts/messedgroups.txt");
	    
		///////////////////////////////// 
	    // Parameters
	    
	    // Set importance threshold
	    double threshold = 0.1;
	    
	    // Set minimum word length
	    int minWordLength = 3;

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
	    importantWords = help.getImportantTags(tags, threshold, minWordLength);
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
	
	public void exportInTableFormat()
	{
	    TagsToCSV writer_tag = new TagsToCSV("Tag.csv");
	    TagsToCSV writer_track = new TagsToCSV("Track.csv");
	    TagsToCSV writer_tt = new TagsToCSV("TT.csv");
		
	    writer_tag.writeTableTag(tags);
	    writer_track.writeTableTrack(tags);    
	    writer_tt.writeTableTT(tags);
	}

	//
	// All send methods
	
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
	    
	    for(String s: vocabPre.keySet())
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
		return grouping.getJaccardGroupsJSON();
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
}