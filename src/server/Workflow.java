package server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import processing.Composite;
import processing.Helper;
import processing.Postprocess;
import processing.Preprocess;
import processing.Spellcorrect;
import processing.Weighting;
import core.ImportCSV;
import core.Tag;
import core.json.gridOverview;

public class Workflow {
	
	// Initialize variables and classes
	private static final Logger log = Logger.getLogger("Logger");
  	private Helper help = new Helper();
    private ImportCSV im = new ImportCSV();
    private Weighting weighting = new Weighting();

    
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Parameters
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    // Data
    private List<Tag> tags; 
    private Map<String, Double> vocabPre = new HashMap<String, Double>();
    private Map<String, Double> vocabPost = new HashMap<String, Double>();
    
    // Initialize pipeline steps
    // The index selects the working copy. 0 = original
    private Preprocess preprocess = new Preprocess(1);
    private Spellcorrect spellcorrect = new Spellcorrect(2);
    private Composite composite = new Composite(3);
	private Postprocess postprocess = new Postprocess(4);

    
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Load data - Dataset 0
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
	public void init()
	{
		log.info("Initialize\n");
		
	    // Load data
	    tags = im.importTags("reduced_music.csv");

	    log.info("Data loaded\n");
	}
	
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Preprocessing - Dataset 1
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public void computePreprocessing()
	{
		help.resetStep(tags, 1);
		
		// Set to lower case
		preprocess.setToLowerCase(tags);
		
		// Remove characters
		preprocess.removeCharacters(tags);
		
		// Replace characters
		preprocess.replaceCharacters(tags);
		
		// Compute word frequency
		preprocess.computeWordFreq(tags);
		
		// Update tags
		preprocess.applyFilter(tags);
		
		// Create preFilter vocab
		weighting.vocab(tags, vocabPre, 1);
		
		// Cluster words for further use
		spellcorrect.clustering(tags, vocabPre, preprocess.getWhitelistWords());
	}
	
	// Apply changes
	public void applyPreFilter(double threshold)
	{
		// Set threshold
		preprocess.setFilter(threshold);
		
		// Apply
		computePreprocessing();
	}
	
	public void applyPreRemove(String chars)
	{
		// Set characters for removal
		preprocess.setRemove(chars);
		
		// Apply
		computePreprocessing();
	}
	
	public void applyPreReplace(String json)
	{
		List<Map<String, Object>> map = help.jsonStringToList(json);
		
		preprocess.setReplace(map);
		
		// Apply
		computePreprocessing();
	}
	
	public void applyPreDictionary(String json)
	{
		List<Map<String, Object>> map = help.jsonStringToList(json);
		
		preprocess.setDictionary(map);
		
		// Apply
		computePreprocessing();
	}
	
	// Send Params
	public double sendPreFilterParams()
	{
		return preprocess.getFilter();
	}
	
	public String sendPreRemoveParams()
	{
		return preprocess.getRemove();
	}
	
	public List<String> sendPreReplaceParams()
	{
		return preprocess.getReplace();
	}
	
	public List<String> sendPreDictionaryParams()
	{
		return preprocess.getDictionary();
	}
	
	// Send Data
	public String sendPreFilter()
	{
	    return help.objectToJsonString(preprocess.preparePreFilter());
	}
	
	public String sendPreFilterHistogram()
	{
	    return help.objectToJsonString(preprocess.preparePreFilterHistogram());
	}
	
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Spell Correction - Dataset 2
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public void computeSpellCorrect()
	{
		// Reset current stage
		help.resetStep(tags, 2);
		
		// Apply clustering
		spellcorrect.applyClustering(tags);
		
		// Compute further data
		composite.group(tags);
	}
	
	// Apply changes
	public void applySpellImportance(double threshold)
	{
		// Set threshold
		spellcorrect.setSpellImportance(threshold);
		
		// Apply
		computeSpellCorrect();
	}
	
	public void applySpellSimilarity(double threshold)
	{
		// Set threshold
		spellcorrect.setSpellSimilarity(threshold);
		
		// Apply
		computeSpellCorrect();
	}
	
	// Send Params
	public double sendSpellImportanceParams()
	{
		return spellcorrect.getSpellImportance();
	}
	
	public double sendSpellSimilarityParams()
	{
		return spellcorrect.getSpellSimilarity();
	}
	
	// Send Data
	public String sendCluster(String tag)
	{
	    return help.objectToJsonString(spellcorrect.prepareCluster(tag));
	}
	
	public String sendSimilarityHistogram()
	{
	    return help.objectToJsonString(spellcorrect.prepareSimilarityHistogram());
	}
	
	public String sendReplacements(double threshold)
	{
	    return help.objectToJsonString(spellcorrect.prepareReplacements(threshold));
	}
	
	public String sendVocab()
	{
	    return help.objectToJsonString(help.prepareVocab(vocabPre));
	}
	
	public String sendPreVocabHistogram()
	{
	    return help.objectToJsonString(help.prepareVocabHistogram(vocabPre));
	}
	
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Composites - Dataset 3
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public void computeGroups()
	{
		help.resetStep(tags, 3);
		
		// Compute all word groups
	    composite.applyGroups(tags);
	    
	    // Provide further data
	    weighting.vocab(tags, vocabPost, 3);
	}
	
