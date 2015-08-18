package processing;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tags.Tag;

public class Weighting {

	public void byWeightedMean(List<Tag> tags, List<String> blacklist)
	{
	    /////////////////////////////////
	    // Variables
	    Map<String, Double> tag_words = new HashMap<String, Double>();
	    
	    long listeners, playcount, lastfmweight;
	    String key;
	    //int listeners_min = 0,listeners_max = 0,playcount_min = 0,playcount_max = 0;
	    //double listeners_scale = 0.0, playcount_scale = 0.0;
	    double importance, max_w = 0;
	    double q_listeners = 1, q_playlist = 1;
	    double value;
	    
		/////////////////////////////////
		// Configuration
	    
		// Set weights for the weighted normalized weight for each tag/song pair
		// Listeners
		q_listeners = 1;
		// Playcount
		q_playlist = 1;

		/////////////////////////////////
		// Algorithm	

	    // Compute a weighted normalized weight for each tag/song pair
	    for(Tag t: tags)
	    {
	    	listeners = t.getListeners();
	    	playcount = t.getPlaycount();
	    	lastfmweight = t.getLastFMWeight();
	    	
	    	// If the playcount is negative I ignore it
	    	if(playcount <= 0) 
    		{
    			q_playlist = 0;
    			// If playcount is 0 i get a weight of NaN
    			playcount = 1;
    		}
	    		    	
	    	importance = lastfmweight*(q_listeners*Math.log(listeners)+q_playlist*Math.log(playcount));
	    	
	    	t.setImportance(importance); 
	    	
	    }
	    
	    // Summing up the weights
	    for(int i = 0;i < tags.size(); i++)
	    {	    	    		
    		importance = tags.get(i).getImportance();
    		key = tags.get(i).getTagName();

    		if(tag_words.containsKey(key))
    		{
    			value = tag_words.get(key);
    			
    			// Sum up the weight over all songs
    			tag_words.put(key, value + importance);
    		}
    		else
    		{
    			tag_words.put(key, importance);
    		}
	    }
	    
	    // maximum importance of tag_words
	    for(String s: tag_words.keySet())
	    {
	    	if(tag_words.get(s) > max_w)
	    	{
	    		max_w = tag_words.get(s);
	    	}
	    }
	    
	    // normalizing tags
	    for(Tag t:tags)
	    {
	    	importance = tag_words.get(t.getTagName());
	    	
	    	t.setImportance(importance/max_w);
	    }
  
	    tag_words = null;
	}
}
