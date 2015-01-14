package tags;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
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
    
    PhoneticStringSimilarity phon = new PhoneticStringSimilarity();
    Soundex sound = new Soundex();
    RefinedSoundex rsound = new RefinedSoundex();
    Caverphone2 cave = new Caverphone2();
    ColognePhonetic colo = new ColognePhonetic();
    DaitchMokotoffSoundex dai = new DaitchMokotoffSoundex();
    Nysiis nys = new Nysiis();
    Metaphone meta = new Metaphone();
    DoubleMetaphone dmeta = new DoubleMetaphone();
    
    // Decision with Torsten: Removing all tracks with less than six tags.
		// pro.deleteTracksWithTagsLessThan(6);
    
    // Examples phonetic codes
    String test = "Awesome song";
    
    System.out.println("My Implementation:");
    System.out.println("RefinedSoundex: String: "+test+" Code: "+phon.refinedSoundex(test));
    
    System.out.println("\nLib:");
    System.out.println("Soundex: String: "+test+"; Code: "+sound.encode(test));
    System.out.println("RefinedSoundex: String: "+test+"; Code: "+rsound.encode(test));
    System.out.println("Caverphone2: String: "+test+"; Code: "+cave.encode(test));
    System.out.println("ColognePhonetic: String: "+test+"; Code: "+colo.encode(test));
    System.out.println("DaitchMokotoffSoundex: String: "+test+"; Code: "+dai.encode(test));
    System.out.println("Nysiis: String: "+test+"; Code: "+nys.encode(test));
    System.out.println("Metaphone: String: "+test+"; Code: "+meta.encode(test));
    System.out.println("DoubleMetaphone: String: "+test+"; Code: "+dmeta.encode(test));
    
    test = "This is an awesome song!!!";
    
    System.out.println("\nTesting methods with a longer tag:");
    System.out.println("Soundex: String: "+test+"; Code: "+sound.encode(test));
    System.out.println("RefinedSoundex: String: "+test+"; Code: "+rsound.encode(test));
    System.out.println("Caverphone2: String: "+test+"; Code: "+cave.encode(test));
    System.out.println("ColognePhonetic: String: "+test+"; Code: "+colo.encode(test));
    System.out.println("DaitchMokotoffSoundex: String: "+test+"; Code: "+dai.encode(test));
    System.out.println("Nysiis: String: "+test+"; Code: "+nys.encode(test));
    System.out.println("Metaphone: String: "+test+"; Code: "+meta.encode(test));
    System.out.println("DoubleMetaphone: String: "+test+"; Code: "+dmeta.encode(test));
    
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
