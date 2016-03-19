package processing;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.Tag;

public class Weighting {

	private PlainStringSimilarity psim = new PlainStringSimilarity();

	public void vocab(List<Tag> tags, Map<String, Double> vocab) {
		String key;
		List<String> words;
		double value;
		double max_value = 0d;
		Map<String, Double> sums = new HashMap<String, Double>();
		
		vocab.clear();

		// Summing up the weights
		for (Tag t : tags) {
			key = t.getTag();
			words = psim.create_word_gram(key);

			for (String s : words) {
				if (sums.containsKey(s)) {
					value = sums.get(s);
					
					// Sum up the weight over all songs
					sums.put(s, value + t.getWeight());
					
					if (value + t.getWeight() < 0) {
						throw new IndexOutOfBoundsException("The sum of all weights corresponding to one tag is too big. OVERFLOW!");
					}
				} else {
					sums.put(s, t.getWeight());
				}
			}
		}

		// Take the log of all numbers
		for(Map.Entry<String, Double> e: sums.entrySet())
		{
			if(e.getValue() < 1)
			{
				e.setValue(Math.log(1));
			}
			else
			{
				e.setValue(Math.log(e.getValue()));
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