package processing;

import java.util.*;
import java.util.Map.Entry;

import core.Tag;

public class Similarity {
	
	PlainStringSimilarity psim = new PlainStringSimilarity();
	
	Map<String, HashSet<String>> tag_2grams = new HashMap<>();
	Map<String, String> substitution_list = new HashMap<>();
	
	Map<String, Double> sortedVocab = new HashMap<>();

	public void withVocab(List<Tag> tags, Map<String, Double> vocab, List<String> whiteList, int minWordSize, Map<String, Map<String, Double>> clusters) {
		/////////////////////////////////
		// Variables
		int ngram_size;
		String high;

		clusters.clear();
		tag_2grams.clear();

		// Set the size for the n-gram distance method
		ngram_size = 2;
		
		/////////////////////////////////
		// Algorithm		
		
		// Filter vocabs and create tag/2-character-gram list
		for (String s : vocab.keySet()) {
			// Only use words with more than 2 characters and at least one none numeric character
			if (s.length() >= minWordSize && !s.matches("^\\d+$") && !s.matches("^\\d+s$")) {

				sortedVocab.put(s, vocab.get(s));
				
				// Compute the 2-gram for all words
				tag_2grams.put(s, psim.create_n_gram(s, ngram_size));
			}
		}

		// Sort the vocab by importance
		sortedVocab = Helper.sortByComparatorDouble(sortedVocab);

		//	Debug stuff
		int psize = sortedVocab.size();
		int part = (int) Math.ceil(psize / 30.0);
		int iter = 0;

		// Find white listed words and prioritize them in the similarity computation
		for (String s : whiteList) {
			if (sortedVocab.containsKey(s)) {
				// Debug
				iter++;
				if (iter % part == 0) {
					System.out.print(iter / part + "/30 - ");
				}
				
				// Remove correct word from list
				sortedVocab.remove(s);

				// Find similar words and save them to the substitution list
				findCluster(sortedVocab, s, clusters);
			}
		}

		// Iterate over the rest of the sorted vocab
		for (Iterator<Entry<String, Double>> iterator = sortedVocab.entrySet().iterator(); iterator.hasNext(); ) {
			high = iterator.next().getKey();

			// Debug
			iter++;
			if (iter % part == 0) {
				System.out.print(iter / part + "/30 - ");
			}

			// Remove the most important word from the list
			// This word is treated as truth
			iterator.remove();

			// Find similar words and save them to the substitution list
			findCluster(sortedVocab, high, clusters);
		}
	}
	
	public void applyClusters(List<Tag> tags, Map<String, Double> vocabPre, double simThreshold, double impThreshold, Map<String, Map<String, Double>> clusters) {
		List<String> words;
		String new_tag, high, word;
		double similarity, similarity2;

		substitution_list.clear();

		// Find substitutions with the highest similarity
		for (Entry<String, Map<String, Double>> cluster : clusters.entrySet()) {
			high = cluster.getKey();

			for (Entry<String, Double> e : cluster.getValue().entrySet()) {
				word = e.getKey();
				similarity = e.getValue();

				/*
				if (similarity >= simThreshold && vocabPre.get(word) >= impThreshold) {
					System.out.println(high + " too important tag " + word + " - Similarity " + similarity + " - Importance " + (vocabPre.get(word)));
				}
				*/

				// Check if similarity >= threshold and importance < threshold
				if (similarity >= simThreshold && vocabPre.get(word) < impThreshold) {
					// Add new substitution
					if (!substitution_list.containsKey(word)) {
						substitution_list.put(word, high);
					} else {
						similarity2 = clusters.get(substitution_list.get(word)).get(word);

						// Replace the substitution if the new similarity is bigger than the old one
						if (similarity > similarity2) {
							//System.out.println("Replaced substitution for "+word+": "+similarity2+"->"+similarity);

							substitution_list.put(word, high);
						}
					}
				}
			}
		}

		// Resolve substitution chains
		resolveChain();

		// Apply this a second time, due to the fact that some chains are not resolved in one run
		resolveChain();

		// Replace tags corresponding to the substitution map
		for (Tag t : tags) {
			words = psim.create_word_gram(t.getTag());
			new_tag = "";

			for (String w : words) {
				if (substitution_list.containsKey(w)) {
					new_tag = new_tag + " " + substitution_list.get(w);
				} else {
					new_tag = new_tag + " " + w;
				}
			}

			t.setTag(new_tag.trim());
		}
	}

	private void resolveChain() {
		String high;
		String word;
		for (Entry<String, String> e : substitution_list.entrySet()) {
			high = e.getValue();

			// Find chains
			if (substitution_list.containsKey(high)) {
				word = substitution_list.get(high);

				// Resolve chain
				// A -> B; B -> C   =>   A -> C; B -> C
				e.setValue(word);
			}
		}
	}

	private void findCluster(Map<String, Double> sortedVocab, String high, Map<String, Map<String, Double>> clusters) {
		HashSet<String> h1, h2;
		double similarity;
		Map<String, Double> cluster = new HashMap<>();
		
		// Compute 2-gram character set of the most important word
		h2 = tag_2grams.get(high);

		// Iterate over all less important words
		for (String word : sortedVocab.keySet()) {
			h1 = tag_2grams.get(word);

			// Compute distance
			similarity = psim.jaccard_index(h1, h2);

			// Add each word with a similarity bigger than 0 to the cluster
			if (similarity > 0) {
				cluster.put(word, similarity);
			}
		}

		// Add cluster to the total list
		clusters.put(high, cluster);
	}
}
