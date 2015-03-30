package tags;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class TagsToCSV {
	FileWriter writer;

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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void writeTag(Tag t) {
		createHeader("SongID,SongName,Listeners,Playcount,TagID,TagName,TagWeight");
			try {
				writer.write(t.getSongID()+",\""+t.getSongName()+"\","+t.getListeners()+","+t.getPlaycount()+","+t.getTagID()+",\""+t.getTagName()+"\","+t.getTagWeight()+"\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		/*
		try {
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}*/
	}
	
	public void closeWriteTag() {	
		try {
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
