package processing;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import core.ImportCSV;
import core.TagLast;
import core.TagsToCSV;

public class WorkflowLast {
	
	// Initialize variables and classes
	private static final Logger log = Logger.getLogger("Logger");
    private Helper help = new Helper();
    private ImportCSV im = new ImportCSV();
    private WeightingLast weighting = new WeightingLast();
    private Regex regex = new Regex();
    private SimilarityComplex similarity = new SimilarityComplex();
    Grouping grouping = new Grouping();
    
    // Full data set
    private List<TagLast> tags;
	
	public void init()
	{
		log.info("Initialize\n");
		
	    // Load data
	    tags = im.importLastTags("raw_subset_tags.csv");
	    
	    // Set first history step
	    for(TagLast t: tags)
	    {
	    	t.addHistoryStep(t.getOriginalTagName());
	    }

	    log.info("Data loaded\n");
	}
	
	public void nlpPipeline()
	{
		/////////////////////////////////
	    // Variable initialization  
	    TagsToCSV writer = new TagsToCSV("tags_nlp_pipeline.csv");
		
	    // Create word blacklist
	    List<String> articles = im.importCSV("dicts/article.txt");
	    List<String> preps = im.importCSV("dicts/prep.txt");
	    List<String> custom = im.importCSV("dicts/custom.txt");
	    
	    List<String> whitelist = new ArrayList<String>();
	    //whitelist.add("favoritas");
	    
	    List<String> blacklist = new ArrayList<String>();
	    blacklist.addAll(preps);
	    blacklist.addAll(articles);
	    blacklist.addAll(custom);
	    blacklist.add("");
	    
	    List<String> remove = new ArrayList<String>();
	    remove.add("'");
	    
	    List<String> replace = new ArrayList<String>();
	    replace.add("-, ");
	    replace.add("_, ");
	    replace.add(":, ");
	    replace.add(";, ");
	    replace.add("/, ");

		/////////////////////////////////
	    // Algorithm
	    
	    // Characters
	    removeReplaceCharacters(tags, remove, replace);
	    log.info("Character editing finished\n");
	    
	    // Blacklist
	    removeBlacklistedWords(tags, blacklist);
	    log.info("Blacklist finished\n");
	    
	    // Occurrence filter
	    filterTags(tags, 2);
	    log.info("Filtering finished\n");
	    
	    // Weighting
	    weighting.byWeightedMean(tags, "weighting_nlp", true);
	    log.info("Weighting finished\n");
	    
	    // Similarity replacement
	    similarity.withPhoneticsAndNgrams(tags, 0.65f,"first", whitelist, 3, true);
	    
	    // Resolve errors from replacements
	    help.correctTagsAndIDs(tags);
	    log.info("1st similiarity replacement finished\n");
	    
	    // Output
	    writer.writeTagListWithHistory(tags);
	}
	
	public void grouping()
	{
	    int maxGroupSize = 3;
	
	    TagsToCSV writer = new TagsToCSV("tags_grouping.csv");
	    
	    List<String> whitelist = new ArrayList<String>();
	    //whitelist.add("on synths");
	    //whitelist.add("Brutal Death Metal");
	    
	    // Prioritize whitelist if one exists
	    if(whitelist.size() > 0)
	    {
		    grouping.whitelist(tags, whitelist, maxGroupSize);
		    log.info("whitelist grouping finished\n");
	    }
	    
		// Find word groups
	    for(int i = 2; i<=maxGroupSize;i++)
	    {
	    	grouping.jaccard(tags, i, 0.4d, 2, true);
	    }
	    log.info("jaccard grouping finished\n");
	    
	    for(int i = 2; i<=maxGroupSize;i++)
	    {
	    	grouping.frequency(tags, i, 0.1d, true);
	    }
	    log.info("frequency grouping finished\n");

	    // Split words and compute the importance again
	    help.splitCompositeTagLast(tags);
	    
	    weighting.byWeightedMean(tags ,"second", false);
	    log.info("Splitting finished\n");
	    
	    // Output
	    writer.writeTagListCustomWeight(tags);
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
	    important_tags = getImportantWords(tags, threshold, minWordLength);
	    log.info("important tag exttraction finished\n");
		
	    // Remove subjective tags from important words
	    removeSubjectiveWords(tags, subjective, important_tags);
	    log.info("removing subjective words finished\n");
	    
	    // Word separation
	    // Find important words in the unimportant tags
	    regex.findImportantWords(tags, important_tags, threshold, minWordLength, false);
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
	
	public void removeReplaceCharacters(List<TagLast> tags, List<String> remove, List<String> replace)
	{
	    // replace/remove characters
	    help.removeReplaceCharactersAndLowerCase(tags, remove, replace);
	}
	
	public void removeBlacklistedWords(List<TagLast> tags,List<String> blacklist)
	{
		// Remove blacklisted words
	    help.removeBlacklistedWords(tags, blacklist);
	}
	
	public void filterTags(List<TagLast> tags, int minOccu)
	{
		// Remove tags which occurre below minOccu times
		help.removeOutlier(tags, minOccu, true);
	}
	
	public Map<String, String> getImportantWords(List<TagLast> tags, double threshold, int minWordLength)
	{
	    TagsToCSV writer_important = new TagsToCSV("important_tags.csv");
	    WeightingLast weighting = new WeightingLast();
	    Map<String, String> important_tags = new LinkedHashMap<String, String>();
		
		// Weighting words
	    weighting.byWeightedMean(tags, "second", false);
	    log.info("Second time importance\n");
	    
	    // Build popular tags dict on raw data
	    important_tags = help.getImportantTags(tags, threshold, minWordLength);
	    
		writer_important.writeImportantTags(important_tags);
		
		return important_tags;
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
	
	public String getJSON()
	{
	    return help.objectToJsonString(tags);
	}

	public void getDbData()
	{
	    // Load config files
	    InputStream input = null;
	    
		Properties dbconf = new Properties();
	    try {

	      input = new FileInputStream("config.db");
	      dbconf.load(input);

	    } catch (IOException e) { e.printStackTrace(); }
		
		Processor proc = new Processor(dbconf);
		
		proc.getAll();
	}
}
