package tags;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.*;

import mining.*;
import processing.*;

import org.apache.commons.codec.language.*;

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
    
    //DoubleMetaphone phonetic = new DoubleMetaphone();
    ColognePhonetic phonetic = new ColognePhonetic();
    
    List<String> genres = im.importCSV("dicts/genres.txt");
    List<String> articles = im.importCSV("dicts/article.txt");
    List<String> moods = im.importCSV("dicts/moods.txt");
    List<String> preps = im.importCSV("dicts/prep.txt");   
    
    // Create word blacklist
    List<String> blacklist = new ArrayList<String>();
    
    blacklist.addAll(preps);
    blacklist.addAll(articles);
    
    /////////////////////////////////
    // Spell checking
    
    // Get all tags
    List<Tag> tags;
    
    tags = pro.getTagsWithCount();
    
    // Firsts run => Spelling
    // Create the n-grams and put them into a dictionary
    Map<String, Integer> ngrams = new HashMap<String, Integer>();
    List<String> words;
    int value;
    String key;
    
    // Create 1-word-gram/total count dict
    for(int i = 0;i < tags.size(); i++)
    {
    	words = psim.create_word_gram(tags.get(i).getName(),blacklist);
    	
    	for(int j = 0; j < words.size(); j++)
    	{
    		key = words.get(j);
    		
    		//Filter words below 3 characters
    		if(key.length()>2)
    		{
	    		if(ngrams.containsKey(key))
	    		{
	    			value = ngrams.get(key);
	    			
	    			// Sum up the count
	    			ngrams.put(key, value + tags.get(i).getCount());
	    		}
	    		else
	    		{
	    			ngrams.put(key, tags.get(i).getCount());
	    		}
    		}
    	}
    }
    
    // Create phonetic dictionary
    Map<String, Set<String>> phon = new HashMap<String, Set<String>>();
    String p;
    
    for(String k: ngrams.keySet())
    {
    	
    	//p = dmeta.encode(k);
    	
    	p = phonetic.encode(k);
    	
    	if(phon.containsKey(p))
    	{
    		Set<String> temp;
    		
    		temp = phon.get(p);
    		temp.add(k);
    		
    		phon.put(p, temp);
    		
    	}
    	else
    	{
    		Set<String> temp = new HashSet<String>();
    		
    		temp.add(k);
    		
    		phon.put(p, temp);
    	}
    }
    
    // Create word set/count dict  
    for(int i = 0;i < tags.size(); i++)
    {
    	words = psim.create_word_gram(tags.get(i).getName(),blacklist);
    	int sum = 0;
    	
    	for(int j = 0; j < words.size(); j++)
    	{
    		String word = words.get(j);
    		String code = phonetic.encode(word);
    		String high = "";
  			int count = 0;
    		
    		Set<String> str = phon.get(code);
    		
    		for(String s: str)
    		{
    			if(ngrams.get(s) > count)
    			{
    				high = s;
    				count = ngrams.get(s);
    			}
    		}
    		
    		HashSet<String> h1, h2;
    		double dice = 0;
        double jac = 0;
        double cos = 0;
    		
    		h1 = psim.create_n_gram(word, 2);
        h2 = psim.create_n_gram(high, 2);
    		
        dice = psim.dice_coeffizient(h1, h2);
        jac = psim.jaccard_index(h1, h2);
        cos = psim.cosine_similarity(h1, h2);
        
        if(dice > 0.7)
        {
        	words.set(j, high);
        }
    	}
    	
    	tags.get(i).setName(words.toString());
    }
    
    // Second run => Synonyms 

    
    // Decision with Torsten: Removing all tracks with less than six tags.
		// pro.deleteTracksWithTagsLessThan(6);
    
    // Close all
    pro.closeAll();
    
    log.info("END");
  }
}
