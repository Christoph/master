package processing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
  	public List<String> out = new ArrayList<String>();
  	
  	// Debug output
  	Boolean print_groups = true;
  	
    // Create a Pattern object
    Pattern r, l;

    // Now create matcher object.
    Matcher mr, ml;
	
    // String, > 0 == right, list of important tags
	public void matcher(String name, List<String> list)
	{
			for(String s: list)
			{
  			// Compile patterns
			l = Pattern.compile("(.*)("+s.toLowerCase()+")(.*)");  
			r = Pattern.compile("(.*)("+s.toLowerCase()+")(.*)");  
			
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
	  			
	  			if(ms.equals("mexico-lindo y querido"))
	  			{
	  				ms = "mexico-lindo y querido";
	  			}
	  			
	  			out.add(ms.replace(" ", "-"));
	  			
	  			if(rs.length() > 0) matcher(rs, list);
	  			
	  			name = "";
	  		} 			  		
	  		else if(ml.find())
	  		{
	  			String ls = ml.group(1).trim();
	  			String ms = ml.group(2).trim();
	  			String rs = ml.group(3).trim();
	  			
	  			if(ls.length() > 0) matcher(ls, list);

	  			if(ms.equals("mexico-lindo y querido"))
	  			{
	  				ms = "mexico-lindo y querido";
	  			}
	  			
	  			out.add(ms.replace(" ", "-"));
	  			
	  			if(rs.length() > 0) matcher(rs, list);
	  			
	  			name = "";
	  		}
  		}
			

		//out.add(name.replace("-", " ").trim());

	}
	
	public void separateWords(List<Tag> tags, List<String> list)
	{
		for(Tag t: tags)
		{
			// Set tag name
			name = t.getTagName();
			
			// Reset join string and out list
	  		join = "";
	  		out.clear();
	  		
	  		// Apply regex
	  		matcher(name, list);
	  		
	  		// Rebuild string from out
	  		for(String s: out)
	  		{
	  			if(s.length() > 0)
	  			{
	  				join = join.concat(" "+s);
	  			}
	  		}
	  		
	  		join = join.trim();
	  		name = name.trim();
	  		
	  		t.setTagName(join);
	  		
	  		if(!name.equals(join)) separation.add(name+" -> "+join);
		}
		
		// Write temp files
	    if(print_groups) 
    	{	
	    	Collections.sort(separation);
	    	
	    	writer = new TagsToCSV("word_separation.csv");
	    	writer.writeSeparation(separation);
    	}
	}
	
	public void findImportantWords(List<Tag> tags, List<String> words, double threshold)
	{	  
		for(Tag t: tags)
		{
			if(t.getImportance() <= threshold)
			{
				// Set tag name
				name = t.getTagName();
				
				// Reset join string and out list
		  		join = "";
		  		out.clear();
		  		
		  		if(name.equals("acoustic-rhythm-guitars"))
		  		{
		  			name = "acoustic-rhythm-guitars";
		  		}
		  		
		  		// Apply regex
		  		matcher(name, words);
		  					
		  		// Rebuild string from out
		  		for(String s: out)
		  		{
		  			if(s.length() > 0)
		  			{
		  				join = join.concat(" "+s);
		  			}
		  		}
		  		
		  		join = join.trim();
		  		
		  		t.setTagName(join);
		  		
		  		if(!name.equals(join)&&!join.isEmpty()) separation.add(name+" -> "+join);
			}
		}
		
		// Write temp files
	    if(print_groups) 
    	{	
	    	Collections.sort(separation);
	    	
	    	writer = new TagsToCSV("word_separation.csv");
	    	writer.writeSeparation(separation);
    	}
	}
}
