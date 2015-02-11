package tags;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
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
    
    Processor pro = new Processor(dbconf);
    PlainStringSimilarity psim = new PlainStringSimilarity();
    
    DoubleMetaphone dmeta = new DoubleMetaphone();
    
    // Decision with Torsten: Removing all tracks with less than six tags.
		// pro.deleteTracksWithTagsLessThan(6);
    
    // Testing phonetic algorithm with real example
    String a = "rock";
    String b = "I saw this song at the rock festival!!!";
    
    String sa = dmeta.encode(a);
    System.out.println(sa);

    List<String> words;
    List<String> intersect = new ArrayList<String>();

    words = psim.create_word_gram(b);
    intersect.add(sa);

    for(int i = 0; i < words.size(); i++) {
      words.set(i,dmeta.encode(words.get(i))); 
    }
    System.out.println(words.toString());

    intersect.retainAll(words);
    
    System.out.println(intersect);
    
    // Try select from db
    List<String> base;
    List<String> lower;
    int counter = 0;
    
    base = pro.getTagsOccuringMoreThan(20);
    lower = pro.getTagsOccuringLessOrEqualThan(20);
    
    System.out.println(base.size());
    System.out.println(lower.size());
    
    for(String s: lower) {
    	words = psim.create_word_gram(s);
    	
    	words.retainAll(base);
    	
    	if(!words.isEmpty())
    	{
    		System.out.println("Tag "+(counter++)+": \""+s+"\" includes following tags: "+words);
    	}
    }
    
    // Testing the n-gram and distance methods
    String s1 = "work";
    String s2 = "wirk";
    
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

    // Close all
    pro.closeAll();
    
    log.info("END");
  }
}
