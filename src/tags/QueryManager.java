package tags;

import java.sql.*;

public class QueryManager {
  private ResultSet result;

  // Need to be closed in the closeAll method
  private PreparedStatement selectRowFromArtist;
  private PreparedStatement insertIntoArtist;
  private PreparedStatement selectRowFromTrack;
  private PreparedStatement selectIDFromArtist;
  private PreparedStatement insertIntoTrack;

  // Constructor
  public QueryManager(Connection conn) {
    try {
      // Initialize preparedstatments
      selectRowFromArtist = conn.prepareStatement("SELECT * FROM Artist WHERE Name = ?");
      insertIntoArtist = conn.prepareStatement("INSERT INTO Artist VALUES(DEFAULT,?)");
      selectRowFromTrack = conn.prepareStatement("SELECT * FROM Track WHERE Name = ? AND ID = ?");
      selectIDFromArtist = conn.prepareStatement("SELECT ID FROM Artist WHERE Name = ?");
      insertIntoTrack = conn.prepareStatement("INSERT INTO Track VALUES(DEFAULT,?,?)");
    } catch (SQLException e) { e.printStackTrace(); }
  }

  public Boolean existsArtist(String artist)
  {
    Boolean check = false;
    
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
      result = null;
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

  public int getIDFromArtist(String artist)
  {
    int id = 0;
    try {
      // Execute the query
      selectIDFromArtist.setString(1, artist);
      result = selectIDFromArtist.executeQuery();

      // Map the result
      result.next();
      id = result.getInt("ID");

      // Close the resultset
      result.close();
      result = null;
    } catch (SQLException e) { e.printStackTrace(); }

      return id;
  }

  public Boolean existsTrack(String track, String artist)
  {
    Boolean check = false;
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
      result = null;
    } catch (SQLException e) { e.printStackTrace(); }

    return check;
  }

  public void closeAll()
  {
    try {
      // Close all prepared statements
      selectRowFromArtist.close();
      insertIntoArtist.close();
      selectIDFromArtist.close();
      selectRowFromTrack.close();
    } catch (SQLException e) { e.printStackTrace(); }
  }
}
