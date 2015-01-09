package processing;

import java.util.HashSet;

public class PlainStringSimilarity {

  public HashSet<String> create_n_gram(String text, int n) {
    // Initialize variables
    HashSet<String> set = new HashSet<String>();
    int c = 0;
    int l = text.length();
    
    text = " "+text+" ";

    // Compute maximum combinations.
    if(n==1)
    {
      c = l;
    }
    else
    {
      c = l + 3 - n;
    }

    // Create a list of unique n-grams.
    for(int i = 0;i<c;i++)
    {
      set.add(text.substring(i,i+(n)));
    }

    return set;
  }
  
  public float dice_coeffizient(HashSet<String> set1, HashSet<String> set2)
  {
  	// Save initial set1.
   	HashSet<String> num = new HashSet<String>();
   	num.addAll(set1);
  	
  	// convert set1 to the intersection of both sets.
  	num.retainAll(set2);
  	
  	//Compute the coefficient.
  	int n = (2*num.size());
  	int d = (set1.size()+set2.size());

		return n/d;
  }
  
  public float jaccard_index(HashSet<String> set1, HashSet<String> set2)
  {
  	// Save initial set1.
  	HashSet<String> num = new HashSet<String>();
  	HashSet<String> deno = new HashSet<String>();
  	num.addAll(set1);
  	deno.addAll(set1);
  	
  	// Compute intersection.
  	num.retainAll(set2);
  	
  	// Compute union.
  	deno.addAll(set2);
  	
  	// Compute the index.
  	int n = num.size();
  	int d = deno.size();
  	
  	return n/d;
  }
  
  public float cosine_similarity(HashSet<String> set1, HashSet<String> set2)
  {
  	return 0;
  }
}
