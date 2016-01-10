package processing;


import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import core.tags.Tag;

public class Similarity {
	
    PlainStringSimilarity psim = new PlainStringSimilarity();
	
    Map<String, Double> tag_words = new HashMap<String, Double>();
    Map<String, HashSet<String>> tag_2grams = new HashMap<String, HashSet<String>>();
    Map<String, Set<String>> phonetic_groups = new HashMap<String, Set<String>>();
    Map<String, String> substitution_list = new HashMap<String, String>();
	
    Map<String, Double> sortedVocab = new HashMap<String, Double>();
    
    public void withVocab(List<? extends Tag> tags, Map<String, Double> vocab, List<String> whiteList, int minWordSize, Map<String, Map<String, Double>> clusters)
	{
	    /////////////////////////////////
	    // Variables
	    int ngram_size;
	    String high;
	    

		// Set the size for the n-gram distance method
		ngram_size = 2;
		
		/////////////////////////////////
		// Algorithm		
		
	    // Filter vocabs and create tag/2-character-gram list
	    for(String s: vocab.keySet())
	    {
			// Only use words with more than 2 characters and at least one none numeric character
			if(s.length()>=minWordSize && !s.matches("^\\d+$") && !s.matches("^\\d+s$"))
			{
		    	
				sortedVocab.put(s, vocab.get(s));
				
				// Compute the 2-gram for all words
				tag_2grams.put(s, psim.create_n_gram(s, ngram_size));
			}
	    }
	    
	    // Sort the vocab by importance
	    sortedVocab = sortByComparator(sortedVocab);
	    
	    //	Debug stuff
	    int psize = sortedVocab.size();
	    int part = psize/30;
	    int iter = 0;
	    
        // Find white listed words and prioritize them in the similarity computation
		for(String s: whiteList)
		{
			if(sortedVocab.containsKey(s))
			{
		    	  // Debug
		    	  iter++;
		    	  if(iter%part == 0)
		    	  {
		    		  System.out.println(iter/part+"/30");
		    	  }
				
				// Remove correct word from list
				sortedVocab.remove(s);
		  
				// Find similar words and save them to the substitution list
		        findCluster(sortedVocab, s, clusters);
			}
		}
	    
	    // Iterate over the rest of the sorted vocab
	    //for(String tag: sortedVocab.keySet())
		for(Iterator<Entry<String, Double>> iterator = sortedVocab.entrySet().iterator(); iterator.hasNext();)
	      {
			high = iterator.next().getKey();
			
	    	  // Debug
	    	  iter++;
	    	  if(iter%part == 0)
	    	  {
	    		  System.out.println(iter/part+"/30");
	    	  }
	          
	          // Remove the most important word from the list
	          // This word is treated as truth
	          iterator.remove();
	          
	          // Find similar words and save them to the substitution list
	          findCluster(sortedVocab, high, clusters);
		}   
	}
	
    public void applyClusters(List<? extends Tag> tags, double threshold)
    {
	    List<String> words;
	    String  new_tag;
	    HashSet<String> h1, h2, h3;
	    double similarity, similarity2;
    	
	    /*
	    if(!substitution_list.containsKey(word))
        {
            similarity = 0;
            
            h1 = tag_2grams.get(word);
            
            // Choose distance methods
            similarity = psim.jaccard_index(h1, h2);
            
            // Add each similar word the the cluster
            if(similarity > 0)
            {
          	  cluster.put(word, similarity);
            }
            
            // Check if the ngram method gives a similarity > threshold
            if(similarity > threshold)
            {
              substitution_list.put(word, high);
            }  
        }
        else
        {
      	  similarity = 0;
      	  similarity2 = 0;
            
            h1 = tag_2grams.get(word);
            h3 = tag_2grams.get(substitution_list.get(word));
            
            similarity = psim.jaccard_index(h1, h2);
            similarity2 = psim.jaccard_index(h1, h3);
	              
            // Add each similar word the the cluster
            if(similarity > 0)
            {
          	  cluster.put(word, similarity);
            }
            
      	  // Replace old substitution if the new one is better
            if(similarity >= similarity2)
            {          
          	  System.out.println(word+":"+similarity2+"->"+similarity);
          	  
	              substitution_list.put(word, high);
            }
         }
	    */
	    
    	// Replace tags corresponding to the substitution map
	    for(Tag t: tags)
	    {
	      words = psim.create_word_gram(t.getTagName());
	      new_tag = "";
	    	
	      for(String w: words)
	      {	    	  
	    	  if(substitution_list.containsKey(w))
	    	  {
	    		  new_tag = new_tag + " " + substitution_list.get(w);
	    	  }
	    	  else
	    	  {
	    		  new_tag = new_tag + " " + w;
	    	  }
	        
	      }

	      t.setTagName(new_tag.trim());
	    }
    }
    
	private void findCluster(Map<String, Double> sortedVocab, String high, Map<String, Map<String, Double>> clusters)
	{
	    HashSet<String> h1, h2;
	    double similarity;
	    Map<String, Double> cluster = new HashMap<String, Double>();
		
        // Compute 2-gram character set of the most important word
        h2 = tag_2grams.get(high);
	    
	      // Iterate over all less important words
	      for(String word: sortedVocab.keySet())
	      {
              similarity = 0;
              
              h1 = tag_2grams.get(word);
              
              // Compute distance
              similarity = psim.jaccard_index(h1, h2);
              
              // Add each word with a similarity bigger than 0 to the cluster
              if(similarity > 0)
              {
            	  cluster.put(word, similarity);
              }
	      }
	      
	      // Add cluster to the total list
	      clusters.put(high, cluster);
	}
	
	private static Map<String, Double> sortByComparator(Map<String, Double> unsortMap) {

		// Convert Map to List
		List<Map.Entry<String, Double>> list = 
			new LinkedList<Map.Entry<String, Double>>(unsortMap.entrySet());

		// Sort list with comparator, to compare the Map values
		Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
			public int compare(Map.Entry<String, Double> o1,
                                           Map.Entry<String, Double> o2) {
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});

		// Convert sorted map back to a Map
		Map<String, Double> sortedMap = new LinkedHashMap<String, Double>();
		for (Iterator<Map.Entry<String, Double>> it = list.iterator(); it.hasNext();) {
			Map.Entry<String, Double> entry = it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}
}
