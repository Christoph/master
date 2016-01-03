package processing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import core.tags.Tag;
import core.tags.TagsToCSV;

public class Helper {
	
	   StringLengthComparator slc = new StringLengthComparator();
		PlainStringSimilarity psim = new PlainStringSimilarity();
	  
	  public void removeTagsWithoutWords(List<? extends Tag> tags)
	  {
		    // Remove tags with no words    
		    for(Iterator<? extends Tag> iterator = tags.iterator(); iterator.hasNext();)
		    {
				Tag t = iterator.next();
				  
				if(t.getTagName().length() == 0)
				{
					iterator.remove();
				}
		    }
	  }
	  
	  public void removeRareWords(List<? extends Tag> tags, int minOccu, Boolean verbose)
	  {
		  	Map<String, Integer> occu = new HashMap<String, Integer>();
		  	List<String> output = new ArrayList<String>();
		  	String key = "";
		  	String temp = "";
		  	
		  	int value;
		    List<String> words;
			TagsToCSV writer;
		  	
		  	for(Tag t: tags)
		  	{
		  		words = psim.create_word_gram(t.getTagName());
		  		
		  		for(int j = 0; j < words.size(); j++)
				{
					key = words.get(j); 	
		  			
			  		if(occu.containsKey(key))
					{
						value = occu.get(key);
						
						// Sum up the count
						occu.put(key, value + 1);
					}
					else
					{
						occu.put(key, 1);
					}
				}
		  	}
		  	
		    // Remove tags which occurs less than minOccu times 
		  	for(Tag t: tags)
		    {
		  		words = psim.create_word_gram(t.getTagName());
		  		temp = "";
				
		  		for(int j = 0; j < words.size(); j++)
				{
					key = words.get(j); 	
		  			
					if(occu.get(key) < minOccu)
					{
						// Remove word
						words.set(j, "");
						
						// Add to output list
						output.add(key);
					}
				}
		  		
		  		// Rebuild tags and save them
		  		for(String s: words)
		  		{
		  			if(s.length()>0)
		  			{
		  				temp = temp + s + " ";
		  			}
		  		}
		  		
		  		t.setTagName(temp.trim());
		    }
		  	
			// Write temp files
		    if(verbose) 
	    	{
		    	writer = new TagsToCSV("filtered_words.csv");
		    	writer.writeFilteredWords(output);
	    	}
	  }
	  
	  public void removeRareComposites(List<? extends Tag> tags, int minOccu, Boolean verbose)
	  {
		  	Map<String, Integer> occu = new HashMap<String, Integer>();
		  	List<String> output = new ArrayList<String>();
		  	String key = "";
		  	int value;
			TagsToCSV writer;
		  	
		  	for(Tag t: tags)
		  	{
		  		key = t.getTagName();
	  			
		  		if(occu.containsKey(key))
				{
					value = occu.get(key);
					
					// Sum up the count
					occu.put(key, value + 1);
				}
				else
				{
					occu.put(key, 1);
				}
		  	}
		  	
		    // Remove tags which occurs less than minOccu times 
		  	for(Tag t: tags)
		    {
				key = t.getTagName(); 	
	  			
				if(occu.get(key) < minOccu)
				{
					// Remove tag
					t.setTagName("");
					
					// Add to output list
					output.add(key);
				}
		    }
		  	
		  	//removeTagsWithoutWords(tags);
		  	
			// Write temp files
		    if(verbose) 
	    	{
		    	writer = new TagsToCSV("filtered_composites.csv");
		    	writer.writeFilteredWords(output);
	    	}
	  }
	  
	  public void removeBlacklistedWords(List<? extends Tag> tags, List<String> blacklist)
	  {
		  String name, uptated;    
		  List<String> list = new ArrayList<String>();
		  PlainStringSimilarity psim = new PlainStringSimilarity();
		  
		  for(Tag tag: tags)
		  {
			  name = tag.getTagName();
			  uptated = "";
			  
			  list = psim.create_word_gram(name);
			  
			  list.removeAll(blacklist);
			  
			  for(String s: list)
			  {
				  uptated = uptated + " " + s;
			  }
			  
			  tag.setTagName(uptated.trim());
		  }
		  
		  removeTagsWithoutWords(tags);
	  }
	  
