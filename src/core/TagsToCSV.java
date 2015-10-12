package core;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class TagsToCSV {
	FileWriter writer;
	Boolean head = true;

	public TagsToCSV(String file) {
		try {
			writer = new FileWriter("output/"+file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private void createHeader(String header) {
		try {
			writer.write(header+"\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void writeSubs(Map<String, String> subs)
	{
	    // Prepare data for export
	    Map<String, String> exp_subs = new HashMap<String, String>();
	    String key;

	    for(String val: subs.keySet())
	    {
	      key = subs.get(val);

	      if(exp_subs.containsKey(key))
	      {
	        exp_subs.put(key,exp_subs.get(key)+","+val);
	      }
	      else
	      {
	        exp_subs.put(key,val);
	      }
	    }
		
	    createHeader("Truth,Replaced");
		
	    TreeMap<String, String> out = new TreeMap<String, String>(exp_subs);
	    
		for(String s:out.keySet())
		{
			try {
				writer.write("\""+s+"\" ,\""+out.get(s)+"\"\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		try {
			writer.flush();
			writer.close();
			exp_subs.clear();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void writeTagWeightMap(Map<String, Double> filtered, Map<String, Double> accepted)
	{		
	    createHeader("Tag,Weight,Accepted");
		
	    for(String s:accepted.keySet())
		{
			try {
				writer.write("\""+s+"\" ,"+accepted.get(s)+" ,"+1+"\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	    
		for(String s:filtered.keySet())
		{
			try {
				writer.write("\""+s+"\" ,"+filtered.get(s)+" ,"+0+"\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		try {
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void writeGroups(Map<String, Double> groups)
	{		
	    createHeader("Tag,Strength");
		
		for(String s:groups.keySet())
		{
			try {
				writer.write("\""+s+"\" ,"+groups.get(s)+"\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		try {
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void writeTagOccu(Map<String, Long> word_groups)
	{		
	    createHeader("Tag,Occurences");
		
		for(String s:word_groups.keySet())
		{
			try {
				writer.write("\""+s+"\" ,"+word_groups.get(s)+"\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		try {
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void writeTagNames(List<TagLast> data) {
		
    createHeader("Original,Processed");

		for(TagLast t:data)
		{
			try {
				writer.write(t.getOriginalTagName()+" ,"+t.getTagName()+"\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		try {
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void writeTagList(List<TagLast> data) {
		
		createHeader("SongID,SongName,Listeners,Playcount,TagID,TagName,LastFMWeight");

		for(TagLast t:data)
		{
			try {
				writer.write(t.getCarrierID()+",\""+t.getCarrierName()+"\","+t.getListeners()+","+t.getPlaycount()+","+t.getTagID()+",\""+t.getTagName()+"\","+t.getTagWeight()+"\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		try {
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void writeTagListCustomWeight(List<TagLast> data) {
		
		createHeader("ID,SongID,SongName,Listeners,Playcount,TagID,TagName,LastFMWeight,Importance,ArtistID");

		for(TagLast t:data)
		{
			try {
				writer.write(t.getID()+","+t.getCarrierID()+",\""+t.getCarrierName()+"\","+t.getListeners()+","+t.getPlaycount()+","+t.getTagID()+",\""+t.getTagName()+"\","+t.getTagWeight()+","+t.getImportance()+","+t.getArtistID()+"\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		try {
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void writeTableTag(List<TagLast> data) {
		
		createHeader("ID,Name,Importance");
		
		Map<String, Double> tags = new HashMap<String, Double>();
	    double importance;
	    String name;
		
		for(TagLast t:data)
		{
			name = t.getTagID()+","+t.getTagName();
			importance = t.getImportance();
			
			if(!tags.containsKey(name))
			{
				tags.put(name, importance);
			}
		}
		
		for(String s: tags.keySet())
		{
			try {
				writer.write(s+","+tags.get(s)+"\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		try {
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void writeTableTT(List<TagLast> data) {
		
		createHeader("ID,TrackID,TagID,LastFMWeight,Importance");
		
		for(TagLast t:data)
		{
			try {
				writer.write(t.getID()+","+t.getCarrierID()+","+t.getTagID()+","+t.getTagWeight()+","+t.getImportance()+"\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		try {
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void writeTableTrack(List<TagLast> data) {
	
	createHeader("ID,Name,ArtistID,Listeners,Playcount");

	Map<String, String> tracks = new HashMap<String, String>();
    String name, numbers;
	
	for(TagLast t:data)
	{
		name = t.getCarrierID()+","+t.getCarrierName();
		numbers = ","+t.getArtistID()+","+t.getListeners()+","+t.getPlaycount();
		
		if(!tracks.containsKey(name))
		{
			tracks.put(name, numbers);
		}
	}
	
	for(String s: tracks.keySet())
	{
		try {
				writer.write(s+tracks.get(s)+"\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	try {
		writer.flush();
		writer.close();
	} catch (IOException e) {
		e.printStackTrace();
	}
}
	
	public void writeTag(TagLast t) {
		if(head) 
		{
			createHeader("SongID,SongName,Listeners,Playcount,TagID,TagName,TagLastFMWeight");
			head = false;
		}
		
		try {
			writer.write(t.getCarrierID()+",\""+t.getCarrierName()+"\","+t.getListeners()+","+t.getPlaycount()+","+t.getTagID()+",\""+t.getTagName()+"\","+t.getTagWeight()+"\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void writeTagOccurrences(List<TagLast> tags) {

		createHeader("TagName,Occurrence");
		
		Map<String, Integer> occu = new HashMap<String, Integer>();
		String name = "";
		int value = 0;
		
		for(TagLast t: tags)
    	{
    		name = t.getTagName();				

    		if(occu.containsKey(name))
    		{
    			value = occu.get(name);
    			
    			// Sum up the count
    			occu.put(name, value + 1);
    		}
    		else
    		{
    			occu.put(name, 1);
    		}
    	}
		
		try {
			for(String s: occu.keySet())
			{
				writer.write("\""+s+"\","+occu.get(s)+"\n");
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void closeWriteTag() {	
		try {
			writer.flush();
			writer.close();
			head = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void writeLines(List<String> data, String header) {
		
		createHeader(header);
		
		for(String s:data)
		{
			try {
				writer.write(s+"\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		try {
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
public void writeImportantTags(Map<String, String> tags) {
		
		createHeader("TagName, TagID, Importance");
		
		for(Map.Entry<String, String> entry : tags.entrySet())
		{
			try {
				writer.write(entry.getKey()+","+entry.getValue()+"\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		try {
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
