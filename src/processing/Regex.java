package processing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import core.tags.Tag;
import core.tags.TagsToCSV;

public class Regex {

	protected String name = "";
	protected String join = "";

	protected String ls = "";
	protected String ms = "";
	protected String rs = "";
	
	protected String e ="";
  	
	protected TagsToCSV writer;
  	protected List<String> separation = new ArrayList<String>();
  	protected List<String> numbers = new ArrayList<String>();
  	protected List<String> replacements;
  	protected List<String> out = new ArrayList<String>();
  	protected Helper help = new Helper();
  	
  	// Debug output
  	protected Boolean print_groups = true;
  	
    // Create a Pattern object
  	protected Pattern r, l;
    
    // Pattern dict
  	protected Map<String, Pattern> patterns_greedy = new HashMap<String, Pattern>();
  	protected Map<String, Pattern> patterns_conservative = new HashMap<String, Pattern>();

    // Now create matcher object.
  	protected Matcher mr, ml;
  	
  	// Parameters
  	
    // Set importance threshold
    private double threshold = 0.1;
    
    // Set minimum word length
    private int minWordLength = 3;
	
    // String, > 0 == right, list of important tags
	public void matcher(String name, Map<String, String> list, int minWordLength, Boolean useAllWords)
	{
		name = " "+name+" ";
		
			for(Map.Entry<String, String> entry : list.entrySet())
			{
			// Fetch data	
			e = entry.getKey();
				
  			// Compile patterns
			// If bigger than min word length do substring search
			// else only full word search
			if(e.length() > minWordLength)
			{
				l = patterns_greedy.get(e);  
				r = patterns_greedy.get(e);  
			}
			else
			{
				l = patterns_conservative.get(e);  
				r = patterns_conservative.get(e); 
			}

	  		// Find matches
	  		mr = r.matcher(name);
	  		ml = l.matcher(name);
	  		
	  		// Check if a match happened
	  		if(mr.find())
	  		{
	  			String ls = mr.group(1).trim();
	  			String ms = mr.group(2).trim();
	  			String rs = mr.group(3).trim();
	  			
	  			if(ls.length() > 0) matcher(ls, list,minWordLength, useAllWords);
	  			
	  			out.add(ms.trim());
	  			
	  			if(rs.length() > 0) matcher(rs, list, minWordLength, useAllWords);
	  			
	  			name = "";
	  		} 			  		
	  		else if(ml.find())
	  		{
	  			String ls = ml.group(1).trim();
	  			String ms = ml.group(2).trim();
	  			String rs = ml.group(3).trim();
	  			
	  			if(ls.length() > 0) matcher(ls, list, minWordLength, useAllWords);
	  			
	  			out.add(ms);
	  			
	  			if(rs.length() > 0) matcher(rs, list, minWordLength, useAllWords);
	  			
	  			name = "";
	  		}
  		}
			
		//Removing this line let the matcher only use words from the list of important words
		//All other words will be removed
		if(useAllWords == true) out.add(name.trim());

	}
	
	public void replaceCustomWords(List<? extends Tag> tags, List<String> patterns, String prefix)
	{
		String name, reg, rep, temp;
		String[] row;
		replacements = new ArrayList<String>();
		Matcher match;
		
		Map<Pattern, String> custom = new HashMap<Pattern, String>();
		
		// Precompile patterns
		for(String p: patterns)
		{
			row = p.split(",");
			reg = "\\b"+row[0]+"\\b";
			rep = row[1].trim();
			
			custom.put(Pattern.compile(reg), rep);
		}
		
		for(Tag t: tags)
		{
			name = t.getTagName();
			
			for(Map.Entry<Pattern, String> entry : custom.entrySet())
			{
				match = entry.getKey().matcher(name);
				
				if(match.find() && !entry.getValue().equals(name))
				{
					temp = match.replaceAll(entry.getValue());
			  		
			  		if(!name.equals(temp)) 
		  			{
		  				replacements.add(name+" -> "+temp);
		  			}
			  		
			  		name = temp;
				}
			}
			
	  		t.setTagName(name.trim());	
		}
		
		// Write temp files
	    if(print_groups)
    	{	
	    	Collections.sort(replacements);

	    	writer = new TagsToCSV("replacements_"+prefix+".csv");
	    	writer.writeLines(replacements,"replacements");
    	}
	    
	}
}
