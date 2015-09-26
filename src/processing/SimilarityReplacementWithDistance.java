package processing;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.language.DoubleMetaphone;

import core.Tag;
import core.TagsToCSV;

public class SimilarityReplacementWithDistance {
	
	DamerauLevenshteinAlgorithm dla = new DamerauLevenshteinAlgorithm(1, 1, 1, 1);
	
	public void withPhoneticsAndNgrams(List<Tag> tags, List<String> blacklist, float threshold, String filename_suffix)
	{
	    /////////////////////////////////
	    // Variables
	    PlainStringSimilarity psim = new PlainStringSimilarity();
	    Helper helper = new Helper();
	    TagsToCSV writer_subs;
	    TagsToCSV writer_subs_count;
		
	    Map<String, Double> tag_words = new HashMap<String, Double>();
	    Map<String, Set<String>> phonetic_groups = new HashMap<String, Set<String>>();
	    Map<String, String> substitution_list = new HashMap<String, String>();
	    
	    List<String> words;
	    Set<String> temp;
	    double value, importance;
	    int ngram_size;
	    float similarity;
	    String key, phonetic, new_tag;
	    Boolean print_substitutions;
	    HashSet<String> h1, h2;
	    
		/////////////////////////////////
		// Configuration
        
		// Choose phonetic algorithm
		DoubleMetaphone phonetic_algorithm = new DoubleMetaphone();
		//ColognePhonetic phonetic = new ColognePhonetic();
		
		// Print substitution list
		print_substitutions = true;
		
		// Set the size for the n-gram distance method
		ngram_size = 2;
		
		// Choose distance metric on line 143
		
		/////////////////////////////////
		// Algorithm		
	    
	    // Create a 1-word-gram/max weight dict
	    for(int i = 0;i < tags.size(); i++)
	    {
	    	words = psim.create_word_gram(tags.get(i).getTagName(),blacklist);
	    	
	    	for(int j = 0; j < words.size(); j++)
	    	{
	    		key = words.get(j); 		
	    		
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
	    
	    // Create word set/count dict  
	    for(int i = 0;i < tags.size(); i++)
	    {
	      words = psim.create_word_gram(tags.get(i).getTagName(),blacklist);
	    	
	      for(int j = 0; j < words.size(); j++)
	      {
	    	String tag = words.get(j);
	    	
	    	// Check if the substitution already exists
	        if(!substitution_list.containsKey(tag))
	        {
	          // Get all words with the same phonetic code
	          String code = phonetic_algorithm.encode(tag);
	          String high = "";
	          
	          // Get all phonetics with a edit distance of 1
	          Set<String> group = new HashSet<String>();
	          
	          for(String p: phonetic_groups.keySet())
	          {
	        	  if(dla.execute(p, code)<2)
	        	  {
	        		  group.add(p);
	        	  }
	          }
	          
	          Set<String> phonetics = new HashSet<String>();
	          
	          for(String c: group)
	          {
	        	  phonetics.addAll(phonetic_groups.get(c));
	          }
	          
	          // Iterate over those
	          while(!phonetics.isEmpty())
	          {
	            importance = 0;
	            
	            // Find the word with the highest importance count
	            for(String s: phonetics)
	            {
	              if(tag_words.get(s) >= importance)
	              {
	                high = s;
	                importance = tag_words.get(s);
	              }
	            }
	            
	            // Iterate over the phonetic similar words and find similar words with distance methods
	            for(Iterator<String> iterator = phonetics.iterator(); iterator.hasNext();)
	            {
	              String word = iterator.next();
	              similarity = 0;
	              
	              h1 = psim.create_n_gram(word, ngram_size);
	              h2 = psim.create_n_gram(high, ngram_size);
	              
	              // Choose distance methods
	              //similarity = psim.dice_coeffizient(h1, h2);
	              similarity = psim.jaccard_index(h1, h2);
	              //similarity = psim.cosine_similarity(h1, h2);
	              
	              // Check if the ngram method gives a similarity > threshold
	              if(similarity > threshold)
	              {
	                substitution_list.put(word, high);
	                iterator.remove();
	              }  
	            }
	          }
	        }   
	      }
	    }
	    
	    tag_words = null;
	    phonetic_groups = null;
	    
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
	      words = psim.create_word_gram(t.getTagName(),blacklist);
	      new_tag = "";
	    	
	      for(String w: words)
	      {
	        new_tag = new_tag + " " + substitution_list.get(w);
	      }

	      t.setTagName(new_tag.trim());
	    }
	    
	    helper.removeTagsWithoutWords(tags);
	    
	    substitution_list = null;
	}
	
}
