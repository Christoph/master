package processing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import core.Tag;

public class Regex {

	protected String name = "";
	protected String join = "";

	protected String ls = "";
	protected String ms = "";
	protected String rs = "";
	
	protected String e = "";

	protected List<String> numbers = new ArrayList<String>();
	protected List<String> replacements;
	protected List<String> out = new ArrayList<String>();
	protected Helper help = new Helper();

	// Debug output
	protected Boolean print_groups = true;

	// Create a Pattern object
	protected Pattern r, l;

	// Pattern dict
	protected Map<String, Pattern> patterns_greedy = new HashMap<String, Pattern>();
	protected Map<String, Pattern> patterns_conservative = new HashMap<String, Pattern>();

	// Now create matcher object.
	protected Matcher mr, ml;

	// Temporary


	public void apply(List<Tag> tags, List<String> salvageWords, Map<String, String> salvagedData, Boolean useAllWords, int index) {
		String tag, newTag;
		List<String> temp = new ArrayList<String>();
		String[] words;

		for (Tag t : tags) {
			tag = t.getTag(index);
			temp.clear();
			newTag = "";

			words = tag.split(" ");

			for (String w : words) {
				if (salvagedData.keySet().contains(w)) {
					temp.add(salvagedData.get(w));
				} else if (salvageWords.contains(w)) {
					temp.add(w);
				} else {
					if (useAllWords) {
						temp.add(w);
					}
				}
			}

			// Rebuild string from temp
			for (String s : temp) {
				if (s.length() > 0) {
					newTag = newTag.concat(" " + s).trim();
				}
			}

			t.setTag(index, newTag);
		}

		help.removeTagsWithoutWords(tags, index);
	}

	public void findImportantWords(Map<String, Double> vocabPost, List<String> importantWords, Map<String, String> salvagedData, double threshold, int minWordLength, int index) {
		salvagedData.clear();

		System.out.print(vocabPost.size());
		int part = vocabPost.size() / 30;
		int iter = 0;

		// Precompile patterns
		for (String s : importantWords) {
			patterns_greedy.put(s, Pattern.compile("(.*)(" + s + ")(.*)"));
			patterns_conservative.put(s, Pattern.compile("(\\s)(" + s + ")(\\s)"));
		}

		for (Entry<String, Double> e : vocabPost.entrySet()) {
			iter++;
			if (iter % part == 0) {
				System.out.print("->" + iter);
			}
			
			// Set tag name
			name = e.getKey();
			
			if (e.getValue() < threshold) {
				// Reset join string and out list
				join = "";
				out.clear();

				// Apply regex
				matcher(name, importantWords, minWordLength);

				// Rebuild string from out
				for (String s : out) {
					if (s.length() > 0) {
						join = join.concat(" " + s);
					}
				}

				join = join.trim();

				// Add the extraction to the output
				if (!name.equals(join) && !join.isEmpty()) {
					salvagedData.put(name, join);
				}
			}
		}
	}
	
	
	// String, > 0 == right, list of important tags
	public void matcher(String name, List<String> importantWords, int minWordLength) {
		name = " " + name + " ";
		
		for (String e : importantWords) {
			// Compile patterns
			// If bigger than min word length do substring search
			// else only full word search
			if (e.length() > minWordLength) {
				l = patterns_greedy.get(e);
				r = patterns_greedy.get(e);
			} else {
				l = patterns_conservative.get(e);
				r = patterns_conservative.get(e);
			}

			// Find matches
			mr = r.matcher(name);
			ml = l.matcher(name);

			// Check if a match happened
			if (mr.find()) {
				String ls = mr.group(1).trim();
				String ms = mr.group(2).trim();
				String rs = mr.group(3).trim();

				if (ls.length() > 0) matcher(ls, importantWords, minWordLength);

				out.add(ms.trim());

				if (rs.length() > 0) matcher(rs, importantWords, minWordLength);

				name = "";
			} else if (ml.find()) {
				String ls = ml.group(1).trim();
				String ms = ml.group(2).trim();
				String rs = ml.group(3).trim();

				if (ls.length() > 0) matcher(ls, importantWords, minWordLength);

				out.add(ms);

				if (rs.length() > 0) matcher(rs, importantWords, minWordLength);

				name = "";
			}
		}
	}
	
	public List<String> replaceCustomWords(List<String> importantWords, List<String> patterns, int index) {
		String[] row;
		
		for (String s : patterns) {
			row = s.split(",");
			
			if (importantWords.contains(row[0])) {
				importantWords.set(importantWords.indexOf(row[0]), row[1]);
			}
		}
		
		return importantWords;
	}
}
