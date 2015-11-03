package processing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import core.Tag;
import core.TagLast;
import core.TagsToCSV;

public class Helper {
	
	   StringLengthComparator slc = new StringLengthComparator();
		PlainStringSimilarity psim = new PlainStringSimilarity();
	  
	  public void removeTagsWithoutWords(List<? extends Tag> tags)
	  {
		    // Remove tags with no words    
		    for(Iterator<? extends Tag> iterator = tags.iterator(); iterator.hasNext();)
		    {
				Tag t = iterator.next();
				  
				if(t.getTagName().length() == 0)
				{
					iterator.remove();
				}
		    }
	  }
	  
	  public void removeOutlier(List<? extends Tag> tags, int minOccu, Boolean verbose)
	  {
		  	Map<String, Integer> occu = new HashMap<String, Integer>();
		  	List<String> output = new ArrayList<String>();
		  	String key = "";
		  	String temp = "";
		  	
		  	int value;
		    List<String> words;
			TagsToCSV writer;
		  	
		  	for(Tag t: tags)
		  	{
		  		words = psim.create_word_gram(t.getTagName());
		  		
		  		for(int j = 0; j < words.size(); j++)
				{
					key = words.get(j); 	
		  			
			  		if(occu.containsKey(key))
					{
						value = occu.get(key);
						
						// Sum up the count
						occu.put(key, value + 1);
					}
					else
					{
						occu.put(key, 1);
					}
				}
		  	}
		  	
		    // Remove tags which occurs less than minOccu times 
		  	for(Tag t: tags)
		    {
		  		words = psim.create_word_gram(t.getTagName());
		  		temp = "";
				
		  		for(int j = 0; j < words.size(); j++)
				{
					key = words.get(j); 	
		  			
					if(occu.get(key) < minOccu)
					{
						// Remove word
						words.set(j, "");
						
						// Add to output list
						output.add(key);
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
		  	
			// Write temp files
		    if(verbose) 
	    	{
		    	writer = new TagsToCSV("filtered_words.csv");
		    	writer.writeFilteredWords(output);
	    	}
	  }
	  
	  public void removeBlacklistedWords(List<? extends Tag> tags, List<String> blacklist)
	  {
		  String name, uptated;    
		  List<String> list = new ArrayList<String>();
		  PlainStringSimilarity psim = new PlainStringSimilarity();
		  
		  for(Tag tag: tags)
		  {
			  name = tag.getTagName();
			  uptated = "";
			  
			  list = psim.create_word_gram(name);
			  
			  list.removeAll(blacklist);
			  
			  for(String s: list)
			  {
				  uptated = uptated + " " + s;
			  }
			  
			  tag.setTagName(uptated.trim());
		  }
		  
		  removeTagsWithoutWords(tags);
	  }
	  
	  public void removeReplaceCharactersAndLowerCase(List<? extends Tag> tags, List<String> remove, List<String> replace)
	  {
		  String updated;    
		  
		  for(Tag tag: tags)
		  {
			  // Remove characters
			  updated = tag.getTagName().toLowerCase().replaceAll(remove.toString(), "");
			  
			  // Replace characters
			  for(String s: replace)
			  {
				  String temp[] = s.split(",");
				  updated = updated.replaceAll(temp[0], temp[1]);
			  }
			  
			  tag.setTagName(updated);
		  }
	  }
	  
	  public void correctTagsAndIDs(List<TagLast> data)
	  {
		  	// TagName: TagID
		  	Map<String, Integer> tags = new HashMap<String, Integer>();
		  	// "TrackID,TagName": LastFMWeight
		  	Map<String, Integer> song_name = new HashMap<String, Integer>();
		  	
	    	Set<String> used = new HashSet<String>();
		  	
		    int ID, weight;
		    String name, key;

		    // Find maximum LastFMWeight per song/tag pair
		    for(TagLast t: data)
		    {
				ID = t.getCarrierID();
				name = t.getTagName();
				key = ID+name;
				weight = t.getTagWeight();

				if(song_name.containsKey(key))
				{
					if(weight > song_name.get(key))
					{
						song_name.put(key, weight);
					}
				}
				else
				{
					song_name.put(key, weight);
				}
		    }
		    
		    // Resolve multiple equal tags per song
		    for(TagLast t: data)
		    {
				ID = t.getCarrierID();
				name = t.getTagName();
				key = ID+name;
				weight = t.getTagWeight();

				if(song_name.containsKey(key))
				{
					if(weight < song_name.get(key))
					{
						// This marks the tag object as removable
						t.setTagName("");
					}
					
					if(weight == song_name.get(key) && used.contains(key))
					{
						// This marks the tag object as removable
						t.setTagName("");
					}
					else if(weight == song_name.get(key))
					{
						used.add(key);
					}
				}
		    }
		    
		    // Resolve multiple tag ids
			for(TagLast t:data)
			{
				ID = t.getTagID();
				name = t.getTagName();
				
				if(tags.containsKey(name))
				{
					if(ID != tags.get(name))
					{
						t.setTagID(tags.get(name));
					}
				}
				else
				{
					if(name.length()>0) tags.put(name, ID);
				}
			}	
			
			  removeTagsWithoutWords(data);
	  }
	  
	  public Map<String, String> getImportantTags(List<? extends Tag> tags, double threshold, int minWordLength)
	  {
		  Map<String, Double> important = new HashMap<String, Double>();
		  Map<String, Integer> tagid = new HashMap<String, Integer>();
		  List<String> temp = new ArrayList<String>();
		  Map<String, String> out = new LinkedHashMap<String, String>();
		  
		  for(Tag t: tags)
		  {
			  if(t.getImportance() >= threshold && t.getTagName().length() >= minWordLength)
			  {				  
				  important.put(t.getTagName(),t.getImportance());
				  tagid.put(t.getTagName(), t.getTagID());
				  temp.add(t.getTagName());
			  }
		  }
		  
		  Collections.sort(temp, slc);
		  
		  for(String l: temp)
		  {
			  if(tagid.get(l) != null)
			  {
				  out.put(l.replace(" ", "-"), tagid.get(l).toString()+","+important.get(l).toString());
			  }
			  else
			  {
				  out.put(l, "Additional Tag");
			  }
			  
		  }
		  
		  return out;
	  }
	  
	  public void splitCompositeTagLast(List<TagLast> tags)
	  {
		  	String tag = "";
		  	String name[] = null;
			List<TagLast> tt = new ArrayList<TagLast>();
		  
			  for(TagLast t: tags)
			  {
				  tag = t.getTagName();
				  
				  if(tag.contains(" "))
				  {
					  name = tag.split(" ");
					  
					  // Replace current name by the first word
					  t.setTagName(name[0]);
					  
					  // Create for all other words new entries
					  for(int i = 1; i<name.length;i++)
					  {
						  tt.add(new TagLast(1, name[i], t.getOriginalTagName(), t.getPlaycount(), t.getTagID(), t.getImportance(), t.getTagWeight(), t.getCarrierID(), t.getCarrierName(), t.getListeners(), t.getArtistID())); 	
					  }
				  }
			  }
		  
			  if(tt.size() > 0)
			  {
				  	// Add all new entries
				  	tags.addAll(tt);
				  
				  	// Fix IDs and so on
				  	correctTagsAndIDs(tags);
				  
				    // Reset index
				    for(int i = 1; i<=tags.size(); i++)
				    {
				    	tags.get(i-1).setID(i);
				    }
			  }
	  }
	  
	  public <T> String objectToJsonString(List<T> list)
	  {
		  	List<String> out = new ArrayList<String>();
		    
			//ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
			ObjectWriter ow = new ObjectMapper().writer();
			
			try {
				
				for(T t: list)
				{
					out.add(ow.writeValueAsString(t));
				}
				
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
			
			return out.toString();
	  }
}
