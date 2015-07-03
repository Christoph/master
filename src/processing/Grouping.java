package processing;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tags.Tag;
import tags.TagsToCSV;

public class Grouping {
	public void groupBy(List<Tag> tags, List<String> blacklist, int size, double acceptance_value, String filename_suffix) {
		/////////////////////////////////
		// Variables
		PlainStringSimilarity psim = new PlainStringSimilarity();
		
		TagsToCSV writer_groups;
		TagsToCSV writer_acc;
		
		Map<String, Integer> word_count = new HashMap<String, Integer>();
		Map<String, Integer> word_groups = new HashMap<String, Integer>();
		Map<String, Double> group_weight = new HashMap<String, Double>();
		Map<String, Double> good_groups = new HashMap<String, Double>();
	    Map<String, Double> subs = new HashMap<String, Double>();
	    
	    List<String> words, groups, temp;
	    int value;
		double nom, deno, strength, min_o = 1, max_o = 0;
		String key, new_tag;
		Boolean print_groups, print_accepted;
		
		/////////////////////////////////
		// Configuration
		
		// Print temp files
	    print_groups = true;
		print_accepted = true;
		
		/////////////////////////////////
		// Algorithm
		
		// Create a 1-word-gram/total occurrences
		for(int i = 0;i < tags.size(); i++)
		{
			words = psim.create_word_gram(tags.get(i).getTagName(),blacklist);
			
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
					word_count.put(key, 1);
				}
			}
		}
		
		// Create a n-word-gram/total occurrences
		for(int i = 0;i < tags.size(); i++)
		{
			words = psim.create_word_n_gram(tags.get(i).getTagName(),size,blacklist);
			
			for(int j = 0; j < words.size(); j++)
			{
				key = words.get(j); 		
				
				if(word_groups.containsKey(key))
				{
					value = word_groups.get(key);
					
					// Sum up the count
					word_groups.put(key, value + 1);
				}
				else
				{
					word_groups.put(key, 1);
				}
			}
		}
		
		// Compute binding strength
		for(String k: word_groups.keySet())
		{
			words = psim.create_word_gram(k,blacklist);
			deno = 0;
			
			for(String s: words)
			{
				deno = deno + word_count.get(s);
			}
			
			nom = word_groups.get(k);
			strength = nom/deno;
			group_weight.put(k, strength);
			
			// Find min max
			if(strength < min_o) min_o = strength;
			if(strength >= max_o) max_o = strength;
		}
		
		word_count = null;
		
		// Normalize
		for(String s: group_weight.keySet())
		{
			strength = (group_weight.get(s)-min_o)/(max_o - min_o);
			
			// Set acceptance border
			if(strength >= acceptance_value) good_groups.put(s, strength);
		}
		
		group_weight = null;
		
		// Write temp files
	    if(print_groups) 
    	{
	    	writer_groups = new TagsToCSV("groups_complex_"+filename_suffix+".csv");
	    	writer_groups.writeTagOccu(word_groups);
    	}
	    
	    word_groups = null;
	    
	    if(print_accepted) 
    	{
	    	writer_acc = new TagsToCSV("accepted_groups_complex_"+filename_suffix+".csv");
	    	writer_acc.writeGroups(good_groups);
    	}
	    
	    // Replace tag groups
	    for(Tag t: tags)
	    {	    	
		  groups = psim.create_word_n_gram(t.getTagName(),size,blacklist);
		  words = psim.create_word_gram(t.getTagName(),blacklist);
	      new_tag = "";
	    	
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
	
		      temp = psim.create_word_gram(key,blacklist);
		      subs.remove(key);
		      
		      if(words.containsAll(temp))
		      {
			      words.removeAll(temp);
			      
			      key = key.replace(" ", "-");
			      
			      new_tag = new_tag+" "+key;
		      }  
	      } while(subs.size() > 0);
	      
	      // Add missing single word tags
	      for(String s : words)
	      {
	    	  new_tag = new_tag+" "+s;
	      }
	      
	      // Replace tag name
	      t.setTagName(new_tag.trim());
	    }
	    
	    subs = null;
	    good_groups = null;
	}
}
