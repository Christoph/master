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
    ImportCSV im = new ImportCSV();
    
    // Full data set
    private List<TagLast> tags;
	
	public void init()
	{
		log.info("Initialize\n");
		
	    // Load data
	    tags = im.importLastTags("raw_subset_tags.csv");

	    log.info("Data loaded\n");
	}
	
	public void nlpPipeline()
	{
		/////////////////////////////////
	    // Variable initialization  
	    SimilarityComplex similarity = new SimilarityComplex();
	    WeightingLast weighting = new WeightingLast();

	    TagsToCSV writer = new TagsToCSV("tags_nlp_pipeline.csv");
		
	    // Create word blacklist
	    List<String> articles = im.importCSV("dicts/article.txt");
	    List<String> preps = im.importCSV("dicts/prep.txt");
	    List<String> custom = im.importCSV("dicts/custom.txt");
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
	    
	    // replace/remove characters
	    help.removeReplaceCharactersAndLowerCase(tags, remove, replace);
	    
	    // Remove blacklisted words
	    help.removeBlacklistedWords(tags, blacklist);
	    
	    // Weighting
	    weighting.byWeightedMean(tags, "weighting_nlp", false);
	    
	    // Similarity replacement
	    similarity.withPhoneticsAndNgrams(tags, 0.65f,"first", false);
	    
	    // Resolve errors from replacements
	    help.correctTagsAndIDs(tags);
	    help.removeTagsWithoutWords(tags);
	    log.info("1st similiarity replacement finished\n");
	    
	    // Output
	    writer.writeTagListCustomWeight(tags);
	}
	
	public void grouping()
	{
	    Grouping_Simple grouping = new Grouping_Simple();
	    Grouping complex_grouping = new Grouping();
	
	    TagsToCSV writer = new TagsToCSV("tags_grouping.csv");
	    
		// Find word groups
	    complex_grouping.groupBy(tags, 3,0.4d,"three", false);
	    complex_grouping.groupBy(tags, 2,0.4d,"two", false);
	    log.info("complex grouping finished\n");
	    
	    grouping.groupBy(tags, 3,0.1d,"three", false);
	    grouping.groupBy(tags, 2,0.1d,"two", false);
	    log.info("simple grouping finished\n");
	    
	    // Output
	    writer.writeTagListCustomWeight(tags);
	}
	
	public void regex()
	{
		/////////////////////////////////
	    // Variable initialization  
		
	    WeightingLast weighting = new WeightingLast();
	    SimilarityComplex similarity = new SimilarityComplex();
	    Regex regex = new Regex();
	    
	    TagsToCSV writer_tags = new TagsToCSV("tags_Regex.csv");
	    TagsToCSV writer_important = new TagsToCSV("important_tags.csv");
	    TagsToCSV writer_important_filtered = new TagsToCSV("important_tags_filtered.csv");
	    
	    Map<String, String> important_tags = new LinkedHashMap<String, String>();
	    
	    List<String> subjective = im.importCSV("dicts/subjective.txt");
	    List<String> synonyms = im.importCSV("dicts/synonyms.txt");
	    List<String> messedup = im.importCSV("dicts/messedgroups.txt");
	    
		///////////////////////////////// 
	    // Parameter
	    
	    // Set importance threshold
	    double threshold = 0.006;
	    
	    // Set minimum word length
	    int minWordLength = 3;

		///////////////////////////////// 
	    // Algorithm
	    
	    // Again similarity replacement
	    // TODO should be replaced with regex
	    similarity.withPhoneticsAndNgrams(tags, 0.65f,"second", false);
	    
	    // Resolve errors from replacements
	    help.correctTagsAndIDs(tags);
	    help.removeTagsWithoutWords(tags);
	    log.info("2st similiarity replacement finished\n");
	    
	    
	    
	    
	    
	    // Synonym replacing regex
	    regex.replaceCustomWords(tags, synonyms,"synonyms");
	    
	    // Weighting words without filtering
	    weighting.byWeightedMean(tags, "second", false);
	    log.info("Second time importance\n");
	    
	    // Build popular tags dict on raw data
	    important_tags = help.getImportantTags(tags, threshold, minWordLength);
	    
		writer_important.writeImportantTags(important_tags);
		
	    // Remove subjective tags
	    for(String s: subjective)
	    {
	    	if(important_tags.containsKey(s))
	    	{
	        	important_tags.remove(s); 
	    	}
	    	else
	    	{
	    		System.out.println(s);
	    	}
	    }
	    
	    writer_important_filtered.writeImportantTags(important_tags);
	    log.info("Important tag extraction finished\n"); 
	    
	    // Word separation
	    // Find important words in the unimportant tags
	    regex.findImportantWords(tags, important_tags, threshold, minWordLength);
	    log.info("Word separation finished\n");
	    
	    // Reset index
	    for(int i = 1; i<=tags.size(); i++)
	    {
	    	tags.get(i-1).setID(i);
	    }
	    
	    // Messed up groups replacement
	    regex.replaceCustomWords(tags, messedup,"cleaning");
	    
	    // Weighting words as last step 
	    weighting.byWeightedMean(tags ,"third", false);
	    log.info("Last time importance\n");
	    
	    // Output
	    writer_tags.writeTagListCustomWeight(tags);
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
