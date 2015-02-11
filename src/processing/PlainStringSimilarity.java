package processing;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class PlainStringSimilarity {

  public HashSet<String> create_n_gram(String text, int n) 
  {
    // Initialize variables
    HashSet<String> set = new HashSet<String>();
    int c = 0;
    int l = text.length();
    
    text = " "+text+" ";

    // Compute maximum combinations
    if(n==1)
    {
      c = l;
    }
    else
    {
      c = l + 3 - n;
    }

    // Create a list of unique n-grams
    for(int i = 0;i<c;i++)
    {
      set.add(text.substring(i,i+(n)));
    }

    return set;
  }
  
  public List<String> create_word_gram(String text) {
  	// Initialize variables
    List<String> set = new ArrayList<String>();
    
    if(!text.contains(" "))
    {
    	set.add(text);
    }
    else
    {   	
    	for(String s: text.split("\\s+"))
    	{
    		set.add(s);
    	}
    }
    
    return set;
  }
  
  public double dice_coeffizient(HashSet<String> set1, HashSet<String> set2)
  {
  	// Initialize variables
   	HashSet<String> num = new HashSet<String>();
   	num.addAll(set1);
  	
  	// Compute the intersection of both sets
  	num.retainAll(set2);
  	
  	//Compute the coefficient
  	int n = (2*num.size());
  	double d = (set1.size()+set2.size());

		return n/d;
  }
  
  public double jaccard_index(HashSet<String> set1, HashSet<String> set2)
  {
  	// Initialize variables
  	HashSet<String> num = new HashSet<String>();
  	HashSet<String> deno = new HashSet<String>();
  	num.addAll(set1);
  	deno.addAll(set1);
  	
  	// Compute intersection
  	num.retainAll(set2);
  	
  	// Compute union
  	deno.addAll(set2);
  	
  	// Compute the index
  	int n = num.size();
  	double d = deno.size();
  	
  	return n/d;
  }
  
  public double cosine_similarity(HashSet<String> set1, HashSet<String> set2)
  {
  	// Initialize variables
   	HashSet<String> full = new HashSet<String>();
   	HashSet<String> A = new HashSet<String>();
   	HashSet<String> B = new HashSet<String>();
   	HashSet<String> num = new HashSet<String>();
   	double deno = 0.0f;
   	
  	// Create union of both sets
   	full.addAll(set1);
   	full.addAll(set2);
   	
   	// Create the intersections
   	// Maybe not necessary
   	A.addAll(set1);
   	B.addAll(set2);
   	A.retainAll(full);
   	B.retainAll(full);
   	
   	// Intersection of A and B is the numerator
   	num.addAll(A);
    num.retainAll(B);
   	
    // Compute the denominator
    // There are only positive ones in the sets which leads to this simple formula
    deno = Math.sqrt(A.size())+Math.sqrt(B.size());
  	
  	return num.size()/deno;
  }
}
