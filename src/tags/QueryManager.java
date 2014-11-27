package tags;

import java.sql.*;
import de.umass.lastfm.*;

public class QueryManager {
  // Need to be closed in the closeAll method
  private PreparedStatement selectRowFromArtist;
  private PreparedStatement selectIDFromArtist;
  private PreparedStatement insertIntoArtist;

  private PreparedStatement selectRowFromTrack;
  private PreparedStatement selectIDFromTrack;
  private PreparedStatement insertIntoTrack;

  private PreparedStatement selectRowFromTag;
  private PreparedStatement selectIDFromTag;
  private PreparedStatement insertIntoTag;

  private PreparedStatement selectRowFromTT;
  private PreparedStatement insertIntoTT;

  // Constructor
  public QueryManager(Connection conn) {
    try {
      // Initialize preparedstatments
      selectRowFromArtist = conn.prepareStatement("SELECT * FROM Artist WHERE Name = ?");
      selectIDFromArtist = conn.prepareStatement("SELECT ID FROM Artist WHERE Name = ?");
      insertIntoArtist = conn.prepareStatement("INSERT INTO Artist VALUES(DEFAULT,?)");

      selectRowFromTrack = conn.prepareStatement("SELECT * FROM Track WHERE Name = ? AND ID = ?");
      selectIDFromTrack = conn.prepareStatement("SELECT ID FROM Track WHERE Name = ? AND ID = ?");
      insertIntoTrack = conn.prepareStatement("INSERT INTO Track VALUES(DEFAULT,?,?)");
      
      selectRowFromTag = conn.prepareStatement("SELECT * FROM Tag WHERE Name = ?");
      selectIDFromTag = conn.prepareStatement("SELECT ID FROM Tag WHERE Name = ?");
      insertIntoTag = conn.prepareStatement("INSERT INTO Tag VALUES(DEFAULT,?)");
      
      selectRowFromTT = conn.prepareStatement("SELECT * FROM TT WHERE TrackID = ? AND TagID = ?");
      insertIntoTT = conn.prepareStatement("INSERT INTO TT VALUES(DEFAULT,?,?,?)");

    } catch (SQLException e) { e.printStackTrace(); }
  }

  public Boolean existsArtist(String artist)
  {
    Boolean check = false;
    ResultSet result;
    try {
      // Execute the query
      selectRowFromArtist.setString(1,artist);
      result = selectRowFromArtist.executeQuery();
      
      // Check if the row exists
      if(result.next()){
      check = true;
      }
      
      // Close resultset
      result.close();
    } catch (SQLException e) { e.printStackTrace(); }

    return check;
  }

  public int getIDFromArtist(String artist)
  {
    int id = 0;
    ResultSet result;
    try {
      // Execute the query
      selectIDFromArtist.setString(1, artist);
      result = selectIDFromArtist.executeQuery();

      // Map the result
      result.next();
      id = result.getInt("ID");

      // Close the resultset
      result.close();
    } catch (SQLException e) { e.printStackTrace(); }

      return id;
  }

  public Boolean existsTrack(String track, String artist)
  {
    Boolean check = false;
    ResultSet result;
    try {
      //Execute the query
      selectRowFromTrack.setString(1,track);
      selectRowFromTrack.setInt(2,getIDFromArtist(artist));
      result = selectRowFromTrack.executeQuery();
      
      // Check if the row exists
      if(result.next()){
      check = true;
      }

      // Close resultset
      result.close();
    } catch (SQLException e) { e.printStackTrace(); }

    return check;
  }

  public int getIDFromTrack(String track, String artist)
  {
    int id = 0;
    ResultSet result;
    try {
      // Execute the query
      selectIDFromTrack.setString(1, track);
      selectIDFromTrack.setInt(2,getIDFromArtist(artist));
      result = selectIDFromTrack.executeQuery();

      // Map the result
      result.next();
      id = result.getInt("ID");

      // Close the resultset
      result.close();
    } catch (SQLException e) { e.printStackTrace(); }

      return id;
  }

  public Boolean existsTag(Tag tag)
  {
    Boolean check = false;
    ResultSet result;
    try {
      // Execute the query
      selectRowFromTag.setString(1,tag.getName());
      result = selectRowFromTag.executeQuery();
      
      // Check if the row exists
      if(result.next()){
      check = true;
      }
      
      // Close resultset
      result.close();
    } catch (SQLException e) { e.printStackTrace(); }

    return check;
  }

  public int getIDFromTag(Tag tag)
  {
    int id = 0;
    ResultSet result;
    try {
      // Execute the query
      selectIDFromTag.setString(1, tag.getName());
      result = selectIDFromTag.executeQuery();

      // Map the result
      result.next();
      id = result.getInt("ID");

      // Close the resultset
      result.close();
    } catch (SQLException e) { e.printStackTrace(); }

      return id;
  }

  public Boolean existsTT(String track, String artist, Tag tag)
  {
    Boolean check = false;
    ResultSet result;
    try {
      // Execute the query
      selectRowFromTT.setInt(1, getIDFromTrack(track,artist));
      selectRowFromTT.setInt(2, getIDFromTag(tag));
      result = selectRowFromTT.executeQuery();
      
      // Check if the row exists
      if(result.next()){
      check = true;
      }
      
      // Close resultset
      result.close();
    } catch (SQLException e) { e.printStackTrace(); }

    return check;
  }

  public void insertArtist(String artist)
  {
    try {
      // Execute query
     insertIntoArtist.setString(1,artist);
     insertIntoArtist.executeUpdate();
    } catch (SQLException e) { e.printStackTrace(); }
  }

  public void insertTrack(String track, String artist)
  {
    try {
      // Execute query
     insertIntoTrack.setString(1,track);
     insertIntoTrack.setInt(2,getIDFromArtist(artist));
     insertIntoTrack.executeUpdate();
    } catch (SQLException e) { e.printStackTrace(); }
  }

  public void insertTag(Tag tag)
  {
    try {
      // Execute query
     insertIntoTag.setString(1,tag.getName());
     insertIntoTag.executeUpdate();
    } catch (SQLException e) { e.printStackTrace(); }
  }

  public void insertTT(String track, String artist, Tag tag)
  {
    try {
      // Execute query
     insertIntoTT.setInt(1, getIDFromTrack(track,artist));
     insertIntoTT.setInt(2, getIDFromTag(tag));
     insertIntoTT.setInt(3, tag.getCount());
     insertIntoTT.executeUpdate();
    } catch (SQLException e) { e.printStackTrace(); }
  }

  public void closeAll()
  {
    try {
      // Close all prepared statements
      selectRowFromArtist.close();
      selectIDFromArtist.close();
      insertIntoArtist.close();

      selectRowFromTrack.close();
      selectIDFromTrack.close();
      insertIntoTrack.close();

      selectRowFromTag.close();
      selectIDFromTag.close();
      insertIntoTag.close();

      selectRowFromTT.close();
      insertIntoTT.close();
    } catch (SQLException e) { e.printStackTrace(); }
  }
}
