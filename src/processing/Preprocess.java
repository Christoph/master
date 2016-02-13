package processing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import core.json.gridHist;
import core.json.gridVocab;
import core.tags.Tag;
import core.tags.TagLast;

public class Preprocess {

	// Classes
    private Weighting weighting = new Weighting();
	private PlainStringSimilarity psim = new PlainStringSimilarity();
	
    // Data
    private Map<String, Double> tagsFreq = new HashMap<String, Double>();
	
	// Parameters
	private double filter;
	private String remove;
	private List<String> replace = new ArrayList<String>();
    private List<String> whitelistWords = new ArrayList<String>();
    private List<String> whitelistGroups = new ArrayList<String>();
    private List<String> blacklist = new ArrayList<String>();
	
	public Preprocess() {
		// Default parameters
		replace.add("-, ");
		replace.add("_, ");
		replace.add(":, ");
		replace.add(";, ");
		replace.add("/, ");
		
		setFilter(0d);
		setRemove("'");
	}
	
	// Create a Word/Occurences map
	public void computeWordFreq(List<TagLast> tags)
	{
		setToLowerCase(tags);
		
		weighting.vocabByFrequency(tags, tagsFreq);
	}

	// Remove all words below the threshold
	public void applyFilter(List<TagLast> tags) 
	{
	  	String key = "";
	  	String temp = "";
	    List<String> words;

	    if(tagsFreq.size() > 0)
	    {
		  	for(Tag t: tags)
		    {
		  		words = psim.create_word_gram(t.getTagName());
		  		temp = "";
				
		  		for(int j = 0; j < words.size(); j++)
				{
					key = words.get(j); 	
		  			
					if(tagsFreq.get(key) < filter && key.length() > 0)
					{
						// Remove word
						words.set(j, "");
					}
				}
		  		
		  		// Rebuild tags and save them
		  		for(String s: words)
		  		{
		  			if(s.length()>0)
		  			{
		  				temp = temp + s + " ";
		  			}
		  		}
		  		
		  		t.setTagName(temp.trim());
		    }
	    }
	}
	
	  public void removeCharacters(List<TagLast> tags)
	  {
		  String updated;    
		  
		  for(Tag tag: tags)
		  {
			  updated = tag.getTagName();
			  
			  // Remove characters
			  if(remove.length() > 0) 
			  {
				  	updated = updated.replaceAll("["+remove+"]", "");
			  }
	
			  tag.setTagName(updated);
		  }
	  }
	  
	  public void replaceCharacters(List<TagLast> tags)
	  {
		  String updated;    
		  
		  for(Tag tag: tags)
		  {
			  updated = tag.getTagName();
			  
			  if(replace.size() > 0)
			  {
				  // Replace characters
				  for(String s: replace)
				  {
					  String temp[] = s.split(",");
					  updated = updated.replaceAll(temp[0], temp[1]);
				  }
			  }

			  tag.setTagName(updated);
		  }
	  }
	  
	  public void setToLowerCase(List<? extends Tag> tags)
	  {
		  String updated;    
		  
		  for(Tag tag: tags)
		  {
			  updated = tag.getTagName().toLowerCase();

			  tag.setTagName(updated);
		  }
	  }

	public List<gridVocab> preparePreFilter()
	{
	    List<gridVocab> tags_filtered = new ArrayList<gridVocab>();
	    
	    for(String s: tagsFreq.keySet())
	    {
	    	tags_filtered.add(new gridVocab(s, tagsFreq.get(s)));
	    }

	    return tags_filtered;
	}
	
	public List<gridHist> preparePreFilterHistogram()
	{
	    List<gridHist> hist = new ArrayList<gridHist>();
	    Map<Double, Long> temp = new HashMap<Double, Long>();
	    
	    for(Entry<String, Double> c: tagsFreq.entrySet())
	    {
    		if(temp.containsKey(c.getValue()))
    		{
    			temp.put(c.getValue(), temp.get(c.getValue()) + 1);
    		}
    		else
    		{
    			temp.put(c.getValue(), (long) 1);
    		}

	    }
	    
	    for(double d: temp.keySet())
	    {
	    	hist.add(new gridHist(d, temp.get(d)));
	    }

	    return hist;
	}
	  
	public double getFilter() {
		return filter;
	}

	public void setFilter(double filter) {
		this.filter = filter;
	}

	public String getRemove() {
		return remove;
	}

	public void setRemove(String remove) {
		this.remove = remove;
	}

	public List<String> getReplace() {
		return replace;
	}

	public void setReplace(List<Map<String, Object>> map) {
		
		replace.clear();
		
		for(int i = 0; i < map.size(); i++)
		{
			replace.add(map.get(i).get("replace")+","+map.get(i).get("by"));
		}
	}

	public List<String> getDictionary() {
		List<String> temp = new ArrayList<String>();
		
		temp.addAll(whitelistWords);
		temp.addAll(whitelistGroups);
		
		return temp;
	}

	public void setDictionary(List<Map<String, Object>> map) {
		String tag;
		whitelistGroups.clear();
		whitelistWords.clear();

		for(int i = 0; i < map.size(); i++)
		{
			tag = map.get(i).get("tag")+"";
			
			if(tag.contains(" "))
			{
				whitelistGroups.add(tag);
				
				for(String s: tag.split(" "))
				{
					whitelistWords.add(s);
				}
			}
			else
			{
				if(!whitelistWords.contains(tag)) whitelistWords.add(tag);
			}
		}
	}

	public List<String> getWhitelistWords() {
		return whitelistWords;
	}
	
	public List<String> getWhitelistGroups() {
		return whitelistGroups;
	}

	public List<String> getBlacklist() {
		return blacklist;
	}

	public void setBlacklist(List<String> blacklist) {
		this.blacklist = blacklist;
	}
}
