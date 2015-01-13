package tags;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.logging.*;

import mining.*;
import processing.*;

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
    log.info("Import");
    
    DBImport dbi = new DBImport(dbconf);
    
    // Import tracks from lastfm.
    //dbi.mineAndImportCSV();

    // Close all
    dbi.closeAll();
    
    log.info("Import Finished");
    
    ////////////////////////////////////////////////////////////////
    /// DATA Processing
    ////////////////////////////////////////////////////////////////
    log.info("Data Processing");
    
    Processor pro = new Processor(dbconf);
    PlainStringSimilarity psim = new PlainStringSimilarity();
    
    // Decision with Torsten: Removing all tracks with less than six tags.
		// pro.deleteTracksWithTagsLessThan(6);
    
    // Testing phonetic algorithms
    PhoneticStringSimilarity phon = new PhoneticStringSimilarity();
    
    String s1 = "work";
    String s2 = "wirk";

    String p1 = phon.refinedSoundex(s1);
    String p2 = phon.refinedSoundex(s2);
    
    // Testing the n-gram and distance methods
    double dice = 0;
    double jac = 0;
    double cos = 0;
    HashSet<String> h1, h2;
    
    h1 = psim.create_n_gram(p1, 2);
    h2 = psim.create_n_gram(p2, 2);
    
    dice = psim.dice_coeffizient(h1, h2);
    jac = psim.jaccard_index(h1, h2);
    cos = psim.cosine_similarity(h1, h2);
    
    // Close all
    pro.closeAll();
    
    log.info("END");
  }
}
