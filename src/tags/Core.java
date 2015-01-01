package tags;

import java.sql.SQLException;
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
    int missingTracks = 0;
    int maxTries = 2;
    int currentTries = 0;
    Boolean retry = true;
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
    	// Set and reset variables.
	    counter++;
	    currentTries = 0;
	    retry = true;
	    
	    // Extract the artist and track.
	    line = l.split(",");
	    
      if(line.length < 2)
      {
        line = new String[]{"",""};
      }

	    artist = line[0].trim();
	    track = line[1].trim();
	    
	    // Check if the names smaller than the db cell.
	    if(artist.length() <= maxString && track.length() <= maxString)
	    {
	    	// Retries three times.
	    	while(retry)
	    	{
	    		// Mine and insert with exception handling.
	    		try
		    	{
				    tags = last.mineTags(track, artist);
				    db.insert(track,artist,tags);
				    
				    retry = false;
		    	}
		    	catch (de.umass.lastfm.CallException e)
		    	{
		    		if(currentTries >= maxTries)
		    		{
		    			retry = false;
		    			missingTracks++;
		    			
		    			log.severe(e.getMessage()+"at Row: "+counter+"; Artist: "+artist+"; Track: "+track);
			    		e.printStackTrace();
		    		}
		    	}
	    		catch (SQLException e) {
	    			if(currentTries >= maxTries)
		    		{
		    			retry = false;
		    			missingTracks++;
		    			
		    			log.severe(e.getMessage()+"at Row: "+counter+"; Artist: "+artist+"; Track: "+track);
			    		e.printStackTrace();
		    		}
		  		}
		    	catch (Exception e)
		    	{
		    		if(currentTries >= maxTries)
		    		{
			    		retry = false;
			    		missingTracks++;
			    		
			    		log.severe(e.getMessage()+"at Row: "+counter+"; Artist: "+artist+"; Track: "+track);
			    		e.printStackTrace();
		    		}
		    	}
	    		
	    		// Wait to stay below 5 cals per second.
			    try { Thread.sleep(250); } catch (InterruptedException e) { e.printStackTrace(); }
			    
			    // Increase the current retry counter.
			    currentTries++;
	    	}
		    
		    // Log message all 100 tracks
		    if(counter%100 == 0)
		    {
		    	log.info("Imported "+counter+" rows; "+ "Tracks without tags: "+last.getNumberOfTaglessTracks()+" Missing Tracks: "+missingTracks+" Too long Tags: "+db.getTooLongTags());
		    }
	    }
	    else
	    {
	    	missingTracks++;
	    }
    }

    // Close all
    db.closeAll();
    
    log.info("Imported "+counter+" rows; "+ "Tracks without tags: "+last.getNumberOfTaglessTracks()+" Missing Tracks: "+missingTracks+" Too long Tags: "+db.getTooLongTags());
    log.info("End");
  }
}
