package processing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import core.Tag;
import core.json.gridHistFreq;
import core.json.gridVocab;

public class Preprocess {

	// Variables
	int index;
	
	// Classes
	private PlainStringSimilarity psim = new PlainStringSimilarity();
	
	// Parameters
	private int filter;
	private String remove;
	private List<String> replace = new ArrayList<String>();
	private List<String> blacklist;
	
	public Preprocess(int index, List<String> blacklist) {
		// Set working copy
		this.index = index;
		this.blacklist = blacklist;

		// Default parameters
		replace.add("-, ");
		replace.add("_, ");
		replace.add(":, ");
		replace.add(";, ");
		replace.add("/, ");
		
		setFilter(0);
		setRemove("'");
	}

	// Remove all words below the threshold
	public void applyFilter(List<Tag> tags, Map<String, Long> tagsFreq) {
		String key = "";
		String temp = "";
		List<String> words;

		if (tagsFreq.size() > 0) {
			for (Tag t : tags) {
				words = psim.create_word_gram(t.getTag(index));
				temp = "";
				
				for (int j = 0; j < words.size(); j++) {
					key = words.get(j);
					if (key.length() > 0) {
						if (tagsFreq.get(key) < filter && key.length() > 0) {
							// Remove word
							words.set(j, "");
						}
					}
				}

				// Rebuild tags and save them
				for (String s : words) {
					if (s.length() > 0) {
						temp = temp + s + " ";
					}
				}

				t.setTag(index, temp.trim());
			}
		}
	}
	
	public void removeCharacters(List<Tag> tags) {
		String updated;

		for (Tag tag : tags) {
			updated = tag.getTag(index);

			// Remove characters
			if (remove.length() > 0) {
				updated = updated.replaceAll("[" + Pattern.quote(remove) + "]", "");
			}

			tag.setTag(index, updated);
		}
	}

	public void replaceCharacters(List<Tag> tags) {
		String updated;

		for (Tag tag : tags) {
			updated = tag.getTag(index);

			if (replace.size() > 0) {
				// Replace characters
				for (String s : replace) {
					String temp[] = s.split(",");
					updated = updated.replaceAll(Pattern.quote(temp[0]), temp[1]);
				}
			}

			tag.setTag(index, updated);
		}
	}

	public List<gridVocab> preparePreFilter(Map<String, Long> tagsFreq) {
		List<gridVocab> tags_filtered = new ArrayList<gridVocab>();

		for (String s : tagsFreq.keySet()) {
			tags_filtered.add(new gridVocab(s, tagsFreq.get(s)));
		}

		return tags_filtered;
	}
	
	public List<gridHistFreq> preparePreFilterHistogram(Map<String, Long> tagsFreq) {
		List<gridHistFreq> hist = new ArrayList<gridHistFreq>();
		Map<Long, Long> temp = new HashMap<Long, Long>();

		for (Entry<String, Long> c : tagsFreq.entrySet()) {
			if (temp.containsKey(c.getValue())) {
				temp.put(c.getValue(), temp.get(c.getValue()) + 1);
			} else {
				temp.put(c.getValue(), (long) 1);
			}

		}

		for (long d : temp.keySet()) {
			hist.add(new gridHistFreq(d, temp.get(d)));
		}

		return hist;
	}

	public int getFilter() {
		return filter;
	}

	public void setFilter(int filter) {
		this.filter = filter;
	}

	public String getRemove() {
		return remove;
	}

	public void setRemove(String remove) {
		this.remove = remove;
	}

	public List<String> getReplace() {
		return replace;
	}

	public void setReplace(List<Map<String, Object>> map) {
		
		replace.clear();
		
		for (int i = 0; i < map.size(); i++) {
			replace.add(map.get(i).get("replace") + "," + map.get(i).get("by"));
		}
	}

	public void setDictionary(List<Map<String, Object>> map) {
		String tag;
		blacklist.clear();

		for (int i = 0; i < map.size(); i++) {
			tag = String.valueOf(map.get(i).get("word"));
			
			if (tag.length() > 0) {
				blacklist.add(tag);
			}
		}
	}
}
