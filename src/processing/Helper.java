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

import tags.Tag;

public class Helper {
	
	   StringLengthComparator slc = new StringLengthComparator();
	  
	  public void removeTagsWithoutWords(List<Tag> tags)
	  {
		    // Remove tags with no words    
		    for(Iterator<Tag> iterator = tags.iterator(); iterator.hasNext();)
		    {
				Tag t = iterator.next();
				  
				if(t.getTagName().length() == 0)
				{
					iterator.remove();
				}
		    }
	  }
	  
	  public void correctTagsAndIDs(List<Tag> data)
	  {
		  	// TagName: TagID
		  	Map<String, Integer> tags = new HashMap<String, Integer>();
		  	// "TrackID,TagName": LastFMWeight
		  	Map<String, Integer> song_name = new HashMap<String, Integer>();
		  	
	    	Set<String> used = new HashSet<String>();
		  	
		    int ID, weight;
		    String name, key;

		    // Find maximum LastFMWeight per song/tag pair
		    for(Tag t: data)
		    {
				ID = t.getSongID();
				name = t.getTagName();
				key = ID+name;
				weight = t.getLastFMWeight();

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
		    for(Tag t: data)
		    {
				ID = t.getSongID();
				name = t.getTagName();
				key = ID+name;
				weight = t.getLastFMWeight();

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
			for(Tag t:data)
			{
				ID = t.getTagID();
				name = t.getTagName();
				
				if(tags.containsKey(name))
				{
					if(ID != tags.get(name))
					{
						t.setTagID(tags.get(name));
						
						System.out.println("Duplicate TagID: "+name);
					}
				}
				else
				{
					if(name.length()>0) tags.put(name, ID);
				}
			}	
	  }
	  
	  public Map<String, String> getImportantTags(List<Tag> tags, double threshold, int minWordLength)
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
				  out.put(l, tagid.get(l).toString()+","+important.get(l).toString());
			  }
			  else
			  {
				  out.put(l, "Additional Tag");
			  }
			  
		  }
		  
		  return out;
	  }
}
