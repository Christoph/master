package tags;

import java.util.Collection;
import de.umass.lastfm.*;

public class Core {

  private static Collection<Tag> tags;

  public static void main(String[] args) {
	DB db = new DB();
	LastFM last = new LastFM();
		
	System.out.println("Start");
	
	tags = last.mineTags("Metallica", "Nothing Else Matters");
    db.insert(tags);
    
    System.out.println("End");
  }
}
