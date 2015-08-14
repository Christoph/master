package processing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import tags.Tag;

public class Helper {
	  
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
	  
	  public List<String> getImportantTags(List<Tag> tags, double threshold, int minWordLength)
	  {
		  List<String> important = new ArrayList<String>();
		  
		  for(Tag t: tags)
		  {
			  if(t.getImportance() > threshold && t.getTagName().length() > minWordLength)
			  {
				  important.add(t.getTagName());
			  }
		  }
		  
		  return important;
	  }
}
