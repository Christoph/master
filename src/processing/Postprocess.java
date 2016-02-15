package processing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import core.Tag;
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
	    
	  	// Parameters
		private double postFilter;
		private int minWordLength;
		private Boolean useAllWords;
		private List<String> postReplace = new ArrayList<String>();
	
	public Postprocess(int index) {
		this.index = index;
		
		// Default parameter
		minWordLength = 4;
		postFilter = 0.5;
		useAllWords = false;
	}
	
	public void initializeSalvaging(Map<String, Double> vocabPost)
	{
	    // Get important words
		importantWords = help.getImportantTags(vocabPost, postFilter);
		
		// Get salvage words
		//salvageWords = regex.replaceCustomWords(importantWords, postReplace, index);
	}
	
	public void computeSalvaging(Map<String, Double> vocabPost)
	{
	    // Find important words in the unimportant tags
	    //regex.findImportantWords(tags, salvageWords, postFilter, minWordLength, useAllWords, index);
	}
	
	public void applySalvaging(List<Tag> tags)
	{
		//regex.apply(tags);
		
		//help.splitCompositeTag(tags, index);
		//help.correctTags(tags);
		
	    //help.removeDashes(tags, index);
	}

	public List<gridVocab> prepareImportantWords()
	{
	    List<gridVocab> tags_filtered = new ArrayList<gridVocab>();
	    
	    for(String s: importantWords)
	    {
	    	tags_filtered.add(new gridVocab(s, 0));
	    }

	    return tags_filtered;
	}
	
	public List<gridVocab> prepareSalvageWords()
	{
	    List<gridVocab> tags_filtered = new ArrayList<gridVocab>();
	    
	    for(String s: salvageWords)
	    {
	    	tags_filtered.add(new gridVocab(s, 0));
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
		
		for(int i = 0; i < map.size(); i++)
		{
			postReplace.add(map.get(i).get("replace")+","+map.get(i).get("by"));
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

}
