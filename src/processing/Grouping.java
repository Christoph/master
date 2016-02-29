package processing;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import core.Tag;

public class Grouping {
	
	PlainStringSimilarity psim = new PlainStringSimilarity();

	public void group(List<? extends Tag> tags, int maxGroupSize, int minOccurrence, TreeMap<Double, Map<String, Integer>> jaccard_groups, TreeMap<Double, Map<String, Integer>> frequent_groups, int index) {
		jaccard_groups.clear();
		frequent_groups.clear();
		
		// Compute groups
		jaccard(tags, maxGroupSize, minOccurrence, jaccard_groups, index);
		frequency(tags, maxGroupSize, frequent_groups, index);
	}
	
	private void jaccard(List<? extends Tag> tags, int maxGroupSize, int minOccurrence, TreeMap<Double, Map<String, Integer>> jaccard_groups, int index) {
		for (int size = maxGroupSize; size >= 2; size--) {
			/////////////////////////////////
			// Variables		
			Map<String, Long> word_count = new HashMap<String, Long>();
			Map<String, Long> word_groups = new HashMap<String, Long>();
			Map<String, Double> groups_strength = new HashMap<String, Double>();

			List<String> words;
			long value;
			double nom, deno, strength, min_o = 1, max_o = 0;
			String key;
			
			/////////////////////////////////
			// Algorithm
			
			// Create a 1-word-gram/total occurrences dict
			for (int i = 0; i < tags.size(); i++) {
				words = psim.create_word_gram(tags.get(i).getTag(index));
				
				countOccurrences(word_count, words);
			}
			
			// Create a n-word-gram/total occurrences
			for (int i = 0; i < tags.size(); i++) {
				words = psim.create_word_n_gram(tags.get(i).getTag(index), size);
				
				for (int j = 0; j < words.size(); j++) {
					key = words.get(j);
					
					if (word_groups.containsKey(key)) {
						value = word_groups.get(key);
						
						// Sum up the count
						word_groups.put(key, value + 1l);
					} else {
						word_groups.put(key, 1l);
					}
				}
			}
			
			// Compute binding strength
			for (String k : word_groups.keySet()) {
				words = psim.create_word_gram(k);
				deno = 0;
				
				for (String s : words) {
					deno = deno + word_count.get(s);
				}
				
				nom = word_groups.get(k);
				strength = nom / deno;
				//TODO: Work in progress: Filter
				if (nom > minOccurrence) groups_strength.put(k, strength);
				
				// Find min max
				if (strength < min_o) min_o = strength;
				if (strength >= max_o) max_o = strength;
			}
			
			// Normalize
			normalizeGroup(jaccard_groups, size, groups_strength, min_o, max_o);
		}
	}

	private void normalizeGroup(TreeMap<Double, Map<String, Integer>> jaccard_groups, int size, Map<String, Double> groups_strength, double min_o, double max_o) {
		double strength;
		for (String s : groups_strength.keySet()) {
			strength = (groups_strength.get(s) - min_o) / (max_o - min_o);

			// Add all groups
			if (jaccard_groups.containsKey(strength)) {
				jaccard_groups.get(strength).put(s, size);
			} else {
				jaccard_groups.put(strength, new HashMap<String, Integer>());
				jaccard_groups.get(strength).put(s, size);
			}
		}
	}
	
	private void frequency(List<? extends Tag> tags, int maxGroupSize, TreeMap<Double, Map<String, Integer>> frequent_groups, int index) {
		for (int size = maxGroupSize; size >= 2; size--) {
			/////////////////////////////////
			// Variables
			Map<String, Long> word_count = new HashMap<String, Long>();

			List<String> words;

			long value;
			double strength, min_o = 1, max_o = 0;
			String key;

			/////////////////////////////////
			// Algorithm
			
			// Create a n-word-gram/total occurrences
			for (int i = 0; i < tags.size(); i++) {
				words = psim.create_word_n_gram(tags.get(i).getTag(index), size);

				countOccurrences(word_count, words);
			}

			// Find min and max
			for (long k : word_count.values()) {
				if (k < min_o) min_o = k;
				if (k >= max_o) max_o = k;
			}

			// Normalize
			normalizeFrequent(frequent_groups, size, word_count, min_o, max_o);
		}
	}

	private void normalizeFrequent(TreeMap<Double, Map<String, Integer>> frequent_groups, int size, Map<String, Long> word_count, double min_o, double max_o) {
		double strength;
		for (String s : word_count.keySet()) {
			strength = (word_count.get(s) - min_o) / (max_o - min_o);

			// Add all groups
			if (frequent_groups.containsKey(strength)) {
				frequent_groups.get(strength).put(s, size);
			} else {
				frequent_groups.put(strength, new HashMap<String, Integer>());
				frequent_groups.get(strength).put(s, size);
			}
		}
	}

	private void countOccurrences(Map<String, Long> word_count, List<String> words) {
		String key;
		long value;
		for (int j = 0; j < words.size(); j++) {
			key = words.get(j);

			if (word_count.containsKey(key)) {
				value = word_count.get(key);

				// Sum up the count
				word_count.put(key, value + 1);
			} else {
				word_count.put(key, 1l);
			}
		}
	}
}
