package processing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.Tag;
import core.json.gridRepl;
import core.json.gridVocab;

public class Postprocess {

	// Classes
	private Helper help = new Helper();
	private Regex regex = new Regex();

	// Data
	private List<String> importantWords = new ArrayList<>();
	private List<String> salvageWords = new ArrayList<>();
	protected Map<String, String> salvagedData = new HashMap<>();

	// Parameters
	private double postFilter = 1;
	private int minWordLength = 3;
	private Boolean useAllWords = false;
	private Boolean splitTags = false;
	private List<String> postReplace = new ArrayList<>();
	private List<String> postRemove = new ArrayList<>();

	public void updateImportantWords(Map<String, Double> vocabPost) {
		importantWords = help.getImportantTags(vocabPost, postFilter);
	}

	public void updateSalvageWords()
	{
	}

	public void computeSalvaging(Map<String, Double> vocabPost) {
		// Find important words in the unimportant tags
		regex.findImportantWords(vocabPost, importantWords, salvagedData, postFilter, minWordLength, postRemove, postReplace);
	}
	
	public void applySalvaging(List<Tag> tags) {
		regex.apply(tags, importantWords, salvagedData, useAllWords, postRemove, postReplace);
		
		if (splitTags) {
			help.splitCompositeTag(tags);
		}

		help.correctTags(tags);

		//help.removeDashes(tags, index);
	}

	public List<gridVocab> prepareImportantWords() {
		List<gridVocab> tags_filtered = new ArrayList<>();

		for (String s : importantWords) {
			tags_filtered.add(new gridVocab(s, 0));
		}

		return tags_filtered;
	}
	
	public List<gridRepl> prepareSalvagedData() {
		List<gridRepl> tags_filtered = new ArrayList<>();

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

	public List<String> getPostReplace() {
		return postReplace;
	}

	public List<String> getPostRemove() {
		return postRemove;
	}

	public void setPostReplace(List<Map<String, Object>> map) {
		postReplace.clear();

		for(String s: map.get(0).keySet())
		{
			postReplace.add(s+","+map.get(0).get(s));
		}
	}

	public void setPostRemove(List<Map<String, Object>> map) {
		postRemove.clear();

		for(String s: map.get(0).keySet())
		{
			postRemove.add(s);
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
