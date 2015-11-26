package processing;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.language.DoubleMetaphone;

import core.tags.Tag;
import core.tags.TagsToCSV;

public class Similarity {
	
    PlainStringSimilarity psim = new PlainStringSimilarity();
	
    Map<String, Double> tag_words = new HashMap<String, Double>();
    Map<String, HashSet<String>> tag_2grams = new HashMap<String, HashSet<String>>();
    Map<String, Set<String>> phonetic_groups = new HashMap<String, Set<String>>();
    Map<String, String> substitution_list = new HashMap<String, String>();
	
	public void withPhoneticsAndNgrams(List<? extends Tag> tags, float threshold, String filename_suffix, List<String> whiteList, int minWordSize, Boolean verbose)
	{
	    /////////////////////////////////
	    // Variables

	    TagsToCSV writer_subs;
	    TagsToCSV writer_subs_count;
	    
        Set<String> word_group = new HashSet<String>();
        
	    List<String> words;
	    Set<String> temp;
	    double value, importance;
	    int ngram_size;
	    String key, phonetic, new_tag;
	    Boolean print_substitutions;
	    
		/////////////////////////////////
		// Configuration
        
		// Choose phonetic algorithm
		DoubleMetaphone phonetic_algorithm = new DoubleMetaphone();
		//ColognePhonetic phonetic = new ColognePhonetic();
		
		// Print substitution list
		print_substitutions = verbose;
		
		// Set the size for the n-gram distance method
		ngram_size = 2;
		
		/////////////////////////////////
		// Algorithm		
		
	    // Create a 1-word-gram/max weight dict and a 1-word-gram/2-character-gram list
	    for(int i = 0;i < tags.size(); i++)
	    {
	    	words = psim.create_word_gram(tags.get(i).getTagName());
	    	
	    	for(int j = 0; j < words.size(); j++)
	    	{
	    		key = words.get(j); 		
	    		
	    		// Only use words with more than 2 characters and at least one none numeric character
	    		// TODO: Think about this filter	 
	    		if(key.length()>=minWordSize && !key.matches("^\\d+$") && !key.matches("^\\d+s$"))
	    		{
		    		if(tag_words.containsKey(key))
		    		{
		    			value = tag_words.get(key);
		    			
		    			// Find max
		    			if(value < tags.get(i).getImportance())
		    			{
		    				tag_words.put(key, tags.get(i).getImportance());
		    			}
		    		}
		    		else
		    		{
		    			value = tags.get(i).getImportance();
		    			
		    			tag_words.put(key, value);
		    			
		    			// Compute the 2-gram for all words
		    			tag_2grams.put(key, psim.create_n_gram(key, ngram_size));
		    		}
	    		}
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
	    
	    System.out.println(psize);
	    
	    // Iterate over all phonetic codes
	      for(String phon: phonetic_groups.keySet())
	      {
	    	  iter++;
	    	  if(iter%part == 0)
	    	  {
	    		  System.out.print("->"+iter);
	    	  }
	    	  
	          word_group.clear();
	    	
	          // Get all words with the same phonetic code
	          String high = "";
	          
	          word_group.addAll(phonetic_groups.get(phon));
	          
	          // Find white listed words and prioritize them
		      for(String s: whiteList)
		      {
		    	  if(word_group.contains(s))
		    	  {
	        		  word_group.remove(s);
	        		  
			          findSimilarities(word_group, s, threshold);
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
		          
		          findSimilarities(word_group, high, threshold);
		        }
		}   
	    
	    // Export substitution list
	    if(print_substitutions)
	    {
	    	Map<String, String> out = new HashMap<String, String>();
	    	Map<String, Long> count = new HashMap<String, Long>();
	 
	    	for(String s: substitution_list.keySet())
	    	{
	    		String str = substitution_list.get(s);
	    		if(!s.equals(str))
	    		{		
	    			out.put(s,substitution_list.get(s));
	    		}
	    	}
	    	
	    	for(String s: out.keySet())
	    	{	    		
	  	      key = out.get(s);

		      if(count.containsKey(key))
		      {
		    	  count.put(key,count.get(key)+1);
		      }
		      else
		      {
		    	  count.put(key,1l);
		      }
	    	}
	    	
	    	writer_subs = new TagsToCSV("subs_"+filename_suffix+".csv");
	    	writer_subs.writeSubs(out);
	    	
	    	writer_subs_count = new TagsToCSV("subs_count_"+filename_suffix+".csv");
	    	writer_subs_count.writeTagOccu(count);
	    }
	    
	    // Replace tags corresponding to the subs dict
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
	
	private void findSimilarities(Set<String> word_group, String high, float threshold)
	{
	    HashSet<String> h1, h2, h3;
	    float similarity, similarity2;
		
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
			              
	            	  // Replace old substitution if the new one is better
		              if(similarity >= similarity2)
		              {          
		            	  System.out.println(word+":"+similarity2+"->"+similarity);
		            	  
			              substitution_list.put(word, high);
				          iterator.remove();
		              }
		           }
	      	}
	}
	
}
