package processing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import core.Tag;
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

    // Now create matcher object.
    Matcher mr, ml;
	
    // String, > 0 == right, list of important tags
	public void matcher(String name, Map<String, String> list, int minWordLength)
	{
		name = " "+name+" ";
		
			for(Map.Entry<String, String> entry : list.entrySet())
			{
			// Fetch data	
			e = entry.getKey().toLowerCase();
				
  			// Compile patterns
			// If bigger than min word length do substring search
			// else only full word search
			if(e.length() > minWordLength)
			{
				l = Pattern.compile("(.*)("+e+")(.*)");  
				r = Pattern.compile("(.*)("+e+")(.*)");  
			}
			else
			{
				l = Pattern.compile("(\\s)("+e+")(\\s)");  
				r = Pattern.compile("(\\s)("+e+")(\\s)"); 
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
	  			
	  			if(ls.length() > 0) matcher(ls, list,minWordLength);
	  			
	  			out.add(ms.trim());
	  			
	  			if(rs.length() > 0) matcher(rs, list, minWordLength);
	  			
	  			name = "";
	  		} 			  		
	  		else if(ml.find())
	  		{
	  			String ls = ml.group(1).trim();
	  			String ms = ml.group(2).trim();
	  			String rs = ml.group(3).trim();
	  			
	  			if(ls.length() > 0) matcher(ls, list, minWordLength);
	  			
	  			out.add(ms);
	  			
	  			if(rs.length() > 0) matcher(rs, list, minWordLength);
	  			
	  			name = "";
	  		}
  		}
			

		//Removing this line let the matcher only use words from the list of important words
		//All other words will be removed
		//out.add(name.replace("-", " ").trim());

	}
	
	public void replaceCustomWords(List<Tag> tags, List<String> patterns, String prefix)
	{
		String name, reg, rep, temp;
		String[] row;
		replacements = new ArrayList<String>();
		
		for(Tag t: tags)
		{
			name = t.getTagName();
			
			for(String p: patterns)
			{
				row = p.split(",");
				reg = "\\b"+row[0]+"\\b";
				rep = row[1].trim();
		 
		  		temp = name.replaceAll(reg, rep);
		  		
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
	
	public void findImportantWords(List<Tag> tags, Map<String, String> words, double threshold, int minWordLength)
	{	  
		List<Tag> tt = new ArrayList<Tag>();
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
			name = t.getTagName();
			
			if(t.getImportance() < threshold)
			{
				// TT3 Save bad rows
				tt3 += 1;
				
				// Reset join string and out list
		  		join = "";
		  		out.clear();
		  		
		  		// Apply regex
		  		matcher(name, words, minWordLength);
		  		
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
				// Check if the word is on the subjective list
				if(words.containsKey(name))
				{
					// Save TT2
					tt2+=1;
					
					// Reset join string and out list
			  		join = name.replace(" ", "-");
			  	
			  		t.setTagName(join);
				}
				else
				{
					tt4+=1;
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
		
		tags.addAll(tt);
		
		numbers.add("Number of important tags: "+tt2);
		numbers.add("Number of tags with an importance < threshold: "+tt3);
		numbers.add("Number of unimportant tags (in subjective list, importance >= "+threshold+" and length >= "+minWordLength+"): "+tt4);
		
	    help.correctTagsAndIDs(tags);
	    help.removeTagsWithoutWords(tags);
	    
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
}
