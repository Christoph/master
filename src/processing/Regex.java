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
import core.TagLast;
import core.TagsToCSV;

public class Regex {

	String name = "";
  	String join = "";

	String ls = "";
	String ms = "";
	String rs = "";
	
	String e ="";
  	
  	TagsToCSV writer;
  	List<String> separation = new ArrayList<String>();
  	List<String> numbers = new ArrayList<String>();
  	List<String> replacements;
  	public List<String> out = new ArrayList<String>();
  	Helper help = new Helper();
  	
  	// Debug output
  	Boolean print_groups = true;
  	
    // Create a Pattern object
    Pattern r, l;
    
    // Pattern dict
    Map<String, Pattern> patterns_greedy = new HashMap<String, Pattern>();
    Map<String, Pattern> patterns_conservative = new HashMap<String, Pattern>();

    // Now create matcher object.
    Matcher mr, ml;
	
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
				//l = Pattern.compile("(.*)("+e+")(.*)");  
				//r = Pattern.compile("(.*)("+e+")(.*)");  
				l = patterns_greedy.get(e);  
				r = patterns_greedy.get(e);  
			}
			else
			{
				//l = Pattern.compile("(\\s)("+e+")(\\s)");  
				//r = Pattern.compile("(\\s)("+e+")(\\s)"); 
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
				
				temp = match.replaceAll(entry.getValue());
		  		
		  		if(!name.equals(temp)) 
	  			{
	  				replacements.add(name+" -> "+temp);
	  			}
		  		
		  		name = temp;
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
	
	public void findImportantWords(List<TagLast> tags, Map<String, String> words, double threshold, int minWordLength, Boolean useAllWords)
	{	  
		int tt3 = 0, tt2 = 0, tt4 = 0;

	    numbers.add("Number of tags: "+tags.size());
		
	    System.out.print(tags.size());
	    int part = tags.size()/30;
	    int iter = 0;
	    
		for(Iterator<TagLast> iterator = tags.iterator(); iterator.hasNext();)
        {
			TagLast t = iterator.next();
			
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
		  		t.setTagName(join);
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
					t.setTagName("");
				}
			}
			else
			{
				// Save number of unimportant tags
				tt4+=1;
				
				// Delete tag which is not important and too short.
				t.setTagName("");
			}
		}
		
		numbers.add("Number of important tags: "+tt2);
		numbers.add("Number of tags with an importance < threshold: "+tt3);
		numbers.add("Number of unimportant tags (in subjective list, importance < "+threshold+" and length < "+minWordLength+"): "+tt4);
		
		help.splitCompositeTagLast(tags);
		help.correctTagsAndIDs(tags);
	    
	    numbers.add("Number of final tags: "+tags.size());
		
		// Write temp files
	    if(print_groups) 
    	{	
	    	Collections.sort(separation);
	    	
	    	writer = new TagsToCSV("word_separation.csv");
	    	writer.writeLines(separation,"separations");
	    	
	    	writer = new TagsToCSV("numbers.csv");
	    	writer.writeLines(numbers,"Stats");
    	}
	}
	
	public void findGroups(List<TagLast> tags, Boolean verbose)
	{
		Set<String> groups = new HashSet<String>();
		List<String> output = new ArrayList<String>();
	    Map<String, String> subs = new HashMap<String, String>();
	    
	    TagsToCSV writer;
		
		String words[] = null;
		String name = "";
		
		//Find all groups
		for(Tag t: tags)
		{
			if(t.getTagName().contains("-"))
			{
				words = t.getTagName().split(" ");
				
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
			name = t.getTagName();
			words = name.split(" +");
			
			for(String s: words)
			{
				if(subs.keySet().contains(s))
				{
					name = name.replace(s, subs.get(s));
					output.add(s+"->"+subs.get(s));
				}
			}
			
			t.setTagName(name);
		}
		
	    if(verbose) 
    	{
	    	writer = new TagsToCSV("groups_found.csv");
	    	writer.writeFound(output);
    	}
	}
}
