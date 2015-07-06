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
    Grouping complex_grouping = new Grouping();
    
    TagsToCSV writer_taglist = new TagsToCSV("tags_processed.csv");
    TagsToCSV writer_tags = new TagsToCSV("tags.csv");
    TagsToCSV writer_tag = new TagsToCSV("Tag.csv");
    TagsToCSV writer_track = new TagsToCSV("Track.csv");
    TagsToCSV writer_tt = new TagsToCSV("TT.csv");
    
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
    
	///////////////////////////////// 
    // Algorithm
    
    // Get all tags
    List<Tag> tags;
    tags = pro.getAll();
    
    writer_tags.writeTagList(tags);
    log.info("Data loaded\n");
    
    // Basic spell checking
    checker.withPhoneticsAndNgrams(tags, blacklist,0.6f,"first");
    log.info("1st similiarity replacement finished\n");
    
    // Find word groups
    complex_grouping.groupBy(tags, blacklist, 3,0.4d,"three");
    complex_grouping.groupBy(tags, blacklist, 2,0.4d,"two");
    log.info("complex grouping finished\n");
    
    grouping.groupBy(tags, blacklist, 3,0.2d,"three");
    grouping.groupBy(tags, blacklist, 2,0.2d,"two");
    log.info("simple grouping finished\n");
    
    // Again spell checking
    checker.withPhoneticsAndNgrams(tags, blacklist,0.7f,"second");
    log.info("2st similiarity replacement finished\n");
    
    // Weighting words without filtering
    filter.byWeightedMean(tags, blacklist,0.0d);
    log.info("weigthing and filtering finished\n");    
    
    writer_taglist.writeTagListCustomWeight(tags);
    writer_tt.writeTableTT(tags);
    writer_tag.writeTableTag(tags);
    writer_track.writeTableTrack(tags);
    
	/////////////////////////////////
    // End
    
    // Close all
    pro.closeAll();
    
    log.info("END");
  }
}
