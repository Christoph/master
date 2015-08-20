package processing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tags.Tag;
import tags.TagsToCSV;

public class Regex {

	String name = "";
  	String join = "";

	String ls = "";
	String ms = "";
	String rs = "";
  	
  	TagsToCSV writer;
  	List<String> separation = new ArrayList<String>();
  	List<String> numbers = new ArrayList<String>();
  	public List<String> out = new ArrayList<String>();
  	Helper help = new Helper();
  	
  	// Debug output
  	Boolean print_groups = true;
  	
    // Create a Pattern object
    Pattern r, l;

    // Now create matcher object.
    Matcher mr, ml;
	
    // String, > 0 == right, list of important tags
	public void matcher(String name, Map<String, String> list)
	{
			for(Map.Entry<String, String> entry : list.entrySet())
			{
  			// Compile patterns
			l = Pattern.compile("(.*)("+entry.getKey().toLowerCase()+")(.*)");  
			r = Pattern.compile("(.*)("+entry.getKey().toLowerCase()+")(.*)");  
			
	  		// Find matches
	  		mr = r.matcher(name);
	  		ml = l.matcher(name);
	  		
	  		// Check if a match happened
	  		if(mr.find())
	  		{
	  			String ls = mr.group(1).trim();
	  			String ms = mr.group(2).trim();
	  			String rs = mr.group(3).trim();
	  			
	  			if(ls.length() > 0) matcher(ls, list);
	  			
	  			out.add(ms);
	  			
	  			if(rs.length() > 0) matcher(rs, list);
	  			
	  			name = "";
	  		} 			  		
	  		else if(ml.find())
	  		{
	  			String ls = ml.group(1).trim();
	  			String ms = ml.group(2).trim();
	  			String rs = ml.group(3).trim();
	  			
	  			if(ls.length() > 0) matcher(ls, list);
	  			
	  			out.add(ms);
	  			
	  			if(rs.length() > 0) matcher(rs, list);
	  			
	  			name = "";
	  		}
  		}
			

		//out.add(name.replace("-", " ").trim());

	}
	
	public void replaceCustomWords(List<Tag> tags, List<String> patterns)
	{
		String name, reg, rep, temp;
		String[] row;
		
	    // Create a Pattern object
	    Pattern pattern;

	    // Now create matcher object.
	    Matcher match;
		
		for(Tag t: tags)
		{
			name = t.getTagName();
			
			for(String p: patterns)
			{
				row = p.split(",");
				reg = row[0];
				rep = row[1];
		 
		  		temp = name.replaceAll(reg, rep);
		  		System.out.println(name+" -> "+temp);
		  		
		  		t.setTagName(temp);
			}
		}
	}
	
	public void findImportantWords(List<Tag> tags, Map<String, String> words, double threshold, int minWordLength)
	{	  
		List<Tag> tt = new ArrayList<Tag>();
		int tt3 = 0, tt2 = 0, tt4 = 0;
		
	    //	Debug stuff
	    int psize = tags.size();
	    int part = psize/50;
	    int iter = 0;
	    
	    System.out.println("size of tags: "+psize);
		
		for(Iterator<Tag> iterator = tags.iterator(); iterator.hasNext();)
        {
			Tag t = iterator.next();
			
			iter++;
	    	  if(iter%part == 0)
	    	  {
	    		  System.out.print("->"+iter);
	    	  }
			
			// Set tag name
			name = t.getTagName();
			
			if(t.getImportance() < threshold)
			{
				// TT3 Save bad rows
				tt3 += 1;
				
				// Reset join string and out list
		  		join = "";
		  		out.clear();
		  		
		  		// Apply regex
		  		matcher(name, words);
		  					
		  		// Rebuild string from out
		  		for(String s: out)
		  		{	
		  			if(s.length() > 0)
		  			{
		  				join = join.concat(" "+s);
		  			} 
		  			
		  			String tagid = words.get(s).split(",")[0];
		  			String importance = words.get(s).split(",")[1];
		  			
		  			tt.add(new Tag(1, s.replace(" ", "-"), t.getPlaycount(), Integer.parseInt(tagid), Double.parseDouble(importance), t.getLastFMWeight(), t.getSongID(), t.getSongName(), t.getListeners(),t.getArtistID())); 	
		  		
		  		}
		  		
		  		join = join.trim();

		  		if(!name.equals(join)&&!join.isEmpty()) separation.add(name+" -> "+join);
		  		
		  		iterator.remove();
			}
			else if(t.getImportance() >= threshold && name.length() >=  minWordLength)// Fix important tags
			{	
				// Save TT2
				tt2+=1;
				
				// Reset join string and out list
		  		join = name.replace(" ", "-");
		  	
		  		t.setTagName(join);
			}
			else
			{
				// Save number of unimportant tags
				tt4+=1;
				// Delete tag which is not important and too short.
				t.setTagName("");
			}
		}
		
		tags.addAll(tt);
		
		System.out.println("\nTT2: "+tt2);
		numbers.add("");
		System.out.println("TT3: "+tt3);
		numbers.add("");
		System.out.println("TT4: "+tt4);
		
	    help.removeTagsWithoutWords(tags);
	    
	    System.out.println("TT5: "+tags.size());
		
		// Write temp files
	    if(print_groups) 
    	{	
	    	Collections.sort(separation);
	    	
	    	writer = new TagsToCSV("word_separation.csv");
	    	writer.writeSeparation(separation);
	    	
	    	writer = new TagsToCSV("numbers.csv");
	    	writer.writeSeparation(numbers);
    	}
	}
}
