package processing;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.tags.Tag;
import core.tags.TagsToCSV;

public class GroupingOriginal {
	
	PlainStringSimilarity psim = new PlainStringSimilarity();
	
	public void jaccard(List<? extends Tag> tags, int size, double acceptance_value, int minOccurrence, Boolean verbose) {
		/////////////////////////////////
		// Variables
		TagsToCSV writer_groups;
		TagsToCSV writer_acc;
		
		Map<String, Long> word_count = new HashMap<String, Long>();
		Map<String, Long> word_groups = new HashMap<String, Long>();
		Map<String, Double> group_weight = new HashMap<String, Double>();
		Map<String, Double> good_groups = new HashMap<String, Double>();
	    
	    List<String> words;
	    long value;
		double nom, deno, strength, min_o = 1, max_o = 0;
		String key;
		Boolean print_groups, print_accepted;
		
		/////////////////////////////////
		// Configuration
		
		// Print temp files
	    print_groups = verbose;
		print_accepted = verbose;
		
		/////////////////////////////////
		// Algorithm
		
		// Create a 1-word-gram/total occurrences dict
		for(int i = 0;i < tags.size(); i++)
		{
			words = psim.create_word_gram(tags.get(i).getTagName());
			
			for(int j = 0; j < words.size(); j++)
			{
				key = words.get(j); 		
				
				if(word_count.containsKey(key))
				{
					value = word_count.get(key);
					
					// Sum up the count
					word_count.put(key, value + 1);
				}
				else
				{
					word_count.put(key, 1l);
				}
			}
		}
		
		// Create a n-word-gram/total occurrences
		for(int i = 0;i < tags.size(); i++)
		{
			words = psim.create_word_n_gram(tags.get(i).getTagName(),size);
			
			for(int j = 0; j < words.size(); j++)
			{
				key = words.get(j); 		
				
				if(word_groups.containsKey(key))
				{
					value = word_groups.get(key);
					
					// Sum up the count
					word_groups.put(key, value + 1l);
				}
				else
				{
					word_groups.put(key, 1l);
				}
			}
		}
		
		// Compute binding strength
		for(String k: word_groups.keySet())
		{
			words = psim.create_word_gram(k);
			deno = 0;
			
			for(String s: words)
			{	
				deno = deno + word_count.get(s);
			}
			
			nom = word_groups.get(k);
			strength = nom/deno;
			//TODO: Work in progress: Filter
			if(nom > minOccurrence) group_weight.put(k, strength);
			
			// Find min max
			if(strength < min_o) min_o = strength;
			if(strength >= max_o) max_o = strength;
		}
		
		// Normalize
		for(String s: group_weight.keySet())
		{
			strength = (group_weight.get(s)-min_o)/(max_o - min_o);
			
			// Set acceptance border
			if(strength >= acceptance_value) good_groups.put(s, strength);
		}
		
		// Write temp files
	    if(print_groups) 
    	{
	    	writer_groups = new TagsToCSV("groups_jaccard_"+size+".csv");
	    	writer_groups.writeTagOccu(word_groups);
    	}
	    
	    if(print_accepted) 
    	{
	    	writer_acc = new TagsToCSV("accepted_jaccard_complex_"+size+".csv");
	    	writer_acc.writeGroups(good_groups);
    	}
	    
	    // Replace tag groups
	    replaceGroups(tags, good_groups, size);
	}
	
	public void frequency(List<? extends Tag> tags, int size, double acceptance_value, Boolean verbose) {
	    /////////////////////////////////
	    // Variables
		PlainStringSimilarity psim = new PlainStringSimilarity();
		
	    TagsToCSV writer_groups;
	    TagsToCSV writer_acc;
		
	    Map<String, Long> word_count = new HashMap<String, Long>();
	    Map<String, Double> good_groups = new HashMap<String, Double>();
	    
	    List<String> words;
	    
	    long value;
	    double strength, min_o = 1, max_o = 0;
	    String key;
	    Boolean print_groups, print_accepted;
	    
		/////////////////////////////////
		// Configuration
	    
		// Print temp files
	    print_groups = verbose;
		print_accepted = verbose;
	    
		/////////////////////////////////
		// Algorithm
		
	    // Create a n-word-gram/total occurrences
	    for(int i = 0;i < tags.size(); i++)
	    {
	    	words = psim.create_word_n_gram(tags.get(i).getTagName(),size);
	    	
	    	for(int j = 0; j < words.size(); j++)
	    	{
	    		key = words.get(j); 		

	    		if(word_count.containsKey(key))
	    		{
	    			value = word_count.get(key);
	    			
	    			// Sum up the count
	    			word_count.put(key, value + 1);
	    		}
	    		else
	    		{
	    			word_count.put(key, 1l);
	    		}
	    	}
	    }
	    
	    // Find min and max
	    for(long k: word_count.values())
	    {   	    	
	    	if(k < min_o) min_o = k;
	    	if(k >= max_o) max_o = k;
	    }
	    
	    // Normalize the range and move the accepted groups
	    for(String s: word_count.keySet())
	    {
	    	strength = (word_count.get(s)-min_o)/(max_o - min_o);
	    	
	    	// Set acceptance border
	    	if(strength >= acceptance_value) good_groups.put(s, strength);
	    }
	    
	    // Write temp files
	    if(print_groups) 
    	{
	    	writer_groups = new TagsToCSV("groups_frequency_"+size+".csv");
	    	writer_groups.writeTagOccu(word_count);
    	}
	    
	    word_count = null;
	    
	    if(print_accepted) 
    	{
	    	writer_acc = new TagsToCSV("accepted_groups_frequency_"+size+".csv");
	    	writer_acc.writeGroups(good_groups);
    	}
	    
	    // Replace tag groups
	    replaceGroups(tags, good_groups, size);
	}
	
	public void whitelist(List<? extends Tag> tags, List<String> whitelist, int maxGroupSize)
	{
	    Map<String, Double> good_groups = new HashMap<String, Double>();
		
	    for(int i = 2; i<=maxGroupSize;i++)
	    {
		    for(String s: whitelist)
		    {
		    	if(s.split(" ").length == i)
		    	{
			    	good_groups.put(s, 1d);
		    	}
		    }
		    
			replaceGroups(tags, good_groups, i);
			
			good_groups.clear();
	    }
	}
	
	private void replaceGroups(List<? extends Tag> tags, Map<String, Double> good_groups, int size)
	{
	    Map<String, Double> subs = new HashMap<String, Double>();
	    
	    List<String> groups;
		double max_o = 0;
		String key, name;
		
		// Replace tag groups
	    for(Tag t: tags)
	    {	    	
	    	name = t.getTagName();
	    	groups = psim.create_word_n_gram(name,size);
	    	
	      // Find possible substitutions
	      for(String s : groups)
	      {
		      if(good_groups.containsKey(s))
		      {
		    	  subs.put(s, good_groups.get(s));
		      }
	      }
	      
	      do
	      {
		      max_o = 0;
		      key = "";
		      
		      // Find maximum
		      for(String s : subs.keySet())
		      {
		    	  double k = subs.get(s);
		    	  
		    	  if(k >= max_o) 
	    		  {
		    		  max_o = k;
		    		  key = s;
	    		  }
		      }
	
		      subs.remove(key);
		      
		      if(name.contains(key))
		      {
			      name = name.replaceAll(key, key.replace(" ", "-"));
		      }
		      
	      } while(subs.size() > 0);
	      
	      t.setTagName(name);
	    }
	}
}
