package tags;

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
			writer = new FileWriter(file);
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
	
	public void writeTagNames(List<Tag> data) {
		
    createHeader("Original,Processed");

		for(Tag t:data)
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
	
	public void writeTagList(List<Tag> data) {
		
		createHeader("SongID,SongName,Listeners,Playcount,TagID,TagName,LastFMWeight");

		for(Tag t:data)
		{
			try {
				writer.write(t.getSongID()+",\""+t.getSongName()+"\","+t.getListeners()+","+t.getPlaycount()+","+t.getTagID()+",\""+t.getTagName()+"\","+t.getLastFMWeight()+"\n");
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
	
	public void writeTagListCustomWeight(List<Tag> data) {
		
		createHeader("ID,SongID,SongName,Listeners,Playcount,TagID,TagName,LastFMWeight,Importance,ArtistID");

		for(Tag t:data)
		{
			try {
				writer.write(t.getTTID()+","+t.getSongID()+",\""+t.getSongName()+"\","+t.getListeners()+","+t.getPlaycount()+","+t.getTagID()+",\""+t.getTagName()+"\","+t.getLastFMWeight()+","+t.getImportance()+","+t.getArtistID()+"\n");
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
	
	public void writeTableTag(List<Tag> data) {
		
		createHeader("ID,\"Name\"");
	    Map<String, Integer> tags = new HashMap<String, Integer>();
	    int ID;
	    String name;

		for(Tag t:data)
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
				tags.put(name, ID);
			}
		}
		
		for(String s:tags.keySet())
		{
			try {
				writer.write(tags.get(s)+",\""+s+"\""+"\n");
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
	
	public void writeTableTT(List<Tag> data) {
		
		createHeader("ID,TrackID,TagID,LastFMWeight,Importance");
		
		for(Tag t:data)
		{
			try {
				writer.write(t.getTTID()+","+t.getSongID()+","+t.getTagID()+","+t.getLastFMWeight()+","+t.getImportance()+"\n");
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

	public void writeTableTrack(List<Tag> data) {
	
	createHeader("ID,\"Name\",ArtistID,Listeners,Playcount");
	int temp = 0;

	for(Tag t:data)
	{
		try {
			if(t.getSongID() != temp)
			{
				writer.write(t.getSongID()+",\""+t.getSongName()+"\","+t.getArtistID()+","+t.getListeners()+","+t.getPlaycount()+"\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		temp = t.getSongID();
	}
	
	try {
		writer.flush();
		writer.close();
	} catch (IOException e) {
		e.printStackTrace();
	}
}
	
	public void writeTag(Tag t) {
		if(head) 
		{
			createHeader("SongID,SongName,Listeners,Playcount,TagID,TagName,TagLastFMWeight");
			head = false;
		}
		
		try {
			writer.write(t.getSongID()+",\""+t.getSongName()+"\","+t.getListeners()+","+t.getPlaycount()+","+t.getTagID()+",\""+t.getTagName()+"\","+t.getLastFMWeight()+"\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void writeTagOccurrences(List<Tag> tags) {

		createHeader("TagName,Occurrence");
		
		Map<String, Integer> occu = new HashMap<String, Integer>();
		String name = "";
		int value = 0;
		
		for(Tag t: tags)
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

	public void writeSeparation(List<String> separation) {
		
		createHeader("Separations");
		
		for(String s:separation)
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
	
public void writeImportantTags(Map<String, Double> tags) {
		
		createHeader("Important Tags");
		
		for(Map.Entry<String, Double> entry : tags.entrySet())
		{
			try {
				writer.write(entry.getKey()+":"+entry.getValue()+"\n");
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
