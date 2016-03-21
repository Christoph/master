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

	protected String e = "";

	protected List<String> out = new ArrayList<>();
	protected Helper help = new Helper();

	// Create a Pattern object
	protected Pattern r, l;

	// Pattern dict
	protected Map<String, Pattern> patterns_greedy = new HashMap<>();
	protected Map<String, Pattern> patterns_conservative = new HashMap<>();

	// Now create matcher object.
	protected Matcher mr, ml;

	public void apply(List<Tag> tags, List<String> importantWords, Map<String, String> salvagedData, Boolean useAllWords, List<String> postRemove, List<String> postReplace) {
		String tag, newTag;
		List<String> temp = new ArrayList<>();
		String[] words;
		List<String> salvageWords;

		// Remove the marked words before the regex
		salvageWords = removeWords(importantWords, postRemove);

		for (Tag t : tags) {
			tag = t.getTag();
			temp.clear();
			newTag = "";

			words = tag.split(" ");

			for (String w : words) {
				if (salvagedData.keySet().contains(w)) {
					temp.add(removeWord(salvagedData.get(w), postRemove));
				}
				else if (salvageWords.contains(w))
				{
					temp.add(w);
				}
				else
				{
					if (useAllWords)
					{
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

			t.setTag(replaceWords(newTag, postReplace));
		}

		help.removeTagsWithoutWords(tags);
	}

	public void findImportantWords(Map<String, Double> vocabPost, List<String> importantWords, Map<String, String> salvagedData, double threshold, int minWordLength, List<String> postRemove, List<String> postReplace) {
		salvagedData.clear();
		List<String> temp;

		// Remove the marked words before the regex
		temp = removeWords(importantWords, postRemove);

		System.out.print(vocabPost.size());
		int part = vocabPost.size() / 30;
		int iter = 0;

		// Precompile patterns
		for (String s : temp) {
			patterns_greedy.put(s, Pattern.compile("(.*)(" + Pattern.quote(s) + ")(.*)"));
			patterns_conservative.put(s, Pattern.compile("(\\s)(" + Pattern.quote(s) + ")(\\s)"));
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
				matcher(name, temp, minWordLength);

				// Rebuild string from out
				for (String s : out) {
					if (s.length() > 0) {
						join = join.concat(" " + s);
					}
				}

				join = join.trim();

				// Add the extraction to the output
				if (!name.equals(join) && !join.isEmpty()) {
					salvagedData.put(replaceWords(name, postReplace), replaceWords(join, postReplace));
				}
			}
		}

		System.out.println("regex end");
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
	
	public String removeWord(String text, List<String> postRemove) {
		String out = text;

		// Replace Words
		if(out.length() > 0)
		{
			// Remove
			for(String s: postRemove)
			{
				out = out.replaceAll(Pattern.quote(s),"");
			}
		}

		return out;
	}

	public String replaceWords(String text, List<String> patterns) {
		String[] row;
		String out = text;

		// Replace Words
		if(out.length() > 0)
		{
			for (String s : patterns) {
				row = s.split(",");

				out = out.replaceAll(Pattern.quote(row[0]),row[1]);
			}
		}

		return out;
	}

	public List<String> removeWords(List<String> importantWords, List<String> postRemove) {
		List<String> out = new ArrayList<>();
		out.addAll(importantWords);

		// Remove words
		out.removeAll(postRemove);

		return out;
	}
}
