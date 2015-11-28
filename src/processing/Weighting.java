package processing;

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
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.stream.Collectors;

import core.tags.Tag;
import core.tags.TagsToCSV;

public class Weighting {

    private PlainStringSimilarity psim = new PlainStringSimilarity();
	
	public void byWeightedMeanOfImportance(List<Tag> tags, String prefix, Boolean verbose) {
	    /////////////////////////////////
	    // Variables
	    Map<String, Double> tag_words = new HashMap<String, Double>();
	    
	  	TagsToCSV writer;
	  	List<String> weights = new ArrayList<String>();
	  	Set<String> temp = new HashSet<String>();
	    
	
	    String key;
	    double importance, max_w = 0;
	    double value;
	    Boolean print_groups;
	    
		/////////////////////////////////
		// Configuration
		
		// Verbose
		print_groups = verbose;	
	
		/////////////////////////////////
		// Algorithm	
	    
	    // Summing up the weights
	    for(int i = 0;i < tags.size(); i++)
	    {	    	    		
			importance = tags.get(i).getImportance();
			key = tags.get(i).getTagName();
	
			if(tag_words.containsKey(key))
			{
				value = tag_words.get(key);
				
				// Sum up the weight over all songs
				tag_words.put(key, value + importance);
			}
			else
			{
				tag_words.put(key, importance);
			}
	    }
	    
	    // maximum importance of tag_words
	    for(String s: tag_words.keySet())
	    {
	    	if(tag_words.get(s) > max_w)
	    	{
	    		max_w = tag_words.get(s);
	    	}
	    }
	    
	    // normalizing tags
	    for(Tag t:tags)
	    {
	    	importance = tag_words.get(t.getTagName());
	    	
	    	t.setImportance(importance/max_w);
	    	
	    	weights.add(t.getTagName()+","+importance/max_w);
	    }
	    
	    // Write temp files
	    if(print_groups) 
		{	
	    	// Remove duplicates and sort
	    	temp.addAll(weights);
	    	
	    	weights.clear();
	    	
	    	weights.addAll(temp);
	    	Collections.sort(weights);
	    	
	    	writer = new TagsToCSV("importance_"+prefix+".csv");
	    	writer.writeLines(weights,"importance");
		}
		
	
	    tag_words = null;
	}

	public void tagsByFrequency(List<? extends Tag> tags)
	{
	    String key;
	    int value;
	    int max_occ = 0;
	    Map<String, Integer> counts = new HashMap<String, Integer>();
	    
		
	    // Summing up the occurrences
	    for(Tag t: tags)
	    {	    	    		
			key = t.getTagName();
	
			if(counts.containsKey(key))
			{
				value = counts.get(key);
				
				// Sum up the weight over all songs
				counts.put(key, value + 1);
				
    			// Find max
    			if(value + 1 > max_occ)
    			{
    				max_occ = value + 1;
    			}
			}
			else
			{
				counts.put(key, 1);
			}
	    }
	    
	    // Normalizing frequency and setting it as importance
	    for(Tag t: tags)
	    {
			key = t.getTagName();
	    	
	    	t.setImportance(counts.get(key)/max_occ);
	    }
	}
	

	
	public void vocabByFrequency(List<? extends Tag> tags, Map<String, Double> vocab, String prefix, Boolean verbose)
	{
	    String key;
	    List<String> words;
	    int counter;
	    double value;
	    double max_occ = 0d;
	  	TagsToCSV writer;
	    Map<String, Integer> counts = new HashMap<String, Integer>();
		
	    // Summing up the occurrences
	    for(Tag t: tags)
	    {	    	    		
			key = t.getTagName();
	    	words = psim.create_word_gram(key);
	
	    	for(String s : words)
	    	{
	    		if(counts.containsKey(s))
				{
					counter = counts.get(s);
					
					// Sum up the weight over all songs
					counts.put(s, counter + 1);
					
	    			// Find max
	    			if(counter + 1 > max_occ)
	    			{
	    				max_occ = counter + 1;
	    			}
				}
				else
				{
					counts.put(s, 1);
				}
	    	}
	    }
	    
	    // Normalizing frequency and setting it as importance in vocab
	    for(String s: counts.keySet())
	    {
			key = s;
			value = counts.get(s)/max_occ;
	    	
	    	vocab.put(key, value);
	    }
	    
	    // Write temp files
	    if(verbose) 
		{	
	    	writer = new TagsToCSV("vocab_"+prefix+".csv");
	    	writer.writeVocab(sortByComparator(vocab));
		}
	}
	
	private static Map<String, Double> sortByComparator(Map<String, Double> unsortMap) {

		// Convert Map to List
		List<Map.Entry<String, Double>> list = 
			new LinkedList<Map.Entry<String, Double>>(unsortMap.entrySet());

		// Sort list with comparator, to compare the Map values
		Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
			public int compare(Map.Entry<String, Double> o1,
                                           Map.Entry<String, Double> o2) {
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});

		// Convert sorted map back to a Map
		Map<String, Double> sortedMap = new LinkedHashMap<String, Double>();
		for (Iterator<Map.Entry<String, Double>> it = list.iterator(); it.hasNext();) {
			Map.Entry<String, Double> entry = it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}
}