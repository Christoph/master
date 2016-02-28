package processing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import core.Tag;
import core.json.gridCluster;
import core.json.gridHist;
import core.json.gridRepl;

public class Spellcorrect {
	
	// Variables
	private int index;
	private Map<String, Map<String, Double>> vocabClusters = new HashMap<String, Map<String, Double>>();
	private TreeMap<Double, Map<String, String>> simClusters = new TreeMap<Double, Map<String, String>>();

	// Classes
	private Similarity similarity = new Similarity();

	// Parameters
	private double spellImportance;
	private double spellSimilarity;
	private int minWordSize;
	private List<String> whitelistWords;
	private List<String> whitelistGroups;
	private List<String> whitelistVocab;

	public Spellcorrect(int index, List<String> whitelistWords, List<String> whitelistGroups, List<String> whitelistVocab) {
		this.index = index;
		this.whitelistWords = whitelistWords;
		this.whitelistGroups = whitelistGroups;
		this.whitelistVocab = whitelistVocab;
		
		// Initial values
		setSpellImportance(0.5);
		setSpellSimilarity(0.7);
		setMinWordSize(3);
	}
	
	public void clustering(List<Tag> tags, Map<String, Double> vocabPre, List<String> whitelist) {
		// compute similarities
		similarity.withVocab(tags, vocabPre, whitelist, getMinWordSize(), vocabClusters);

		// create similarity clusters
		createSimClusters();
	}
	
	public void applyClustering(List<Tag> tags, Map<String, Double> vocabPre) {
		similarity.applyClusters(tags, vocabPre, spellSimilarity, spellImportance, vocabClusters, index);
		
		// Resolve errors from replacements
		//help.correctTags(tags, 2);
	}
	
	private void createSimClusters() {
		String head = "";
		String key = "";
		double value = 0;
		
		simClusters.clear();
		
		for (Entry<String, Map<String, Double>> c : vocabClusters.entrySet()) {
			head = c.getKey();

			for (Entry<String, Double> e : c.getValue().entrySet()) {
				key = e.getKey();
				value = e.getValue();

				if (simClusters.containsKey(value)) {
					simClusters.get(value).put(head, key);
				} else {
					// Create inner map
					simClusters.put(value, new HashMap<String, String>());

					// Add first line
					simClusters.get(value).put(head, key);
				}
			}
		}
	}
	
	public List<gridCluster> prepareCluster(String tag, Map<String, Double> vocabPre) {
		List<gridCluster> tags_filtered = new ArrayList<gridCluster>();

		if (vocabClusters.containsKey(tag)) {
			for (String s : vocabClusters.get(tag).keySet()) {
				tags_filtered.add(new gridCluster(s, vocabPre.get(s), vocabClusters.get(tag).get(s)));
			}
		}

		return tags_filtered;
	}
	
	public List<gridHist> prepareSimilarityHistogram() {
		List<gridHist> hist = new ArrayList<gridHist>();
		Map<Double, Long> temp = new HashMap<Double, Long>();

		for (Entry<String, Map<String, Double>> c : vocabClusters.entrySet()) {
			for (double s : c.getValue().values()) {
				if (temp.containsKey(s)) {
					temp.put(s, temp.get(s) + 1);
				} else {
					temp.put(s, (long) 1);
				}
			}
		}

		for (double d : temp.keySet()) {
			hist.add(new gridHist(d, temp.get(d)));
		}

		return hist;
	}
	
	public int prepareReplacements(double sim, double imp, Map<String, Double> vocabPre) {
		int replacements = 0;

		for(Map<String, Double> cluster: vocabClusters.values())
		{
			for(Entry<String, Double> e: cluster.entrySet())
			{
				double a = e.getValue();
				double b = vocabPre.get(e.getKey());

				if(e.getValue() > sim && vocabPre.get(e.getKey()) < imp)
				{
					replacements += 1;
				}
			}
		}

		return replacements;
	}

	public double getSpellImportance() {
		return spellImportance;
	}

	public void setSpellImportance(double spellImportance) {
		this.spellImportance = spellImportance;
	}

	public double getSpellSimilarity() {
		return spellSimilarity;
	}

	public void setSpellSimilarity(double spellSimilarity) {
		this.spellSimilarity = spellSimilarity;
	}

	public void setDictionary(List<Map<String, Object>> map) {
		String tag;
		whitelistGroups.clear();
		whitelistWords.clear();
		whitelistVocab.clear();

		for (int i = 0; i < map.size(); i++) {
			tag = String.valueOf(map.get(i).get("tag"));

			if (tag.length() > 0) {
				if (tag.contains(" ")) {
					whitelistGroups.add(tag);

					// Build vocab
					for (String s : tag.split(" ")) {
						if (!whitelistVocab.contains(tag)) whitelistVocab.add(s);
					}
				} else {
					whitelistWords.add(tag);

					// Build vocab
					if (!whitelistVocab.contains(tag)) whitelistVocab.add(tag);
				}
			}
		}
	}

	public int getMinWordSize() {
		return minWordSize;
	}

	public void setMinWordSize(int minWordSize) {
		this.minWordSize = minWordSize;
	}
}