	  public void removeReplaceCharactersAndLowerCase(List<? extends Tag> tags, List<String> remove, List<String> replace)
	  {
		  String updated;    
		  
		  for(Tag tag: tags)
		  {
			  updated = tag.getTagName().toLowerCase();
			  
			  // Remove characters
			  if(remove.size() > 0) 
			  {
				  	updated = updated.replaceAll(remove.toString(), "");
			  }
			  
			  if(replace.size() > 0)
			  {
				  // Replace characters
				  for(String s: replace)
				  {
					  String temp[] = s.split(",");
					  updated = updated.replaceAll(temp[0], temp[1]);
				  }
			  }

			  tag.setTagName(updated);
		  }
	  }
	  
	 
	  public Map<String, String> getImportantTags(List<? extends Tag> tags, double threshold, int minWordLength)
	  {
		  Map<String, Double> important = new HashMap<String, Double>();
		  Map<String, Integer> tagid = new HashMap<String, Integer>();
		  List<String> temp = new ArrayList<String>();
		  Map<String, String> out = new LinkedHashMap<String, String>();
		  
		  for(Tag t: tags)
		  {
			  if(t.getImportance() >= threshold && t.getTagName().length() >= minWordLength)
			  {				  
				  important.put(t.getTagName(),t.getImportance());
				  tagid.put(t.getTagName(), t.getTagID());
				  temp.add(t.getTagName());
			  }
		  }
		  
		  Collections.sort(temp, slc);
		  
		  for(String l: temp)
		  {
			  if(tagid.get(l) != null)
			  {
				  out.put(l.replace(" ", "-"), tagid.get(l).toString()+","+important.get(l).toString());
			  }
			  else
			  {
				  out.put(l, "Additional Tag");
			  }
			  
		  }
		  
		  return out;
	  }
	  
	  public void removeDashes(List<? extends Tag> tags)
	  {
		  String name = "";
		  
		  for(Tag t: tags)
		  {
			  name = t.getTagName();

			  t.setTagName(name.replaceAll("\\s*-\\s*", " "));
		  }
	  }
	  
	  public void addHistoryStep(List<? extends Tag> tags)
	  {
		    // Add a history step
		    for(Tag t: tags)
		    {
		    	t.addHistoryStep(t.getTagName());
		    }
	  }
	  
	  public <T> String objectToJsonString(List<T> list)
	  {
		  	List<String> out = new ArrayList<String>();
		    
			//ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
			ObjectWriter ow = new ObjectMapper().writer();
			
			try {
				
				for(T t: list)
				{
					out.add(ow.writeValueAsString(t));
				}
				
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
			
			return out.toString();
	  }
	  
	  public String objectToJsonString(Map<String, Double> list)
	  {
		  	List<String> out = new ArrayList<String>();
		    
			ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
			//ObjectWriter ow = new ObjectMapper().writer();
			
			try {
				
				for(Entry<String, Double> entry: list.entrySet())
				{
					out.add(ow.writeValueAsString(entry));
				}
				
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
			
			return out.toString();
	  }
	  
	  public void extractCorrectGroupsAndWords(List<? extends Tag> tags, String marker, List<String> groups, List<String> words)
	  {
		  List<String> temp;
		  String shingle[];
		  String word;
		  TagsToCSV writer;
		  
		  for(Tag t: tags)
		  {
			  temp = psim.create_word_gram(t.getTagName());
			  
			  for(String s: temp)
			  {
				  if(s.contains(marker))
				  {
					  word = s.replace(marker, " ");
					  
					  if(!groups.contains(word)) groups.add(word);
					  
					  shingle = word.split(" ");
					  
					  for(String w: shingle)
					  {
						  if(!words.contains(w)) words.add(w);
					  }
				  }
			  }
		  }
		  
		  writer = new TagsToCSV("good_groups.csv");
		  writer.writeStringList(groups, "Groups");
		  
		  writer = new TagsToCSV("good_words.csv");
		  writer.writeStringList(words, "Words");
	  }
}
