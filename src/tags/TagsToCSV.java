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
		
		if(head)
	    {
	      createHeader("Truth,Replaced");
	      head = false;
	    }
		
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
	
	public void writeTagNames(List<Tag> data) {
    if(head)
    {
      createHeader("Original,Processed");
      head = false;
    }

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
			// TODO Auto-generated catch block
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
