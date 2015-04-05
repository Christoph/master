package processing;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import tags.Tag;
import tags.TagsToCSV;

public class Filter {

	public void byWeightedMean(List<Tag> tags, List<String> blacklist)
	{
	    /////////////////////////////////
	    // Variables
	    PlainStringSimilarity psim = new PlainStringSimilarity();
	    TagsToCSV writer_filtered;
	    TagsToCSV writer_accepted;
		
	    Map<String, Double> wordgrams = new HashMap<String, Double>();
	    Map<String, Integer> totaloccur = new HashMap<String, Integer>();
	    Map<String, Double> filtered = new HashMap<String, Double>();
	    
	    List<String> words;
	    int l, p, w;
	    String key, new_tag = "";
	    int lmin = 0,lmax = 0,pmin = 0,pmax = 0;
	    double lscale = 0.0, pscale = 0.0;
	    double qw = 1, ql = 1, qp = 1;
	    double value, cutoff;
	    Boolean print_filtered, print_accepted;
	    
		/////////////////////////////////
		// Configuration
	    
	    // Remove all tags below cutoff percent
	    cutoff = 5;
	    
		// Set weights for the weighted normalized weight for each tag/song pair
		// Lastfmweight
		qw = 1;
		// Listeners
		ql = 2;
		// Playcount
		qp = 1;
		
		// Print temp files
		print_filtered = false;
		print_accepted = false;
	    
		/////////////////////////////////
		// Algorithm	
		
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
	    if(print_filtered) 
    	{
		    writer_filtered = new TagsToCSV("filtered.csv");
    		writer_filtered.writeTagWeightMap(filtered);
    	}
	    
	    if(print_accepted) 
    	{
		    writer_accepted = new TagsToCSV("accepted.csv");
    		writer_accepted.writeTagWeightMap(wordgrams);
    	}
	    
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
	    
	    wordgrams = null;
	    totaloccur = null;
	    filtered = null;
	}
}
