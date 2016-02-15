package processing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import core.Tag;
import core.json.gridHist;
import core.json.gridVocab;

public class Helper {
	
	   StringLengthComparator slc = new StringLengthComparator();
	   PlainStringSimilarity psim = new PlainStringSimilarity();
	  
	   public void wordFrequency(List<? extends Tag> tags, Map<String, Long> tagsFrequency, int index)
		{
		    String key;
		    long value;
		    long max_value = 0;
		    List<String> words;
		    
		    tagsFrequency.clear();
			
		    // Summing up the occurrences
		    for(Tag t: tags)
		    {	    	    		
				key = t.getTag(index);
		  		words = psim.create_word_gram(key);
		  		
		  		for(String s : words)
				{
					if(tagsFrequency.containsKey(s))
					{
						value = tagsFrequency.get(s);
						
						// Sum up the weight over all songs
						tagsFrequency.put(s, value + 1);
						
		    			// Find max
		    			if(value + 1 > max_value)
		    			{
		    				max_value = value + 1;
		    			}
					}
					else
					{
						tagsFrequency.put(s, (long) 1);
					}
				}
		    }
		}
	   
	   public void correctTags(List<Tag> tags, int index)
		  {
			  	// "TagName": Weight
			  	Map<String, Double> song_name = new HashMap<String, Double>();
			  	
		    	Set<String> used = new HashSet<String>();
			  	
			    double weight;
			    String key;

			    // Find maximum Weight per song/tag pair
			    for(Tag t: tags)
			    {
					key = t.getTag(index);
					weight = t.getWeight();

					if(song_name.containsKey(key))
					{
						if(weight > song_name.get(key))
						{
							song_name.put(key, weight);
						}
					}
					else
					{
						song_name.put(key, weight);
					}
			    }
			    
			    // Resolve multiple equal tags per song
			    for(Tag t: tags)
			    {
					key = t.getTag(index);
					weight = t.getWeight();

					if(song_name.containsKey(key))
					{
						if(weight < song_name.get(key))
						{
							// This marks the tag object as removable
							t.setTag(index, "");
						}
						
						if(weight == song_name.get(key) && used.contains(key))
						{
							// This marks the tag object as removable
							t.setTag(index, "");
						}
						else if(weight == song_name.get(key))
						{
							used.add(key);
						}
					}
			    }
				
				removeTagsWithoutWords(tags, index);
		  }
	   
	   public void splitCompositeTag(List<Tag> tags, int index)
		  {
			  	String tag = "";
			  	String name[] = null;
				List<Tag> tt = new ArrayList<Tag>();
				List<String> temp;
			  
				  for(Tag t: tags)
				  {
					  tag = t.getTag(index);
					  
					  if(tag.contains(" "))
					  {
						  name = tag.split(" ");
						  
						  // Replace current name by the first word
						  t.setTag(index, name[0]);
						  
						  // Create for all other words new entries
						  for(int i = 1; i<name.length;i++)
						  {
							  temp = t.getTag();
							  temp.set(index, name[i]);
							  
							  tt.add(new Tag(t.getItem(), temp, t.getWeight(), t.getImportance())); 	
						  }
					  }
				  }
			  
				  if(tt.size() > 0)
				  {
					  	// Add all new entries
					  	tags.addAll(tt);
					  
					  	// Fix IDs and so on
					  	//correctTags(tags);
				  }
		  }
	   
	  public void removeTagsWithoutWords(List<Tag> tags, int index)
	  {
		    // Remove tags with no words    
		    for(Iterator<? extends Tag> iterator = tags.iterator(); iterator.hasNext();)
		    {
				Tag t = iterator.next();
				  
				if(t.getTag(index).length() == 0)
				{
					iterator.remove();
				}
		    }
	  }
	  
	  /*
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
	  */
	  
	  public void removeBlacklistedWords(List<Tag> tags, List<String> blacklist, int index)
	  {
		  String name, uptated;    
		  List<String> list = new ArrayList<String>();
		  PlainStringSimilarity psim = new PlainStringSimilarity();
		  
		  for(Tag tag: tags)
		  {
			  name = tag.getTag(index);
			  uptated = "";
			  
			  list = psim.create_word_gram(name);
			  
			  list.removeAll(blacklist);
			  
			  for(String s: list)
			  {
				  uptated = uptated + " " + s;
			  }
			  
			  tag.setTag(index, uptated.trim());
		  }
		  
		  removeTagsWithoutWords(tags, index);
	  }
	  