	// Apply changes
	public void applyCompositeFrequent(double threshold)
	{
		// Set threshold
		composite.setFrequentThreshold(threshold);
		
		// Apply
		computeGroups();
	}
	
	public void applyCompositeUnique(double threshold)
	{
		// Set threshold
		composite.setJaccardThreshold(threshold);
		
		// Apply
		computeGroups();
	}
	
	public void applyCompositeSize(int maxGroupSize)
	{
	    // Set max group size
	    composite.setMaxGroupSize(maxGroupSize);
	    
		// Apply
		computeGroups();
	}
	
	public void applyCompositeSplit(Boolean split)
	{
	    // Set max group size
	    composite.setSplit(split);
	    
		// Apply
		computeGroups();
	}

	// Send Params
	public double sendCompFrequentParams()
	{
		return composite.getFrequentThreshold();
	}
	
	public double sendCompUniqueParams()
	{
		return composite.getJaccardThreshold();
	}
	
	public int sendCompSizeParams()
	{
		return composite.getMaxGroupSize();
	}
	
	public Boolean sendCompSplitParams()
	{
		return composite.getSplit();
	}

	// Send Data
	public String sendFrequentGroups()
	{
		return help.objectToJsonString(composite.prepareFrequentGroups());
	}
	
	public String sendUniqueGroups()
	{
		return help.objectToJsonString(composite.prepareUniqueGroups());
	}
	
	public String sendFrequentHistogram()
	{
		return help.objectToJsonString(composite.prepareFrequentHistogram());
	}
	
	public String sendUniqueHistogram()
	{
		return help.objectToJsonString(composite.prepareUniqueHistogram());
	}

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Postprocessing - Dataset 4
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public void prepareSalvaging()
	{
		postprocess.initializeSalvaging(vocabPost);
	}
	
	public void computeSalvaging()
	{
		postprocess.computeSalvaging(vocabPost);
	}
	
	// Apply changes
	public void applySalvaging()
	{
		help.resetStep(tags, 4);
		
		postprocess.applySalvaging(tags);
	}
	
	public void applyPostFilter(double threshold)
	{
		// Set threshold
		postprocess.setPostFilter(threshold);
		
		// Apply
		prepareSalvaging();
	}
	
	public void applyPostReplace(String json)
	{
		List<Map<String, Object>> map = help.jsonStringToList(json);
		
		postprocess.setPostReplace(map);
		
		// Apply
		prepareSalvaging();
	}
	
	public void applyPostLength(int minLength)
	{
		// Set threshold
		postprocess.setMinWordLength(minLength);
		
		// Apply
		prepareSalvaging();
	}
	
	public void applyPostAll(Boolean all)
	{
		// Set threshold
		postprocess.setUseAllWords(all);
		
		// Apply
		prepareSalvaging();
	}
	
	// Send Params
	public double sendPostFilterParams()
	{
		return postprocess.getPostFilter();
	}
	
	public int sendPostLengthParams()
	{
		return postprocess.getMinWordLength();
	}
	
	public Boolean sendPostAllParams()
	{
		return postprocess.getUseAllWords();
	}
	
	public List<String> sendPostReplaceParams()
	{
		return postprocess.getPostReplace();
	}
	
	// Send Data
	public String sendPostVocab()
	{
	    return help.objectToJsonString(help.prepareVocab(vocabPost));
	}
	
	public String sendPostVocabHistogram()
	{
	    return help.objectToJsonString(help.prepareVocabHistogram(vocabPost));
	}
	
	public String sendPostImportant()
	{
	    return help.objectToJsonString(postprocess.prepareImportantWords());
	}
	
	public String sendPostSalvage()
	{
	    return help.objectToJsonString(postprocess.prepareSalvageWords());
	}

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Finalize
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public String sendFinal()
	{
		Supplier<List<gridOverview>> supplier = () -> new ArrayList<gridOverview>();

	    List<gridOverview> tags_filtered = tags.stream()
	    		.map(p -> new gridOverview(p.getTag(4), p.getItem(), p.getImportance()))
	    		.collect(Collectors.toCollection(supplier));
	    
	    return help.objectToJsonString(tags_filtered); 
	}

	
	/*
	public String sendHistory(String data) {
		
	    Supplier<List<gridHistory>> supplier = () -> new ArrayList<gridHistory>();
	    List<Integer> ids = new ArrayList<Integer>();
	    
	    if(data.length()>0)
	    {
		    String[] temp = data.split(",");
		    
		    for(String s: temp)
		    {
		    	ids.add(Integer.parseInt(s));
		    }
	    }
	    
	    List<gridHistory> tags_filtered = tags.stream()
	    		.filter(p -> ids.contains(p.getID()) )
	    		.map(p -> new gridHistory(p.getHistory()))
	    		.collect(Collectors.toCollection(supplier));
	    
	    return help.objectToJsonString(tags_filtered);
	}
	*/

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Main
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public String sendOverview(int index)
	{
		Supplier<List<gridOverview>> supplier = () -> new ArrayList<gridOverview>();

	    List<gridOverview> tags_filtered = tags.stream()
	    		.map(p -> new gridOverview(p.getTag(index), p.getItem(), p.getImportance()))
	    		.collect(Collectors.toCollection(supplier));
	    
	    return help.objectToJsonString(tags_filtered); 
	}
}
