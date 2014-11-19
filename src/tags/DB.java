package tags;

import java.sql.*;
import java.util.Collection;
import java.util.Properties;

import de.umass.lastfm.*;

public class DB {
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

  public void insert(String track, String artist, Collection<Tag> tags) {
  try {     
    // Checks
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
  } 
  finally {
    try {
      // Close statements
      querymanager.closeAll();
      // Close connection
      conn.close();
    } catch (SQLException e) { e.printStackTrace(); }
  }
  }
}
