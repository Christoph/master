package processing.lastFM;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import core.tags.TagLast;
import processing.Helper;

public class HelperLast extends Helper {

	public HelperLast() {
		// TODO Auto-generated constructor stub
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
	 
	 
}
