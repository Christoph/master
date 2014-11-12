package tags;

import java.util.Collection;
import de.umass.lastfm.*;

public class Core {

	private static Collection<Tag> tags;

	public static void main(String[] args) {
    System.out.println("Start");

    LastFM last = new LastFM();

    tags = last.mineTags("Metallica", "Nothin Else Matters");
    
    System.out.println("End");
	}

}
