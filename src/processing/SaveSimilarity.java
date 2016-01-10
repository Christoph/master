package processing;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.language.DoubleMetaphone;

import core.tags.Tag;

public class SaveSimilarity {
	
    PlainStringSimilarity psim = new PlainStringSimilarity();
	
    Map<String, Double> tag_words = new HashMap<String, Double>();
    Map<String, HashSet<String>> tag_2grams = new HashMap<String, HashSet<String>>();
    Map<String, Set<String>> phonetic_groups = new HashMap<String, Set<String>>();
    Map<String, String> substitution_list = new HashMap<String, String>();
	
    public void withVocab(List<? extends Tag> tags, Map<String, Double> vocab, float threshold, List<String> whiteList, int minWordSize, Map<String, Map<String, Double>> clusters)
	{
	    /////////////////////////////////
	    // Variables
	    
        Set<String> word_group = new HashSet<String>();
        
	    List<String> words;
	    Set<String> temp;
	    double importance;
	    int ngram_size;
	    String phonetic, new_tag;
	    
		/////////////////////////////////
		// Configuration
        
		// Choose phonetic algorithm
		DoubleMetaphone phonetic_algorithm = new DoubleMetaphone();
		//ColognePhonetic phonetic = new ColognePhonetic();
		
		// Set the size for the n-gram distance method
		ngram_size = 2;
		
		/////////////////////////////////
		// Algorithm		
		
	    // Filter words and create tag/2-character-gram list
	    for(String s: vocab.keySet())
	    {
			// Only use words with more than 2 characters and at least one none numeric character
			if(s.length()>=minWordSize && !s.matches("^\\d+$") && !s.matches("^\\d+s$"))
			{
		    	
				tag_words.put(s, vocab.get(s));
				
				// Compute the 2-gram for all words
				tag_2grams.put(s, psim.create_n_gram(s, ngram_size));
			}
	    }
	    
	    // Create phonetic dictionary
	    for(String k: tag_words.keySet())
	    {
	    	phonetic = phonetic_algorithm.encode(k);
	    	
	    	if(phonetic_groups.containsKey(phonetic))
	    	{
	    		temp = phonetic_groups.get(phonetic);
	    		temp.add(k);
	    		
	    		phonetic_groups.put(phonetic, temp);
	    		
	    	}
	    	else
	    	{
	    		temp = new HashSet<String>();
	    		
	    		temp.add(k);
	    		
	    		phonetic_groups.put(phonetic, temp);
	    	}
	    }
	    
	    //	Debug stuff
	    int psize = phonetic_groups.size();
	    int part = psize/30;
	    int iter = 0;
	    
	    // Iterate over all phonetic codes
	      for(String phon: phonetic_groups.keySet())
	      {
	    	  // Debug
	    	  iter++;
	    	  if(iter%part == 0)
	    	  {
	    		  System.out.println(iter/part+"/30");
	    	  }
	    	  
	    	  // Reset temp variables
	          word_group.clear();
	          String high = "";
	    	
	          // Get all words with the same phonetic code
	          word_group.addAll(phonetic_groups.get(phon));
	          
	          // Find white listed words and prioritize them in the similarity computation
		      for(String s: whiteList)
		      {
		    	  if(word_group.contains(s))
		    	  {
		    		  // Remove correct word from list
	        		  word_group.remove(s);
	        		  
	        		  // Find similar words and save them to the substitution list
			          findSimilarities(word_group, s, threshold, clusters);
		    	  }
		      }
	          
	          	// Go over all remaining words
		  		while(!word_group.isEmpty())
		        {
		          importance = 0;
		          
		          // Find the word with the highest importance count
		          for(String s: word_group)
		          {
		        	if(tag_words.get(s) >= importance)
		            {
		              high = s;
		              importance = tag_words.get(s);
		            }
		          }
		          
		          // Remove the most important word from the list
		          // This word is treated as truth
		          word_group.remove(high);
		          
		          // Find similar words and save them to the substitution list
		          findSimilarities(word_group, high, threshold, clusters);
		        }
		}   
	    
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
	
	private void findSimilarities(Set<String> word_group, String high, float threshold, Map<String, Map<String, Double>> clusters)
	{
	    HashSet<String> h1, h2, h3;
	    double similarity, similarity2;
	    Map<String, Double> cluster = new HashMap<String, Double>();
		
        // Compute 2-gram character set of the most important word
        h2 = tag_2grams.get(high);
	    
	      // Iterate over the phonetic similar words and find similar words with distance methods
	      for(Iterator<String> iterator = word_group.iterator(); iterator.hasNext();)
	      {
	              String word = iterator.next();
	              
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
		                iterator.remove();
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
				          iterator.remove();
		              }
		           }
	      	}
	      
	      // Add cluster to the total list
	      clusters.put(high, cluster);
	}
	
}
