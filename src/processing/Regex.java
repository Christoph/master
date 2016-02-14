package processing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import core.Tag;

public class Regex {

	protected String name = "";
	protected String join = "";

	protected String ls = "";
	protected String ms = "";
	protected String rs = "";
	
	protected String e ="";
  	
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
	
    public void findImportantWords(List<Tag> tags, Map<String, String> words, double threshold, int minWordLength, Boolean useAllWords, int index)
	{	  
		int tt3 = 0, tt2 = 0, tt4 = 0;

	    numbers.add("Number of tags: "+tags.size());
		
	    System.out.print(tags.size());
	    int part = tags.size()/30;
	    int iter = 0;
	    
		for(Iterator<Tag> iterator = tags.iterator(); iterator.hasNext();)
        {
			Tag t = iterator.next();
			
	    	  iter++;
	    	  if(iter%part == 0)
	    	  {
	    		  System.out.print("->"+iter);
	    	  }
			
			// Set tag name
			name = t.getTag(index);
			
			if(t.getImportance() < threshold)
			{
				// TT3 Save bad rows
				tt3 += 1;
				
				// Reset join string and out list
		  		join = "";
		  		out.clear();
		  		
		  		// Precompile patterns
		  		for(String s: words.keySet())
		  		{
		  			patterns_greedy.put(s, Pattern.compile("(.*)("+s.toLowerCase()+")(.*)"));
		  			patterns_conservative.put(s, Pattern.compile("(\\s)("+s.toLowerCase()+")(\\s)"));
		  		}
		  		
		  		// Apply regex
		  		matcher(name, words, minWordLength, useAllWords);
		  		
		  		// Rebuild string from out
		  		for(String s: out)
		  		{	
		  			if(s.length() > 0)
		  			{
		  				join = join.concat(" "+s);
		  			} 	  		
		  		}
		  		
		  		join = join.trim();

		  		// Add the extraction to the output
		  		if(!name.equals(join)&&!join.isEmpty()) 
	  			{
	  				separation.add(name+" -> "+join);
	  			}
		  		
		  		// Set tag name
		  		t.setTag(index, join);
			}
			else if(t.getImportance() >= threshold && name.length() >=  minWordLength)
			{		
				// Check if the word is on the subjective list
				if(words.containsKey(name)) // Not on the subjective list
				{
					// Save TT2
					tt2+=1;
				}
				else // On the subjective list
				{
					// Save number of unimportant tags
					tt4+=1;
					
					// Delete tag which is not important and too short.
					t.setTag(index, "");
				}
			}
			else
			{
				// Save number of unimportant tags
				tt4+=1;
				
				// Delete tag which is not important and too short.
				t.setTag(index, "");
			}
		}
		
		numbers.add("Number of important tags: "+tt2);
		numbers.add("Number of tags with an importance < threshold: "+tt3);
		numbers.add("Number of unimportant tags (in subjective list, importance < "+threshold+" and length < "+minWordLength+"): "+tt4);
		
		help.splitCompositeTagLast(tags, index);
		//help.correctTags(tags);
	    
	    numbers.add("Number of final tags: "+tags.size());
	}
	
	public void findGroups(List<Tag> tags, int index)
	{
		Set<String> groups = new HashSet<String>();
		List<String> output = new ArrayList<String>();
	    Map<String, String> subs = new HashMap<String, String>();
		
		String words[] = null;
		String name = "";
		
		//Find all groups
		for(Tag t: tags)
		{
			if(t.getTag(index).contains("-"))
			{
				words = t.getTag(index).split(" ");
				
				for(String s: words)
				{
					if(s.contains("-"))
					{
						groups.add(s);
					}
				}
			}
		}
		
		// Create substitution list
		for(String s: groups)
		{
			subs.put(s.replace("-", ""), s);
		}
		
		//Find groups
		for(Tag t: tags)
		{
			name = t.getTag(index);
			words = name.split(" +");
			
			for(String s: words)
			{
				if(subs.keySet().contains(s))
				{
					name = name.replace(s, subs.get(s));
					output.add(s+"->"+subs.get(s));
				}
			}
			
			t.setTag(index, name);
		}
	}
    
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
	
	public void replaceCustomWords(List<? extends Tag> tags, List<String> patterns, int index)
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
			name = t.getTag(index);
			
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
			
	  		t.setTag(index, name.trim());	
		}
	}
}
