package processing;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.Tag;
import core.TagsToCSV;

public class Grouping_Simple {
	
	public void groupBy(List<? extends Tag> tags, int size, double acceptance_value, Boolean verbose) {
	    /////////////////////////////////
	    // Variables
		PlainStringSimilarity psim = new PlainStringSimilarity();
		
	    TagsToCSV writer_groups;
	    TagsToCSV writer_acc;
		
	    Map<String, Long> word_count = new HashMap<String, Long>();
	    Map<String, Double> good_groups = new HashMap<String, Double>();
	    Map<String, Double> subs = new HashMap<String, Double>();
	    
	    List<String> words, groups, temp;
	    
	    long value;
	    double strength, min_o = 1, max_o = 0;
	    String key, new_tag;
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
	    	writer_groups = new TagsToCSV("groups_simple_"+size+".csv");
	    	writer_groups.writeTagOccu(word_count);
    	}
	    
	    word_count = null;
	    
	    if(print_accepted) 
    	{
	    	writer_acc = new TagsToCSV("accepted_groups_simple_"+size+".csv");
	    	writer_acc.writeGroups(good_groups);
    	}
	    
	    // Replace tag groups
	    for(Tag t: tags)
	    {	    	
		  groups = psim.create_word_n_gram(t.getTagName(),size);
		  words = psim.create_word_gram(t.getTagName());
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
	
		      temp = psim.create_word_gram(key);
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
	    
	    good_groups = null;
	    subs = null;
	}
}
