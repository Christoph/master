package processing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.Tag;
import core.json.gridRepl;
import core.json.gridVocab;

public class Postprocess {

	// Variables
	int index;

	// Classes
	private Helper help = new Helper();
	private Regex regex = new Regex();

	// Data
	private List<String> importantWords = new ArrayList<String>();
	private List<String> salvageWords = new ArrayList<String>();
	protected Map<String, String> salvagedData = new HashMap<String, String>();

	// Parameters
	private double postFilter;
	private int minWordLength;
	private Boolean useAllWords;
	private Boolean splitTags;
	private List<String> postReplace = new ArrayList<String>();
	
	public Postprocess(int index) {
		this.index = index;
		
		// Default parameter
		minWordLength = 3;
		postFilter = 0.25;
		useAllWords = false;
		splitTags = false;
	}
	
	public void initializeSalvaging(Map<String, Double> vocabPost) {
		// Get important words
		importantWords = help.getImportantTags(vocabPost, postFilter);
		
		// Get salvage words
		salvageWords = regex.replaceCustomWords(importantWords, postReplace, index);
	}
	
	public void computeSalvaging(Map<String, Double> vocabPost) {
		// Find important words in the unimportant tags
		regex.findImportantWords(vocabPost, salvageWords, salvagedData, postFilter, minWordLength, index);
		System.out.println(salvagedData.toString());
	}
	
	public void applySalvaging(List<Tag> tags) {
		regex.apply(tags, salvageWords, salvagedData, useAllWords, index);
		
		if (splitTags) {
			help.splitCompositeTag(tags, index);
			help.correctTags(tags, index);
		}
		
		//help.removeDashes(tags, index);
	}

	public List<gridVocab> prepareImportantWords() {
		List<gridVocab> tags_filtered = new ArrayList<gridVocab>();

		for (String s : importantWords) {
			tags_filtered.add(new gridVocab(s, 0));
		}

		return tags_filtered;
	}
	
	public List<gridVocab> prepareSalvageWords() {
		List<gridVocab> tags_filtered = new ArrayList<gridVocab>();

		for (String s : salvageWords) {
			tags_filtered.add(new gridVocab(s, 0));
		}

		return tags_filtered;
	}
	
	public List<gridRepl> prepareSalvagedData() {
		List<gridRepl> tags_filtered = new ArrayList<gridRepl>();

		for (String s : salvagedData.keySet()) {
			tags_filtered.add(new gridRepl(salvagedData.get(s), s, 0));
		}

		return tags_filtered;
	}

	public double getPostFilter() {
		return postFilter;
	}

	public void setPostFilter(double postFilter) {
		this.postFilter = postFilter;
	}

	public List<String> getImportantWords() {
		return importantWords;
	}

	public void setImportantWords(List<String> importantWords) {
		this.importantWords = importantWords;
	}

	public List<String> getPostReplace() {
		return postReplace;
	}

	public void setPostReplace(List<Map<String, Object>> map) {
		postReplace.clear();
		
		for (int i = 0; i < map.size(); i++) {
			postReplace.add(map.get(i).get("replace") + "," + map.get(i).get("by"));
		}
	}

	public int getMinWordLength() {
		return minWordLength;
	}

	public void setMinWordLength(int minWordLength) {
		this.minWordLength = minWordLength;
	}

	public Boolean getUseAllWords() {
		return useAllWords;
	}

	public void setUseAllWords(Boolean useAllWords) {
		this.useAllWords = useAllWords;
	}

	public Boolean getSplitTags() {
		return splitTags;
	}

	public void setSplitTags(Boolean splitTags) {
		this.splitTags = splitTags;
	}

}
