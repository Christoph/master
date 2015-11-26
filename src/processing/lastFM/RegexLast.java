package processing.lastFM;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import core.tags.Tag;
import core.tags.TagLast;
import core.tags.TagsToCSV;
import processing.Regex;

public class RegexLast extends Regex {

  	protected HelperLast help = new HelperLast();
	
	public RegexLast() {
		// TODO Auto-generated constructor stub
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
