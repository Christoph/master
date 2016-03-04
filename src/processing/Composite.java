package processing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import core.Tag;
import core.json.gridGroup;
import core.json.gridHist;

public class Composite {

	// Classes
	private Grouping grouping = new Grouping();
	private Helper help = new Helper();
	
	// Data
	private List<String> whitelist = new ArrayList<>();
	private TreeMap<Double, Map<String, Integer>> jaccard_groups = new TreeMap<>();
	private TreeMap<Double, Map<String, Integer>> frequent_groups = new TreeMap<>();
	
	
	// Parameters
	private double jaccardThreshold = 1;
	private double frequentThreshold = 1;
	private int maxGroupSize = 3;
	private int minOccurrence = 1;
	private Boolean split = false;

	public Composite(List<String> whitelistGroups) {
		// Set working copy
		this.whitelist = whitelistGroups;
	}
	
	public void group(List<Tag> tags) {
		grouping.group(tags, maxGroupSize, getMinOccurrence(), jaccard_groups, frequent_groups);
	}

	public void applyGroups(List<Tag> tags) {
		TreeMap<Double, Map<String, Integer>> temp = new TreeMap<>();
		List<String> subs = new ArrayList<>();
		String name;
		
		// Merge all relevant items into one list
		addRelevantItems(temp, frequent_groups, frequentThreshold);
		addRelevantItems(temp, jaccard_groups, jaccardThreshold);

		// Add substitutions in correct order: whitelist > highest group + highest strength > rest
		if (whitelist.size() > 0) {
			subs.addAll(whitelist);
		}
		
		for (Entry<Double, Map<String, Integer>> st : temp.descendingMap().entrySet()) {
			subs.addAll(Helper.sortByComparatorInteger(st.getValue()).keySet());
		}
		
		// Replace word groups
		for (Tag t : tags) {
			name = t.getTag();

			for (String s : subs) {
				if (name.contains(s)) {
					name = name.replaceAll(Pattern.quote(s), s.replace(" ", "-"));
				}
			}

			t.setTag(name);
		}

		// Find groups without spaces hardrock -> hard-rock
		findGroups(tags);

		if (split) {
			help.splitCompositeTag(tags);
			help.correctTags(tags);
		}
	}

	private void addRelevantItems(TreeMap<Double, Map<String, Integer>> temp, TreeMap<Double, Map<String, Integer>> groups, double threshold) {
		for (Entry<Double, Map<String, Integer>> st : groups.descendingMap().entrySet()) {
			if (st.getKey() >= threshold) {
				if (!temp.containsKey(st.getKey())) {
					temp.put(st.getKey(), new HashMap<>());
				}

				temp.get(st.getKey()).putAll(st.getValue());
			}
		}
	}
	
	private void findGroups(List<Tag> tags) {
		Set<String> groups = new HashSet<>();
		Map<String, String> subs = new HashMap<>();
		
		String words[];
		String name;
		
		//Find all groups
		for (Tag t : tags) {
			if (t.getTag().contains("-")) {
				words = t.getTag().split(" ");
				
				for (String s : words) {
					if (s.contains("-")) {
						groups.add(s);
					}
				}
			}
		}
		
		// Create substitution list
		for (String s : groups) {
			subs.put(s.replace("-", ""), s);
		}
		
		//Find groups
		for (Tag t : tags) {
			name = t.getTag();
			words = name.split(" +");
			
			for (String s : words) {
				if (subs.keySet().contains(s)) {
					name = name.replace(s, subs.get(s));
				}
			}
			
			t.setTag(name);
		}
	}

	public List<gridGroup> prepareUniqueGroups() {
		List<gridGroup> temp = new ArrayList<>();



		for (Entry<Double, Map<String, Integer>> s : jaccard_groups.descendingMap().entrySet()) {
			for (Entry<String, Integer> e : Helper.sortByComparatorInteger(s.getValue()).entrySet()) {
				temp.add(new gridGroup(e.getKey(), s.getKey()));
			}
		}
		
		return temp;
	}

	public List<gridGroup> prepareFrequentGroups() {
		List<gridGroup> temp = new ArrayList<>();
		
		for (Entry<Double, Map<String, Integer>> s : frequent_groups.descendingMap().entrySet()) {
			for (Entry<String, Integer> e : Helper.sortByComparatorInteger(s.getValue()).entrySet()) {
				temp.add(new gridGroup(e.getKey(), s.getKey()));
			}
		}
		return temp;
	}
	
	public List<gridHist> prepareFrequentHistogram() {
		return getGridHists(frequent_groups);
	}

	public List<gridHist> prepareUniqueHistogram() {
		return getGridHists(jaccard_groups);
	}

	private List<gridHist> getGridHists(TreeMap<Double, Map<String, Integer>> groups) {
		List<gridHist> hist = new ArrayList<>();
		Map<Double, Long> temp = new HashMap<>();

		for (Entry<Double, Map<String, Integer>> c : groups.entrySet()) {
			temp.put(c.getKey(), (long) c.getValue().size());
		}

		for (double d : temp.keySet()) {
			hist.add(new gridHist(d, temp.get(d)));
		}

		return hist;
	}

	public double getJaccardThreshold() {
		return jaccardThreshold;
	}

	public void setJaccardThreshold(double jaccardThreshold) {
		this.jaccardThreshold = jaccardThreshold;
	}

	public double getFrequentThreshold() {
		return frequentThreshold;
	}

	public void setFrequentThreshold(double frequentThreshold) {
		this.frequentThreshold = frequentThreshold;
	}

	public int getMaxGroupSize() {
		return maxGroupSize;
	}

	public void setMaxGroupSize(int maxGroupSize) {
		this.maxGroupSize = maxGroupSize;
	}

	public Boolean getSplit() {
		return split;
	}

	public void setSplit(Boolean split) {
		this.split = split;
	}

	public int getMinOccurrence() {
		return minOccurrence;
	}

	public void setMinOccurrence(int minOccurrence) {
		this.minOccurrence = minOccurrence;
	}
}
