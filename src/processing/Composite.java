package processing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import core.Tag;
import core.json.gridGroup;
import core.json.gridHist;

public class Composite {

	// Variables
	int index;
	
	// Classes
    private Grouping grouping = new Grouping();
  	private Helper help = new Helper();
	
    // Data
	private List<String> whitelist = new ArrayList<String>();
	private TreeMap<Double, Map<String, Integer>> jaccard_groups = new TreeMap<Double, Map<String, Integer>>();
	private TreeMap<Double, Map<String, Integer>> frequent_groups = new TreeMap<Double, Map<String, Integer>>();
	
	
	// Parameters
	private double jaccardThreshold = 0;
	private double frequentThreshold = 0;
	private int maxGroupSize = 0;
	private int minOccurrence = 0;
	private Boolean split;
    
	public Composite(int index) {
		// Set working copy
		this.index = index;
		
		// Default parameters
		maxGroupSize = 3;
		minOccurrence = 2;
		jaccardThreshold = 0.70;
		frequentThreshold = 0.35;
		split = true;
	}
	
	public void group(List<Tag> tags)
	{
		grouping.group(tags, maxGroupSize, minOccurrence, jaccard_groups, frequent_groups, index-1);
	}

	public void applyGroups(List<Tag> tags)
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
	    	name = t.getTag(index);
	    	
	    	for(String s: subs)
	    	{
	    		if(name.contains(s))
	    		{
	    			name = name.replaceAll(Pattern.quote(s), s.replace(" ", "-"));
	    		}
	    	}
	    	
	    	t.setTag(index, name);
		}
	    
	    // Find groups without spaces hardrock -> hard-rock
	    findGroups(tags, index);
	    
	    if(split)
	    {
	    	help.splitCompositeTag(tags, index);
	    	//help.correctTags(tags, index);
	    }
	}
	
	private void findGroups(List<Tag> tags, int index)
	{
		Set<String> groups = new HashSet<String>();
	    Map<String, String> subs = new HashMap<String, String>();
		
		String words[] = null;
		String name = "";
		
		//Find all groups
		for(Tag t: tags)
		{
			if(t.getTag(index).contains("-"))
			{
				words = t.getTag(index).split(" ");
				
				for(String s: words)
				{
					if(s.contains("-"))
					{
						groups.add(s);
					}
				}
			}
		}
		
		// Create substitution list
		for(String s: groups)
		{
			subs.put(s.replace("-", ""), s);
		}
		
		//Find groups
		for(Tag t: tags)
		{
			name = t.getTag(index);
			words = name.split(" +");
			
			for(String s: words)
			{
				if(subs.keySet().contains(s))
				{
					name = name.replace(s, subs.get(s));
				}
			}
			
			t.setTag(index, name);
		}
	}

    public List<gridGroup> prepareUniqueGroups() {	
		List<gridGroup> temp = new ArrayList<gridGroup>();
		
		for(Entry<Double, Map<String, Integer>> s: jaccard_groups.descendingMap().entrySet())
		{
			for(Entry<String, Integer> e: Helper.sortByComparatorInteger(s.getValue()).entrySet())
			{
				temp.add(new gridGroup(e.getKey(), s.getKey()));
			}
		}
		
		return temp;
	}

	public List<gridGroup> prepareFrequentGroups() {
		List<gridGroup> temp = new ArrayList<gridGroup>();
		
		for(Entry<Double, Map<String, Integer>> s: frequent_groups.descendingMap().entrySet())
		{
			for(Entry<String, Integer> e: Helper.sortByComparatorInteger(s.getValue()).entrySet())
			{
				temp.add(new gridGroup(e.getKey(), s.getKey()));
			}
		}
		return temp;
	}
	
	public List<gridHist> prepareFrequentHistogram() {	
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
		
		return hist;
	}

	public List<gridHist> prepareUniqueHistogram() {
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
		
		return hist;
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

	public Boolean getSplit() {
		return split;
	}

	public void setSplit(Boolean split) {
		this.split = split;
	}

}
