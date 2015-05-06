package tags;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.*;

import processing.*;
import processing.Filter;

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
    SpellChecking checker = new SpellChecking();
    Filter filter = new Filter();
    Grouping_Simple grouping = new Grouping_Simple();
    //Grouping grouping = new Grouping();
    
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
    
	/////////////////////////////////
    // Algorithm
    
    // Basic spell checking
    checker.withPhoneticsAndNgrams(tags, blacklist);
    
    // Find word groups
    grouping.groupBy(tags, blacklist, 2);
    
    // Filter words which have a weighted mean < 5%
    filter.byWeightedMean(tags, blacklist);
        
    writer_taglist.writeTagList(tags);
    
    // Maybe outdated
    // Decision with Torsten: Removing all tracks with less than six tags.
	// pro.deleteTracksWithTagsLessThan(6);
    
	/////////////////////////////////
    // End
    
    // Close all
    pro.closeAll();
    
    log.info("END");
  }
}
