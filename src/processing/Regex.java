package processing;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tags.Tag;

public class Regex {

	public void separateWords(List<Tag> tags, List<String> list)
	{
	  	String name = "";
	  	String join = "";
	  	
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
			  		if(s.equals("metal"))
			  		{
			  			s = "metal";
			  		}
		  			
		  			for(int i = 1;i<7;i++)
		  			{
		  				if(m.group(i)!=null)
		  				{
		  	  				join = join + " " + m.group(i);	
		  				}
		  			}
		  			
					System.out.println("");
					System.out.println("Song:"+t.getSongName());
					System.out.println("Old:"+name);
		  			System.out.println("Match:"+s);
		  			System.out.println("New:"+join);

		  			name = join;
		  			
		  			join = "";
		  			m.reset();
		  		}
	  		}
	  		
	  		t.setTagName(name);
		}
	}
}
