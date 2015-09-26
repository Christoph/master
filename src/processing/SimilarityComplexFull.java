package processing;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import core.Tag;
import core.TagsToCSV;

public class SimilarityComplexFull {
	
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
	    Map<String, String> substitution_list = new HashMap<String, String>();
	    
        Set<String> word_group = new HashSet<String>();
        
	    List<String> words;
	    double value, importance;
	    float similarity, similarity2;
	    int ngram_size;
	    String key, new_tag;
	    Boolean print_substitutions;
	    HashSet<String> h1, h2, h3;
	    
		/////////////////////////////////
		// Configuration

		// Print substitution list
		print_substitutions = true;
		
		// Set the size for the n-gram distance method
		ngram_size = 2;
		
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
	    
	    System.out.println("word/weight dict created");
	    
	    word_group.addAll(tag_words.keySet());
	    
	    //	Debug stuff
	    int psize = word_group.size();
	    int part = psize/30;
	    int iter = 0;
	    
	    System.out.println("Total tag count:"+word_group.size());
	    
	    // Iterate over all tags
	      for(int i =0; i<word_group.size();i++)
	      {
	    	  iter++;
	    	  if(iter%part == 0)
	    	  {
	    		  System.out.print("->"+iter);
	    	  }
	
	          while(!word_group.isEmpty())
	          {
	            importance = 0;
	            
	            String high = "";
	            
				// Find the word with the highest importance count
	            for(String s: word_group)
	            {
	              if(tag_words.get(s) >= importance)
	              {
	                high = s;
	                importance = tag_words.get(s);
	              }
	            }
	            
	            word_group.remove(high);
	            
	            // Iterate over all words and find similar words with distance methods
	            for(Iterator<String> iterator = word_group.iterator(); iterator.hasNext();)
	            {
	              String word = iterator.next();
	              
	              if(!substitution_list.containsKey(word))
	              {
		              similarity = 0;
		              
		              h1 = psim.create_n_gram(word, ngram_size);
		              h2 = psim.create_n_gram(high, ngram_size);
		              
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
		              
		              h1 = psim.create_n_gram(word, ngram_size);
		              h2 = psim.create_n_gram(high, ngram_size);
		              
			              similarity = psim.jaccard_index(h1, h2);
			              
			              h3 = psim.create_n_gram(substitution_list.get(word), ngram_size);
			              
			              similarity2 = psim.jaccard_index(h1, h3);
			              
		            	  // Replace old substitution if the new one is better
			              if(similarity >= similarity2)
			              {          	  
				                substitution_list.put(word, high);
					            iterator.remove();
			              }
		              }
		            }
		          } 
		        }   
	      
	    tag_words = null;
	    
	    System.out.println("\nsubstitution dict created");
	    
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
	    
	    System.out.println("export finished");
	    
	    // Replace tags corresponding to the subs dict
	    for(Tag t: tags)
	    {
	      words = psim.create_word_gram(t.getTagName(),blacklist);
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
	    
	    System.out.println("replacement finished");
	    
	    // Resolve errors from replacements
	    helper.correctTagsAndIDs(tags);
	    helper.removeTagsWithoutWords(tags);
	    
	    substitution_list = null;
	}
	
}
