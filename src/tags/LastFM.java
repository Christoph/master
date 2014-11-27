package tags;

import java.util.Collection;

import de.umass.lastfm.*;

public class LastFM {
	private String key = "97d879c2f3a01fbbfb007be0eb86734e"; 

  //Get the tags from last.fm
  public Collection<Tag> mineTags(String title, String artist) {
    return Track.getTopTags(artist, title, key);    
  }
}
