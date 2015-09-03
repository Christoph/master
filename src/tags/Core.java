package tags;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
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
    //Processor pro = new Processor(dbconf);
    ImportCSV im = new ImportCSV();
    //SpellChecking similarity = new SpellChecking();
    //SimilarityReplacement similarity = new SimilarityReplacement();
    //SimilarityReplacementWithDistance similarity = new SimilarityReplacementWithDistance();
    //SimilarityReplacementCompleteEditDistance similarity = new SimilarityReplacementCompleteEditDistance();
    SimilarityComplex similarity = new SimilarityComplex();
    //SimilarityComplexFull similarity = new SimilarityComplexFull();
    Weighting weighting = new Weighting();
    Helper help = new Helper();
    Grouping_Simple grouping = new Grouping_Simple();
    Grouping complex_grouping = new Grouping();
    Regex regex = new Regex();
    
    TagsToCSV writer_taglist = new TagsToCSV("tags_final.csv");
    //TagsToCSV writer_tags = new TagsToCSV("tags_raw.csv");
    TagsToCSV writer_tag = new TagsToCSV("Tag.csv");
    TagsToCSV writer_track = new TagsToCSV("Track.csv");
    TagsToCSV writer_tt = new TagsToCSV("TT.csv");
    TagsToCSV writer_important = new TagsToCSV("important_tags.csv");
    TagsToCSV writer_important_filtered = new TagsToCSV("important_tags_filtered.csv");
    
    //List<String> genres = im.importCSV("dicts/genres.txt");
    //List<String> spotify = im.importCSV("dicts/spotifygenres.txt");
    //List<String> moods = im.importCSV("dicts/moods.txt");
    
    Map<String, String> important_tags = new LinkedHashMap<String, String>();
    
    List<String> articles = im.importCSV("dicts/article.txt");
    List<String> preps = im.importCSV("dicts/prep.txt");
    List<String> custom = im.importCSV("dicts/custom.txt");
    List<String> subjective = im.importCSV("dicts/subjective.txt");
    List<String> synonyms = im.importCSV("dicts/synonyms.txt");
    List<String> messedup = im.importCSV("dicts/messedgroups.txt");
    
    // Create word blacklist
    List<String> blacklist = new ArrayList<String>();
    
    blacklist.addAll(preps);
    blacklist.addAll(articles);
    // Ignoring single characters
    blacklist.addAll(custom);
    // Special character
    blacklist.add("");
    
	///////////////////////////////// 
    // Algorithm
    
    // Set importance threshold
    // 0.007 -> 500 tags
    // 0.004 -> 1000 tags
    double threshold = 0.006;
    
    // Set minimum word length
    int minWordLength = 3;
    
    // Get all tags
    List<Tag> tags;
    
    // From DB
    /*
    tags = pro.getAll();
    log.info("Data loaded\n");
    
    // Weighting words without filtering
    weighting.byWeightedMean(tags, blacklist,"first");
    log.info("First time importance finished\n");
    
    // Write out raw tags with weight
    writer_tags.writeTagListCustomWeight(tags);    
    System.out.println("tt0: "+tags.size());
    */
    /*
    // From csv file saved above
    tags = im.importTags("raw_spotify_tags.csv");
    //tags = im.importTags("raw_subset_tags.csv");
    log.info("Data loaded\n");
    
    // This line is here so i dont forget to remove it when i start from the middle
    TagsToCSV writer_cleanup = new TagsToCSV("tags_cleaned.csv");
    
    // Similarity replacement
    similarity.withPhoneticsAndNgrams(tags, blacklist,0.7f,"first");
    log.info("1st similiarity replacement finished\n");
    
    // Find word groups
    complex_grouping.groupBy(tags, blacklist, 3,0.4d,"three");
    complex_grouping.groupBy(tags, blacklist, 2,0.4d,"two");
    log.info("complex grouping finished\n");
    
    grouping.groupBy(tags, blacklist, 3,0.2d,"three");
    grouping.groupBy(tags, blacklist, 2,0.2d,"two");
    log.info("simple grouping finished\n");
    
    // Again similarity replacement
    similarity.withPhoneticsAndNgrams(tags, blacklist,0.65f,"second");
    log.info("2st similiarity replacement finished\n");
    
    // Write out cleaned tags with weight
    writer_cleanup.writeTagListCustomWeight(tags);
    System.out.println("tt1: "+tags.size());
    */
    
    // Start after similarity and grouping
    tags = im.importTags("tags_cleaned.csv");

    // Synonym replacing regex
    regex.replaceCustomWords(tags, synonyms,"synonyms");
    
    // Weighting words without filtering
    weighting.byWeightedMean(tags, blacklist,"second");
    log.info("Second time importance\n");
    
    // Build popular tags dict on raw data
    important_tags = help.getImportantTags(tags, threshold, minWordLength);
    
	writer_important.writeImportantTags(important_tags);
	
    // Remove subjective tags
    for(String s: subjective)
    {
    	if(important_tags.containsKey(s))
    	{
        	important_tags.remove(s); 
    	}
    	else
    	{
    		System.out.println(s);
    	}
    }
    
    writer_important_filtered.writeImportantTags(important_tags);
    log.info("Important tag extraction finished\n"); 
    
    // Word separation
    // Find important words in the unimportant tags
    regex.findImportantWords(tags, important_tags, threshold, minWordLength);
    log.info("Word separation finished\n");
    
    // Reset index
    for(int i = 1; i<=tags.size(); i++)
    {
    	tags.get(i-1).setTTID(i);
    }
    
    // Messed up groups replacement
    regex.replaceCustomWords(tags, messedup,"cleaning");
    
    // Weighting words as last step 
    weighting.byWeightedMean(tags, blacklist,"third");
    log.info("Last time importance\n");
    
    writer_tag.writeTableTag(tags);
    writer_track.writeTableTrack(tags);    
    writer_tt.writeTableTT(tags);
    
    // Write out final tags with weight
    writer_taglist.writeTagListCustomWeight(tags);
    
	/////////////////////////////////
    // End
    
    // Close all
    //pro.closeAll();
    
    log.info("END");
  }
}
