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
	
	public void check(List<Tag> tags, List<String> blacklist)
	{
	    /////////////////////////////////
	    // Variables
	    PlainStringSimilarity psim = new PlainStringSimilarity();
		TagsToCSV writer_subs;
		
	    Map<String, Integer> wordgrams = new HashMap<String, Integer>();
	    Map<String, Set<String>> phon = new HashMap<String, Set<String>>();
	    Map<String, String> subs = new HashMap<String, String>();
	    
	    List<String> words;
	    int value, count, ngram_size;
	    double dist;
	    String key, p, new_tag;
	    Boolean export_substitutions;
        HashSet<String> h1, h2;
	    
		/////////////////////////////////
		// Configuration
		
		// Choose phonetic algorithm
		DoubleMetaphone phonetic = new DoubleMetaphone();
		//ColognePhonetic phonetic = new ColognePhonetic();
		
		// Set output of substitution list
		export_substitutions = false;
		
		// Set the size for the n-gram distance method
		ngram_size = 2;
		
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

	    		if(wordgrams.containsKey(key))
	    		{
	    			value = wordgrams.get(key);
	    			
	    			// Sum up the count
	    			wordgrams.put(key, value + tags.get(i).getListeners());
	    		}
	    		else
	    		{
	    			wordgrams.put(key, tags.get(i).getListeners());
	    		}
	    	}
	    }
	    
	    // Create phonetic dictionary
	    for(String k: wordgrams.keySet())
	    {
	    	
	    	//p = dmeta.encode(k);
	    	
	    	p = phonetic.encode(k);
	    	
	    	if(phon.containsKey(p))
	    	{
	    		Set<String> temp;
	    		
	    		temp = phon.get(p);
	    		temp.add(k);
	    		
	    		phon.put(p, temp);
	    		
	    	}
	    	else
	    	{
	    		Set<String> temp = new HashSet<String>();
	    		
	    		temp.add(k);
	    		
	    		phon.put(p, temp);
	    	}
	    }
	    
	    // Create word set/count dict  
	    for(int i = 0;i < tags.size(); i++)
	    {
	    	words = psim.create_word_gram(tags.get(i).getTagName(),blacklist);
	    	
	    	for(int j = 0; j < words.size(); j++)
	    	{
	    		String tag = words.get(j);

	        if(!subs.containsKey(tag))
	        {
	          String code = phonetic.encode(tag);
	          String high = "";
	          
	          Set<String> phonetics = phon.get(code);
	          
	          while(!phonetics.isEmpty())
	          {
	            count = 0;

	            for(String s: phonetics)
	            {
	              if(wordgrams.get(s) > count)
	              {
	                high = s;
	                count = wordgrams.get(s);
	              }
	            }
	            
	            for(Iterator<String> iterator = phonetics.iterator(); iterator.hasNext();)
	            {
	              String word = iterator.next();
	              dist = 0;
	              
	              h1 = psim.create_n_gram(word, ngram_size);
	              h2 = psim.create_n_gram(high, ngram_size);
	              
	              // Choose distance methods
	              dist = psim.dice_coeffizient(h1, h2);
	              //dist = psim.jaccard_index(h1, h2);
	              //dist = psim.cosine_similarity(h1, h2);
	              
	              if(dist > 0.7)
	              {
	                subs.put(word, high);
	                iterator.remove();
	              }  
	            }
	          }
	        }   
	      }
	    }

	    // Export substitution list
	    if(export_substitutions)
	    {
	    	writer_subs = new TagsToCSV("subs.csv");
	    	writer_subs.writeSubs(subs);
	    }
	    
	    // Replace tags corresponding to the subs dict
	    for(Tag t: tags)
	    {
	      words = psim.create_word_gram(t.getTagName(),blacklist);
	      new_tag = "";
	    	
	      for(String w: words)
	      {
	        new_tag = new_tag + " " + subs.get(w);
	      }

	      t.setTagName(new_tag);
	    }
	    
	    wordgrams = null;
	    phon = null;
	    subs = null;
	}
	
}
