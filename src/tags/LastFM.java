package tags;

import java.util.Collection;
import de.umass.lastfm.*;

public class LastFM {
	private String key = "97d879c2f3a01fbbfb007be0eb86734e"; 

  public Collection<Tag>  mineTags(String artist, String title) {
    return Track.getTopTags(artist, title, key);    
  }
}
