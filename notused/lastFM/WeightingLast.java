package processing.lastFM;

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

import processing.PlainStringSimilarity;
import core.tags.Tag;
import core.tags.TagLast;
import core.tags.TagsToCSV;

public class WeightingLast {

    private PlainStringSimilarity psim = new PlainStringSimilarity();
	
    public void importanceFunction(List<TagLast> tags)
    {
	    long listeners, playcount, lastfmweight;
	    double q_listeners = 1, q_playlist = 1;
	    double importance;
    	
	    for(TagLast t: tags)
	    {
	    	listeners = t.getListeners();
	    	playcount = t.getPlaycount();
	    	lastfmweight = t.getTagWeight();
	    	
	    	// If the playcount is negative I ignore it
	    	if(playcount <= 0) 
    		{
    			q_playlist = 0;
    			// If playcount is 0 I get a weight of NaN
    			playcount = 1;
    		}
	    		    	
	    	importance = lastfmweight*(q_listeners*Math.log(listeners)+q_playlist*Math.log(playcount));
	    	
	    	t.setImportance(importance); 
	    	
	    }
    }
    
	public void byWeightedMean(List<TagLast> tags, String prefix, Boolean verbose)
	{
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

	    // Compute a weighted normalized weight for each tag/song pair
		importanceFunction(tags);
	    
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
	    for(TagLast t:tags)
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
	
	public void vocabByImportance(List<TagLast> tags, Map<String, Double> vocab, String prefix, Boolean verbose)
	{
	    String key;
	    List<String> words;
	    double sum, importance;
	    double value;
	    double max_imp = 0d;
	  	TagsToCSV writer;
	    Map<String, Double> sums = new HashMap<String, Double>();
	    
	    //Supplier<List<TagLast>> supplier = () -> new LinkedList<TagLast>();
	    // Filtering out tags without a name
	    //List<TagLast> tags_filtered = tags.stream().filter(p -> p.getTagName().length() > 0).collect(Collectors.toCollection(supplier));
	    
	    vocab.clear();
	    
	    // Compute a weighted normalized weight for each tag/song pair
		importanceFunction(tags);
	    
	    // Summing up the importance scores per word 
	    for(Tag t: tags)
	    {	    	    		
			key = t.getTagName();
			importance = t.getImportance();
	    	words = psim.create_word_gram(key);
	
	    	for(String s : words)
	    	{
    			if(sums.containsKey(s))
				{
					sum = sums.get(s);

					// Sum up the importance over all songs
					sums.put(s, sum + importance);
					
	    			// Find max
	    			if(sum + importance > max_imp)
	    			{
	    				max_imp = sum + importance;
	    			}
				}
				else
				{
					sums.put(s, importance);
				}
	    	}
	    }
	    
	    // Normalizing importance and setting it as importance in vocab
	    for(String s: sums.keySet())
	    {
			key = s;
			value = sums.get(s)/max_imp;
	    	
	    	vocab.put(key, value);
	    }
	    
	    
	    // Composite tags get max vocab importance
	    for(TagLast t:tags)
	    {
			key = t.getTagName();
			
	    	words = psim.create_word_gram(key);
	    	value = 0;
	
	    	// Get the maximum importance score
	    	for(String s : words)
	    	{
	    		importance = vocab.get(s);
	    		
	    		if(importance >= value) value = importance;
	    	}

	    	// Set max importance
	    	t.setImportance(value);
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
