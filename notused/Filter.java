package processing;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import core.TagLast;
import core.TagsToCSV;

public class Filter {

	public void byWeightedMean(List<TagLast> tags, List<String> blacklist, double cutoff)
	{
	    /////////////////////////////////
	    // Variables
	    PlainStringSimilarity string_similarity = new PlainStringSimilarity();
	    Helper helper = new Helper();
	    TagsToCSV writer_filtered;
		
	    Map<String, Double> tag_words = new HashMap<String, Double>();
	    Map<String, Long> total_word_occurrence = new HashMap<String, Long>();
	    Map<String, Double> filtered_words = new HashMap<String, Double>();
	    
	    List<String> words;
	    long listeners, playcount, lastfmweight;
	    String key, new_tag = "";
	    //int listeners_min = 0,listeners_max = 0,playcount_min = 0,playcount_max = 0;
	    //double listeners_scale = 0.0, playcount_scale = 0.0;
	    double importance, max_w = 0, min_w = 0;
	    double q_lastfmweight = 1, q_listeners = 1, q_playlist = 1;
	    double value;
	    Boolean print_filtered;
	    
		/////////////////////////////////
		// Configuration
	    
		// Set weights for the weighted normalized weight for each tag/song pair
		// Lastfmweight
		q_lastfmweight = 1;
		// Listeners
		q_listeners = 1;
		// Playcount
		q_playlist = 1;
		
		// Print temp files
		print_filtered = false;

		/////////////////////////////////
		// Algorithm	
		
	    // get min and max for listeners and playcount
		/*
	    for(Tag t: tags)
	    {
	    	listeners = t.getListeners();
	    	playcount = t.getPlaycount();
	    	
	    	if(listeners > listeners_max) listeners_max = listeners;
	    	if(listeners <= listeners_min) listeners_min = listeners;
	    	
	    	if(playcount > playcount_max) playcount_max = playcount;
	    	
	    	// Some songs have a playcount of -1 and i ignore them
	    	if(playcount > -1)
	    	{
	    		if(playcount <= playcount_min) playcount_min = playcount;
	    	}
	    }
	    */
	    
	    // Compute scaling values
	    // Using this values, tagweight, listeners and playcount have the same interval: [0,100]
	    // listeners_scale = (listeners_max - listeners_min) / 100.0;
	    // playcount_scale = (playcount_max - playcount_min) / 100.0;

	    // Compute a weighted normalized weight for each tag/song pair
	    for(TagLast t: tags)
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
	    	
	    	// Compute the weighted normalized weight
	    	//t.setWeight((q_lastfmweight*lastfmweight+q_listeners*((listeners-listeners_min)/listeners_scale)+q_playlist*((playcount-playcount_min)/playcount_scale))/(q_lastfmweight+q_listeners+q_playlist));
	    	
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
	    for(TagLast t:tags)
	    {
	    	importance = tag_words.get(t.getTagName());
	    	
	    	t.setImportance(importance/max_w);
	    }
	    
	    /*
	    // Compute total occurrences of all words 
	    for(int i = 0;i < tags.size(); i++)
	    {
	    	words = string_similarity.create_word_gram(tags.get(i).getTagName(),blacklist);
	    	
	    	for(int j = 0; j < words.size(); j++)
	    	{
	    		key = words.get(j); 		

	    		if(total_word_occurrence.containsKey(key))
	    		{   			
	    			// Sum up the occurrences
	    			total_word_occurrence.put(key, total_word_occurrence.get(key) + 1);
	    		}
	    		else
	    		{
	    			total_word_occurrence.put(key, (long) 1);
	    		}
	    	}
	    }
	    
	    // Compute the weighted normalized mean for each word 

	    for(Iterator<Map.Entry<String, Double>> iterator = tag_words.entrySet().iterator(); iterator.hasNext(); ) 
	    {
	        Map.Entry<String, Double> entry = iterator.next();
	        
	        entry.setValue(entry.getValue()/total_word_occurrence.get(entry.getKey()));
	        
	        // Delete the entry if its lower than the cutoff
	       
	        if(entry.getValue() < cutoff) 
	        {
	        	filtered_words.put(entry.getKey(), entry.getValue());
	        	iterator.remove();
	        }
	        
	    }
		*/
	    
	    //total_word_occurrence = null;
	    
	    // Write temp files
	    if(print_filtered) 
    	{
		    writer_filtered = new TagsToCSV("tag_weights.csv");
    		writer_filtered.writeTagWeightMap(filtered_words,tag_words);
    	}
	    
	    filtered_words = null;
	    
	    double total_weight = 0;
	    int counter = 0;
	    
	    // Set weights
	    /*
	    for(Tag t: tags)
	    {
	    	words = string_similarity.create_word_gram(t.getTagName(),blacklist);
	    	
	    	total_weight = 0;
	    	counter = 0;
	    	
	    	for(String s: words)
	    	{
    			total_weight  = total_weight + tag_words.get(s);
    			counter++;
	    	}
	    	
	    	
	    	t.setImportance(total_weight/counter);
	    }
	    */
	    
	    // helper.removeTagsWithoutWords(tags);
	    
	    tag_words = null;
	}
}
