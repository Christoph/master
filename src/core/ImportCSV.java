package core;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.Normalizer;
import java.util.*;

public class ImportCSV {

  private List<String> lines;

public List<String> importCSV(String data)
  {
    lines = new ArrayList<String>();
    BufferedReader br = null;
    
    try {
      String line;
      // From tags_cleaned.csv      
      br = new BufferedReader(new InputStreamReader(
              new FileInputStream(data), "UTF8"));
      
		while ((line = br.readLine()) != null) 
		{
			lines.add(Normalizer.normalize(line, Normalizer.Form.NFC));
		}
		
		br.close();
    } catch (IOException e) { e.printStackTrace(); }
    return lines;
  }
  
  public List<TagLast> importLastTags(String data)
  {
    List<String> lines;
    String[] temp;

    lines = importCSV(data);
    
	List<TagLast> tags = new ArrayList<TagLast>();
    
    for(String l: lines)
    {
    	temp = l.split(",");

    	tags.add(new TagLast(Integer.parseInt(temp[0]), temp[6].replace("\"", ""), temp[6].replace("\"", ""), Integer.parseInt(temp[4]), Integer.parseInt(temp[5]), Double.parseDouble(temp[8]), Integer.parseInt(temp[7]), Integer.parseInt(temp[1]),temp[2].replace("\"", ""), Integer.parseInt(temp[3]),Integer.parseInt(temp[9])));

    }
    return tags;
  }
  
  public List<TagMovie> importMovieTags(String data)
  {
    List<String> lines;
    String[] temp;
    String t;

    lines = importCSV(data);
    
	List<TagMovie> tags = new ArrayList<TagMovie>();
    
    for(String l: lines)
    {
    	t = l.replace(", ", "; ").replace("\"", "");
    	temp = t.split(",");
    	temp[16] = temp[16].replace("; ", ", ");
    	
    	if(!temp[7].equals("\\N"))
    	{
    		try
    		{
    			tags.add(new TagMovie(Integer.parseInt(temp[0]), temp[1], Integer.parseInt(temp[7]), Integer.parseInt(temp[15]), temp[16], temp[16], Integer.parseInt(temp[18]), Integer.parseInt(temp[17]), Integer.parseInt(temp[19]),0d));
    		}
    		catch(NumberFormatException e)
    		{
    			System.out.println(e.getMessage());
    			/*
    			for(String s: temp)
    			{
        			System.out.println(s);
    			}
    			*/
    		}
    	}


    }
    return tags;
  }
  
  public List<TagBook> importBookTags(String data)
  {
    List<String> lines;
    String[] temp;

    lines = importCSV(data);
    
	List<TagBook> tags = new ArrayList<TagBook>();
    
    for(String l: lines)
    {
    	temp = l.split(",");

    	tags.add(new TagBook(Integer.parseInt(temp[0]), temp[1], temp[4], temp[4], Integer.parseInt(temp[5]), Integer.parseInt(temp[6]), Integer.parseInt(temp[7]), 0d));

    }
    return tags;
  }
}