package tags;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
		
		for(String s:exp_subs.keySet())
		{
			try {
				writer.write("\""+s+"\" ,\""+exp_subs.get(s)+"\"\n");
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
	
	public void writeTagOccu(Map<String, Integer> groups)
	{		
	    createHeader("Tag,Occurences");
		
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
		
		createHeader("SongID,SongName,Listeners,Playcount,TagID,TagName,TagWeight");

		for(Tag t:data)
		{
			try {
				writer.write(t.getSongID()+",\""+t.getSongName()+"\","+t.getListeners()+","+t.getPlaycount()+","+t.getTagID()+",\""+t.getTagName()+"\","+t.getTagWeight()+"\n");
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
	
	public void writeTag(Tag t) {
		if(head) 
		{
			createHeader("SongID,SongName,Listeners,Playcount,TagID,TagName,TagWeight");
			head = false;
		}
		
		try {
			writer.write(t.getSongID()+",\""+t.getSongName()+"\","+t.getListeners()+","+t.getPlaycount()+","+t.getTagID()+",\""+t.getTagName()+"\","+t.getTagWeight()+"\n");
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
}
