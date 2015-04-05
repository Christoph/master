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
    TagsToCSV writer = new TagsToCSV("tags.csv");
    SpellChecking checker = new SpellChecking();
    
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
    // Spell checking
    
    // Get all tags
    List<Tag> tags;
    tags = pro.getAll();
    
    //pro.exportAll("tags.csv");
    
    // Basic spell checking
    checker.check(tags, blacklist);

    writer.writeTagNames(tags);
    
    // Removing rare and less used tags
    TagsToCSV writer_filtered = new TagsToCSV("filtered.csv");
    TagsToCSV writer_accepted = new TagsToCSV("accepted.csv");
    TagsToCSV writer_filtered_tags = new TagsToCSV("filtered_tags.csv");
	
    Map<String, Double> wordgrams = new HashMap<String, Double>();
    Map<String, Integer> totaloccur = new HashMap<String, Integer>();
    Map<String, Double> filtered = new HashMap<String, Double>();
    
    List<String> words;
    int l, p, w;
    String key, new_tag = "";
    int lmin = 0,lmax = 0,pmin = 0,pmax = 0;
    double lscale = 0.0, pscale = 0.0;
    double qw = 1, ql = 1, qp = 1;
    double value;
    // Remove all tags below cutoff percent
    double cutoff = 5;
    
    // get min and max for listeners and playcount
    for(Tag t: tags)
    {
    	l = t.getListeners();
    	p = t.getPlaycount();
    	
    	if(l > lmax) lmax = l;
    	if(l <= lmin) lmin = l;
    	
    	if(p > pmax) pmax = p;
    	// Some songs have a playcount of -1 and i ignore them
    	if(p > -1)
    	{
    		if(p <= pmin) pmin = p;
    	}
    }
    
    // Compute scaling values
    // Using this tagweight, listeners and playcount have the same interval [0,100]
    lscale = (lmax - lmin) / 100.0;
    pscale = (pmax - pmin) / 100.0;
    
	// Set weights for the weighted normalized weight for each tag/song pair
	// Lastfmweight
	qw = 1;
	// Listeners
	ql = 2;
	// Playcount
	qp = 1;
    
    // Compute a weighted normalized weight for each tag/song pair
    for(Tag t: tags)
    {
    	l = t.getListeners();
    	p = t.getPlaycount();
    	w = t.getTagWeight();
    	
    	// If the playcount is negative I ignore it
    	if(p < 0) qp = 0;
    	
    	// Compute the weighted normalized weight
    	t.setWeight((qw*w+ql*((l-lmin)/lscale)+qp*((p-pmin)/pscale))/(qw+ql+qp));
    }
    
    // Create a 1-word-gram/weigthed average dict
    // Summing up the weights
    for(int i = 0;i < tags.size(); i++)
    {
    	words = psim.create_word_gram(tags.get(i).getTagName(),blacklist);
    	
    	for(int j = 0; j < words.size(); j++)
    	{
    		key = words.get(j); 		

    		if(wordgrams.containsKey(key))
    		{
    			value = wordgrams.get(key);
    			
    			// Sum up the weight
    			wordgrams.put(key, value + tags.get(i).getWeight());
    		}
    		else
    		{
    			wordgrams.put(key, tags.get(i).getWeight());
    		}
    	}
    }
    
    // Compute total occurrences of all words
    for(int i = 0;i < tags.size(); i++)
    {
    	words = psim.create_word_gram(tags.get(i).getTagName(),blacklist);
    	
    	for(int j = 0; j < words.size(); j++)
    	{
    		key = words.get(j); 		

    		if(totaloccur.containsKey(key))
    		{   			
    			// Sum up the occurrences
    			totaloccur.put(key, totaloccur.get(key) + 1);
    		}
    		else
    		{
    			totaloccur.put(key, 1);
    		}
    	}
    }
    
    // Compute the weighted normalized mean for each word   
    for(Iterator<Map.Entry<String, Double>> iterator = wordgrams.entrySet().iterator(); iterator.hasNext(); ) 
    {
        Map.Entry<String, Double> entry = iterator.next();
        
        entry.setValue(entry.getValue()/totaloccur.get(entry.getKey()));
        
        if(entry.getValue() <= cutoff) 
        {
        	filtered.put(entry.getKey(), entry.getValue());
        	iterator.remove();
        }
    }
    
    // Write files
    writer_filtered.writeTagWeightMap(filtered);
    writer_accepted.writeTagWeightMap(wordgrams);
    
    // Remove filtered words from all tags
    for(Tag t: tags)
    {
    	words = psim.create_word_gram(t.getTagName(),blacklist);
    	new_tag = "";
    	
    	for(String s: words)
    	{
    		if(wordgrams.containsKey(s))
    		{
    			new_tag = new_tag + " " + s;
    		}
    	}
    	
    	t.setTagName(new_tag);
    }
    
    writer_filtered_tags.writeTagNames(tags);
    
    // Decision with Torsten: Removing all tracks with less than six tags.
	// pro.deleteTracksWithTagsLessThan(6);
    
    // Close all
    pro.closeAll();
    
    log.info("END");
  }
}
