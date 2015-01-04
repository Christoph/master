package mining;

import java.sql.*;
import java.util.logging.Logger;

import de.umass.lastfm.*;

public class QueryManager {
	// Initialize logger
	private Logger log = Logger.getLogger("Logger");
	
  // Need to be closed in the closeAll method
  private PreparedStatement selectIDFromArtist;
  private PreparedStatement insertIntoArtist;

  private PreparedStatement selectIDFromTrack;
  private PreparedStatement insertIntoTrack;

  private PreparedStatement selectIDFromTag;
  private PreparedStatement insertIntoTag;

  private PreparedStatement selectRowFromTT;
  private PreparedStatement insertIntoTT;

  // Constructor
  public QueryManager(Connection conn) {
    try {
      // Initialize preparedstatments
      selectIDFromArtist = conn.prepareStatement("SELECT ID FROM Artist WHERE Name = ?");
      insertIntoArtist = conn.prepareStatement("INSERT INTO Artist VALUES(DEFAULT,?)");

      selectIDFromTrack = conn.prepareStatement("SELECT ID FROM Track WHERE Name = ? AND ArtistID = ?");
      insertIntoTrack = conn.prepareStatement("INSERT INTO Track VALUES(DEFAULT,?,?)");
      
      selectIDFromTag = conn.prepareStatement("SELECT ID FROM Tag WHERE Name = ?");
      insertIntoTag = conn.prepareStatement("INSERT INTO Tag VALUES(DEFAULT,?)");
      
      selectRowFromTT = conn.prepareStatement("SELECT * FROM TT WHERE TrackID = ? AND TagID = ?");
      insertIntoTT = conn.prepareStatement("INSERT INTO TT VALUES(DEFAULT,?,?,?)");

    } catch (SQLException e) { 
    	log.severe("Error in the DB constructor."+e.getSQLState()+e.getMessage());
    	
    	e.printStackTrace(); 
    }
  }

  public Boolean existsArtist(String artist) throws SQLException
  {
    Boolean check = false;
    ResultSet result;

      // Execute the query
      selectIDFromArtist.setString(1,artist);
      result = selectIDFromArtist.executeQuery();
      
      // Check if the row exists
      if(result.next()){
      check = true;
      }
      
      // Close resultset
      result.close();

    return check;
  }

  public int getIDFromArtist(String artist) throws SQLException
  {
    int id = 0;
    ResultSet result;

      // Execute the query
      selectIDFromArtist.setString(1, artist);
      result = selectIDFromArtist.executeQuery();

      // Map the result
      result.next();
      id = result.getInt("ID");

      // Close the resultset
      result.close();

      return id;
  }

  public Boolean existsTrack(String track, String artist) throws SQLException
  {
    Boolean check = false;
    ResultSet result;

      //Execute the query
      selectIDFromTrack.setString(1,track);
      selectIDFromTrack.setInt(2,getIDFromArtist(artist));
      result = selectIDFromTrack.executeQuery();
      
      // Check if the row exists
      if(result.next()){
      check = true;
      }

      // Close resultset
      result.close();

    return check;
  }

  public int getIDFromTrack(String track, String artist) throws SQLException
  {
    int id = 0;
    ResultSet result;

      // Execute the query
      selectIDFromTrack.setString(1, track);
      selectIDFromTrack.setInt(2,getIDFromArtist(artist));
      result = selectIDFromTrack.executeQuery();

      // Map the result
      result.next();
      id = result.getInt("ID");

      // Close the resultset
      result.close();

      return id;
  }

  public Boolean existsTag(Tag tag) throws SQLException
  {
    Boolean check = false;
    ResultSet result;

      // Execute the query
      selectIDFromTag.setString(1,tag.getName());
      result = selectIDFromTag.executeQuery();
      
      // Check if the row exists
      if(result.next()){
      check = true;
      }
      
      // Close resultset
      result.close();

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
    } catch (SQLException e) 
    { 
    	log.severe("There might be a problem with the tag length. The tag name is: "+tag.getName());
    	log.severe(e.getSQLState());
    	log.severe(e.getStackTrace().toString());
    	
    	e.printStackTrace(); 
    }

      return id;
  }

  public Boolean existsTT(String track, String artist, Tag tag) throws SQLException
  {
    Boolean check = false;
    ResultSet result;

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

    return check;
  }

  public void insertArtist(String artist) throws SQLException
  {
      // Execute query
     insertIntoArtist.setString(1,artist);
     insertIntoArtist.executeUpdate();
  }

  public void insertTrack(String track, String artist) throws SQLException
  {
      // Execute query
     insertIntoTrack.setString(1,track);
     insertIntoTrack.setInt(2,getIDFromArtist(artist));
     insertIntoTrack.executeUpdate();
  }

  public void insertTag(Tag tag) throws SQLException
  {
      // Execute query
     insertIntoTag.setString(1,tag.getName());
     insertIntoTag.executeUpdate();
  }

  public void insertTT(String track, String artist, Tag tag) throws SQLException
  {
      // Execute query
     insertIntoTT.setInt(1, getIDFromTrack(track,artist));
     insertIntoTT.setInt(2, getIDFromTag(tag));
     insertIntoTT.setInt(3, tag.getCount());
     insertIntoTT.executeUpdate();
  }

  public void closeAll() throws SQLException
  {
      // Close all prepared statements
      selectIDFromArtist.close();
      insertIntoArtist.close();

      selectIDFromTrack.close();
      insertIntoTrack.close();

      selectIDFromTag.close();
      insertIntoTag.close();

      selectRowFromTT.close();
      insertIntoTT.close();
  }
}
