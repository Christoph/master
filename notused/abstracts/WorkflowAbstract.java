package processing.abstracts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import processing.Grouping;
import processing.Helper;
import processing.Similarity;
import processing.Weighting;
import core.ImportCSV;
import core.tags.Tag;
import core.tags.TagsToCSV;

public class WorkflowAbstract {
	
	// Initialize variables and classes
	private static final Logger log = Logger.getLogger("Logger");
  	private Helper help = new Helper();
    private ImportCSV im = new ImportCSV();
    private Weighting weighting = new Weighting();
    private Similarity similarity = new Similarity();
    Grouping grouping = new Grouping();
    
    // Full data set
    public List<Tag> tags;
    private Map<String, Double> vocab = new HashMap<String, Double>();
    private List<String> good_groups = new ArrayList<String>();
	private List<String> good_words = new ArrayList<String>();
    //private List<String> blacklist = new ArrayList<String>();
    private List<String> remove = new ArrayList<String>();
    private List<String> replace = new ArrayList<String>();
	
	public void init()
	{
		log.info("Initialize\n");
		
	    // Load data
	    tags = im.importAbstracts("abstracts_short.txt");
	    
	    // Set first history step
	    for(Tag t: tags)
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
		
	    //List<String> articles = im.importCSV("dicts/article.txt");
	    //List<String> preps = im.importCSV("dicts/prep.txt");
	    //List<String> custom = im.importCSV("dicts/custom.txt");
	    
	    //blacklist.addAll(preps);
	    //blacklist.addAll(articles);
	    //blacklist.addAll(custom);
	    //blacklist.add("");
	    
	    remove.add("'");
	    
	    //replace.add("-, ");
	    replace.add("_, ");
	    replace.add(":, ");
	    replace.add(";, ");

	    int minWordSize = 3;
	    
		/////////////////////////////////
	    // Algorithm
	    
	    // Extract good groups
	    help.extractCorrectGroupsAndWords(tags, "_", good_groups, good_words);
	    
	    // Characters
	    //help.removeReplaceCharactersAndLowerCase(tags, remove, replace);
	    log.info("Character editing finished\n");
	    
	    // Blacklist
	    //help.removeBlacklistedWords(tags, blacklist);
	    //log.info("Blacklist finished\n");
	    
	    // Weighting
	    //weighting.vocabByFrequency(tags, vocab, "first", true);
	    log.info("Weighting finished\n");
	    
	    // Similarity replacement
	    //similarity.withVocab(tags, vocab, 0.70f, "first", good_words, minWordSize, true);
	    log.info("1st similiarity replacement finished\n");
	    
	    // Output
	    writer.writeTags(tags);
	}
	
	public void grouping()
	{
	    int maxGroupSize = 3;
	
	    TagsToCSV writer = new TagsToCSV("tags_grouping.csv");
	    TagsToCSV writer_json = new TagsToCSV("data.json");
	    TagsToCSV writer_final = new TagsToCSV("abstracts.txt");
	    
	    // Prioritize whitelist if one exists
	    if(good_groups.size() > 0)
	    {
		    //grouping.whitelist(tags, good_groups, maxGroupSize);
		    log.info("whitelist grouping finished\n");
	    }
	    
		// Find word groups
	    for(int i = 2; i<=maxGroupSize;i++)
	    {
	    	//grouping.jaccard(tags, i, 0.4d, 2, true);
	    }
	    log.info("jaccard grouping finished\n");
	    
	    for(int i = 2; i<=maxGroupSize;i++)
	    {
	    	//grouping.frequency(tags, i, 0.1d, true);
	    }
	    log.info("frequency grouping finished\n");
	    
	    // Weight again
	    //weighting.vocabByFrequency(tags, vocab, "second", true);
	    
	    // Export vocab
	    String json = help.objectToJsonString(vocab);
	    writer_json.writeJson(json, "");
	    
	    // Remove dashes
	    help.removeDashes(tags);

	    // Output
	    writer.writeTags(tags);
	    writer_final.writeTags(tags);
	}
}
