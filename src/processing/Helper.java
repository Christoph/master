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

	public void wordFrequency(List<Tag> tags, Map<String, Long> tagsFrequency) {
		String key;
		long value;
		long max_value = 0;
		List<String> words;

		tagsFrequency.clear();

		// Summing up the occurrences
		for (Tag t : tags) {
			key = t.getTag();
			words = psim.create_word_gram(key);

			for (String s : words) {
				if (tagsFrequency.containsKey(s)) {
					value = tagsFrequency.get(s);

					// Sum up the weight over all songs
					tagsFrequency.put(s, value + 1);

					// Find max
					if (value + 1 > max_value) {
						max_value = value + 1;
					}
				} else {
					tagsFrequency.put(s, (long) 1);
				}
			}
		}
	}

	public void setToLowerCase(List<Tag> tags) {
		String updated;

		for (Tag tag : tags) {
			updated = tag.getTag().toLowerCase();

			tag.setTag(updated);
		}
	}

	public void correctTags(List<Tag> tags) {
		// "Tag+Item": Weight
		Map<String, Double> song_name = new HashMap<>();

		Set<String> used = new HashSet<>();

		double weight;
		String key, item ,tag;

		// Find maximum Weight per song/tag pair
		for (Tag t : tags) {
			tag = t.getTag();
			weight = t.getWeight();
			item = t.getItem();

			key = tag+item;

			if (song_name.containsKey(key)) {
				if (weight > song_name.get(key)) {
					song_name.put(key, weight);
				}
			} else {
				song_name.put(key, weight);
			}
		}

		// Resolve multiple equal tags per song
		for (Tag t : tags) {
			tag = t.getTag();
			weight = t.getWeight();
			item = t.getItem();

			key = tag+item;

			if (song_name.containsKey(key)) {
				if (weight < song_name.get(key)) {
					// This marks the tag object as removable
					t.setTag("");
				}

				if (weight == song_name.get(key) && used.contains(key)) {
					// This marks the tag object as removable
					t.setTag("");
				} else if (weight == song_name.get(key)) {
					used.add(key);
				}
			}
		}

		removeTagsWithoutWords(tags);
	}

	public void splitCompositeTag(List<Tag> tags) {
		String tag;
		String name[];
		List<Tag> tt = new ArrayList<>();
		String temp;

		for (Tag t : tags) {
			tag = t.getTag();

			if (tag.contains(" ")) {
				name = tag.split(" ");

				// Replace current name by the first word
				t.setTag(name[0]);

				// Create for all other words new entries
				for (int i = 1; i < name.length; i++) {
					temp = name[i];

					tt.add(new Tag(t.getId(), t.getItem(), temp, t.getWeight(), t.getImportance(), 1));
				}
			}
		}

		if (tt.size() > 0) {
			// Add all new entries
			tags.addAll(tt);
		}
	}

	public void removeTagsWithoutWords(List<Tag> tags) {
		// Remove tags with no words
		for (Iterator<? extends Tag> iterator = tags.iterator(); iterator.hasNext(); ) {
			Tag t = iterator.next();

			if (t.getTag().length() == 0) {
				iterator.remove();
			}
		}
	}

	public void removeBlacklistedWords(List<Tag> tags, List<String> blacklist) {
		String name, uptated;
		List<String> list;
		PlainStringSimilarity psim = new PlainStringSimilarity();

		for (Tag tag : tags) {
			name = tag.getTag();
			uptated = "";

			list = psim.create_word_gram(name);

			list.removeAll(blacklist);

			for (String s : list) {
				uptated = uptated + " " + s;
			}

			tag.setTag(uptated.trim());
		}

		removeTagsWithoutWords(tags);
	}

	public List<String> getImportantTags(Map<String, Double> vocabPost, double threshold) {
		List<String> temp = new ArrayList<>();

		for (Entry<String, Double> e : vocabPost.entrySet()) {
			if (e.getValue() >= threshold) {
				temp.add(e.getKey());
			}
		}

		return temp;
	}

	public void removeDashes(List<Tag> tags) {
		String name;

		for (Tag t : tags) {
			name = t.getTag();

			t.setTag(name.replaceAll("\\s*-\\s*", " "));
		}
	}

	public <T> String objectToJsonString(List<T> list) {
		List<String> out = new ArrayList<>();

		//ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		ObjectWriter ow = new ObjectMapper().writer();

		try {

			for (T t : list) {
				out.add(ow.writeValueAsString(t));
			}

		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		return out.toString();
	}

	// Converts JSON string to an object map
	public List<Map<String, Object>> jsonStringToList(String json) {
		ObjectMapper mapper = new ObjectMapper();

		List<Map<String, Object>> map = new ArrayList<>();

		try {
			// convert JSON string to Map
			map = mapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {
			});

		} catch (IOException e) {
			e.printStackTrace();
		}
		return map;
	}

	public static Map<String, Double> sortByComparatorDouble(Map<String, Double> unsorted) {

		// Variables
		Map<String, Double> sortedMap = new LinkedHashMap<>();

		// Convert map to list
		List<Map.Entry<String, Double>> list =
				new LinkedList<>(unsorted.entrySet());

		// Sort list with comparator
		// Sort in decreasing order
		Collections.sort(list, (o1, o2) -> (o2.getValue()).compareTo(o1.getValue()));

		// Convert sorted map back to a map
		for (Iterator<Map.Entry<String, Double>> it = list.iterator(); it.hasNext(); ) {
			Map.Entry<String, Double> entry = it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}

		return sortedMap;
	}

	public static Map<String, Integer> sortByComparatorInteger(Map<String, Integer> unsorted) {

		// Variables
		Map<String, Integer> sortedMap = new LinkedHashMap<>();

		// Convert map to list
		List<Map.Entry<String, Integer>> list =
				new LinkedList<>(unsorted.entrySet());

		// Sort list with comparator
		// Sort in decreasing order
		Collections.sort(list, (o1, o2) -> (o2.getValue()).compareTo(o1.getValue()));

		// Convert sorted map back to a map
		for (Iterator<Map.Entry<String, Integer>> it = list.iterator(); it.hasNext(); ) {
			Map.Entry<String, Integer> entry = it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}

		return sortedMap;
	}

	public static Map<String, Long> sortByComparatorLong(Map<String, Long> unsorted) {

		// Variables
		Map<String, Long> sortedMap = new LinkedHashMap<>();

		// Convert map to list
		List<Map.Entry<String, Long>> list =
				new LinkedList<>(unsorted.entrySet());

		// Sort list with comparator
		// Sort in decreasing order
		Collections.sort(list, (o1, o2) -> (o2.getValue()).compareTo(o1.getValue()));

		// Convert sorted map back to a map
		for (Iterator<Map.Entry<String, Long>> it = list.iterator(); it.hasNext(); ) {
			Map.Entry<String, Long> entry = it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}

		return sortedMap;
	}

	public void resetStep(List<List<Tag>> tags, int index) {
		// Clear old values
		tags.get(index).clear();

		// The clone
		List<Tag> temp = new ArrayList<>();

		// Cloning
		for(Tag t: tags.get(index-1))
		{
			temp.add(new Tag(t));
		}

		// Copy From previous step
		tags.set(index, temp);
	}

	public List<gridHist> prepareVocabHistogram(Map<String, Double> vocab) {
		List<gridHist> hist = new ArrayList<>();
		Map<Double, Long> temp = new HashMap<>();

		for (Entry<String, Double> c : vocab.entrySet()) {
			if (temp.containsKey(c.getValue())) {
				temp.put(c.getValue(), temp.get(c.getValue()) + 1);
			} else {
				temp.put(c.getValue(), (long) 1);
			}

		}

		for (double d : temp.keySet()) {
			hist.add(new gridHist(d, temp.get(d)));
		}

		return hist;
	}

	public List<gridVocab> prepareVocab(Map<String, Double> vocab) {
		List<gridVocab> tags_filtered = new ArrayList<>();

		for (String s : Helper.sortByComparatorDouble(vocab).keySet()) {
			tags_filtered.add(new gridVocab(s, vocab.get(s)));
		}

		return tags_filtered;
	}
}
