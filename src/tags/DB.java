package tags;

import java.sql.*;
import java.util.Collection;
import java.util.Properties;
import java.util.logging.Logger;

import de.umass.lastfm.*;

public class DB {
	//Initialize logger
	private Logger log = Logger.getLogger("Logger");
	
  private String connectionString, user, pass;
  private Connection conn;

  private QueryManager querymanager;

  public DB(Properties config) {
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

  public void insert(String track, String artist, Collection<Tag> tags) throws SQLException {
    for(Tag t: tags)
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
  
    //Committing the changes
    conn.commit();
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
