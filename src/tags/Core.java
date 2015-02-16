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
    
    DoubleMetaphone dmeta = new DoubleMetaphone();
    
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
    List<RawTag> raw;
    
    raw = pro.getTagsWithCount();
    
    // Create the n-grams and put them into a dictionary
    Map<String, Integer> ngrams = new HashMap<String, Integer>();
    List<String> words;
    int value;
    String key;
    
    for(int i = 0;i < raw.size(); i++)
    {
    	words = psim.create_total_word_gram(raw.get(i).getName());
    	
    	//Removing blacklisted words
    	words.removeAll(blacklist);
    	
    	for(int j = 0; j < words.size(); j++)
    	{
    		key = words.get(j);
    		
    		//Filter words below 2 characters
    		if(key.length()>1)
    		{
	    		if(ngrams.containsKey(key))
	    		{
	    			value = ngrams.get(key);
	    			
	    			ngrams.put(key, value + raw.get(i).getCount());
	    		}
	    		else
	    		{
	    			ngrams.put(key, raw.get(i).getCount());
	    		}
    		}
    	}
    }
    
    // old
    /////////////////////////////////
    // Split the tags into popular and not popular ones
    List<String> base;
    List<String> lower;
    int counter = 0;
    
    base = pro.getTagsOccuringMoreThan(20);
    lower = pro.getTagsOccuringLessOrEqualThan(20);
    
    List<String> total = new ArrayList<String>();
    
    total.addAll(base);
    total.addAll(lower);
    
    
    // Create a set of all base word grams
    // I use the HashSet to have each gram only once in the list
    HashSet<String> base_grams = new HashSet<String>();
    // List<String> base_grams = new ArrayList<String>();
    
    for(String s: base)
    {
    	base_grams.addAll(psim.create_total_word_gram(s));
    }
    
    /*
    for(String s: lower) {
    	words = psim.create_total_word_gram(s);
      
    	words.retainAll(base_grams);
    	
    	if(!words.isEmpty())
    	{
    		System.out.println("Tag "+(counter++)+": \""+s+"\" includes following tags: "+words);
    	}
    }
    */
    
    /////////////////////////////////
    // Testing the n-gram and distance methods
    String s1 = "hip hop";
    String s2 = "hip-hop";
    
    double dice = 0;
    double jac = 0;
    double cos = 0;
    HashSet<String> h1, h2;
    
    h1 = psim.create_n_gram(s1, 2);
    h2 = psim.create_n_gram(s2, 2);
    
    dice = psim.dice_coeffizient(h1, h2);
    jac = psim.jaccard_index(h1, h2);
    cos = psim.cosine_similarity(h1, h2);
    
    System.out.println("\nDistance measures:");
    System.out.println("StringA: "+s1+"; StringB: "+s2);
    System.out.println("Dice coefficient: "+dice);
    System.out.println("Jaccard similarity: "+jac);
    System.out.println("Cosine similarity: "+cos);

    // Testing phonetic algorithm
    String a = "hip hop";
    String b = "I saw this song at the hip hop festival!!!";
    
    String sa = dmeta.encode(a);
    System.out.println(sa);

    List<String> intersect = new ArrayList<String>();

    words = psim.create_word_gram(b);
    intersect.add(sa);

    for(int i = 0; i < words.size(); i++) {
      words.set(i,dmeta.encode(words.get(i))); 
    }
    System.out.println(words.toString());

    intersect.retainAll(words);
    
    System.out.println(intersect);
    
    // Decision with Torsten: Removing all tracks with less than six tags.
		// pro.deleteTracksWithTagsLessThan(6);
    
    // Close all
    pro.closeAll();
    
    log.info("END");
  }
}
