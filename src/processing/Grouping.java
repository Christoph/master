package processing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import core.json.gridGroup;
import core.json.gridHist;
import core.tags.Tag;
import core.tags.TagsToCSV;

public class Grouping {
	
	PlainStringSimilarity psim = new PlainStringSimilarity();
  	private Helper help = new Helper();
  	
	// Current group strengths
	private TreeMap<Double, Map<String, Integer>> jaccard_groups = new TreeMap<Double, Map<String, Integer>>();
	private TreeMap<Double, Map<String, Integer>> frequent_groups = new TreeMap<Double, Map<String, Integer>>();
	private List<String> whitelist = new ArrayList<String>();
	
    // Parameters
	private double jaccardThreshold = 0;
	private double frequentThreshold = 0;
	private int maxGroupSize = 3;
	private int minOccurrence = 2;
	private Boolean verbose = false;
	
	public void group(List<? extends Tag> tags, List<String> whitelistGroups)
	{		
		// Clear old groups
		jaccard_groups.clear();
		frequent_groups.clear();
		
		// Compute groups
		jaccard(tags);
		frequency(tags);
	}
	
	public void jaccard(List<? extends Tag> tags) {
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
				
		    	// Add all groups
				if(jaccard_groups.containsKey(strength))
				{
					jaccard_groups.get(strength).put(s, size);
				}
				else
				{
					jaccard_groups.put(strength, new HashMap<String, Integer>());
					jaccard_groups.get(strength).put(s, size);
				}
			}
			
			// Write temp files
		    if(verbose) 
	    	{
		    	writer_groups = new TagsToCSV("groups_jaccard_"+size+".csv");
		    	writer_groups.writeTagOccu(word_groups);
	    	}
	    }
	}
	
	public void frequency(List<? extends Tag> tags) {
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
		    	
		    	// Add all groups
				if(frequent_groups.containsKey(strength))
				{
					frequent_groups.get(strength).put(s, size);
				}
				else
				{
					frequent_groups.put(strength, new HashMap<String, Integer>());
					frequent_groups.get(strength).put(s, size);
				}
		    }
		    
		    // Write temp files
		    if(verbose) 
	    	{
		    	writer_groups = new TagsToCSV("groups_frequency_"+size+".csv");
		    	writer_groups.writeTagOccu(word_count);
	    	}
	    }
	}
	
	public void applyGroups(List<? extends Tag> tags)
	{
		TreeMap<Double, Map<String, Integer>> temp = new TreeMap<Double, Map<String, Integer>>();
		List<String> subs = new ArrayList<String>();
		String name;
		
		// Merge all relevant items into one list
		for(Entry<Double, Map<String, Integer>> st: frequent_groups.descendingMap().entrySet())
		{
			if(st.getKey() >= frequentThreshold)
			{
				if(!temp.containsKey(st.getKey()))
				{
					temp.put(st.getKey(), new HashMap<String, Integer>());
				}
				
				temp.get(st.getKey()).putAll(st.getValue());
			}
		}
		
		for(Entry<Double, Map<String, Integer>> st: jaccard_groups.descendingMap().entrySet())
		{
			if(st.getKey() >= jaccardThreshold)
			{
				if(!temp.containsKey(st.getKey()))
				{
					temp.put(st.getKey(), new HashMap<String, Integer>());
				}
				
				temp.get(st.getKey()).putAll(st.getValue());
			}
		}
		
		// Add substitutions in correct order: whitelist > highest group + highest strength > rest
		if(whitelist.size() > 0)
		{
			subs.addAll(whitelist);
		}
		
		for(Entry<Double, Map<String, Integer>> st: temp.descendingMap().entrySet())
		{
			subs.addAll(Helper.sortByComparatorInteger(st.getValue()).keySet());
		}
		
		// Replace word groups
	    for(Tag t: tags)
	    {
	    	name = t.getTagName();
	    	
	    	for(String s: subs)
	    	{
	    		if(name.contains(s))
	    		{
	    			name = name.replaceAll(s, s.replace(" ", "-"));
	    		}
	    	}
	    	
	    	t.setTagName(name);
		}
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

	public List<String> getWhitelist() {
		return whitelist;
	}

	public void setWhitelist(List<String> whitelist) {
		this.whitelist = whitelist;
	}

	public String getUniqueGroupsJSON() {	
		List<gridGroup> temp = new ArrayList<gridGroup>();
		
		for(Entry<Double, Map<String, Integer>> s: jaccard_groups.descendingMap().entrySet())
		{
			for(Entry<String, Integer> e: Helper.sortByComparatorInteger(s.getValue()).entrySet())
			{
				temp.add(new gridGroup(e.getKey(), s.getKey()));
			}
		}
		
		return help.objectToJsonString(temp);
	}

	public String getFrequentGroupsJSON() {
		List<gridGroup> temp = new ArrayList<gridGroup>();
		
		for(Entry<Double, Map<String, Integer>> s: frequent_groups.descendingMap().entrySet())
		{
			for(Entry<String, Integer> e: Helper.sortByComparatorInteger(s.getValue()).entrySet())
			{
				temp.add(new gridGroup(e.getKey(), s.getKey()));
			}
		}
		return help.objectToJsonString(temp);
	}
	
	public String getFrequentHistogramJSON() {	
	    List<gridHist> hist = new ArrayList<gridHist>();
	    Map<Double, Long> temp = new HashMap<Double, Long>();
	    
	    for(Entry<Double, Map<String, Integer>> c: frequent_groups.entrySet())
	    {
    		temp.put(c.getKey(), (long) c.getValue().size());
	    }
	    
	    for(double d: temp.keySet())
	    {
	    	hist.add(new gridHist(d, temp.get(d)));
	    }
		
		return help.objectToJsonString(hist);
	}

	public String getUniqueHistogramJSON() {
	    List<gridHist> hist = new ArrayList<gridHist>();
	    Map<Double, Long> temp = new HashMap<Double, Long>();
	    
	    for(Entry<Double, Map<String, Integer>> c: jaccard_groups.entrySet())
	    {
    		temp.put(c.getKey(), (long) c.getValue().size());
	    }
	    
	    for(double d: temp.keySet())
	    {
	    	hist.add(new gridHist(d, temp.get(d)));
	    }
		
		return help.objectToJsonString(hist);
	}
}
