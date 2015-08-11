package tags;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
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
    SimilarityReplacement similarity = new SimilarityReplacement();
    SimilarityReplacementWithDistance distance = new SimilarityReplacementWithDistance();
    Filter filter = new Filter();
    Helper help = new Helper();
    Grouping_Simple grouping = new Grouping_Simple();
    Grouping complex_grouping = new Grouping();
    Regex regex = new Regex();
    StringLengthComparator slc = new StringLengthComparator();
    
    TagsToCSV writer_taglist = new TagsToCSV("tags_processed.csv");
    TagsToCSV writer_tags = new TagsToCSV("tags.csv");
    TagsToCSV writer_tag = new TagsToCSV("Tag.csv");
    TagsToCSV writer_track = new TagsToCSV("Track.csv");
    TagsToCSV writer_tt = new TagsToCSV("TT.csv");
    TagsToCSV writer_important = new TagsToCSV("tags_important.csv");
    
    // Using spotify genres: Words with 3 chars: dub, emo gay, Jit ,IDM, Mod, MPB, Noh, Oi!, pop, rai, rap, r&b, ska , son, bop, ccm, 
    // removing a few because of too much substring replacement within the genre list:
    // emo -> emotional...
    // mod -> modern...
    // rai -> praise straight...
    // son -> song ...
    // same in the lastfmgenres list
    // Customgenres is lastfm + spotify without words above and only single words no combinations
    // In addition i removed synonyms and used the lastfm words as basis: lastfm -> electronic spotify -> removed: Electro,electrinica,electronic
    // The lastfm list is without emo and "pop punk". The last one because pop and punk are each separately in the list
    // Total consists is a handmade list of words:
    // Handselected moods -> 
    // 
    List<String> genres = im.importCSV("dicts/genres.txt");
    List<String> lastfm = im.importCSV("dicts/lastfmgenres.txt");
    List<String> spotify = im.importCSV("dicts/spotifygenres.txt");
    List<String> moods = im.importCSV("dicts/moods.txt");
    
    Set<String> temp = new HashSet<String>();
    List<String> important_tags = new ArrayList<String>();
    
    List<String> articles = im.importCSV("dicts/article.txt");
    List<String> preps = im.importCSV("dicts/prep.txt");
    
    // Create word blacklist
    List<String> blacklist = new ArrayList<String>();
    
    blacklist.addAll(preps);
    blacklist.addAll(articles);
    // Somehow some tags have words without characters...
    blacklist.add("");
    
	///////////////////////////////// 
    // Algorithm
    
    // Get all tags
    List<Tag> tags;
    tags = pro.getAll(genres);
    log.info("Data loaded\n");
    
    // Weighting words without filtering
    filter.byWeightedMean(tags, blacklist,0.0d);
    log.info("weigthing and filtering finished\n");
    
    // Write out raw tags with weight
    writer_tags.writeTagListCustomWeight(tags);
    
    // Build popular tags dict on raw data
    important_tags = help.getImportantTags(tags, 0.01);
    
    // Add tags to the set
    temp.addAll(lastfm);
    temp.addAll(moods);
    temp.addAll(important_tags);

    // Reset list and add unique tags
    important_tags.clear();
    important_tags.addAll(temp);

    // Sort the list: decreasing length
	Collections.sort(important_tags, slc);
	writer_important.writeImportantTags(important_tags);
    log.info("Important tag extraction finished\n"); 
    
    // Word separation
    regex.separateWords(tags, important_tags);
    log.info("Word separation finished\n");
    
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
    
    // Export Tag before TT!
    writer_tag.writeTableTag(tags);
    writer_track.writeTableTrack(tags);
    // TrackID 42 TagID 7 exists two times! line 515, 529 in TT
    
    writer_tt.writeTableTT(tags);
    
    writer_taglist.writeTagListCustomWeight(tags);
    
	/////////////////////////////////
    // End
    
    // Close all
    pro.closeAll();
    
    log.info("END");
  }
}
