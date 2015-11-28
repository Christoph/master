package processing.abstracts;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import processing.Grouping;
import processing.Helper;
import processing.Similarity;
import processing.Weighting;
import processing.lastFM.HelperLast;
import processing.lastFM.RegexLast;
import processing.lastFM.WeightingLast;
import core.ImportCSV;
import core.db.Processor;
import core.tags.Tag;
import core.tags.TagLast;
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
    private List<Tag> tags;
    private Map<String, Double> vocab = new HashMap<String, Double>();
	
	public void init()
	{
		log.info("Initialize\n");
		
	    // Load data
	    tags = im.importAbstracts("abstracts.txt");
	    
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
		
	    // Create word blacklist
	    //List<String> articles = im.importCSV("dicts/article.txt");
	    //List<String> preps = im.importCSV("dicts/prep.txt");
	    //List<String> custom = im.importCSV("dicts/custom.txt");
	    
	    List<String> whitelist = new ArrayList<String>();
	    //whitelist.add("favoritas");
	    
	    //List<String> blacklist = new ArrayList<String>();
	    //blacklist.addAll(preps);
	    //blacklist.addAll(articles);
	    //blacklist.addAll(custom);
	    //blacklist.add("");
	    
	    //List<String> remove = new ArrayList<String>();
	    //remove.add("'");
	    
	    //List<String> replace = new ArrayList<String>();
	    //replace.add("-, ");
	    //replace.add("_, ");
	    //replace.add(":, ");
	    //replace.add(";, ");
	    //replace.add("/, ");

	    int minWordSize = 3;
	    
		/////////////////////////////////
	    // Algorithm
	    
	    // Characters
	    //removeReplaceCharacters(tags, remove, replace);
	    //log.info("Character editing finished\n");
	    
	    // Blacklist
	    //removeBlacklistedWords(tags, blacklist);
	    //log.info("Blacklist finished\n");
	    
	    // Weighting
	    weighting.vocabByFrequency(tags, vocab, "first", true);
	    log.info("Weighting finished\n");
	    
	    // Similarity replacement
	    similarity.withVocab(tags, vocab, 0.65f,"first", whitelist, minWordSize, true);
	    log.info("1st similiarity replacement finished\n");
	    
	    // Output
	    writer.writeAbstracts(tags);
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
	    //help.splitCompositeTagLast(tags);
	    
	    //weighting.byWeightedMean(tags ,"second", false);
	    log.info("Splitting finished\n");
	    
	    // Output
	    //writer.writeTagListCustomWeight(tags);
	}
}
