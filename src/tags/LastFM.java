package tags;

import java.util.Collection;
import java.util.Properties;
import java.util.logging.Logger;

import de.umass.lastfm.*;

public class LastFM {
	private String key;
  private Logger logger = Logger.getLogger("Logger");

  public LastFM(Properties config) {
  	key = config.getProperty("API");
	}

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
