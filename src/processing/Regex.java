package processing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tags.Tag;
import tags.TagsToCSV;

public class Regex {

	public void separateWords(List<Tag> tags, Set<String> list)
	{
	  	String name = "";
	  	String join = "";
	  	TagsToCSV writer;
	  	List<String> separation = new ArrayList<String>();
	  	
	  	// Debug output
	  	Boolean print_groups = true;
	  	
	    // Create a Pattern object
	    Pattern r;

	    // Now create matcher object.
	    Matcher m;
		
		for(Tag t: tags)
		{
			// Set tag name
			name = t.getTagName();

			// Reset join string
	  		join = "";
	  		
	  		for(String s: list)
	  		{
	  			r = Pattern.compile("(.*\\w+)("+s.toLowerCase()+")(.*)|(.*)("+s.toLowerCase()+")(\\w+.*)");  
	  			
		  		// Find matches
		  		m = r.matcher(name);
		  		
		  		if(m.find())
		  		{	
		  			for(int i = 1;i<7;i++)
		  			{
		  				if(m.group(i)!=null)
		  				{
		  	  				join = join + " " + m.group(i);	
		  				}
		  			}
		  			
		  			
		  			separation.add(s+":"+name+"->"+join);
		  			
					//System.out.println("");
					//System.out.println("Song:"+t.getSongName());
					//System.out.println("Old:"+name);
		  			//System.out.println("Match:"+s);
		  			//System.out.println("New:"+join);

		  			name = join;
		  			
		  			join = "";
		  			m.reset();
		  		}
	  		}
	  		
	  		t.setTagName(name);
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
