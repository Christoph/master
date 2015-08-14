package processing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import tags.Tag;

public class Helper {
	
	   StringLengthComparator slc = new StringLengthComparator();
	  
	  public void removeTagsWithoutWords(List<Tag> tags)
	  {
		    // Remove tags with no words    
		    for(Iterator<Tag> iterator = tags.iterator(); iterator.hasNext();)
		    {
				Tag t = iterator.next();
				  
				if(t.getTagName().length() == 0)
				{
					iterator.remove();
				}
		    }
	  }
	  
	  public Map<String, Double> getImportantTags(List<Tag> tags, double threshold, int minWordLength)
	  {
		  Map<String, Double> important = new HashMap<String, Double>();
		  List<String> temp = new ArrayList<String>();
		  Map<String, Double> out = new LinkedHashMap<String, Double>();
		  
		  for(Tag t: tags)
		  {
			  if(t.getImportance() > threshold && t.getTagName().length() > minWordLength)
			  {				  
				  important.put(t.getTagName(),t.getImportance());
				  temp.add(t.getTagName());
			  }
		  }
		  
		  Collections.sort(temp, slc);
		  
		  for(String l: temp)
		  {
			  out.put(l, important.get(l));
		  }
		  
		  return out;
	  }
}
