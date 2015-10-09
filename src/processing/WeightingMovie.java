package processing;

import java.util.List;
import core.TagMovie;

public class WeightingMovie extends Weighting {

	public void importance(List<TagMovie> tags, String prefix, Boolean verbose)
	{
	    long audienceScore, criticScore, tagWeight;
	    double importance;
		
	    // Compute a weighted normalized weight for each tag/song pair
	    for(TagMovie t: tags)
	    {
	    	audienceScore = t.getRtAudienceScore();
	    	criticScore = t.getRtCriticScore();
	    	tagWeight = t.getTagWeight();
	    		    	
	    	importance = tagWeight*(audienceScore+criticScore);
	    	
	    	t.setImportance(importance); 
	    	
	    }
	    
	    byWeightedMean(tags, prefix, verbose);
	}
}