	  public List<String> getImportantTags(Map<String, Double> vocabPost, double threshold)
	  {
		  List<String> temp = new ArrayList<String>();
		  
		  for(Entry<String,Double> e: vocabPost.entrySet())
		  {
			  if(e.getValue() >= threshold)
			  {
				  temp.add(e.getKey());
			  }
		  }
		  
		  return temp;
	  }
	  
	  public void removeDashes(List<Tag> tags, int index)
	  {
		  String name = "";
		  
		  for(Tag t: tags)
		  {
			  name = t.getTag(index);

			  t.setTag(index, name.replaceAll("\\s*-\\s*", " "));
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
	  
	  // Converts JSON string to an object map
	  public List<Map<String, Object>> jsonStringToList(String json)
	  {
			ObjectMapper mapper = new ObjectMapper();

			List<Map<String, Object>> map = new ArrayList<Map<String, Object>>();
		  
			try {
				// convert JSON string to Map
				map = mapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {});
				
			} catch (JsonParseException e) {
				e.printStackTrace();
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return map;
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
	  
	  /*
	  public void extractCorrectGroupsAndWords(List<? extends Tag> tags, String marker, List<String> groups, List<String> words, int index)
	  {
		  List<String> temp;
		  String shingle[];
		  String word;
		  
		  for(Tag t: tags)
		  {
			  temp = psim.create_word_gram(t.getTag(index));
			  
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
	  }
	  */
	  
		public static Map<String, Double> sortByComparatorDouble(Map<String, Double> unsorted) {

			// Variables
			Map<String, Double> sortedMap = new LinkedHashMap<String, Double>();
			
			// Convert map to list
			List<Map.Entry<String, Double>> list = 
				new LinkedList<Map.Entry<String, Double>>(unsorted.entrySet());

			// Sort list with comparator
			// Sort in decreasing order
			Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
				public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
					return (o2.getValue()).compareTo(o1.getValue());
				}
			});

			// Convert sorted map back to a map
			for (Iterator<Map.Entry<String, Double>> it = list.iterator(); it.hasNext();) {
				Map.Entry<String, Double> entry = it.next();
				sortedMap.put(entry.getKey(), entry.getValue());
			}
			
			return sortedMap;
		}
		
		public static Map<String, Integer> sortByComparatorInteger(Map<String, Integer> unsorted) {

			// Variables
			Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
			
			// Convert map to list
			List<Map.Entry<String, Integer>> list = 
				new LinkedList<Map.Entry<String, Integer>>(unsorted.entrySet());

			// Sort list with comparator
			// Sort in decreasing order
			Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
				public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
					return (o2.getValue()).compareTo(o1.getValue());
				}
			});

			// Convert sorted map back to a map
			for (Iterator<Map.Entry<String, Integer>> it = list.iterator(); it.hasNext();) {
				Map.Entry<String, Integer> entry = it.next();
				sortedMap.put(entry.getKey(), entry.getValue());
			}
			
			return sortedMap;
		}

		public void provideTagsForNextStep(List<Tag> tags, int index) {
			for(Tag t: tags)
			{
				t.setTag(index+1, t.getTag(index));
			}
		}
		
		public void resetStep(List<Tag> tags, int index) {
			for(Tag t: tags)
			{
				t.setTag(index, t.getTag(index-1));
			}
		}
		
		public List<gridHist> prepareVocabHistogram(Map<String, Double> vocab)
		{
		    List<gridHist> hist = new ArrayList<gridHist>();
		    Map<Double, Long> temp = new HashMap<Double, Long>();
		    
		    for(Entry<String, Double> c: vocab.entrySet())
		    {
	    		if(temp.containsKey(c.getValue()))
	    		{
	    			temp.put(c.getValue(), temp.get(c.getValue()) + 1);
	    		}
	    		else
	    		{
	    			temp.put(c.getValue(), (long) 1);
	    		}

		    }
		    
		    for(double d: temp.keySet())
		    {
		    	hist.add(new gridHist(d, temp.get(d)));
		    }

		    return hist;
		}
		
		public List<gridVocab> prepareVocab(Map<String, Double> vocab)
		{
		    List<gridVocab> tags_filtered = new ArrayList<gridVocab>();
		    
		    for(String s: vocab.keySet())
		    {
		    	tags_filtered.add(new gridVocab(s, vocab.get(s)));
		    }

		    return tags_filtered;
		}
}
