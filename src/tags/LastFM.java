package tags;

import java.util.Collection;
import java.util.logging.Logger;

import de.umass.lastfm.*;

public class LastFM {
	private String key = "97d879c2f3a01fbbfb007be0eb86734e"; 
  private Logger logger = Logger.getLogger("Logger");

  //Get the tags from last.fm
  public Collection<Tag> mineTags(String title, String artist) {
  	Collection<Tag> tag = null;
  
    tag = Track.getTopTags(artist, title, key);	

    if(tag.isEmpty())
    {
      logger.warning("Couldn't find \""+title+"\" from \""+artist+"\".");
    }
  	
  	return tag;
  }
}
