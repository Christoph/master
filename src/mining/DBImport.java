package mining;

import java.sql.*;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import de.umass.lastfm.*;

public class DBImport {
	//Initialize logger
	private Logger log = Logger.getLogger("Logger");
	
	//Initializing variables
  private String connectionString, user, pass;
  private Connection conn;
  private int longTag = 0;
  
  private Collection<Tag> tags;
  private String[] line;
  private List<String> lines;
  private int counter = 0;
  private int missingTracks = 0;
  private int maxTries = 2;
  private int currentTries = 0;
  private Boolean retry = true;
  private String artist, track;
  
  //150 is the size of the cell in the table
  int maxString = 150;
  
  // Initialize classes
  LastFM last;
  ImportCSV data = new ImportCSV();

  private QueryManager querymanager;

  public DBImport(Properties config) {
  	last = new LastFM(config);
  	
    connectionString = config.getProperty("database");
    user = config.getProperty("user");
    pass = config.getProperty("password");

    try {
		conn = DriverManager.getConnection(connectionString, user, pass);
	} catch (SQLException e) {
		e.printStackTrace();
	}
    querymanager = new QueryManager(conn);
  }

  public void mineAndImportCSV()
  {
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
      	log.severe("Artist or Track from file missing.");
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
				    insert(track,artist,tags);
				    
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
		    	log.info("Imported "+counter+" rows; "+ "Tracks without tags: "+last.getNumberOfTaglessTracks()+" Missing Tracks: "+missingTracks+" Too long Tags: "+getTooLongTags());
		    }
	    }
	    else
	    {
	    	missingTracks++;
	    }
    }
    log.info("Imported "+counter+" rows; "+ "Tracks without tags: "+last.getNumberOfTaglessTracks()+" Missing Tracks: "+missingTracks+" Too long Tags: "+getTooLongTags());
  }
  
  private void insert(String track, String artist, Collection<Tag> tags) throws SQLException {
    for(Tag t: tags)
    {
    	if(t.getName().length() <= 150)
    	{
	      // Check if the artist exists
	      if(!querymanager.existsArtist(artist))
	      {
	        querymanager.insertArtist(artist);
	      }
	      
	      // Check if the track exists
	      if(!querymanager.existsTrack(track,artist))
	      {
	        querymanager.insertTrack(track,artist);
	      }
	
	      // Check if the tag exists
	      if(!querymanager.existsTag(t))
	      {
	        querymanager.insertTag(t);
	      }
	      
	      // Check if the tag/track combination exists
	      if(!querymanager.existsTT(track,artist,t))
	      {
	        querymanager.insertTT(track,artist,t);
	      }
    	}
    	else
    	{
	    	log.info("Too long Tag: "+t.getName()+" From Track: "+track+" Artist: "+artist);
    		longTag++;
    	}
    }
  
    //Committing the changes
    conn.commit();
  }
  
  private int getTooLongTags()
  {
  	return longTag;
  }

  public void closeAll()
  {
    try {
      // Close statements
      querymanager.closeAll();
      // Close connection
      conn.close();
    } catch (SQLException e) { 
    	log.severe("Error while closing all connections."+e.getSQLState()+e.getMessage());
    	
    	e.printStackTrace(); 
    }
  }
}
