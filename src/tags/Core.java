package tags;

import java.util.Collection;
import java.util.List;

import de.umass.lastfm.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.*;

public class Core {

  private static final Logger log = Logger.getLogger("Logger");

  public static void main(String[] args) {
  // Initializing the logger
	Handler handler;

	try {
      handler = new FileHandler( "log.txt" );
      handler.setFormatter(new SimpleFormatter());
      log.addHandler( handler );
    } catch (SecurityException e1) { e1.printStackTrace();
    } catch (IOException e1) { e1.printStackTrace(); }
	
    log.info("Initializing");

    // Initializing variables
    Collection<Tag> tags;
    String[] line;
    List<String> lines;
    InputStream input = null;
    int counter = 0;
    String artist, track;
    
    // 150 is the size of the cell in the table
    int maxString = 150;

    // Load config files
    Properties dbconf = new Properties();
    try {

      input = new FileInputStream("config.db");
      dbconf.load(input);

    } catch (IOException e) { e.printStackTrace(); }

    // Initializing classes 
    DB db = new DB(dbconf);
    LastFM last = new LastFM(dbconf);
    ImportCSV data = new ImportCSV();

    log.info("Import");
    
    // Import data
    lines = data.importCSV();
    
    for(String l :lines)
    {
	    counter++;
	    
	    line = l.split(",");
	    
	    artist = line[0].trim();
	    track = line[1].trim();
	    
	    if(artist.length() <= maxString && track.length() <= maxString)
	    {
	    	try
	    	{
			    tags = last.mineTags(track, artist);
			    db.insert(track,artist,tags);
	    	}
	    	catch (de.umass.lastfm.CallException e)
	    	{
	    		log.severe(e.getMessage()+"at Row: "+counter+"; Artist: "+artist+"; Track: "+track);
	    		
	    		e.printStackTrace();
	    	}
	    	catch (Exception e)
	    	{
	    		log.severe(e.getMessage()+"at Row: "+counter+"; Artist: "+artist+"; Track: "+track);
	    		
	    		e.printStackTrace();
	    	}
		    
		    // Wait to stay below 5 cals per second.
		    try { Thread.sleep(250); } catch (InterruptedException e) { e.printStackTrace(); }
		    
		    // Log message all 100 tracks
		    if(counter%100 == 0)
		    {
		    	log.info("Imported "+counter+" rows; "+ "Tracks with tags: "+(counter-last.getNumberOfTaglessTracks())+"; Tracks without tags: "+last.getNumberOfTaglessTracks());
		    }
	    }
    }

    // Close all
    db.closeAll();

    log.info("End");
  }
}
