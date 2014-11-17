package tags;

import java.sql.*;
import java.util.Collection;
import java.util.Properties;

import org.apache.commons.dbutils.*;

import de.umass.lastfm.*;

public class DB {
  private String connectionString user, pass;
  private String tagQuery, artistQuery, trackQuery;
  private int trackID, tagID;
  private Connection conn;

  private QueryRunner run;
  private Query querymanager;

  public DB(Properties config) {
    connectionString = config.getProperty("database");
    user = config.getProperty("user");
    pass = config.getProperty("password");

    querymanager = new Query();
  }

  public void insert(Collection<Tag> tags) {
    //Initialization
    run = new QueryRunner();
  
    // Checks
    for(Tag t: tags)
    {
      // Check if the artist exists
      if("SELECT Name FROM Artist WHERE Name = "+t.getArtist() == t.getArtist());
      {
        artistQuery = "";
      }
      else
      {
        artistQuery = "INSERT INTO Artist VALUES(DEFAULT,'"+t.getArtist()+"');";
      }
      
      // Check if the track exists
      if("SELECT Name FROM Track WHERE Name = "+t.getTrack() == t.getTrack());
      {
        trackID = "SELECT ID FROM Track WHERE Name = "+t.getTrack()";
        trackQuery = "";
      }
      else
      {
        trackQuery = "INSERT INTO Track VALUES(DEFAULT,'"+t.getTrack()+"',"+"SELECT ID FROM Artist WHERE Name = "+t.getArtist();+");";
      }

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

      // Execute the querys
      try {     
        conn = DriverManager.getConnection(connectionString, user, pass);
        run.update(conn, query);
      } catch(SQLException e) {
        e.printStackTrace();
      } finally {
        DbUtils.closeQuietly(conn);
      }
    }
  }
}
