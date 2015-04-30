package tags;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.*;

import mining.*;
import processing.*;
import processing.Filter;

import org.apache.commons.codec.language.*;
import org.apache.commons.codec.language.bm.Rule.PhonemeList;

public class Core {

  private static final Logger log = Logger.getLogger("Logger");

  public static void main(String[] args) {
  // Initializing the logger
	Handler handler;

	try {
      handler = new FileHandler( "log.txt" );
      handler.setFormatter(new SimpleFormatter());
      log.addHandler( handler );
    } catch (SecurityException e1) { e1.printStackTrace();
    } catch (IOException e1) { e1.printStackTrace(); }
	
    log.info("Initializing");

    // Initializing variables
    InputStream input = null;

    // Load config files
    Properties dbconf = new Properties();
    try {

      input = new FileInputStream("config.db");
      dbconf.load(input);

    } catch (IOException e) { e.printStackTrace(); }

    ////////////////////////////////////////////////////////////////
    /// DATA IMPORT
    ////////////////////////////////////////////////////////////////
    /*
    log.info("Import");
    
    DBImport dbi = new DBImport(dbconf);
    
    // Import tracks from lastfm.
    dbi.mineAndImportCSV();

    // Close all
    dbi.closeAll();
    
    log.info("Import Finished");
    */
    ////////////////////////////////////////////////////////////////
    /// DATA Processing
    ////////////////////////////////////////////////////////////////
    
    log.info("Data Processing");
    
    /////////////////////////////////
    // Variable initialization  
    Processor pro = new Processor(dbconf);
    ImportCSV im = new ImportCSV();
    PlainStringSimilarity psim = new PlainStringSimilarity();
    SpellChecking checker = new SpellChecking();
    Filter filter = new Filter();
    Grouping_Simple grouping = new Grouping_Simple();
    
    TagsToCSV writer_taglist = new TagsToCSV("tags_processed.csv");
    
    //List<String> genres = im.importCSV("dicts/genres.txt");
    List<String> articles = im.importCSV("dicts/article.txt");
    //List<String> moods = im.importCSV("dicts/moods.txt");
    List<String> preps = im.importCSV("dicts/prep.txt");   
    List<String> custom = im.importCSV("dicts/custom.txt"); 
    
    // Create word blacklist
    List<String> blacklist = new ArrayList<String>();
    
    blacklist.addAll(preps);
    blacklist.addAll(articles);
    blacklist.addAll(custom);
    // Somehow some tags have words without characters...
    blacklist.add("");
    
    // Get all tags
    List<Tag> tags;
    tags = pro.getAll();
    
    // Exports all data
    //pro.exportAll("tags.csv");
    
    // Basic spell checking
    checker.withPhoneticsAndNgrams(tags, blacklist);
    
    // Find word groups
    grouping.groupBy(tags, blacklist, 2);
    
    // VARIATION 1
    /*
    /////////////////////////////////
    // Variables
    TagsToCSV writer_2words_groups = new TagsToCSV("groups.csv");
    TagsToCSV writer_word_occu = new TagsToCSV("occu.csv");
	
    Map<String, Integer> word_count = new HashMap<String, Integer>();
    Map<String, Integer> word_groups = new HashMap<String, Integer>();
    Map<String, Double> group_weight = new HashMap<String, Double>();
    Map<String, Double> filtered_groups = new HashMap<String, Double>();
    
    List<String> words;
    int value;
    double nom, deno, strength, min_o = 1, max_o = 0;
    String key, group;
     
    // Create a 1-word-gram/total occurrences
    for(int i = 0;i < tags.size(); i++)
    {
    	words = psim.create_word_gram(tags.get(i).getTagName(),blacklist);
    	
    	for(int j = 0; j < words.size(); j++)
    	{
    		key = words.get(j); 		

    		if(word_count.containsKey(key))
    		{
    			value = word_count.get(key);
    			
    			// Sum up the count
    			word_count.put(key, value + 1);
    		}
    		else
    		{
    			word_count.put(key, 1);
    		}
    	}
    }
    
    
    // Create a 2-word-gram/total occurrences
    for(int i = 0;i < tags.size(); i++)
    {
    	words = psim.create_word_n_gram(tags.get(i).getTagName(),2,blacklist);
    	
    	for(int j = 0; j < words.size(); j++)
    	{
    		key = words.get(j); 		

    		if(word_groups.containsKey(key))
    		{
    			value = word_groups.get(key);
    			
    			// Sum up the count
    			word_groups.put(key, value + 1);
    		}
    		else
    		{
    			word_groups.put(key, 1);
    		}
    	}
    }
    
    writer_word_occu.writeTagOccu(word_groups);
    
    // Compute binding strength
    for(String k: word_groups.keySet())
    {
    	words = psim.create_word_gram(k,blacklist);
    	deno = 0;
    	group = "";
    	
    	for(String s: words)
    	{
    		deno = deno + word_count.get(s);
    	}
    	
    	nom = word_groups.get(k);
    	strength = nom/deno;
    	group_weight.put(k.replace(" ", "-"), strength);
    	
		// Find min max
    	if(strength < min_o) min_o = strength;
    	if(strength >= max_o) max_o = strength;
    }
    
    // Normalize
    for(String s: group_weight.keySet())
    {
    	strength = (group_weight.get(s)-min_o)/(max_o - min_o);
    	
    	// Set acceptance border
    	if(strength >= 0.1) filtered_groups.put(s, strength);
    }
    
    writer_2words_groups.writeGroups(filtered_groups);
    */
    
    // Filter words which have a weighted mean < 5%
    //filter.byWeightedMean(tags, blacklist);
        
    //writer_taglist.writeTagList(tags);
    
    // Decision with Torsten: Removing all tracks with less than six tags.
	// pro.deleteTracksWithTagsLessThan(6);
    
    // Close all
    pro.closeAll();
    
    log.info("END");
  }
}
