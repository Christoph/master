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
/*
      // Check if the tag exists
      if("SELECT Name FROM Tag WHERE Name = "+t.getName() == t.getName());
      {
        tagID = "SELECT ID FROM Tag WHERE Name = "+t.getTrack()";
        tagQuery = "";
      }
      else
      {
        tagQuery = "INSERT INTO Tag VALUES(DEFAULT,'"+t.getName()+"');";
      }
      
      // Check if the tag/track combination exists
      if("SELECT * FROM TT WHERE TrackID = "+trackID.toString()+" AND TagID = "+tagID.toString() == null)
      {
        ttQuery = "";
      }
      else
      {
        ttQuery = "INSERT INTO TT VALUES(DEFAULT,"+trackID+","tagID");";
      }
      */
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
