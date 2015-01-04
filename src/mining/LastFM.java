package mining;

import java.util.Collection;
import java.util.Properties;

import de.umass.lastfm.*;

public class LastFM {
	private String key;
  private int count = 0;

  public LastFM(Properties config) {
  	key = config.getProperty("API");
	}

	//Get the tags from last.fm
  public Collection<Tag> mineTags(String title, String artist) {
  	Collection<Tag> tag = null;
  
    tag = Track.getTopTags(artist, title, key);	

    if(tag.isEmpty())
    {
      count++;
    }
  	
  	return tag;
  }
  
  public int getNumberOfTaglessTracks()
  {
  	return count;
  }
}
