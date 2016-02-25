package processing;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.Tag;

public class Weighting {

	private PlainStringSimilarity psim = new PlainStringSimilarity();
	
    /*
	public void tags(List<Tag> tags, Map<String, Double> vocab, int index) {
		
		// Update vocab
		vocab(tags,vocab,index);
		
	    /////////////////////////////////
	    // Variables
	    Map<String, Double> tag_words = new HashMap<String, Double>();
	    
	  	List<String> weights = new ArrayList<String>();
	  	Set<String> temp = new HashSet<String>();
	    
	
	    String key;
	    double importance, max_w = 0;
	    double value;
	
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
	}
	*/

	public void vocab(List<? extends Tag> tags, Map<String, Double> vocab, int index) {
		String key;
		List<String> words;
		double value;
		double max_value = 0d;
		Map<String, Double> sums = new HashMap<String, Double>();
		
		vocab.clear();

		// Summing up the weights
		for (Tag t : tags) {
			key = t.getTag(index);
			words = psim.create_word_gram(key);

			for (String s : words) {
				if (sums.containsKey(s)) {
					value = sums.get(s);
					
					// Sum up the weight over all songs
					sums.put(s, value + t.getWeight());
					
					if (value + t.getWeight() < 0) {
						throw new IndexOutOfBoundsException("The sum of all weights corresponding to one tag is too big. OVERLOW!");
					}
				} else {
					sums.put(s, t.getWeight());
				}
			}
		}

		// Normalizing frequency and setting it as importance in vocab
		for (String s : sums.keySet()) {
			key = s;
			max_value = Collections.max(sums.values());
			
			// Normalize each value the the total maximum
			value = sums.get(s) / max_value;

			vocab.put(key, value);
		}
	}
}