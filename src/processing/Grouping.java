package processing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.json.gridGroup;
import core.tags.Tag;
import core.tags.TagsToCSV;

public class Grouping {
	
	PlainStringSimilarity psim = new PlainStringSimilarity();
  	private Helper help = new Helper();
  	
	// Current group strengths
	private Map<String, Double> jaccard_group_weight = new HashMap<String, Double>();
	private Map<String, Double> frequent_group_weight = new HashMap<String, Double>();
	
	// Temporary frequent group map
    Map<String, Double> tempGroup = new HashMap<String, Double>();
	
	private double jaccardThreshold = 0;
	private double frequentThreshold = 0;
	private int maxGroupSize = 1;
	
	public void group(List<? extends Tag> tags, List<String> whitelistGroups, int minOccurrence)
	{		
		tempGroup.clear();
		
	    if(whitelistGroups.size() > 0)
	    {
		    whitelist(tags, whitelistGroups);
	    }
		
		jaccard(tags, minOccurrence, false);
		frequency(tags, false);
	}
	
	public void jaccard(List<? extends Tag> tags, int minOccurrence, Boolean verbose) {
	    for(int size = maxGroupSize; size>=2;size--)
	    {
			/////////////////////////////////
			// Variables
			TagsToCSV writer_groups;
			
			Map<String, Long> word_count = new HashMap<String, Long>();
			Map<String, Long> word_groups = new HashMap<String, Long>();
			Map<String, Double> groups_strength = new HashMap<String, Double>();
		    
		    List<String> words;
		    long value;
			double nom, deno, strength, min_o = 1, max_o = 0;
			String key;
			
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
				if(nom > minOccurrence) groups_strength.put(k, strength);
				
				// Find min max
				if(strength < min_o) min_o = strength;
				if(strength >= max_o) max_o = strength;
			}
			
			// Normalize
			for(String s: groups_strength.keySet())
			{
				strength = (groups_strength.get(s)-min_o)/(max_o - min_o);
				
		    	// Add to temp map
				if(strength >= jaccardThreshold)
				{
			    	tempGroup.put(s, strength);
				}
		    	// Add to output map
				jaccard_group_weight.put(s, strength);
			}
			
		    replaceGroups(tags, tempGroup, size);
			
			// Write temp files
		    if(verbose) 
	    	{
		    	writer_groups = new TagsToCSV("groups_jaccard_"+size+".csv");
		    	writer_groups.writeTagOccu(word_groups);
	    	}
	    }
	}
	
	public void frequency(List<? extends Tag> tags, Boolean verbose) {
	    for(int size = maxGroupSize; size>=2;size--)
	    {	
		    /////////////////////////////////
		    // Variables	
		    TagsToCSV writer_groups;
			
		    Map<String, Long> word_count = new HashMap<String, Long>();
		    
		    List<String> words;
		    
		    long value;
		    double strength, min_o = 1, max_o = 0;
		    String key;
		    
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
		    
		    // Normalize
		    for(String s: word_count.keySet())
		    {
		    	strength = (word_count.get(s)-min_o)/(max_o - min_o);
		    	
		    	// Add to temp map
				if(strength >= frequentThreshold)
				{
			    	tempGroup.put(s, strength);
				}
				
		    	// Add to output map
		    	frequent_group_weight.put(s, strength);
		    }
		    
		    replaceGroups(tags, tempGroup, size);
		    
		    // Write temp files
		    if(verbose) 
	    	{
		    	writer_groups = new TagsToCSV("groups_frequency_"+size+".csv");
		    	writer_groups.writeTagOccu(word_count);
	    	}
	    }
	}
	
	public void whitelist(List<? extends Tag> tags, List<String> whitelist)
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

	public void resetGroups()
	{
		jaccard_group_weight.clear();
		frequent_group_weight.clear();
	}
	
	public double getJaccardThreshold() {
		return jaccardThreshold;
	}

	public void setJaccardThreshold(double jaccardThreshold) {
		this.jaccardThreshold = jaccardThreshold;
	}

	public double getFrequentThreshold() {
		return frequentThreshold;
	}

	public void setFrequentThreshold(double frequentThreshold) {
		this.frequentThreshold = frequentThreshold;
	}

	public int getMaxGroupSize() {
		return maxGroupSize;
	}

	public void setMaxGroupSize(int maxGroupSize) {
		this.maxGroupSize = maxGroupSize;
	}

	public String getJaccardGroupsJSON() {	
		List<gridGroup> temp = new ArrayList<gridGroup>();
		
		for(String s: jaccard_group_weight.keySet())
		{
			temp.add(new gridGroup(s, jaccard_group_weight.get(s)));
		}
		
		return help.objectToJsonString(temp);
	}

	public String getFrequentGroupsJSON() {
		List<gridGroup> temp = new ArrayList<gridGroup>();
		
		for(String s: frequent_group_weight.keySet())
		{
			temp.add(new gridGroup(s, frequent_group_weight.get(s)));
		}
		return help.objectToJsonString(temp);
	}
}
