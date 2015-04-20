package processing;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.language.DoubleMetaphone;

import tags.Tag;
import tags.TagsToCSV;

public class SpellChecking {
	
	public void withPhoneticsAndNgrams(List<Tag> tags, List<String> blacklist)
	{
	    /////////////////////////////////
	    // Variables
	    PlainStringSimilarity psim = new PlainStringSimilarity();
	    Helper helper = new Helper();
	    TagsToCSV writer_subs;
		
	    Map<String, Integer> tag_words = new HashMap<String, Integer>();
	    Map<String, Set<String>> phonetic_groups = new HashMap<String, Set<String>>();
	    Map<String, String> substitution_list = new HashMap<String, String>();
	    
	    List<String> words;
	    Set<String> temp;
	    int value, listener_count, ngram_size;
	    double similarity;
	    float threshold;
	    String key, phonetic, new_tag;
	    Boolean print_substitutions;
      HashSet<String> h1, h2;
	    
		/////////////////////////////////
		// Configuration
        
		// Choose phonetic algorithm
		DoubleMetaphone phonetic_algorithm = new DoubleMetaphone();
		//ColognePhonetic phonetic = new ColognePhonetic();
		
		// Print substitution list
		print_substitutions = false;
		
		// Set the size for the n-gram distance method
		ngram_size = 2;
		
		// Acceptance threshold
		threshold = 0.7f;
		
		// Choose distance metric on line 143
		
		/////////////////////////////////
		// Algorithm		
	    
	    // Create a 1-word-gram/total listeners dict
	    for(int i = 0;i < tags.size(); i++)
	    {
	    	words = psim.create_word_gram(tags.get(i).getTagName(),blacklist);
	    	
	    	for(int j = 0; j < words.size(); j++)
	    	{
	    		key = words.get(j); 		

	    		if(tag_words.containsKey(key))
	    		{
	    			value = tag_words.get(key);
	    			
	    			// Sum up the count
	    			tag_words.put(key, value + tags.get(i).getListeners());
	    		}
	    		else
	    		{
	    			tag_words.put(key, tags.get(i).getListeners());
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
	          
	          Set<String> phonetics = phonetic_groups.get(code);
	          
	          // Iterate over those
	          while(!phonetics.isEmpty())
	          {
	            listener_count = 0;
	            
	            // Find the word with the highest listener count
	            for(String s: phonetics)
	            {
	              if(tag_words.get(s) > listener_count)
	              {
	                high = s;
	                listener_count = tag_words.get(s);
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
	              
	              // Check if the ngram method gives a similarity > 70%
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

	    // Export substitution list
	    if(print_substitutions)
	    {
	    	writer_subs = new TagsToCSV("subs.csv");
	    	writer_subs.writeSubs(substitution_list);
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
	    
	    tag_words = null;
	    phonetic_groups = null;
	    substitution_list = null;
	}
	
}
