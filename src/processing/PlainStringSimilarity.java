package processing;

import java.util.HashSet;

public class PlainStringSimilarity {

  public HashSet<String> create_n_gram(String text, int n) {
    // Initialize variables
    HashSet<String> set = new HashSet<String>();
    int c = 0;
    int l = text.length();

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
      set.add(text.substring(i,i+(n-1)));
    }

    return set;
  }
}
